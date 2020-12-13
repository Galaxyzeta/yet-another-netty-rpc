package com.galaxyzeta.common.protocol;

import java.io.Serializable;

public class RpcResponse implements Serializable {
	

	private static final long serialVersionUID = 468626120186808518L;
	
	private boolean error;
	private Object result;
	private Class<?> returnType;
	private int sessionId;
	private Throwable throwable;

	// == getter ==
	public Object getResult() {
		return result;
	}
	public int getSessionId() {
		return sessionId;
	}
	public boolean getError() {
		return error;
	}
	public Throwable getThrowable() {
		return throwable;
	}
	public Class<?> getReturnType() {
		return returnType;
	}

	// == setter ==
	public void setError(boolean error) {
		this.error = error;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}
	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}
	public void setReturnType(Class<?> returnType) {
		this.returnType = returnType;
	}

	// == tostring ==
	@Override
	public String toString() {
		return String.format("RpcResponse{id=%s, error=%s, result=%s, type=%s, throwable=%s}", sessionId, error, result, returnType, throwable);
	}
}
