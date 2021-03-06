package com.galaxyzeta.client;

import java.util.ArrayList;
import java.util.List;

import com.galaxyzeta.common.codec.Serializer;
import com.galaxyzeta.common.codec.SerializerContainer;
import com.galaxyzeta.common.protocol.RpcServiceGroup;
import com.galaxyzeta.common.util.Constant;
import com.galaxyzeta.common.zookeeper.CuratorClient;
import com.galaxyzeta.common.zookeeper.CuratorConfig;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceDiscovery {

	private CuratorClient curatorClient;

	private Serializer serializer = SerializerContainer.getSerializer();

	private static final Logger LOG = LoggerFactory.getLogger(ServiceDiscovery.class);

	public ServiceDiscovery(CuratorConfig zkConfig) {
		curatorClient = new CuratorClient(zkConfig);
	}

	public void pullLatestService() {
		LOG.info("Fetching service...");
		List<RpcServiceGroup> rpcServicesGroups = new ArrayList<>();
		try {
			List<String> servicePaths = curatorClient.getChildrenPathOf(Constant.ZK_DATA);
			if (servicePaths.size() == 0) {
				LOG.warn("No services available !");
			} else {
				for (String singlePath : servicePaths) {
					RpcServiceGroup serviceGroup = (RpcServiceGroup) serializer.decode(curatorClient.getNode(singlePath), RpcServiceGroup.class);
					rpcServicesGroups.add(serviceGroup);
				}
				updateKnownRpcConnection(rpcServicesGroups);
			}
			addEventListener();
		} catch (Exception e) {
			LOG.error("During fetching service, an exception occured: {}", e.getClass());
		}
		
	}

	public void addEventListener() throws Exception {
		curatorClient.addPathChildrenListener(Constant.ZK_DATA, new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				PathChildrenCacheEvent.Type type = event.getType();
				switch (type) {
					case CONNECTION_RECONNECTED:
						LOG.info("Reconnected to zk, try to get latest service list");
						pullLatestService();
						break;
					case CHILD_ADDED:
					case CHILD_UPDATED:
					case CHILD_REMOVED:
						LOG.info("Service info changed, try to get latest service list");
						pullLatestService();
						break;
				}
			}
		});
	}

	public void updateKnownRpcConnection(List<RpcServiceGroup> serviceGroups) {
		ConnectionManager.getInstance().updateAvailableServices(serviceGroups);
		ConnectionManager.getInstance().signalAvailableService();
	}
}
