package com.galaxyzeta.client.loadbalancer;

import java.util.List;

import com.galaxyzeta.common.protocol.RpcServiceGroup;

public interface RpcLoadBalancer {
	RpcServiceGroup route(List<RpcServiceGroup> serviceGroups);
}
