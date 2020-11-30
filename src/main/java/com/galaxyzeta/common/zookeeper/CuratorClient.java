package com.galaxyzeta.common.zookeeper;

import java.util.List;

import com.galaxyzeta.common.util.CommonUtil;
import com.galaxyzeta.common.util.Constant;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * 封装CuratorFramework，对ZK进行操作
 */
public class CuratorClient {

	private CuratorFramework client;

	public CuratorClient(String connectString, int sessionTimeout, int connectionTimeout) {
		client = CuratorFrameworkFactory.builder().connectString(connectString)
                .sessionTimeoutMs(sessionTimeout).connectionTimeoutMs(connectionTimeout)
                .retryPolicy(new ExponentialBackoffRetry(1000, 10))
                .build();
        client.start();
	}

	public CuratorClient(CuratorConfig config) {
		this(CommonUtil.makeConnectionString(config.getZkAddress(), config.getZkPort()), 
			config.getZkSessionTimeout(), config.getZkConnectionTimeout());
	}

	public void createEphemeralNode(String path, byte[] stream) throws Exception {
		client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(makePath(path), stream);
	}

	public List<String> getChildrenPathOf(String path) throws Exception {
		return client.getChildren().forPath(path);
	}

	public byte[] getNode(String path) throws Exception {
		return client.getData().forPath(makePath(path));
	}

	public void addPathChildrenListener(String path, PathChildrenCacheListener listener) throws Exception {
		PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);
        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        pathChildrenCache.getListenable().addListener(listener);
    }

	public final String makePath(String path) {
		return Constant.ZK_DATA + "/" + path;
	} 
}
