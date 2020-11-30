package com.galaxyzeta.common.protocol;

import com.galaxyzeta.common.util.Serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder<Object> {

	private Class<?> encodeType;
	private static final Logger LOG = LoggerFactory.getLogger(RpcDecoder.class);

	public RpcEncoder(Class<?> type) {
		this.encodeType = type;
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		byte[] data = Serializer.encode(msg);
		out.writeInt(data.length);
		out.writeBytes(data);
	}
	
}
