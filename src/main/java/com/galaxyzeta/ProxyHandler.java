package com.galaxyzeta;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyHandler implements InvocationHandler {

	public Object target;

	public ProxyHandler(Object target) {
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("Before method");
		method.invoke(target, args);
		System.out.println("After method");
		return target;
	}

	public static void main(String[] args) {
		IHello hello = new Hello();			// Object to be delegated
		IHello proxyHello = (IHello)Proxy.newProxyInstance(hello.getClass().getClassLoader(), 
			hello.getClass().getInterfaces(), new ProxyHandler(hello));			// Proxy Object
		proxyHello.hello();
	}
	
}
