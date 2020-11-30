package com.galaxyzeta.client;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

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

	private static final Logger LOG = LoggerFactory.getLogger(RpcClientHandler.class);

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
		this.channel = ctx.channel();
		LOG.info("Client RPC Channel registered ! {}", channel);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
		LOG.info("Client RPC Channel unregistered ! {}");
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		LOG.info("Client RPC Channel activated !");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
		LOG.info("Client RPC Channel inactivated ! {}");
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
		rpcFuture.done(response);
	}
}
