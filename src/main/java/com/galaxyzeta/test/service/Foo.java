package com.galaxyzeta.test.service;

import com.galaxyzeta.common.annotation.RpcAutowired;

public class Foo {
	
	@RpcAutowired(version = "1.0")
	private HelloService helloService;
}
