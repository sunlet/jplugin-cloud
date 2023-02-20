package net.jplugin.cloud.rpc.client.extension;

import java.lang.reflect.Method;

import net.jplugin.cloud.rpc.client.imp.RpcClientManager;
import net.jplugin.cloud.rpc.client.kits.RpcUrlKit;
import net.jplugin.common.kits.tuple.Tuple2;
import net.jplugin.core.service.api.RefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jplugin.core.rclient.api.Client;
import net.jplugin.core.rclient.api.IClientHandler;

public class RpcClientHandler implements IClientHandler {

	private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);

	@RefService
	RpcClientManager clientManager;
	/**
	 * 返回代理对象
	 */
	@SuppressWarnings("rawtypes")
	public Object invoke(Client client, Object proxy, Method method, Object[] args) throws Throwable {

		String serviceURL = client.getServiceBaseUrl();
		Tuple2<String, String> urlParseResult = RpcUrlKit.parseEsfUrlInfo(serviceURL);
		String appCode = urlParseResult.first;

		return clientManager.getServiceClient(appCode).invokeRpc(urlParseResult.second, method, args);

//		String serverIp = null;
//		String serverPort = null;
//		boolean async = false;
//		ICallback callback = null;
//		InvocationParam invocationParam = MethodUtil.getAndClearParam();
//		if (invocationParam != null) {
//			String serviceAddress = invocationParam.getServiceAddress();// IP:PORT格式
//			async = (invocationParam.getRpcAsync() == null ? false : invocationParam.getRpcAsync());
//			callback = invocationParam.getRpcCallback();
//			if (!StringKit.isEmpty(serviceAddress)) {
//				String[] ipAndport = serviceAddress.split(":");
//				if (ipAndport.length == 1) {
//					serverIp = ipAndport[0];
//				} else if (ipAndport.length >= 2) {
//					serverIp = ipAndport[0];
//					serverPort = ipAndport[1];
//				}
//			}
//		}





//		boolean debug = AbstractConfig.debugMode();
//		if (logger.isDebugEnabled()) {
//			logger.debug("serverIp=" + serverIp + ",serverPort=" + serverPort + ",serializeType=" + serializeType
//					+ ",async=" + async + ",callback=" + callback + ",debug=" + debug);
//		}
//		if (debug) {
//			if (StringKit.isEmpty(serverIp)) {
//				// 开发模式下优先查找本机IP服务
//				nettyClient = GeneralRouterFactory.getRandomRobinRouter().router(appCode,
//						AbstractContainer.getLocalIp(), null);
//			}
//			if (nettyClient == null) {
//				// 如果未查找到或者指定了IP地址，则按照正常流程查找
//				nettyClient = GeneralRouterFactory.getRandomRobinRouter().router(appCode, serverIp, serverPort);
//			}
//		} else {
//			nettyClient = GeneralRouterFactory.getRandomRobinRouter().router(appCode, serverIp, serverPort);
//		}
//		if (nettyClient == null) {
//			String errorMsg = "[curr-appcode=" + AbstractConfig.getAppcode()
//					+ "] call failed: service inaccessible for server appCode : " + appCode;
//			if (!StringKit.isEmpty(serverIp)) {
//				errorMsg += ",serverIp=" + serverIp;
//			}
//			if (!StringKit.isEmpty(serverPort)) {
//				errorMsg += ",serverPort=" + serverPort;
//			}
//			throw new NullPointerException(errorMsg);
//		}
//		if (logger.isDebugEnabled()) {
//			logger.debug("rpcChannel=" + nettyClient.getChannel());
//		}
//		RpcRequestBean request = new RpcRequestBean().createRequest(ResolverHelper.getServiceName(serviceURL),
//				method.getName(), method.getParameterTypes(), args, serializeType);
//		//request.setAttachments(AttachUtils.createAttachments());
//		request.setRtnclz(method.getGenericReturnType());
//		IChannel nettyChannel = nettyClient.getChannel();
//		if (!IoUtils.isValidChannel(nettyChannel)) {
//			logger.error("channel=" + nettyChannel + " is invalid for " + serviceURL + ", please try again!");
//			throw new NullPointerException("channel=" + nettyChannel + " is invalid for " + serviceURL
//					+ ", please try again!appCode=" + appCode);
//		}
//		if (!async) {
//			// 同步调用
//			return nettyChannel.syncSend(request, IoUtils.getRpcTimeout(invocationParam));
//		} else {
//			// 异步调用
//			nettyChannel.asyncSend(request, true, callback);
//			Class<?> clazz = method.getReturnType();
//			if (clazz != null && clazz.isPrimitive()) {
//				if (logger.isDebugEnabled()) {
//					logger.debug("return clazz type : " + clazz);
//				}
//				if (clazz == boolean.class) {
//					return false;
//				} else if (clazz == char.class) {
//					return 0;
//				} else if (clazz == byte.class) {
//					return 0;
//				} else if (clazz == short.class) {
//					return 0;
//				} else if (clazz == int.class) {
//					return 0;
//				} else if (clazz == long.class) {
//					return 0;
//				} else if (clazz == float.class) {
//					return 0;
//				} else if (clazz == double.class) {
//					return 0;
//				} else if (clazz == void.class) {
//					return Void.TYPE;
//				}
//			}
//			return null;
//		}
	}

}
