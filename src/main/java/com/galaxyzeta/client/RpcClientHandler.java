package com.galaxyzeta.client;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.galaxyzeta.common.codec.Serializer;
import com.galaxyzeta.common.codec.SerializerContainer;
import com.galaxyzeta.common.protocol.RpcRequest;
import com.galaxyzeta.common.protocol.RpcResponse;
import com.galaxyzeta.common.protocol.RpcServiceGroup;
import com.galaxyzeta.common.util.BeatUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

	private volatile Channel channel;
	private HashMap<Integer, RpcFuture> pendingRPC = new HashMap<>();
	private AtomicInteger autoIncrementId = new AtomicInteger();

	private RpcServiceGroup serviceGroup;
	
	private Serializer serializer = SerializerContainer.getSerializer();

	private static final Logger LOG = LoggerFactory.getLogger(RpcClientHandler.class);

	public RpcClientHandler(RpcServiceGroup serviceGroup) {
		this.serviceGroup = serviceGroup;
	}

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

	public void sendPing() {
		channel.writeAndFlush(BeatUtil.getBeat());
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
		LOG.info("Response received: {}", response);
		RpcFuture rpcFuture = pendingRPC.get(response.getSessionId());
		pendingRPC.remove(response.getSessionId());

		response.setResult(serializer.decode(response.getResult(), response.getReturnType()));
		rpcFuture.done(response);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			sendPing();
			LOG.info("Send ping to {}", channel.remoteAddress());
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	@Override
	/**
	 * Trigger when server closed the channel. Need to unregister ConnctionManager's connection data.
	 */
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		LOG.warn("Channel closed...");
		ConnectionManager.getInstance().closeConnection(serviceGroup);
		super.channelUnregistered(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOG.error("Exception was caught: {}. Closing channel...", cause.getClass());
		ctx.channel().close();
	}
}
