package com.galaxyzeta.common.util;

import java.io.ObjectInputStream;

import com.alibaba.fastjson.JSON;

public class Serializer {

	public static byte[] encode(Object item) {
		return JSON.toJSONString(item).getBytes();
	}

	public static Object decode(byte[] stream, Class<?> clazz) {
		return JSON.parseObject(new String(stream), clazz);
	}
}
