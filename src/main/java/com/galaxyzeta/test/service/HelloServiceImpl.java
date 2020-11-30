package com.galaxyzeta.test.service;

import com.galaxyzeta.common.annotation.RpcServiceComponent;

@RpcServiceComponent(interfaceClass = HelloService.class, version = "1.0")
public class HelloServiceImpl implements HelloService {

	@Override
	public String hello() {
		System.out.println("Server OK");
		return "RPC OK";
	}

	@Override
	public String hello(String data) {
		System.out.println("Server OK, data is " + data);
		return data;
	}

	@Override
	public Person person(Person person) {
		person.setUsername("asd");
		return person;
	}
	
}
