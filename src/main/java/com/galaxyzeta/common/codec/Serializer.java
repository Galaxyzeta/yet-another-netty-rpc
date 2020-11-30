package com.galaxyzeta.common.codec;

public interface Serializer {

	byte[] encode(Object item);

	Object decode(byte[] stream, Class<?> clazz);

	Object decode(Object jsonObject, Class<?> clazz);

}
