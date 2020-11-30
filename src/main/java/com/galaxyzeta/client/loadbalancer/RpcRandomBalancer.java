package com.galaxyzeta.client.loadbalancer;

import java.util.List;

import com.galaxyzeta.client.ConnectionManager;
import com.galaxyzeta.common.protocol.RpcServiceGroup;

public class RpcRandomBalancer implements RpcLoadBalancer {

	@Override
	public RpcServiceGroup route(List<RpcServiceGroup> serviceGroups) {
		int size = serviceGroups.size();
		return serviceGroups.get((int)(Math.random() * size));
	}

}
