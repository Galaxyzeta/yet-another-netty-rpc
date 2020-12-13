package com.galaxyzeta.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
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

	private ConcurrentHashMap<String, List<RpcServiceGroup>> knownServiceMap = new ConcurrentHashMap<>();
	private CopyOnWriteArraySet<RpcServiceGroup> knownServiceGroups = new CopyOnWriteArraySet<>();
	private ConcurrentHashMap<RpcServiceGroup, RpcClientHandler> connectionMap = new ConcurrentHashMap<>();
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
		ConcurrentHashMap<String, List<RpcServiceGroup>> serviceMap = new ConcurrentHashMap<>();
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

	/**
	 * Connect to a remote service provider, add a new K-V pair into the connection map.
	 * @param group
	 * @return the Rpc handler that copes with the remote server.
	 * @throws Exception
	 */
	public RpcClientHandler connectServiceGroup(RpcServiceGroup group) throws Exception {
		
		Bootstrap boot = new Bootstrap();
		RpcClientHandlerInitializer initializer = new RpcClientHandlerInitializer(group);
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

	/**
	 * Choose a client handler by the given serviceKey.
	 * @param serviceKey
	 * @return routed RpcClientHandler
	 */
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

	/**
	 * ZK registry is empty, wait on the given condition until signalAvailable Service is called.
	 */
	private void waitForAvailableService() {
		lock.lock();
		try {
			serivceAvailableCondition.await();
		} catch (InterruptedException ie) {
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Indicates that new service has arrived at ZK registry.
	 */
	public void signalAvailableService() {
		lock.lock();
		try {
			serivceAvailableCondition.signalAll();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Remove connection group of a certain serviceGroup when a netty channel is unregistered.
	 * @param serviceGroup
	 */
	public void closeConnection(RpcServiceGroup serviceGroup) {
		connectionMap.remove(serviceGroup);
	}

	/**
	 * Close all connections, gracefully.
	 */
	public void shutdownAllConnections() {
		worker.shutdownGracefully();
	}
}
