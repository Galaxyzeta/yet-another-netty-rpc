package com.galaxyzeta.client;

import java.util.concurrent.Semaphore;

import com.galaxyzeta.common.protocol.RpcResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcFuture {

	private RpcResponse response;
	private boolean isDone = false;
	private Semaphore semaphore = new Semaphore(0);
	private Object lock = new Object();
	private static final Logger LOG = LoggerFactory.getLogger(RpcFuture.class);

	public void done(RpcResponse response) {
		if(! isDone) {
			synchronized(lock) {
				if (! isDone) {
					this.response = response; 
					semaphore.release();
					isDone = true;
				}
			}
		} else {
			LOG.warn("Future already done!");
		}
	}

	public boolean isDone() {
		return this.response != null;
	}

	public RpcResponse get() throws InterruptedException {
		RpcResponse result = null;
		semaphore.acquire();
		result = this.response;
		semaphore.release(1);
		return result;
	}
	
}
