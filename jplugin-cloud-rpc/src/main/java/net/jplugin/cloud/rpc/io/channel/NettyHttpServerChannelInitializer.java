package net.jplugin.cloud.rpc.io.channel;/*
package net.jplugin.cloud.rpc.io.channel;

import net.jplugin.cloud.rpc.io.handler.HttpChannelEventHandler;
import net.jplugin.cloud.rpc.io.handler.HttpMessageHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class NettyHttpServerChannelInitializer extends ChannelInitializer<SocketChannel> {
	private HttpMessageHandler handler;

	public NettyHttpServerChannelInitializer(HttpMessageHandler handler) {
		this.handler = handler;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();		
		pipeline.addLast(new HttpRequestDecoder());
		pipeline.addLast(new HttpObjectAggregator(65536));
		pipeline.addLast(new HttpResponseEncoder());
		pipeline.addLast(new ChunkedWriteHandler());
		pipeline.addLast(new HttpChannelEventHandler(this.handler));
	}

}
*/
