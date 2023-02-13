package net.jplugin.cloud.rpc.io.util;

import net.jplugin.cloud.rpc.common.constant.SerializerCategory;
import net.jplugin.cloud.rpc.common.util.MethodUtil;
import net.jplugin.cloud.rpc.io.channel.IChannel;
import net.jplugin.cloud.rpc.io.future.CallFuture;
import net.jplugin.common.kits.client.InvocationParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RPClientContext {

	String clientAppcode;

	String clientIP;

	String esfPort;

	ChannelContext chanelContext;

	private static final Logger logger = LoggerFactory.getLogger(RPClientContext.class);

	public RPClientContext(String appcode, String ip, String esfport, IChannel channel) {
		this.clientAppcode = appcode;
		this.clientIP = ip;
		this.esfPort = esfport;
		chanelContext = new ChannelContext(channel);
	}

	class ChannelContext {
		IChannel channel;

		public ChannelContext(IChannel channel) {
			this.channel = channel;
		}

		public Object invokeExecute(String srvName, String methodName, Object[] args, int serializeType)
				throws Exception {
			CallFuture<?> cf = null;
			InvocationParam invocationParam = MethodUtil.getAndClearParam();
			try {
				Class<?>[] argsType = null;
				if (args != null) {
					argsType = new Class<?>[args.length];
					for (int i = 0; i < args.length; i++) {
						argsType[i] = args[i].getClass();
					}
				}
				cf = IoUtils.write(serializeType, channel, srvName, methodName, argsType, args);
			} catch (Exception e) {
				logger.error("调用[serviceName=" + srvName + ",methodName=" + methodName + "]异常：" + e);
				throw e;
			}
			if (cf != null) {
				try {
					cf.setTimeout(IoUtils.getRpcTimeout(invocationParam));
					return cf.getVal();
				} finally {
					FutureUtils.removeFutures(cf.getContextId());
				}
			}
			return null;
		}
	}

	public Object invoke(String serviceName, String methodName, Object[] args) throws Exception {
		return chanelContext.invokeExecute(serviceName, methodName, args, SerializerCategory.kryo);
	}

	public Object invoke4Json(String serviceName, String methodName, Object[] args) throws Exception {
		return chanelContext.invokeExecute(serviceName, methodName, args, SerializerCategory.json);
	}

	public String toString() {
		return "RPClientContext[clientAppcode=" + clientAppcode + ",clientIP=" + clientIP + ",esfPort=" + esfPort
				+ ",channel=" + chanelContext.channel + "]";
	}

	public String getClientAppcode() {
		return clientAppcode;
	}

	public String getClientIP() {
		return clientIP;
	}

	public String getEsfPort() {
		return esfPort;
	}
}
