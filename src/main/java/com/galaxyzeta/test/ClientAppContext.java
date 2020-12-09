package com.galaxyzeta.test;

import com.galaxyzeta.test.service.Foo;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ClientAppContext {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-client.xml");
		Foo foo = (Foo) context.getBean("foo");
		foo.getHelloService().hello();
		// HelloService service = (HelloService) context.getBean(HelloService.class);
		// System.out.println(service.hello());
		// System.out.println(service.hello("asd"));
		// System.out.println(service.person(new Person()));
		context.close();
	}
}
