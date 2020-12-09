package com.galaxyzeta.common.util;

import com.galaxyzeta.common.protocol.RpcRequest;
import com.galaxyzeta.common.protocol.RpcRequestType;

public class BeatUtil {

	private static final RpcRequest BEAT = new RpcRequest();

	public static final int BEAT_KEEP_ALIVE = 5;

	static {
		BEAT.setType(RpcRequestType.BEAT);
	}

	public static RpcRequest getBeat() {
		return BEAT;
	}
}
