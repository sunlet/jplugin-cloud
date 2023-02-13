package net.jplugin.cloud.rpc.io.handler;

import net.jplugin.cloud.rpc.io.bootstrap.IClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClientRetryHandler extends ChannelInboundHandlerAdapter {
	protected static final Logger logger = LoggerFactory.getLogger(NettyClientRetryHandler.class);
	private IClient client;

	public NettyClientRetryHandler(IClient client) {
		this.client = client;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		if (this.client != null) {
			this.client.reconnect();
		}
	}

}
