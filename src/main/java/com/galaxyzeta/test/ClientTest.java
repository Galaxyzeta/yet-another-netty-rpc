package com.galaxyzeta.test;

import com.galaxyzeta.client.RpcClient;
import com.galaxyzeta.common.zookeeper.CuratorConfig;
import com.galaxyzeta.test.service.HelloService;
import com.galaxyzeta.test.service.Person;


public class ClientTest {
	public static void main(String[] args) throws Exception {
		CuratorConfig zkConfig = new CuratorConfig("localhost", 12181, 5000, 5000);
		RpcClient client = new RpcClient(zkConfig);
		HelloService service = (HelloService)client.createProxyObject(HelloService.class, "1.0");
		System.out.println(service.hello());
		System.out.println(service.hello("asd"));
		System.out.println(service.person(new Person()));
		System.out.println("Sleep for 5 secs");
		Thread.sleep(1000 * 5);
		System.out.println(service.hello());
		System.out.println(service.hello("asd"));
		System.out.println(service.person(new Person()));
	}
}
