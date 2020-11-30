package com.galaxyzeta.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.galaxyzeta.common.protocol.RpcResponse;
import com.galaxyzeta.test.service.Person;

public class SerializationTest {
	public static void main(String[] args) {
		RpcResponse response = new RpcResponse();
		response.setSessionId(10);
		Person person = new Person();
		person.setUsername("asd");
		response.setResult(person);
		
		// Serializer serializer = new FastJsonSerializer();
		byte[] bytes = JSON.toJSONString(response).getBytes();
		RpcResponse resp = JSON.parseObject(new String(bytes), new TypeReference<RpcResponse>(){});
		System.out.println(resp.getResult());
	}
}
