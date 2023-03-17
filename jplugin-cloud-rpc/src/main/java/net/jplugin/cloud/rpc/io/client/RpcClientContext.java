package net.jplugin.cloud.rpc.io.client;


import net.jplugin.cloud.rpc.common.config.AbstractConfig;
import net.jplugin.cloud.rpc.io.future.CallFuture;
import net.jplugin.cloud.rpc.io.message.RpcMessage;
import net.jplugin.cloud.rpc.io.message.RpcRequest;
import net.jplugin.common.kits.client.ICallback;
import net.jplugin.common.kits.client.InvocationParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

class RpcClientContext {

	private static final Logger logger = LoggerFactory.getLogger(RpcClientContext.class);


	public static Object invokeExecute(ClientChannelHandler channel, String srvName, Method method, Object[] args, String serializeType, InvocationParam invocationParam)
			 {
		return invokeExecute(channel, srvName, method.getName(), method.getGenericParameterTypes(),args, serializeType, invocationParam);
	}

	public static Object invokeExecute(ClientChannelHandler channel, String srvName, String methodName,Type[] argsType, Object[] args, String serializeType, InvocationParam invocationParam)
			 {
		CallFuture<?> cf = null;
		try {
//			Type[] argsType = method.getGenericParameterTypes();

			RpcMessage<RpcRequest> request = RpcMessage.create(RpcMessage.TYPE_CLIENT_REQ);
			request.header(RpcMessage.HEADER_SERIAL_TYPE, serializeType);
			RpcRequest body = new RpcRequest();
			body.setUri(srvName);
			body.setMethodName(methodName);
			body.setArguments(args);
			body.setGenericTypes(argsType);
			request.body(body);

//			request.setAttachments(AttachUtils.createAttachments());
			ICallback callback=null;
			boolean async = false;
			if (invocationParam!=null) {
				callback = invocationParam.getRpcCallback();
				async = invocationParam.getRpcAsync()!=null &&  invocationParam.getRpcAsync();
			}

			cf = channel.asyncSend(request, async, callback);

//			cf = IoUtils.write(serializeType, channel, srvName, method.getName(), argsType, args);
		} catch (Exception e) {
			logger.error("调用[serviceName=" + srvName + ",methodName=" + methodName + "]异常：" + e);
			throw e;
		}
		if (cf != null) {
			try {
				cf.setTimeout(getRpcTimeout(invocationParam));
				return cf.getVal();
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				channel.futureManager.removeFuture(cf.getContextId());
			}
		}
		return null;
	}

	public static long getRpcTimeout(InvocationParam invokeParam) {
		if (invokeParam!=null && invokeParam.getServiceTimeOut()>0){
			return invokeParam.getServiceTimeOut();
		}else{
			return AbstractConfig.getDefaultTimeoutInMills();
		}
	}



//	public static Object invoke4Kryo(ClientChannelHandler channel,String serviceName, String methodName, Object[] args) throws Exception {
//		return invokeExecute(channel,serviceName, methodName, args, IMessageBodySerializer.TYPE_KRYO_REQ);
//	}
//
//	public static Object invoke4Json(ClientChannelHandler channel,String serviceName, String methodName, Object[] args) throws Exception {
//		return invokeExecute(channel,serviceName, methodName, args, IMessageBodySerializer.TYPE_JSON_REQ);
//	}



//	static class IoUtils {
//
//		private static final Logger logger = LoggerFactory.getLogger(IoUtils.class);
//
//		@SuppressWarnings("unchecked")
//		public static <T> CallFuture<T> write(String serializeType, ClientChannelHandler channel, String serviceName, String methodName,
//											  Type[] argsType, Object... args) {
//			if (logger.isDebugEnabled()) {
//				logger.debug("rpcChannel=" + channel);
//			}
//			RpcMessage<RpcRequest> request = RpcMessage.create(RpcMessage.TYPE_CLIENT_REQ);
//			request.header(RpcMessage.HEADER_SERIAL_TYPE, serializeType);
//			RpcRequest body = new RpcRequest();
//			body.setUri(serviceName);
//			body.setMethodName(methodName);
//			body.setArguments(args);
//			body.setGenericTypes(argsType);
//			request.body(body);
//
//
////			request.setAttachments(AttachUtils.createAttachments());
//			CallFuture<?> future = channel.asyncSend(request, false, null);
//			return (CallFuture<T>) future;
//		}
//
//		public static long getRpcTimeout(InvocationParam invokeParam) {
//			if (invokeParam!=null && invokeParam.getServiceTimeOut()>0){
//				return invokeParam.getServiceTimeOut();
//			}else{
//				return AbstractConfig.getDefaultTimeoutInMills();
//			}
//		}
//
//	}
}
