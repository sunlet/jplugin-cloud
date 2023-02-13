package net.jplugin.cloud.rpc.io.bootstrap.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.util.AttributeKey;
import net.jplugin.cloud.rpc.common.bean.ClientHeartBean;
import net.jplugin.cloud.rpc.common.config.AbstractConfig;
import net.jplugin.cloud.rpc.common.config.HostConfig;
import net.jplugin.cloud.rpc.common.constant.NettyChannelEvent;
import net.jplugin.cloud.rpc.common.listener.INotifyListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.jplugin.cloud.rpc.io.bootstrap.IClient;
import net.jplugin.cloud.rpc.io.channel.IChannel;
import net.jplugin.cloud.rpc.io.channel.NettyChannel;
import net.jplugin.cloud.rpc.io.channel.NettyClientChannelInitializer;
import net.jplugin.cloud.rpc.io.handler.RpcMessageHandler;
import net.jplugin.cloud.rpc.io.util.IoUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractClient implements IClient {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractClient.class);

	protected volatile boolean closed;

	protected volatile IChannel channel;

	protected Bootstrap bootstrap;

	protected NioEventLoopGroup workerGroup;

	protected RpcMessageHandler msgHandler;

	protected int workers;

	protected int maxRetrys = 3;

	private int trys = 0;

	private HostConfig remoteHost;

	protected abstract HostConfig getHost();

	private INotifyListener<NettyChannelEvent> eventListener;

	private static final ClientHeartBean clientInfo = new ClientHeartBean();

	private static final AtomicInteger idIndexer = new AtomicInteger(1);

//	/**
//	 * 静态成员，所有实例共享这两个线程，用以监听Client相关的事件
//	 */
//	private static final ExecutorService backExecutors = Executors.newFixedThreadPool(2,
//			new ThreadFactoryBuilder().setDaemon(true).setNameFormat("esf-noticer-%d").build());

	@Override
	public void bootstrap(boolean syncAwait) {
		closed = false;
		trys = 0;
		this.msgHandler = new RpcMessageHandler();
		workerGroup = new NioEventLoopGroup(workers, new ThreadFactoryBuilder().setDaemon(true)
				.setNameFormat("nioEventLoop-" + idIndexer.getAndIncrement() + "-nettyClient-worker-%d").build());
		bootstrap = new Bootstrap();
		bootstrap.group(workerGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, AbstractConfig.getConnectionTimeout())
				.option(ChannelOption.SO_SNDBUF, 1024 * 1024).option(ChannelOption.SO_RCVBUF, 1024 * 1024);
		bootstrap.handler(new NettyClientChannelInitializer(msgHandler, this));
		doConnect(syncAwait);
	}

	@Override
	public void onEvent(INotifyListener<NettyChannelEvent> eventListener) {
		this.eventListener = eventListener;
	}

	@Override
	public IChannel getChannel() {
		ensureActive();
		return this.channel;
	}

	@Override
	public void destroy() {
		try {
			if (logger.isInfoEnabled()) {
				logger.info("client destroy is called, remoteHost=" + remoteHost + ",channel=" + this.channel);
			}
			if (closed) {
				return;
			}
			this.closed = true;
			eventCall(NettyChannelEvent.closed);
			if (this.channel != null) {
				this.channel.close();
			}
			if (this.workerGroup != null) {
				this.workerGroup.shutdownGracefully();
				this.workerGroup = null;
			}
			this.bootstrap = null;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void doConnect(boolean syncAwait) {
		if (closed) {
			if (logger.isInfoEnabled()) {
				logger.info("client connect to remoteHost=" + remoteHost + " had been closed!");
			}
			return;
		} else if (isConnected()) {
			if (logger.isInfoEnabled()) {
				logger.info("client connect to remoteHost=" + remoteHost + " had been connected!");
			}
			return;
		}
		remoteHost = getHost();
		if (logger.isInfoEnabled()) {
			logger.info("begin to connect remoteHost=" + remoteHost);
		}
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(remoteHost.getHostIp(), remoteHost.getPort()));

		future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture after) throws Exception {
				if (after.isSuccess()) {
					if (logger.isInfoEnabled()) {
						logger.info("connect success! server=" + remoteHost);
					}
					closed = false;
					trys = 0;
					initChannel(after.channel());
					eventCall(NettyChannelEvent.connected);
					after.channel().pipeline().fireChannelActive();
					clientInfo.setTimestamp(System.currentTimeMillis());
					after.channel().writeAndFlush(clientInfo);
				} else {
					if (logger.isWarnEnabled()) {
						logger.warn("connect failed, will try after 3~5s...! server=" + remoteHost + ",异常信息："
								+ after.cause());
					}
					eventCall(NettyChannelEvent.disConnected);
					after.channel().close();
					after.channel().pipeline().fireChannelInactive();
				}
			}
		});
		if (syncAwait) {
			future.syncUninterruptibly();
			initChannel(future.channel());
		} else {
			boolean result = future.awaitUninterruptibly(AbstractConfig.getConnectionTimeout(), TimeUnit.MILLISECONDS);
			if (result && future.isSuccess()) {
				initChannel(future.channel());
			}
		}
	}

	private void initChannel(Channel ch) {
		if (IoUtils.isValidChannel(this.channel)) {
			return;
		}
		synchronized (this) {
			if (IoUtils.isValidChannel(this.channel)) {
				return;
			}
			this.channel = new NettyChannel(ch);

		}
	}

	@Override
	public void reconnect() {
		if (isConnected() || closed) {
			return;
		}
		if (trys < maxRetrys) {
			trys++;
			try {
				int waitTime = 3000 + RandomUtils.nextInt(0, 2001);
				TimeUnit.MILLISECONDS.sleep(waitTime);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			if (logger.isInfoEnabled()) {
				logger.info("reconnect mehtod is called for host:" + remoteHost + ",retries=" + trys);
			}
			doConnect(false);
		} else {
			if (logger.isWarnEnabled()) {
				logger.warn("[maxRetries=" + maxRetrys + "] had exhausted, client will stop");
			}
			destroy();
			return;
		}
	}

	public void ensureActive() {
		if (closed) {
			throw new IllegalStateException("netty client is stopped!");
		}
		if (!isConnected()) {
			doConnect(false);
		}
	}

	protected void eventCall(NettyChannelEvent event) {
//		backExecutors.execute(new Runnable() {
//
//			@Override
//			public void run() {
//				try {
//					if (eventListener != null) {
//						if (logger.isInfoEnabled()) {
//							logger.info("EventListener is called for event=" + event + ",host=" + remoteHost);
//						}
//						eventListener.onNotify(event);
//					}
//				} catch (Exception e) {
//					logger.error(e.getMessage(), e);
//				}
//			}
//		});
	}

	@Override
	public void setMaxRetry(int maxRetry) {
		maxRetrys = maxRetry;
		if (maxRetry <= 0) {
			maxRetry = 0;
		}
	}

	@Override
	public boolean isConnected() {
		if (this.channel == null) {
			return false;
		}
		return this.channel.isConnected();
	}

	public HostConfig getCurrentRemoteHost() {
		return this.remoteHost;
	}

}
