package com.galaxyzeta.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.galaxyzeta.common.protocol.RpcRequest;
import com.galaxyzeta.common.protocol.RpcResponse;
import com.galaxyzeta.common.util.Constant;

public class ObjectProxy implements InvocationHandler {

	private Class<?> interfaceClass;
	private String version;

	public ObjectProxy(Class<?> clazz, String version) {
		this.interfaceClass = clazz;
		this.version = version;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		RpcRequest rpcRequest = new RpcRequest();
		rpcRequest.setArgs(args);
		rpcRequest.setServiceName(interfaceClass.getName());
		rpcRequest.setMethodName(method.getName());
		rpcRequest.setVersion(version);
		
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		RpcClientHandler clientHandler = connectionManager.chooseConnection(rpcRequest.getServiceKey());
		if(clientHandler == null) {
			throw new RuntimeException("Connection Failed!");
		}
		RpcFuture future = clientHandler.sendRequest(rpcRequest);
		RpcResponse resp = future.get();
		if(resp.getError() == false) {
			return resp.getResult();
		} else {
			throw resp.getThrowable();
		}
	}
	
}
