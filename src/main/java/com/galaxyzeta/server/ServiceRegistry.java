package com.galaxyzeta.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.galaxyzeta.common.protocol.RpcServiceGroup;
import com.galaxyzeta.common.codec.Serializer;
import com.galaxyzeta.common.codec.SerializerContainer;
import com.galaxyzeta.common.protocol.RpcService;
import com.galaxyzeta.common.util.CommonUtil;
import com.galaxyzeta.common.zookeeper.CuratorClient;
import com.galaxyzeta.common.zookeeper.CuratorConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceRegistry {
	private CuratorClient client;
	private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistry.class);
	private List<RpcService> serviceList = new ArrayList<>();
	private Map<String, Object> serviceMap = new HashMap<>();		// service-key : implBean
	private String host;
	private int port;

	private Serializer serializer = SerializerContainer.getSerializer();

	public ServiceRegistry(String serverHost, int serverPort, CuratorConfig zkConfig) {
		client = new CuratorClient(zkConfig);
		this.host = serverHost;
		this.port = serverPort;
	}

	/**
	 * Add service to a list.
	 */
	public void addService(String serviceName, String version, Object implBean) {
		RpcService info = new RpcService(serviceName, version);
		LOG.info("Adding service {} ...", info);
		serviceList.add(info);
		serviceMap.put(CommonUtil.makeServiceKey(serviceName, version), implBean);
	}

	/**
	 * Publish service  to ZooKeeper
	 */
	public void publishService() throws Exception {
		RpcServiceGroup service = new RpcServiceGroup(host, port, serviceList);
		client.createEphemeralNode(""+service.hashCode(), serializer.encode(service));
		LOG.info("Published service group {} to ZK ...", service);
	}

	public Object getInstanceByServiceKey(String serviceKey) {
		return serviceMap.get(serviceKey);
	}
}
