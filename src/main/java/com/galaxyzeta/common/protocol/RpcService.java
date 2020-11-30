package com.galaxyzeta.common.protocol;

import com.galaxyzeta.common.util.Constant;

public class RpcService {
	private String serviceName;
	private String version;

	public RpcService(String serviceName, String version) {
		this.serviceName = serviceName;
		this.version = version;
	}
	
	public String getServiceName() {
		return serviceName;
	}
	public String getVersion() {
		return version;
	}
	public String getServiceKey() {
		return serviceName + Constant.SERVICE_KEY_SEPERATOR + version;
	}

	public void setServiceName(String serviceKey) {
		this.serviceName = serviceKey;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return this.serviceName + Constant.SERVICE_KEY_SEPERATOR + this.version;
	}
}
