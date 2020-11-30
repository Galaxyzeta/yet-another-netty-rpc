package com.galaxyzeta.common.util;

public class CommonUtil {
	public static String makeServiceKey(Class<?> clazz, String version) {
		return clazz.getName() + Constant.SERVICE_KEY_SEPERATOR + version;
	}

	public static String makeServiceKey(String clazzName, String version) {
		return clazzName + Constant.SERVICE_KEY_SEPERATOR + version;
	}

	public static String makeConnectionString(String address, int port) {
		return address + ":" + port;
	}
}
