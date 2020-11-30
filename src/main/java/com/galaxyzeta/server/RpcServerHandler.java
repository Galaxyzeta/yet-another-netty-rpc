package com.galaxyzeta.server;

import java.lang.reflect.Method;

import com.galaxyzeta.common.protocol.RpcRequest;
import com.galaxyzeta.common.protocol.RpcResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
	
	private static final Logger LOG = LoggerFactory.getLogger(RpcServerHandler.class);

	private ServiceRegistry serviceRegistry;

	public RpcServerHandler(ServiceRegistry registry) {
		this.serviceRegistry = registry;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
		// Read request
		RpcRequest request = msg;
		LOG.info("Incoming Request: {}", request);

		// Handle
		RpcResponse response = new RpcResponse();
		try {
			Object result = handle(request);
			response.setResult(result);
		} catch(Exception e) {
			LOG.warn("While invoking {}, an exception was thrown: {}", request.getFullMethodName(), e.getClass());
			response.setError(true);
			response.setThrowable(e);
		}

		// Send request
		response.setSessionId(request.getSessionId());
		ctx.writeAndFlush(response).sync();
		LOG.info("Response Sent: {}", response);
	}

	public Object handle(RpcRequest request) throws Exception {
		final String serviceKey = request.getServiceKey();
		Object implBean = serviceRegistry.getInstanceByServiceKey(serviceKey);
		if (implBean == null) {
			throw new ClassNotFoundException("Can't find class represented by " + serviceKey);
		}
		Class<?> clazz = implBean.getClass();
		Method method = clazz.getMethod(request.getMethodName(), request.getParameterTypes());
		return method.invoke(implBean, request.getArgs());
	}
}
