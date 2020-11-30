package com.galaxyzeta.client;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.galaxyzeta.common.codec.Serializer;
import com.galaxyzeta.common.codec.SerializerContainer;
import com.galaxyzeta.common.protocol.RpcRequest;
import com.galaxyzeta.common.protocol.RpcResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

	private volatile Channel channel;
	private HashMap<Integer, RpcFuture> pendingRPC = new HashMap<>();
	private AtomicInteger autoIncrementId = new AtomicInteger();
	
	private Serializer serializer = SerializerContainer.getSerializer();

	private static final Logger LOG = LoggerFactory.getLogger(RpcClientHandler.class);

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
		this.channel = ctx.channel();
		LOG.info("Client RPC Channel registered ! {}", channel);
	}

	public RpcFuture sendRequest(RpcRequest request) {
		RpcFuture rpcFuture = new RpcFuture();
		int sessionId = autoIncrementId.getAndIncrement();
		request.setSessionId(sessionId);
		try {
			LOG.info("{}", channel);
			channel.writeAndFlush(request).sync();
			pendingRPC.put(sessionId, rpcFuture);
		} catch (InterruptedException e) {
			LOG.error("Send request failed");
		}
		return rpcFuture;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
		LOG.info("Response received: {}", response);
		RpcFuture rpcFuture = pendingRPC.get(response.getSessionId());
		pendingRPC.remove(response.getSessionId());

		response.setResult(serializer.decode(response.getResult(), response.getReturnType()));
		rpcFuture.done(response);
	}

}
