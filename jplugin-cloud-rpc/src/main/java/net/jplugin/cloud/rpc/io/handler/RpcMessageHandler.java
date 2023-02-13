package net.jplugin.cloud.rpc.io.handler;

import com.esotericsoftware.kryo.KryoException;
import net.jplugin.cloud.common.CloudEnvironment;
import net.jplugin.cloud.rpc.common.bean.ClientHeartBean;
import net.jplugin.cloud.rpc.common.constant.AppConstants;
import net.jplugin.cloud.rpc.common.constant.ReportMonitorConstant;
import net.jplugin.cloud.rpc.common.constant.SerializerCategory;
import net.jplugin.cloud.rpc.common.util.ExceptionUtils;
import net.jplugin.cloud.rpc.common.util.MethodUtil;
import net.jplugin.cloud.rpc.common.util.ReportUtils;
import net.jplugin.cloud.rpc.common.util.StringUtils;
import net.jplugin.cloud.rpc.io.bean.RpcRequestBean;
import net.jplugin.cloud.rpc.io.bean.RpcRespBean;
import net.jplugin.cloud.rpc.io.channel.NettyChannel;
import net.jplugin.cloud.rpc.io.future.CallFuture;
//import net.jplugin.cloud.rpc.io.util.ClientContextUtil;
import net.jplugin.cloud.rpc.io.util.FutureUtils;
import net.jplugin.cloud.rpc.io.util.ThreadPoolManager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.jplugin.cloud.rpc.report.ReportClient;
import net.jplugin.common.kits.JsonKit;
import net.jplugin.common.kits.StringKit;
import net.jplugin.core.rclient.api.RemoteExecuteException;
import net.jplugin.core.service.impl.esf.ESFHelper2;
import net.jplugin.core.service.impl.esf.ESFRPCContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class RpcMessageHandler {

	private ThreadPoolExecutor rpcWorkers;

	private ThreadPoolExecutor  respWorkers;
	private ThreadPoolExecutor  sendHeartWorkers;

	private static final Logger logger = LoggerFactory.getLogger(RpcMessageHandler.class.getName());


	public RpcMessageHandler() {
		rpcWorkers = ThreadPoolManager.INSTANCE.getRpcWorkers();
		sendHeartWorkers=ThreadPoolManager.INSTANCE.getSendHeartWorkers();
		respWorkers = ThreadPoolManager.INSTANCE.getRespWorkers();
	}

	public void dispatcher(ChannelHandlerContext ctx, Object message) {
		if (message instanceof ClientHeartBean) {//客户端与服务端建立连接时
			// 客户端发送的心跳，服务端保存连接
			if (logger.isInfoEnabled()) {
				logger.info("收到客户端信息=>" + message);
			}
			ClientHeartBean clientInfo = (ClientHeartBean) message;
//			ClientContextUtil.addClientInfo(clientInfo.getAppCode(), clientInfo.getRpcPort().toString(),
//					NettyChannel.getChannel(ctx.channel().id().asLongText()));
		} else if (message instanceof RpcRequestBean) {//服务端收到客户端请求
			final long acceptTime = System.currentTimeMillis();
			RpcRequestBean rpcMessage = (RpcRequestBean) message;
			// 服务请求，保存客户端的基本信息与连接
			if (logger.isDebugEnabled()) {
				logger.debug("收到服务请求 act=" + acceptTime + ",cid=" + rpcMessage.getContextId());
			}
//			ClientContextUtil.addClientInfo(rpcMessage.getAppCode(), rpcMessage.getRpcPort().toString(),
//					NettyChannel.getChannel(ctx.channel().id().asLongText()));
//			String appCode= AppEnvirement.INSTANCE.getBasicConfiguration().getAppCode();
			String appCode= CloudEnvironment.INSTANCE.getAppCode();
			if(AppConstants.SEND_HEART_METHOD.equals(rpcMessage.getMethodName())){
				sendHeartWorkers.execute(() -> processRpcRequest(ctx, rpcMessage, acceptTime));
				if(AppConstants.ESF_REGISTRY.equals(appCode)) {
					ReportClient.report(ReportMonitorConstant.SERVICE_RPC_CODE, "sendHeartThreadPoolLinkedBlockingQueueSize", 1, sendHeartWorkers.getQueue().size());
				}
			}else{
				//处理客户端发送请求，通过反射执行服务端方法，并将结果返回给客户端
				rpcWorkers.execute(() -> processRpcRequest(ctx, rpcMessage, acceptTime));
				if(AppConstants.ESF_REGISTRY.equals(appCode)) {
					ReportClient.report(ReportMonitorConstant.SERVICE_RPC_CODE, "rpcThreadPoolLinkedBlockingQueueSize", 1, rpcWorkers.getQueue().size());
				}
			}

		} else if (message instanceof RpcRespBean) {//客户端收到服务端返回处理方法
			RpcRespBean respMessge = (RpcRespBean) message;
			respWorkers.execute(() -> processRpcResp(ctx, respMessge));
		} else {
			ctx.fireChannelRead(message);
		}

	}

	public void processRpcRequest(ChannelHandlerContext ctx, RpcRequestBean request, long acceptTime) {
		if (logger.isDebugEnabled()) {
			logger.debug("开始处理服务请求 act=" + acceptTime + ",cid=" + request.getContextId());
		}
		String srvName = request.getServiceName();
		RpcRespBean rpcResp = new RpcRespBean();
		rpcResp.setContextId(request.getContextId());
		Channel channel = ctx.channel();
		int serializeType = request.getSt();
		rpcResp.setSt(serializeType);
		try {
			Object service = null;
			try {
				service = ESFHelper2.getObject(srvName);
			} catch (Exception e) {
				if (logger.isWarnEnabled()) {
					logger.warn("根据[srv=" + srvName + "]查找服务异常：" + e.getMessage());
				}
			}
			if (service == null) {
				throw new NullPointerException("server-side not found service code of " + srvName);
			}
			String methodName = request.getMethodName();
			Object[] parameters = request.getParameters();
			Class<?>[] parameterTypes = request.getParameterTypes();
			Method method = MethodUtil.findMethod(service.getClass(), methodName, parameterTypes);
			if (method == null) {
				throw new NullPointerException("server-side not found method " + methodName + " of clazz "
						+ service.getClass() + " of service " + service);
			}
			if (serializeType == SerializerCategory.json) {
				Type[] types = method.getGenericParameterTypes();
				checkParamters(parameters, types, srvName, methodName);
				// json序列化
				if (parameters != null) {
					int len = parameters.length;
					Object[] tempParas = new Object[len];
					for (int i = 0; i < len; i++) {
						Object paraI = parameters[i];
						if (StringUtils.nullOrEmptyStr(paraI)) {
							tempParas[i] = paraI;
						} else {
							tempParas[i] = JsonKit.json2Object4Type((String) paraI, types[i]);
						}
					}
					parameters = tempParas;
				}
			}
			ESFRPCContext rcx = new ESFRPCContext();
			try {
				InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
				rcx.setCallerIpAddress(remoteAddress.getAddress().getHostAddress());
				if (logger.isDebugEnabled()) {
					logger.debug("rpc client info => " + remoteAddress);
				}
				Map<String, Object> attachmentMap = request.getAttachments();
				if (attachmentMap != null) {
					String _aid = (String) attachmentMap.get(AppConstants._APP_ID);
					String _atk = (String) attachmentMap.get(AppConstants._APP_TOKEN);
					String _oid = (String) attachmentMap.get(AppConstants._OPER_ID);
					String _otk = (String) attachmentMap.get(AppConstants._OPER_TOKEN);
					String _tenant_id = (String) attachmentMap.get(AppConstants._TENANT_ID);
					String _request_id = (String) attachmentMap.get(AppConstants._REQUEST_ID);
					rcx.setClientAppCode(_aid);
					rcx.setClientAppToken(_atk);
					rcx.setOperatorId(_oid);
					rcx.setOperatorToken(_otk);
					rcx.setTenantId(_tenant_id);
					rcx.setGlobalReqId(_request_id);
				}
				rcx.setRequestUrl(ReportUtils.convertURL(srvName, methodName, parameters));
				rcx.setMsgReceiveTime(acceptTime);
			} catch (Exception e) {
				if (logger.isWarnEnabled()) {
					logger.warn("ESFRPCContext参数封装异常：" + e.getMessage());
				}
			}
			Object invokeResult = ESFHelper2.invokeRPC(rcx, srvName, service, method, parameters);
			rpcResp.setVal(invokeResult);
		} catch (Throwable te) {
			Throwable e = te;
			if (te != null && !(te instanceof RemoteExecuteException) && (te instanceof InvocationTargetException
					|| te.getCause() instanceof InvocationTargetException)) {
				e = ExceptionUtils.unwrapThrowable(te.getCause());
			}
			if (e instanceof RemoteExecuteException) {
				RemoteExecuteException re = (RemoteExecuteException) e;
				if (logger.isWarnEnabled()) {
					logger.warn("[racode=" + request.getAppCode() + ",rip=" + request.getIp() + ",srv=" + srvName
							+ ",mtd=" + request.getMethodName() + ",cid=" + request.getContextId() + ",act="
							+ acceptTime + "]请求执行失败：errno=" + re.getCode() + ",errmsg=" + re.getMessage());
				}
				rpcResp.setCause(e);
			} else {
				logger.error("[racode=" + request.getAppCode() + ",rip=" + request.getIp() + ",srv=" + srvName + ",mtd="
						+ request.getMethodName() + ",cid=" + request.getContextId() + ",act=" + acceptTime + "]请求执行异常："
						+ e.getMessage(), e);
				if (e instanceof RuntimeException) {
					rpcResp.setCause(e);
				} else {
					rpcResp.setCause(new RuntimeException(e.getMessage()));
				}
			}
		}
		if (channel == null || !channel.isActive()) {
			if (logger.isWarnEnabled()) {
				logger.warn("failed to response because the client maybe has closed the connection, rinfo::[act="
						+ acceptTime + ",cid=" + request.getContextId() + ",racode=" + request.getAppCode() + ",rip="
						+ request.getIp() + "]");
			}
		} else {
			channel.writeAndFlush(rpcResp).addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (!future.isSuccess()) {
						logger.error("[act=" + acceptTime + ",cid=" + request.getContextId() + ",racode="
								+ request.getAppCode() + ",rip=" + request.getIp() + "]响应异常："
								+ future.cause().getMessage(), future.cause());
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("请求处理成功 act=" + acceptTime + ",cid=" + request.getContextId() + ",racode="
									+ request.getAppCode() + ",rip=" + request.getIp() + ",cost(ms)="
									+ (System.currentTimeMillis() - acceptTime));
						}
					}
				}
			});
		}
	}

	private void checkParamters(Object[] parameters, Type[] types, String srvName, String method) {
		if (parameters == null || parameters.length == 0) {
			// 参数为空，types应该为null或者length为0;
			if (types == null || types.length == 0) {
				return;
			}
			throw new IllegalArgumentException(
					"请求参数与目标方法参数个数不一致，srvc=" + srvName + ",method=" + method + ",slen=0,dlen=" + types.length);
		} else {
			// 参数不空，则parameters与types个数应该一致
			if (types == null || parameters.length != types.length) {
				throw new IllegalArgumentException("请求参数与目标方法参数个数不一致，srvc=" + srvName + ",method=" + method + ",slen="
						+ parameters.length + ",dlen=" + (types == null ? 0 : types.length));
			}
		}
	}

	// 处理响应时间
	public void processRpcResp(ChannelHandlerContext ctx, RpcRespBean resp) {
		String contextId = resp.getContextId();
		if (StringKit.isEmpty(contextId)) {
			return;
		}
		Channel channel = ctx.channel();
		CallFuture<?> result = FutureUtils.removeFutures(contextId);
		String errorMsg = "cid=" + contextId + ",channel=" + channel;
		if (result == null) {
			if (logger.isWarnEnabled()) {
				logger.warn("null callFuture for " + errorMsg);
			}
			return;
		}
		if (resp.hasError()) {
			result.setException(resp.getCause(), channel.remoteAddress());
		} else {
			Object val = resp.getVal();
			if (resp.getSt() == SerializerCategory.json) {
				// json序列化
				if (StringUtils.nullOrEmptyStr(val)) {
					result.setVal(val);
				} else if (result.getRtnclz() == null) {
					if (logger.isDebugEnabled()) {
						logger.debug("null return type for " + errorMsg + ",val=" + val);
					}
					result.setVal(val);
				} else {
					result.setVal(JsonKit.json2Object4Type((String) val, result.getRtnclz()));
				}
			} else {
				result.setVal(val);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("cid=" + contextId + ",Channel=[" + channel + "], cost(ms) : "
					+ (System.currentTimeMillis() - result.getStartTime()));
		}
	}

	// 处理异常事件
	public void dispatchException(ChannelHandlerContext ctx, Throwable cause) {
		rpcWorkers.execute(() -> processException(ctx, cause));
	}

	private void processException(ChannelHandlerContext ctx, Throwable cause) {
		Channel channel = ctx.channel();
		logger.error("Channel=[" + channel + "]发生异常, msg : " + cause.getMessage(), cause);
		if (cause instanceof KryoException || cause instanceof IOException) {
			if (channel != null) {
				channel.close();
			}
		}
	}
}
