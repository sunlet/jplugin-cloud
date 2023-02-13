package net.jplugin.cloud.rpc.io.util;

import net.jplugin.cloud.rpc.common.config.AbstractConfig;
import net.jplugin.cloud.rpc.common.util.AttachUtils;
import net.jplugin.cloud.rpc.io.bean.RpcRequestBean;
import net.jplugin.cloud.rpc.io.bootstrap.IClient;
import net.jplugin.cloud.rpc.io.channel.IChannel;
import net.jplugin.cloud.rpc.io.future.CallFuture;
import net.jplugin.common.kits.client.InvocationParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoUtils {

	private static final Logger logger = LoggerFactory.getLogger(IoUtils.class);

	@SuppressWarnings("unchecked")
	public static <T> CallFuture<T> write(int serializeType, IChannel channel, String serviceName, String methodName,
			Class<?>[] argsType, Object... args) {
		if (logger.isDebugEnabled()) {
			logger.debug("rpcChannel=" + channel);
		}
		RpcRequestBean request = new RpcRequestBean().createRequest(serviceName, methodName, argsType, args,
				serializeType);
		request.setAttachments(AttachUtils.createAttachments());
		CallFuture<?> future = channel.asyncSend(request, false, null);
		return (CallFuture<T>) future;
	}

	public static boolean isValidClient(IClient client) {
		return client != null && client.isConnected();
	}

	public static boolean isValidChannel(IChannel channel) {
		return channel != null && channel.isConnected();
	}

	public static long getRpcTimeout(InvocationParam invokeParam) {
		Long timeout = AbstractConfig.getDefaultTimeoutInMills();
		if (invokeParam == null || invokeParam.getServiceTimeOut() <= 0) {
			return timeout;
		}
		timeout = (long) invokeParam.getServiceTimeOut();
		return timeout;
	}

}
