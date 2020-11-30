package com.galaxyzeta.common.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializerContainer {
	
	private static Class<? extends Serializer> serializerClass = FastJsonSerializer.class;
	private static volatile Serializer serializer;
	private static final Logger LOG = LoggerFactory.getLogger(SerializerContainer.class);
	private static Object lock = new Object();

	public static Serializer getSerializer() {
		if(serializer == null) {
			synchronized(lock) {
				if(serializer == null) {
					try {
						serializer = serializerClass.getDeclaredConstructor().newInstance();
					} catch (Exception e) {
						LOG.error("Create serializer failed! cause: {}", e.getMessage());
					}
				}
			}
		}
		return serializer;
	}
}
