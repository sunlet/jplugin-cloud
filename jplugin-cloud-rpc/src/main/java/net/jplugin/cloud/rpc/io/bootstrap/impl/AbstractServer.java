package net.jplugin.cloud.rpc.io.bootstrap.impl;

import net.jplugin.cloud.rpc.io.bootstrap.IServer;
import net.jplugin.cloud.rpc.io.channel.IChannel;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractServer implements IServer {

	protected Logger logger = LoggerFactory.getLogger(AbstractServer.class);

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

	@Override
	public List<IChannel> getChannels() {
		return null;
	}

	@Override
	public IChannel getChannel(String channelId) {
		return null;
	}

	@Override
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
		future.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture after) throws Exception {
				if (after.isSuccess()) {
					if (logger.isInfoEnabled()) {
						logger.info("server bootstap success! server-port=" + serverPort);
					}
				} else {
					logger.error("server boostrap failed, will try after 5s...! server-port=" + serverPort + ",异常信息:"
							+ after.cause().getMessage(), after.cause());
					TimeUnit.SECONDS.sleep(5);
					doBind();
				}
			}
		});
		future.syncUninterruptibly();
	}

}
