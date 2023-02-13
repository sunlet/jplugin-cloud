package net.jplugin.cloud.rpc.io.bootstrap.impl.rpc;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.jplugin.cloud.rpc.io.bootstrap.impl.AbstractServer;
import net.jplugin.cloud.rpc.io.channel.NettyServerChannelInitializer;
import net.jplugin.cloud.rpc.io.handler.RpcMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 服务端启动类
 */
public class NettyServer extends AbstractServer {

	private RpcMessageHandler handler;

	public NettyServer(int port, int boss, int workers) {
		this.serverPort = port;
		this.bossThreads = boss;
		this.workerThreads = workers;
		this.handler = new RpcMessageHandler();
	}

	@Override
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
		this.serverBoot.childHandler(new NettyServerChannelInitializer(handler));
		doBind();
	}
}
