package net.jplugin.cloud.rpc.common.filter;

import net.jplugin.core.rclient.api.Client;

import java.lang.reflect.Method;

public class ESFRpcClientFilterContext {
	@SuppressWarnings("rawtypes")
	Client client;
	Object proxy;
	Method method;
	Object[] args;
	int st;

	@SuppressWarnings("rawtypes")
	public ESFRpcClientFilterContext(Client client, Object proxy, Method method, Object[] args, int serializeType) {
		this.client = client;
		this.proxy = proxy;
		this.method = method;
		this.args = args;
		this.st = serializeType;
	}

	@SuppressWarnings("rawtypes")
	public Client getClient() {
		return client;
	}

	public Object getProxy() {
		return proxy;
	}

	public Method getMethod() {
		return method;
	}

	public Object[] getArgs() {
		return args;
	}

	public int getSt() {
		return st;
	}

}
