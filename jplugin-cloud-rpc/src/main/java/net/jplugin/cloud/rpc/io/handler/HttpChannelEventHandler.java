package net.jplugin.cloud.rpc.io.handler;/*
package net.jplugin.cloud.rpc.io.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpChannelEventHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private HttpMessageHandler handler;

	public HttpChannelEventHandler(HttpMessageHandler handler) {
		this.handler = handler;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		this.handler.dispatcherHttpMsg(ctx, request);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		this.handler.dispatchException(ctx, cause);
		// super.exceptionCaught(ctx, cause);
	}

}
*/
