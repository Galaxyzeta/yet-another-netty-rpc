package com.galaxyzeta.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.galaxyzeta.client.loadbalancer.RpcLoadBalancer;
import com.galaxyzeta.client.loadbalancer.RpcRandomBalancer;
import com.galaxyzeta.common.protocol.RpcService;
import com.galaxyzeta.common.protocol.RpcServiceGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ConnectionManager {

	private HashMap<String, List<RpcServiceGroup>> knownServiceMap = new HashMap<>();
	private HashSet<RpcServiceGroup> knownServiceGroups = new HashSet<>();
	private HashMap<RpcServiceGroup, RpcClientHandler> connectionMap = new HashMap<>();
	private NioEventLoopGroup worker = new NioEventLoopGroup();
	private RpcLoadBalancer loadBalancer = new RpcRandomBalancer();

	private ReentrantLock lock = new ReentrantLock();
	private Condition serivceAvailableCondition = lock.newCondition();

	private volatile boolean isRunning = true;

	private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

	private static class SingletonHolder {
		private static ConnectionManager instance = new ConnectionManager();
	}

	public static ConnectionManager getInstance() {
		return SingletonHolder.instance;
	}

	public void updateAvailableServices(List<RpcServiceGroup> serviceGroups) {
		// Convert latest RpcServiceGroups List into a Set
		HashSet<RpcServiceGroup> latestServiceGroups = new HashSet<>();
		for(RpcServiceGroup group: serviceGroups) {
			latestServiceGroups.add(group);
		}
		// Add newly discovered services.
		for(RpcServiceGroup group: latestServiceGroups) {
			if(! knownServiceGroups.contains(group)) {
				knownServiceGroups.add(group);
				// connectServiceGroup(group);
			}
		}
		// Remove stale services.
		for(RpcServiceGroup group: knownServiceGroups) {
			if(! latestServiceGroups.contains(group)) {
				knownServiceGroups.remove(group);
				connectionMap.remove(group);
			}
		}

		updateServiceMap();
		
	}

	private void updateServiceMap() {
		// Make new service map
		HashMap<String, List<RpcServiceGroup>> serviceMap = new HashMap<>();
		for(RpcServiceGroup group: knownServiceGroups) {
			List<RpcService> services = group.getServiceInfoList();
			for(RpcService service: services) {
				final String serviceKey = service.getServiceKey();
				if(serviceMap.containsKey(serviceKey)) {
					serviceMap.get(serviceKey).add(group);
				} else {
					final ArrayList<RpcServiceGroup> li = new ArrayList<>();
					li.add(group);
					serviceMap.put(serviceKey, li);
				}
			}
		}
		this.knownServiceMap = serviceMap;
	}

	public RpcClientHandler connectServiceGroup(RpcServiceGroup group) throws Exception {
		
		Bootstrap boot = new Bootstrap();
		RpcClientHandlerInitializer initializer = new RpcClientHandlerInitializer();
		boot.group(worker)
			.channel(NioSocketChannel.class)
			.handler(initializer);
		ChannelFuture f = boot.connect(group.getAddress(), group.getPort()).sync();
		RpcClientHandler handler = f.channel().pipeline().get(RpcClientHandler.class);
		if(f.isSuccess()){
			LOG.info("Successfully connected to {}:{}", group.getAddress(), group.getPort());
			connectionMap.put(group, handler);
			return handler;
		} else {
			LOG.info("Failed to connect to {}:{}", group.getAddress(), group.getPort());
			connectionMap.remove(group);
			return null;
		}
		
	}

	public RpcClientHandler chooseConnection(String serviceKey) {

		// No service available, need to wait...
		while(isRunning && this.knownServiceGroups.size() == 0) {
			LOG.info("Waiting for available service...");
			waitForAvailableService();
		}

		List<RpcServiceGroup> grouplist = this.knownServiceMap.get(serviceKey);

		if (grouplist != null && grouplist.size() > 0) {
			RpcServiceGroup group = loadBalancer.route(grouplist);
			RpcClientHandler handler;
			if((handler = connectionMap.get(group)) != null) {
				return handler;
			} else {
				try {
					return connectServiceGroup(group);
				} catch (Exception e) {
					return null;
				}
			}
		} else {
			return null;
		}
	}

	private void waitForAvailableService() {
		lock.lock();
		try {
			serivceAvailableCondition.await();
		} catch (InterruptedException ie) {
		} finally {
			lock.unlock();
		}
	}

	public void signalAvailableService() {
		serivceAvailableCondition.signalAll();
	}

	public void shutdownAllConnections() {
		worker.shutdownGracefully();
	}
}
