package net.jplugin.cloud.rpc.io.bootstrap;

import net.jplugin.cloud.rpc.io.channel.IChannel;

import java.util.List;

public interface IServer {

	void boostrap();

//	boolean isBound();

	List<IChannel> getChannels();

	IChannel getChannel(String channelId);

	void destroy();

}
