package net.jplugin.cloud.rpc.io.client;

import io.netty.channel.*;
//import net.jplugin.cloud.rpc.common.bean.ClientHeartBean;
import net.jplugin.cloud.rpc.common.config.AbstractConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.jplugin.cloud.rpc.io.handler.RpcClientMessageHandler;
import net.jplugin.cloud.rpc.io.handler.RpcMessageDecoder;
import net.jplugin.cloud.rpc.io.handler.RpcMessageEncoder;
import net.jplugin.cloud.rpc.io.util.ChannelAttributeUtil;
import net.jplugin.common.kits.AssertKit;
import net.jplugin.common.kits.ThreadFactoryBuilder;
import net.jplugin.core.log.api.LogFactory;
import net.jplugin.core.log.api.Logger;


import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyClient{

	protected static final Logger logger = LogFactory.getLogger(NettyClient.class);

	protected volatile boolean clientClosed = true;

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
	private String remoteAddr;

//	protected abstract HostConfig getHost();

//	private INotifyListener<NettyChannelEvent> eventListener;

//	private static final ClientHeartBean clientInfo = new ClientHeartBean();

	private static final AtomicInteger idIndexer = new AtomicInteger(1);

	private static final ExecutorService backExecutors = Executors.newCachedThreadPool(
			new ThreadFactoryBuilder().setDaemon(true).setNameFormat("esf-reconnect-%d").build());

	private long lastTryConnectTime;
	private long connectRetryLimit = 10000;


	public NettyClient(String remoteIp, int port, int workers) {
		this.remoteHostIp = remoteIp;
		this.remoteHostPort = port;
		this.remoteAddr = remoteIp+":"+port;
		this.workers = workers;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	/**
	 * 判断客户端是否已经被关闭
	 * @return
	 */
	public boolean isClientClosed(){
		return this.clientClosed;
	}

//	public boolean isActive(){
//		Channel channel = this.nettyChannel;
//		return channel!=null && this.nettyChannel.isActive();
//	}

	/**
	 * 关闭这个客户端,释放资源，如果再调用bootstrap，会再次启动！
	 */
	public void closeClient(){
		this.clientClosed = true;
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
		return !clientClosed && nettyChannel!=null && nettyChannel.isActive();
	}


	public ClientChannelHandler getClientChannelHandler(){
		if (!clientClosed && this.nettyChannel!=null && nettyChannel.isActive())
			return ChannelAttributeUtil.getOrCreateClientChannelHandler(this.nettyChannel);
		else
			return null;
	}

	public void bootstrap() {
//		AssertKit.assertTrue(!clientClosed);
		if (clientClosed){
			clientClosed = false;
		}

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
		doConnect();
	}

	public String getRemoteHostIp(){
		return this.remoteHostIp;
	}

	public int getRemoteHostPort() {
		return remoteHostPort;
	}

	private String getRemoteHost(){
		return this.remoteHostIp+":"+remoteHostPort;
	}

	public void mainTainConnection() {
		if (logger.isDebugEnabled()){
			logger.debug("maintain connection for:"+this.getRemoteAddr());
		}
		//判断已经连上，或者已关闭
		if (isConnected() || clientClosed) {
			return;
		}

		//判断时间间隔
		long retryTime = System.currentTimeMillis() - this.lastTryConnectTime;
		if (retryTime < this.connectRetryLimit) {
			if (logger.isInfoEnabled()){
				logger.info("connection retry latter:"+ (connectRetryLimit-retryTime)+"  "+this.getRemoteAddr());
			}
			return;
		}else {
			if (logger.isInfoEnabled()){
				logger.info("connection retry now . "+this.getRemoteAddr());
			}
			//connect
			doConnect();
		}
	}

	private void doConnect() {
		//记录上次开始connect时间
		this.lastTryConnectTime = System.currentTimeMillis();

		if (logger.isInfoEnabled()) {
			logger.info("begin to connect remoteHost=" + getRemoteHost());
		}

		ChannelFuture future = bootstrap.connect(new InetSocketAddress(remoteHostIp, remoteHostPort));

		future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture after) throws Exception {
				if (after.isSuccess()) {
//					closed = false;
//					trys = 0;

					initChannel(after.channel());
					if (logger.isInfoEnabled()) {
						logger.info("connection success. " + getRemoteAddr());
					}

//					eventCall(NettyChannelEvent.connected);
//					after.channel().pipeline().fireChannelActive();
//					clientInfo.setTimestamp(System.currentTimeMillis());
//					after.channel().writeAndFlush(clientInfo);
				} else {
					if (logger.isInfoEnabled()) {
						logger.info("connection failed. " + getRemoteAddr());
					}
//					if (logger.isEnabledFor(Logger.WARN) {
//						logger.warn("connect failed, will try after 3~5s...! server=" + getRemoteHost() + ",异常信息："
//								+ after.cause());
//					}
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
