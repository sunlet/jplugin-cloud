package net.jplugin.cloud.rpc.io.bootstrap.impl.rpc;

import net.jplugin.cloud.rpc.common.config.HostConfig;
import net.jplugin.cloud.rpc.io.bootstrap.impl.AbstractClient;
import net.jplugin.cloud.rpc.io.handler.RpcMessageHandler;

public class NettyClient extends AbstractClient {

	private HostConfig remoteHost;

	public NettyClient(String remoteIp, int port, int workers) {
		this.remoteHost = new HostConfig(remoteIp, port);
		msgHandler = new RpcMessageHandler();
		super.workers = workers;
	}

	@Override
	protected HostConfig getHost() {
		return this.remoteHost;
	}

}
