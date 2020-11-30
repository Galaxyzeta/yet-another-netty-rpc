package com.galaxyzeta.common.zookeeper;

public class CuratorConfig {
	private String zkAddress;
	private int zkPort;
	private int zkConnectionTimeout;
	private int zkSessionTimeout;

	public CuratorConfig(String zkAddress, int zkPort, int zkConnectionTimeout, int zkSessionTimeout) {
		this.zkAddress = zkAddress;
		this.zkPort = zkPort;
		this.zkConnectionTimeout = zkConnectionTimeout;
		this.zkSessionTimeout = zkSessionTimeout;
	}

	public String getZkAddress() {
		return zkAddress;
	}
	public int getZkConnectionTimeout() {
		return zkConnectionTimeout;
	}
	public int getZkPort() {
		return zkPort;
	}
	public int getZkSessionTimeout() {
		return zkSessionTimeout;
	}
}
