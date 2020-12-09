package com.galaxyzeta.client;

import com.galaxyzeta.common.codec.RpcDecoder;
import com.galaxyzeta.common.codec.RpcEncoder;
import com.galaxyzeta.common.protocol.RpcRequest;
import com.galaxyzeta.common.protocol.RpcResponse;
import com.galaxyzeta.common.util.BeatUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

public class RpcClientHandlerInitializer extends ChannelInitializer<Channel> {

	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelPipeline pip = ch.pipeline();

		pip.addLast(new IdleStateHandler(0, 0, BeatUtil.BEAT_KEEP_ALIVE));
		pip.addLast(new RpcDecoder(RpcResponse.class));
		pip.addLast(new RpcEncoder(RpcRequest.class));
		pip.addLast(new RpcClientHandler());

	}
	
}
