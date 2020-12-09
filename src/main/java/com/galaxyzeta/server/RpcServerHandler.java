package com.galaxyzeta.server;

import java.lang.reflect.Method;

import com.galaxyzeta.common.codec.Serializer;
import com.galaxyzeta.common.codec.SerializerContainer;
import com.galaxyzeta.common.protocol.RpcRequest;
import com.galaxyzeta.common.protocol.RpcRequestType;
import com.galaxyzeta.common.protocol.RpcResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
	
	private static final Logger LOG = LoggerFactory.getLogger(RpcServerHandler.class);

	private ServiceRegistry serviceRegistry;

	private Serializer serializer = SerializerContainer.getSerializer();

	public RpcServerHandler(ServiceRegistry registry) {
		this.serviceRegistry = registry;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
		// Read request
		RpcRequest request = msg;
		LOG.info("Incoming Request: {}", request);

		// Incoming heart beat.
		if(request.getType() == RpcRequestType.BEAT) {
			LOG.info("Received Heart-Beat from {}", ctx.channel().remoteAddress());
			return;
		}

		// Parameter decode
		Class<?>[] parameters = request.getParameterTypes();
		Object[] args = request.getArgs();
		if(args != null) {
			for(int i=0; i<args.length; i++) {
				args[i] = serializer.decode(args[i], parameters[i]);
			}
		}
		
		// Handle
		RpcResponse response = new RpcResponse();
		try {
			Object result = handle(request, response);
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

	public Object handle(RpcRequest request, RpcResponse response) throws Exception {
		final String serviceKey = request.getServiceKey();
		Object implBean = serviceRegistry.getInstanceByServiceKey(serviceKey);
		if (implBean == null) {
			throw new ClassNotFoundException("Can't find class represented by " + serviceKey);
		}
		Class<?> clazz = implBean.getClass();
		Method method = clazz.getMethod(request.getMethodName(), request.getParameterTypes());
		response.setReturnType(method.getReturnType());
		return method.invoke(implBean, request.getArgs());
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if(evt instanceof IdleStateEvent) {
			LOG.info("Connection time out with remote peer {}", ctx.channel().remoteAddress());
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOG.error("Exception was caught: {}. Closing channel...", cause.getClass());
		ctx.channel().close();
	}
}
