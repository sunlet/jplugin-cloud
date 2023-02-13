package net.jplugin.cloud.rpc.io.channel;

import net.jplugin.cloud.rpc.io.bootstrap.IClient;
import net.jplugin.cloud.rpc.io.codec.kryo.NettyKryoDecoder;
import net.jplugin.cloud.rpc.io.codec.kryo.NettyKryoEncoder;
import net.jplugin.cloud.rpc.io.handler.NettyClientRetryHandler;
import net.jplugin.cloud.rpc.io.handler.RpcChannelEventHandler;
import net.jplugin.cloud.rpc.io.handler.RpcMessageHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class NettyClientChannelInitializer extends ChannelInitializer<SocketChannel> {
	private RpcMessageHandler handler;

	private IClient client;

	public NettyClientChannelInitializer(RpcMessageHandler handler, IClient client) {
		this.handler = handler;
		this.client = client;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		pipeline.addLast(new NettyKryoDecoder());
		pipeline.addLast(new NettyKryoEncoder());
		pipeline.addLast(new NettyClientRetryHandler(client));
		pipeline.addLast(new RpcChannelEventHandler(handler));
	}

}
