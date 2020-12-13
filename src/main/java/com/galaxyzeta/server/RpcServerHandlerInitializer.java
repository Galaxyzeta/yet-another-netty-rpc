package com.galaxyzeta.server;

import java.util.concurrent.TimeUnit;

import com.galaxyzeta.common.codec.RpcDecoder;
import com.galaxyzeta.common.codec.RpcEncoder;
import com.galaxyzeta.common.protocol.RpcRequest;
import com.galaxyzeta.common.protocol.RpcResponse;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

public class RpcServerHandlerInitializer extends ChannelInitializer<Channel> {

	private ServiceRegistry registry;

	private Integer serverTimeout;

	public RpcServerHandlerInitializer(ServiceRegistry registry, int serverTimeout) {
		this.registry = registry;
		this.serverTimeout = serverTimeout;
	}

	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelPipeline pip = ch.pipeline();
		pip.addLast(new IdleStateHandler(0,0,serverTimeout,TimeUnit.SECONDS));
		pip.addLast(new RpcDecoder(RpcRequest.class));
		pip.addLast(new RpcEncoder(RpcResponse.class));
		pip.addLast(new RpcServerHandler(registry));
		
	}
	
}
