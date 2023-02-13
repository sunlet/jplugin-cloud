package net.jplugin.cloud.rpc.io.handler;

import net.jplugin.cloud.rpc.io.channel.NettyChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RpcChannelEventHandler extends ChannelInboundHandlerAdapter {

	private RpcMessageHandler msgHandler;

	public RpcChannelEventHandler(RpcMessageHandler handler) {
		this.msgHandler = handler;
	}

//	@Override
//	public void channelActive(ChannelHandlerContext ctx) throws Exception {
//		try {
//			new NettyChannel(ctx.channel());
//			super.channelActive(ctx);
//		} finally {
//			NettyChannel.removeChannelIfInactive(ctx.channel());
//		}
//	}

//	@Override
//	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//		String channelId = ctx.channel().id().asLongText();
//		NettyChannel.removeChannel(channelId);
//		super.channelInactive(ctx);
//	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//		try {
			this.msgHandler.dispatcher(ctx, msg);
//		} finally {
//			NettyChannel.removeChannelIfInactive(ctx.channel());
//		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//		try {
			this.msgHandler.dispatchException(ctx, cause);
//		} finally {
//			NettyChannel.removeChannelIfInactive(ctx.channel());
//		}
	}
//
//	@Override
//	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//		try {
//			ctx.flush();
//			super.channelReadComplete(ctx);
//		} finally {
//			NettyChannel.removeChannelIfInactive(ctx.channel());
//		}
//	}

}
