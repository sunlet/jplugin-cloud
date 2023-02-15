package net.jplugin.cloud.rpc.io.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.*;
import net.jplugin.cloud.rpc.common.bean.ClientHeartBean;
import net.jplugin.cloud.rpc.common.config.AbstractConfig;
import net.jplugin.cloud.rpc.common.constant.NettyChannelEvent;
import net.jplugin.cloud.rpc.common.listener.INotifyListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.jplugin.cloud.rpc.io.handler.RpcClientMessageHandler;
import net.jplugin.cloud.rpc.io.handler.RpcMessageDecoder;
import net.jplugin.cloud.rpc.io.handler.RpcMessageEncoder;
import net.jplugin.cloud.rpc.io.util.ChannelAttributeUtil;
import net.jplugin.common.kits.AssertKit;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyClient{

	protected static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

	protected volatile boolean closeClosed;

//	protected volatile IChannel channel;

	protected volatile Channel nettyChannel;

	protected Bootstrap bootstrap;

	protected NioEventLoopGroup workerGroup;

	protected int workers;

//	protected int maxRetrys = 3;

//	private int trys = 0;

//	private HostConfig remoteHost;

	private String remoteHostIp;
	private int remoteHostPort;

//	protected abstract HostConfig getHost();

	private INotifyListener<NettyChannelEvent> eventListener;

	private static final ClientHeartBean clientInfo = new ClientHeartBean();

	private static final AtomicInteger idIndexer = new AtomicInteger(1);

	private static final ExecutorService backExecutors = Executors.newCachedThreadPool(
			new ThreadFactoryBuilder().setDaemon(true).setNameFormat("esf-reconnect-%d").build());

	private long lastTryConnectTime;
	private long connectRetryLimit = 5000;


	public NettyClient(String remoteIp, int port, int workers) {
		this.remoteHostIp = remoteIp;
		this.remoteHostPort = port;
		this.workers = workers;
	}

	/**
	 * 判断客户端是否已经被关闭
	 * @return
	 */
	public boolean isClientClosed(){
		return this.closeClosed;
	}

	/**
	 * 关闭这个客户端
	 */
	public void closeClient(){
		this.closeClosed = true;

		try {
			if (this.nettyChannel != null && this.nettyChannel.isOpen()) {
				try {
					this.nettyChannel.close();
				}catch(Exception e){
					logger.error(e.getMessage(), e);
				}
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

	public boolean isConnected() {
		return !closeClosed && nettyChannel!=null && nettyChannel.isActive();
	}


	public ClientChannelHandler getClientChannelHandler(){
		if (!closeClosed && this.nettyChannel!=null && nettyChannel.isActive())
			return ChannelAttributeUtil.getOrCreateClientChannelHandler(this.nettyChannel);
		else
			return null;
	}

	public void bootstrap(boolean syncAwait) {
		AssertKit.assertTrue(!closeClosed );

//		trys = 0;
		workerGroup = new NioEventLoopGroup(workers, new ThreadFactoryBuilder().setDaemon(true)
				.setNameFormat("nioEventLoop-" + idIndexer.getAndIncrement() + "-nettyClient-worker-%d").build());
		bootstrap = new Bootstrap();
		bootstrap.group(workerGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, AbstractConfig.getConnectionTimeout())
				.option(ChannelOption.SO_SNDBUF, 1024 * 1024).option(ChannelOption.SO_RCVBUF, 1024 * 1024);
		bootstrap.handler(new ChannelInitializer(){
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(new RpcMessageDecoder());
				pipeline.addLast(new RpcMessageEncoder());
				pipeline.addLast(new RpcClientMessageHandler());
			}
		});
		doConnect(syncAwait);
	}


	private String getRemoteHost(){
		return this.remoteHostIp+":"+remoteHostPort;
	}

	public void mainTainConnection() {
		//判断已经连上，或者已关闭
		if (isConnected() || closeClosed) {
			return;
		}

		//判断时间间隔
		if (System.currentTimeMillis() - this.lastTryConnectTime < this.connectRetryLimit)
			return;

		//connect
		doConnect(false);
	}

	private void doConnect(boolean syncAwait) {
		//记录上次开始connect时间
		this.lastTryConnectTime = System.currentTimeMillis();

		if (logger.isInfoEnabled()) {
			logger.info("begin to connect remoteHost=" + getRemoteHost());
		}

		ChannelFuture future = bootstrap.connect(new InetSocketAddress(remoteHostIp, remoteHostPort));

		future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture after) throws Exception {
				if (after.isSuccess()) {
					if (logger.isInfoEnabled()) {
						logger.info("connect success! server=" + getRemoteHost());
					}
//					closed = false;
//					trys = 0;

					initChannel(after.channel());

//					eventCall(NettyChannelEvent.connected);
//					after.channel().pipeline().fireChannelActive();
//					clientInfo.setTimestamp(System.currentTimeMillis());
//					after.channel().writeAndFlush(clientInfo);
				} else {
					if (logger.isWarnEnabled()) {
						logger.warn("connect failed, will try after 3~5s...! server=" + getRemoteHost() + ",异常信息："
								+ after.cause());
					}
//					eventCall(NettyChannelEvent.disConnected);
//					after.channel().close();
//					after.channel().pipeline().fireChannelInactive();
				}
			}
		});



//		if (syncAwait) {
//			future.syncUninterruptibly();
//			initChannel(future.channel());
//		} else {
//			boolean result = future.awaitUninterruptibly(AbstractConfig.getConnectionTimeout(), TimeUnit.MILLISECONDS);
//
//			if (result && future.isSuccess()) {
//				initChannel(future.channel());
//			}
//		}
	}

	private void initChannel(Channel c){
		AssertKit.assertTrue(c!=null && c.isActive());
		this.nettyChannel = c;

//		this.nettyChannel.closeFuture().addListener((future)->{
//			this.reconnect();
//		});

		getClientChannelHandler();
	}






}
