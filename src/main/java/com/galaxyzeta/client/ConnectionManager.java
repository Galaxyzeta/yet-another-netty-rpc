package com.galaxyzeta.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.galaxyzeta.client.loadbalancer.RpcLoadBalancer;
import com.galaxyzeta.client.loadbalancer.RpcRandomBalancer;
import com.galaxyzeta.common.protocol.RpcService;
import com.galaxyzeta.common.protocol.RpcServiceGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ConnectionManager {

	private HashMap<String, List<RpcServiceGroup>> knownServiceMap = new HashMap<>();
	private HashSet<RpcServiceGroup> knownServiceGroups = new HashSet<>();
	private HashMap<RpcServiceGroup, RpcClientHandler> connectionMap = new HashMap<>();

	private RpcLoadBalancer loadBalancer = new RpcRandomBalancer();

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
		NioEventLoopGroup worker = new NioEventLoopGroup();
		Bootstrap boot = new Bootstrap();
		RpcClientHandlerInitializer initializer = new RpcClientHandlerInitializer();
		boot.group(worker)
			.channel(NioSocketChannel.class)
			.handler(initializer);
		ChannelFuture f = boot.connect(group.getAddress(), group.getPort()).sync();
		RpcClientHandler handler = f.channel().pipeline().get(RpcClientHandler.class);
		f.addListener(new ChannelFutureListener(){
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					connectionMap.put(group, handler);
				} else {
					LOG.info("Failed to connect to {}:{}", group.getAddress(), group.getPort());
					connectionMap.remove(group);
				}
			}
		});
		return handler;
	}

	public RpcClientHandler chooseConnection(String serviceKey) {
		RpcServiceGroup group = loadBalancer.route(this.knownServiceMap.get(serviceKey));
		if (group != null) {
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
}
