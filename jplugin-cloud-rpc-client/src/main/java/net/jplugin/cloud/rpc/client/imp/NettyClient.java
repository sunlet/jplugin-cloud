//package net.jplugin.cloud.rpc.client.imp;
//
//import java.net.InetSocketAddress;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import net.jplugin.core.log.api.LogFactory;
//
//import com.google.common.util.concurrent.ThreadFactoryBuilder;
//
//import io.netty.bootstrap.Bootstrap;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelFutureListener;
//import io.netty.channel.ChannelOption;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import net.jplugin.core.log.api.Logger;
//import net.jplugin.cloud.rpc.RpcMessageHandler;
//
//public  class NettyClient {
//
//    protected static final Logger logger = LogFactory.getLogger(NettyClient.class);
//
//    protected volatile boolean closed;
//
//    protected Bootstrap bootstrap;
//
//    protected NioEventLoopGroup workerGroup;
//
//    protected RpcMessageHandler msgHandler;
//
//    protected int workers;
//
//    protected int maxRetrys = 3;
//
//    private int trys = 0;
//
//    private HostConfig remoteHost;
//
//    private INotifyListener<NettyChannelEvent> eventListener;
//
//    private static final AtomicInteger idIndexer = new AtomicInteger(1);
//
//    private static final ExecutorService backExecutors = Executors.newFixedThreadPool(2,
//            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("esf-noticer-%d").build());
//
//
//    public void bootstrap(boolean syncAwait) {
//        closed = false;
//        trys = 0;
//        this.msgHandler = new RpcMessageHandler();
//        workerGroup = new NioEventLoopGroup(workers, new ThreadFactoryBuilder().setDaemon(true)
//                .setNameFormat("nioEventLoop-" + idIndexer.getAndIncrement() + "-nettyClient-worker-%d").build());
//        bootstrap = new Bootstrap();
//        bootstrap.group(workerGroup);
//        bootstrap.channel(NioSocketChannel.class);
//        bootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true)
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, AbstractConfig.getConnectionTimeout())
//                .option(ChannelOption.SO_SNDBUF, 1024 * 1024).option(ChannelOption.SO_RCVBUF, 1024 * 1024);
//        bootstrap.handler(new NettyClientChannelInitializer(msgHandler, this));
//        doConnect(syncAwait);
//    }
//
//    public IChannel getChannel() {
//        ensureActive();
//        return this.channel;
//    }
//
//
//    public void destroy() {
//        try {
//            if (logger.isInfoEnabled()) {
//                logger.info("client destroy is called, remoteHost=" + remoteHost + ",channel=" + this.channel);
//            }
//            if (closed) {
//                return;
//            }
//            this.closed = true;
//            eventCall(NettyChannelEvent.closed);
//            if (this.channel != null) {
//                this.channel.close();
//            }
//            if (this.workerGroup != null) {
//                this.workerGroup.shutdownGracefully();
//                this.workerGroup = null;
//            }
//            this.bootstrap = null;
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//    }
//
//    private void doConnect(boolean syncAwait) {
//        if (closed) {
//            if (logger.isInfoEnabled()) {
//                logger.info("client connect to remoteHost=" + remoteHost + " had been closed!");
//            }
//            return;
//        } else if (isConnected()) {
//            if (logger.isInfoEnabled()) {
//                logger.info("client connect to remoteHost=" + remoteHost + " had been connected!");
//            }
//            return;
//        }
//        remoteHost = getHost();
//        if (logger.isInfoEnabled()) {
//            logger.info("begin to connect remoteHost=" + remoteHost);
//        }
//        ChannelFuture future = bootstrap.connect(new InetSocketAddress(remoteHost.getHostIp(), remoteHost.getPort()));
//
//        future.addListener(new ChannelFutureListener() {
//            public void operationComplete(ChannelFuture after) throws Exception {
//                if (after.isSuccess()) {
//                    if (logger.isInfoEnabled()) {
//                        logger.info("connect success! server=" + remoteHost);
//                    }
//                    closed = false;
//                    trys = 0;
//                    initChannel(after.channel());
//                    eventCall(NettyChannelEvent.connected);
//                    after.channel().pipeline().fireChannelActive();
//                    clientInfo.setTimestamp(System.currentTimeMillis());
//                    after.channel().writeAndFlush(clientInfo);
//                } else {
//                    if (logger.isWarnEnabled()) {
//                        logger.warn("connect failed, will try after 3~5s...! server=" + remoteHost + ",异常信息："
//                                + after.cause());
//                    }
//                    eventCall(NettyChannelEvent.disConnected);
//                    after.channel().close();
//                    after.channel().pipeline().fireChannelInactive();
//                }
//            }
//        });
//        if (syncAwait) {
//            future.syncUninterruptibly();
//            initChannel(future.channel());
//        } else {
//            boolean result = future.awaitUninterruptibly(AbstractConfig.getConnectionTimeout(), TimeUnit.MILLISECONDS);
//            if (result && future.isSuccess()) {
//                initChannel(future.channel());
//            }
//        }
//    }
//
//    private void initChannel(Channel ch) {
//        if (IoUtils.isValidChannel(this.channel)) {
//            return;
//        }
//        synchronized (this) {
//            if (IoUtils.isValidChannel(this.channel)) {
//                return;
//            }
//            this.channel = new NettyChannel(ch);
//        }
//    }
//
//    @Override
//    public void reconnect() {
//        if (isConnected() || closed) {
//            return;
//        }
//        if (trys < maxRetrys) {
//            trys++;
//            try {
//                int waitTime = 3000 + RandomUtils.nextInt(0, 2001);
//                TimeUnit.MILLISECONDS.sleep(waitTime);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//            if (logger.isInfoEnabled()) {
//                logger.info("reconnect mehtod is called for host:" + remoteHost + ",retries=" + trys);
//            }
//            doConnect(false);
//        } else {
//            if (logger.isWarnEnabled()) {
//                logger.warn("[maxRetries=" + maxRetrys + "] had exhausted, client will stop");
//            }
//            destroy();
//            return;
//        }
//    }
//
//    public void ensureActive() {
//        if (closed) {
//            throw new IllegalStateException("netty client is stopped!");
//        }
//        if (!isConnected()) {
//            doConnect(false);
//        }
//    }
//
//    protected void eventCall(NettyChannelEvent event) {
//        backExecutors.execute(new Runnable() {
//
//            @Override
//            public void run() {
//                try {
//                    if (eventListener != null) {
//                        if (logger.isInfoEnabled()) {
//                            logger.info("EventListener is called for event=" + event + ",host=" + remoteHost);
//                        }
//                        eventListener.onNotify(event);
//                    }
//                } catch (Exception e) {
//                    logger.error(e.getMessage(), e);
//                }
//            }
//        });
//    }
//
//    @Override
//    public void setMaxRetry(int maxRetry) {
//        maxRetrys = maxRetry;
//        if (maxRetry <= 0) {
//            maxRetry = 0;
//        }
//    }
//
//    @Override
//    public boolean isConnected() {
//        if (this.channel == null) {
//            return false;
//        }
//        return this.channel.isConnected();
//    }
//
//    public HostConfig getCurrentRemoteHost() {
//        return this.remoteHost;
//    }
//
//}
