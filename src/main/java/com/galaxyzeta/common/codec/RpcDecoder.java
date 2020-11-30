package com.galaxyzeta.common.codec;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class RpcDecoder extends ByteToMessageDecoder {

	private Class<?> decodeType;
	
	private Serializer serializer = SerializerContainer.getSerializer();
	
	private static final Logger LOG = LoggerFactory.getLogger(RpcDecoder.class);

	public RpcDecoder(Class<?> type) {
		this.decodeType = type;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if(in.readableBytes() < 4) {
			return;		// cannot read even a int.
		}
		in.markReaderIndex();
		int datalen = in.readInt();
		int k;
		if ((k = in.readableBytes()) != datalen) {
			LOG.info("Receiving data, Expect: {}, Actual: {}", datalen, k);
			in.resetReaderIndex();
			return;
		}
		byte[] data = new byte[datalen];
		in.readBytes(data);
		out.add(serializer.decode(data, decodeType));
	}
	
}
