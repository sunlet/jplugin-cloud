package net.jplugin.cloud.rpc.io.bootstrap;

import net.jplugin.cloud.rpc.common.constant.NettyChannelEvent;
import net.jplugin.cloud.rpc.common.listener.INotifyListener;
import net.jplugin.cloud.rpc.io.channel.IChannel;


public interface IClient {

	void bootstrap(boolean syncAwait);

	boolean isConnected();

	IChannel getChannel();

	void destroy();

	void reconnect();

	void onEvent(INotifyListener<NettyChannelEvent> eventListener);

	void setMaxRetry(int maxRetry);

}
