package com.galaxyzeta.common.protocol;

import java.io.Serializable;

import com.galaxyzeta.common.util.Constant;

public class RpcRequest implements Serializable {

	private static final long serialVersionUID = 5807839488510451542L;

	private String serviceName;
	private String methodName;
	private Object[] args;
	private Class<?>[] parameterTypes;
	private String version;
	private int sessionId;
	private RpcRequestType type = RpcRequestType.REQUEST;

	// == getter ==
	public Object[] getArgs() {
		return args;
	}
	public String getServiceName() {
		return serviceName;
	}
	public final String getServiceKey() {
		return serviceName + Constant.SERVICE_KEY_SEPERATOR + version;
	}
	public int getSessionId() {
		return sessionId;
	}
	public String getVersion() {
		return version;
	}
	public String getMethodName() {
		return methodName;
	}
	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}
	public RpcRequestType getType() {
		return type;
	}

	// == setter ==
	public void setArgs(Object[] args) {
		this.args = args;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}
	public void setType(RpcRequestType type) {
		this.type = type;
	}

	// == Common ==
	public String getFullMethodName() {
		return this.getServiceKey() + "|" + this.methodName;
	}

	// == tostring ==
	@Override
	public String toString() {
		if (type == RpcRequestType.BEAT)
			return String.format("Beat{}");
		return String.format("RpcRequest{id=%s, target=%s, args=%s, types=%s}", sessionId, getFullMethodName(), args, parameterTypes);
	}
}