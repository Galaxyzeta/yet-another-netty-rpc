package com.galaxyzeta.server;

import com.galaxyzeta.common.codec.RpcDecoder;
import com.galaxyzeta.common.codec.RpcEncoder;
import com.galaxyzeta.common.protocol.RpcRequest;
import com.galaxyzeta.common.protocol.RpcResponse;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

public class RpcServerHandlerInitializer extends ChannelInitializer<Channel> {

	private ServiceRegistry registry;

	public RpcServerHandlerInitializer(ServiceRegistry registry) {
		this.registry = registry;
	}

	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelPipeline pip = ch.pipeline();
		pip.addLast(new RpcDecoder(RpcRequest.class));
		pip.addLast(new RpcEncoder(RpcResponse.class));
		pip.addLast(new RpcServerHandler(registry));
		
	}
	
}
