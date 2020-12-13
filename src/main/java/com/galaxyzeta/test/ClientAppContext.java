package com.galaxyzeta.test;

import com.galaxyzeta.test.service.Foo;
import com.galaxyzeta.test.service.HelloService;
import com.galaxyzeta.test.service.Person;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ClientAppContext {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-client.xml");
		Foo foo = (Foo) context.getBean("foo");
		HelloService service = foo.getHelloService();
		service.hello();
		Person person = new Person();
		Person person2 = service.person(person);
		System.out.println(person == person2);	// false
		// HelloService service = (HelloService) context.getBean(HelloService.class);
		// System.out.println(service.hello());
		// System.out.println(service.hello("asd"));
		// System.out.println(service.person(new Person()));
		context.close();
	}
}
