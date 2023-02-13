package net.jplugin.cloud.rpc.io.channel;

import net.jplugin.cloud.rpc.io.bean.RpcRequestBean;
import net.jplugin.cloud.rpc.io.future.CallFuture;
//import net.jplugin.cloud.rpc.io.util.ClientContextUtil;
import net.jplugin.cloud.rpc.io.util.FutureUtils;
import net.jplugin.cloud.rpc.io.util.IoUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import net.jplugin.common.kits.client.ICallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class NettyChannel implements IChannel {

	private static final Logger logger = LoggerFactory.getLogger(NettyChannel.class);

//	private static final Map<String, NettyChannel> channels = new ConcurrentHashMap<>();

	private transient volatile Channel channel;

	private boolean closed;

	private String channelId;

	private long initTime = System.currentTimeMillis();


	public NettyChannel(Channel channel) {
		if (channel == null || !channel.isActive()) {
			throw new IllegalArgumentException("netty channel is invalid");
		}
//		channelId = channel.id().asLongText();
//		NettyChannel innerChannel = channels.get(channelId);
//		if (!IoUtils.isValidChannel(innerChannel)) {
//			this.channel = channel;
//			closed = false;
//			channels.put(channelId, this);
//		}
	}

	@Override
	public String channelId() {
		return this.channelId;
	}

	@Override
	public SocketAddress localAddress() {
		return this.channel == null ? null : this.channel.localAddress();
	}

	@Override
	public SocketAddress remoteAddress() {
		return this.channel == null ? null : this.channel.remoteAddress();
	}

	@Override
	public boolean isConnected() {
		return this.channel != null && !closed && this.channel.isActive();
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}
		closed = true;
		if (this.channel != null) {
			this.channel.close();
			this.channel = null;
		}
//		channels.remove(channelId);
	}

	@Override
	public Object syncSend(RpcRequestBean request, long timeout) throws Exception {
		CallFuture<?> cf = null;
		try {
			cf = this.asyncSend(request, false, null);
			cf.setTimeout(timeout);
			return cf.getVal();
		} finally {
			if (cf != null) {
				FutureUtils.removeFutures(cf.getContextId());
			}
		}
	}

	private void ensureOpen() {
		if (closed) {
			throw new IllegalStateException("channel is closed");
		}
	}

	private ChannelFuture writeAndFlush(Object obj) {
		return this.channel.writeAndFlush(obj);
	}

	@Override
	public long getInitTime() {
		return this.initTime;
	}

	@Override
	public CallFuture<?> asyncSend(RpcRequestBean request, boolean async, ICallback callback) {
		Objects.requireNonNull(request, "request null!");
		ensureOpen();
		String contentextId = request.getContextId();
		CallFuture<?> callFuture = new CallFuture<>(remoteAddress());
		callFuture.setContextId(contentextId);
		callFuture.setAsync(async);
		callFuture.setCallback(callback);
		callFuture.setRtnclz(request.getRtnclz());
		// 将请求中的返回结果类型置空
		request.setRtnclz(null);
		FutureUtils.addFuture(callFuture);
		this.writeAndFlush(request).addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture cf) throws Exception {
				if (!cf.isSuccess()) {
					logger.error("Request=[" + request + "]异常：" + cf.cause().getMessage(), cf.cause());
					CallFuture<?> cacheFuture = FutureUtils.removeFutures(contentextId);
					if (cacheFuture == null) {
						return;
					}
					cacheFuture.setException(cf.cause(), cf.channel().remoteAddress());
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("请求发送成功，contentextId=" + contentextId + ",currTimestamp="
								+ System.currentTimeMillis() + ",channel=>" + cf.channel());
					}
				}
			}
		});
		return callFuture;
	}

//	public static void removeChannel(String channelId) {
//		if (logger.isDebugEnabled()) {
//			logger.debug("channelsize=" + channels.keySet().size() + ",channels=" + channels);
//		}
//		NettyChannel innerChannel = channels.remove(channelId);
//		if (innerChannel != null) {
//			innerChannel.close();
//			innerChannel = null;
//		}
////		ClientContextUtil.removeClient(channelId);
//	}
//
//	private static IChannel getChannel(String channelId) {
//		return channels.get(channelId);
//	}

	public String toString() {
		return "NettyChannel[ChannelId=" + this.channelId + ",LocalAddress=" + localAddress() + ",RemoteAddress="
				+ remoteAddress() + ",Connected=" + isConnected() + ",Closed=" + closed + ",InitTime(ms)=" + initTime
				+ "]";
	}

//	public static void removeChannelIfInactive(Channel ch) {
//		if (ch != null && !ch.isActive()) {
//			removeChannel(ch.id().asLongText());
//		}
//	}

}
