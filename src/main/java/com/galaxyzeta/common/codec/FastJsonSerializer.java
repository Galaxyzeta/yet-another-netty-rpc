package com.galaxyzeta.common.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class FastJsonSerializer implements Serializer {

	@Override
	public byte[] encode(Object item) {
		return JSON.toJSONString(item).getBytes();
	}

	@Override
	public Object decode(byte[] stream, Class<?> clazz) {
		return JSON.parseObject(new String(stream), clazz);
	}

	@Override
	public Object decode(Object target, Class<?> clazz) {
		if(target instanceof JSONObject)
			return JSON.parseObject(target.toString(), clazz);
		return target;
	}
}
