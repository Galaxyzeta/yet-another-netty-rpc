package com.galaxyzeta.test.service;

import com.galaxyzeta.common.annotation.RpcServiceComponent;

@RpcServiceComponent(interfaceClass = HelloService.class, version = "1.0")
public class HelloServiceImpl implements HelloService {

	@Override
	public String hello() {
		System.out.println("Server OK");
		return "RPC OK";
	}
	
}
