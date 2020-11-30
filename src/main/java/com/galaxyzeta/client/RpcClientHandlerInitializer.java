package com.galaxyzeta.client;

import com.galaxyzeta.common.protocol.RpcDecoder;
import com.galaxyzeta.common.protocol.RpcEncoder;
import com.galaxyzeta.common.protocol.RpcRequest;
import com.galaxyzeta.common.protocol.RpcResponse;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

public class RpcClientHandlerInitializer extends ChannelInitializer<Channel> {

	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelPipeline pip = ch.pipeline();
		pip.addLast(new RpcDecoder(RpcResponse.class));
		pip.addLast(new RpcEncoder(RpcRequest.class));
		pip.addLast(new RpcClientHandler());

	}
	
}
