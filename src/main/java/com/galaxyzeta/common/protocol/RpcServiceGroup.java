package com.galaxyzeta.common.protocol;

import java.util.List;

public class RpcServiceGroup {

	private int port;
	private String address;
	private List<RpcService> serviceInfoList;
	
	// == Constructor ==
	public RpcServiceGroup() {}

	public RpcServiceGroup(String address, int port, List<RpcService> serviceInfoList) {
		this.address = address;
		this.port = port;
		this.serviceInfoList = serviceInfoList;
	}

	// == Getter ==
	public String getAddress() {
		return address;
	}
	public int getPort() {
		return port;
	}
	public void setServiceInfoList(List<RpcService> serviceInfoList) {
		this.serviceInfoList = serviceInfoList;
	}

	// == Setter ==
	public void setAddress(String address) {
		this.address = address;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public List<RpcService> getServiceInfoList() {
		return serviceInfoList;
	}

	@Override
	public int hashCode() {
		int hash = Integer.hashCode(port) + address.hashCode();
		for (RpcService service : serviceInfoList) {
			hash += service.hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		RpcServiceGroup serviceGroup = (RpcServiceGroup)obj;
		return serviceGroup.getAddress().equals(this.address) &&
			serviceGroup.getPort() == this.port &&
			serviceGroup.getServiceInfoList().equals(this.serviceInfoList);
	}

	@Override
	public String toString() {
		return String.format("{RpcServiceGroup=>%s:%d, %s}", address, port, serviceInfoList);
	}
}
