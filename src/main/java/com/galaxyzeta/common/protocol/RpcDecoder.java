package com.galaxyzeta.common.protocol;

import java.util.List;

import com.galaxyzeta.common.util.Serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class RpcDecoder extends ByteToMessageDecoder {

	private Class<?> decodeType;
	private static final Logger LOG = LoggerFactory.getLogger(RpcDecoder.class);

	public RpcDecoder(Class<?> type) {
		this.decodeType = type;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int datalen = in.readInt();
		byte[] data = new byte[datalen];
		if (in.readableBytes() != datalen) {
			LOG.error("Inconsistent data length. Failed to Decode !");
			return;
		}
		in.readBytes(data);
		out.add(Serializer.decode(data, decodeType));
	}
	
}
