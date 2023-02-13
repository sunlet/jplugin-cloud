package net.jplugin.cloud.rpc.io.channel;

import net.jplugin.cloud.rpc.io.bean.RpcRequestBean;
import net.jplugin.cloud.rpc.io.future.CallFuture;
import net.jplugin.common.kits.client.ICallback;

import java.net.SocketAddress;

public interface IChannel {

	// 取得channel ID
	String channelId();

	// 取得本地的地址
	SocketAddress localAddress();

	// 取得远程地址
	SocketAddress remoteAddress();

	// 连接是否建立
	boolean isConnected();

	// 关闭channel
	void close();

	// 不带回调函数的同步写
	Object syncSend(RpcRequestBean request, long timeout) throws Exception;

	// 带回写的异步调用
	CallFuture<?> asyncSend(RpcRequestBean request, boolean async, ICallback callback);

	// 初始時間
	long getInitTime();

}
