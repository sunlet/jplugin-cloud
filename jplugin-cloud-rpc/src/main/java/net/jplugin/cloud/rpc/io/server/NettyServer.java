package net.jplugin.cloud.rpc.io.server;


import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import net.jplugin.cloud.rpc.io.handler.RpcMessageDecoder;
import net.jplugin.cloud.rpc.io.handler.RpcMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.jplugin.cloud.rpc.io.handler.RpcServerMessageHandler;
import net.jplugin.common.kits.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 服务端启动类
 */
public class NettyServer {
	protected Logger logger = LoggerFactory.getLogger(NettyServer.class);

	protected NioEventLoopGroup boss;

	protected NioEventLoopGroup workers;

	protected ServerBootstrap serverBoot;

	protected boolean closed;

	protected int serverPort;

	protected int bossThreads;

	protected int workerThreads;

//	@Override
//	public boolean isBound() {
//		return false;
//	}


	public NettyServer(int port, int boss, int workers) {
		this.serverPort = port;
		this.bossThreads = boss;
		this.workerThreads = workers;
	}

	public void boostrap() {
		closed = false;
		this.serverBoot = new ServerBootstrap();
		this.boss = new NioEventLoopGroup(bossThreads,
				new ThreadFactoryBuilder().setDaemon(true).setNameFormat("nioEventLoop-nettyServer-boss-%d").build());
		this.workers = new NioEventLoopGroup(workerThreads,
				new ThreadFactoryBuilder().setDaemon(true).setNameFormat("nioEventLoop-nettyServer-worker-%d").build());
		this.serverBoot.group(boss, workers);
		this.serverBoot.option(ChannelOption.SO_BACKLOG, 1024);
		this.serverBoot.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		this.serverBoot.option(ChannelOption.SO_REUSEADDR, true);

		this.serverBoot.childOption(ChannelOption.SO_RCVBUF, 1024 * 1024);
		this.serverBoot.childOption(ChannelOption.SO_SNDBUF, 1024 * 1024);
		this.serverBoot.childOption(ChannelOption.TCP_NODELAY, true);
		this.serverBoot.childOption(ChannelOption.SO_KEEPALIVE, true);
		this.serverBoot.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

		this.serverBoot.channel(NioServerSocketChannel.class);

		this.serverBoot.childHandler(new ChannelInitializer(){
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(new RpcMessageDecoder());
				pipeline.addLast(new RpcMessageEncoder());
				pipeline.addLast(new RpcServerMessageHandler());
			}
		});
		doBind();
	}

	public void destroy() {
		if (closed) {
			return;
		}
		closed = true;
		if (boss != null) {
			boss.shutdownGracefully();
			boss = null;
		}
		if (workers != null) {
			workers.shutdownGracefully();
			workers = null;
		}
		if (serverBoot != null) {
			serverBoot = null;
		}
	}

	protected void doBind() {
		if (closed) {
			return;
		}
		ChannelFuture future = this.serverBoot.bind(serverPort);
//		future.addListener(new ChannelFutureListener() {
//
//			@Override
//			public void operationComplete(ChannelFuture after) throws Exception {
//				if (after.isSuccess()) {
//					if (logger.isInfoEnabled()) {
//						logger.info("server bootstap success! server-port=" + serverPort);
//					}
//				} else {
//					logger.error("server boostrap failed, will try after 5s...! server-port=" + serverPort + ",异常信息:"
//							+ after.cause().getMessage(), after.cause());
//					TimeUnit.SECONDS.sleep(5);
//					doBind();
//				}
//			}
//		});
		future.syncUninterruptibly();
		if (!future.isSuccess()){
			throw new RuntimeException("ESF Server start failed, port bind error!",future.cause());
		}

	}
}
