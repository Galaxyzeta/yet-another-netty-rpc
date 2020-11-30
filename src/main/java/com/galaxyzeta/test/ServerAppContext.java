package com.galaxyzeta.test;

import com.galaxyzeta.server.RpcServer;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServerAppContext {
	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-server.xml");
		RpcServer server = (RpcServer)context.getBean("rpcServer");
		server.start();
	}
}
