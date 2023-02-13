package net.jplugin.cloud.rpc.io.handler;/*
package net.jplugin.cloud.rpc.io.handler;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.jplugin.cloud.rpc.common.config.AbstractConfig;
import net.jplugin.cloud.rpc.common.constant.AppConstants;
import net.jplugin.cloud.rpc.common.util.ExceptionUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.util.CharsetUtil;
import net.jplugin.common.kits.StringKit;
import net.jplugin.common.kits.http.ContentKit;
import net.jplugin.core.config.api.ConfigFactory;
import net.jplugin.core.ctx.api.JsonResult;
import net.jplugin.core.rclient.api.RemoteExecuteException;
import net.jplugin.ext.staticweb.api.IContentManager;
import net.jplugin.ext.staticweb.api.IContentManager.Request;
import net.jplugin.ext.staticweb.api.IContentManager.Response;
import net.jplugin.ext.webasic.impl.ESFHelper;
import net.jplugin.ext.webasic.impl.ESFRestContext;
import net.jplugin.ext.webasic.impl.WebDriver;
import net.jplugin.ext.webasic.impl.WebDriver.ControllerMeta;
import net.jplugin.ext.webasic.impl.restm.invoker.CallParam;

public class HttpMessageHandler {

	private static final Logger logger = LoggerFactory.getLogger(HttpMessageHandler.class.getName());

	private Executor httpWorkers;

	boolean cfgInit = false;

	String accessControlAllowOrigin;

	private Integer max_size = 10000;

	private static final String sys_err = "-1";

	public HttpMessageHandler() {
		int min = AbstractConfig.getHttpWorkers();
		int max = AbstractConfig.getMaxHttpWorkers();
		httpWorkers = new ThreadPoolExecutor(min, max, 30, TimeUnit.MINUTES, new LinkedBlockingQueue<>(max_size),
				new ThreadFactoryBuilder().setDaemon(true).setNameFormat("esf-http-executor-%d").build());

	}

	// 消息分发
	public void dispatcherHttpMsg(ChannelHandlerContext ctx, FullHttpRequest msg) {
		if (logger.isDebugEnabled()) {
			logger.debug("HttpRequest=" + msg);
		}
		if (msg == null) {
			return;
		}
		String uri = msg.uri();
		// 通过浏览器访问时默认取网站logo标识，非web网站，不提供logo标识请求
		if (uri.contains("/favicon.ico")) {
			return;
		}
		boolean flag = handleIfStaticResource(ctx, msg);
		if (flag) {
			return;
		}
		Map<String, String> paramMap = new HashMap<>();
		String clientIp = getClientIp(ctx, msg);
		paramMap.put(AppConstants._CLIENT_IP, clientIp);
		if (ContentKit.isApplicationJson(msg.headers().get(HttpHeaderNames.CONTENT_TYPE))) {
			ByteBuf buffer = msg.content();
			if (buffer != null) {
				ByteBufInputStream bis = null;
				try {
					bis = new ByteBufInputStream(buffer);
					int num = bis.available();
					byte[] content = new byte[num];
					bis.read(content);
					bis.close();
					if (content != null && content.length > 0) {
						String json = new String(content, CharsetUtil.UTF_8);
						paramMap.put(CallParam.JSON_KEY, json);
					}
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				} finally {
					if (bis != null) {
						try {
							bis.close();
						} catch (IOException e) {
						}
					}
				}
			}
		}
		HttpMethod hm = msg.method();
		final long acceptTime = System.currentTimeMillis();
		if (hm == HttpMethod.GET) {
			QueryStringDecoder decoder = new QueryStringDecoder(uri);
			Map<String, List<String>> parameters = decoder.parameters();
			if (parameters != null) {
				for (Entry<String, List<String>> ent : parameters.entrySet()) {
					paramMap.put(ent.getKey(), ent.getValue().get(0));
				}
			}
			httpWorkers.execute(() -> processHttpRequest(ctx, msg, paramMap, acceptTime));
		} else if (hm == HttpMethod.POST) {
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), msg);
			List<InterfaceHttpData> bodyHttpDatas = decoder.getBodyHttpDatas();
			if (null != bodyHttpDatas && !bodyHttpDatas.isEmpty()) {
				for (InterfaceHttpData ih : bodyHttpDatas) {
					if (InterfaceHttpData.HttpDataType.Attribute == ih.getHttpDataType()) {
						MemoryAttribute attr = (MemoryAttribute) ih;
						paramMap.put(attr.getName(), attr.getValue());
					}
				}
			}
			httpWorkers.execute(() -> processHttpRequest(ctx, msg, paramMap, acceptTime));
		}
	}

	private boolean handleIfStaticResource(ChannelHandlerContext ctx, FullHttpRequest req) {
		try {
			String reqUri = req.uri();
			int pos = reqUri.indexOf("?");
			if (pos >= 0) {
				reqUri = reqUri.substring(0, pos);
			}
			final String uri = reqUri;
			boolean isAccept = IContentManager.INSTANCE.accept(uri);
			if (isAccept) {
				httpWorkers.execute(() -> {
					try {
						Response resp = IContentManager.INSTANCE.handleRequest(Request.create(uri));
						FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
								Unpooled.copiedBuffer(resp.getContentBytes()));
						Map<String, String> headers = resp.getHeaders();
						if (headers != null) {
							for (Entry<String, String> ent : headers.entrySet()) {
								response.headers().add(ent.getKey(), ent.getValue());
							}
						}
						String controlAllowOrigin = getAccessControlAllowOrigin();
						if (!StringKit.isEmpty(controlAllowOrigin)) {
							response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, controlAllowOrigin);
						}
						ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				});
			}
			return isAccept;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	private void processHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req, Map<String, String> paramMap,
			long acceptTime) {
		FullHttpResponse response = null;
		String jsonResult = null;
		JsonResult result = JsonResult.create();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("paramMap=" + paramMap);
			}
			String preUrl = "";
			HashMap<String, String> headerMap = new HashMap<>();
			try {
				preUrl = req.headers().get(HttpHeaderNames.HOST);
				if (!StringKit.isEmpty(preUrl)) {
					preUrl = req.protocolVersion().protocolName().toLowerCase() + "://" + preUrl;
				}
				HttpHeaders headers = req.headers();
				Iterator<Entry<String, String>> headerIter = headers.iteratorAsString();
				while (headerIter.hasNext()) {
					Entry<String, String> header = headerIter.next();
					if (header != null) {
						if (!StringKit.isEmpty(header.getKey())) {
							headerMap.put(header.getKey(), header.getValue());
						}
					}
				}
			} catch (Exception e) {
				if (logger.isWarnEnabled()) {
					logger.warn("解析header异常：" + e);
				}
			}
			String reqUri = req.uri();
			int pos = reqUri.indexOf("?");
			if (pos >= 0) {
				reqUri = reqUri.substring(0, pos);
			}
			ControllerMeta parseControllerMeta = WebDriver.INSTANCE.parseControllerMeta(reqUri);
			if (parseControllerMeta == null) {
				throw new RemoteExecuteException("404", "service not found for url : " + reqUri);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("service path :" + parseControllerMeta.getServicePath());
			}
			CallParam cp = null;
			ESFRestContext rcx = new ESFRestContext();
			try {
				rcx.setCallerIpAddress(paramMap.remove(AppConstants._CLIENT_IP));
				rcx.setRequestUrl(preUrl + reqUri);
				if (logger.isDebugEnabled()) {
					logger.debug("request url==" + rcx.getRequestUrl());
				}
				rcx.setHeaderMap(headerMap);
				rcx.setMsgReceiveTime(acceptTime);
			} catch (Exception e) {
				if (logger.isWarnEnabled()) {
					logger.warn("设置ESFRestContext异常：" + e);
				}
			}
			try {
				// 获取cookie并解析成map格式
				String cookieVal = req.headers().get(HttpHeaderNames.COOKIE);
				if (!StringKit.isEmpty(cookieVal)) {
					HashMap<String, String> cookieMap = new HashMap<>();
					Set<Cookie> cookieSets = ServerCookieDecoder.LAX.decode(cookieVal);
					if (cookieSets != null) {
						for (Cookie c : cookieSets) {
							// 遍历cookie内容
							cookieMap.put(c.name(), c.value());
						}
					}
					rcx.setCookieMap(cookieMap);
				}
			} catch (Exception e) {
				if (logger.isWarnEnabled()) {
					logger.warn("解析Cookie异常：" + e);
				}
			}
			if (ContentKit.isApplicationJson(req.headers().get(HttpHeaderNames.CONTENT_TYPE))) {
				String json = paramMap.remove(CallParam.JSON_KEY);
				if (json == null) {
					json = "";
				}
				Map<String, String> reqMap = new HashMap<String, String>();
				reqMap.put(CallParam.JSON_KEY, json);
				cp = CallParam.create(CallParam.CALLTYPE_JSON, parseControllerMeta.getServicePath(),
						parseControllerMeta.getOperation(), reqMap);
			} else {
				cp = CallParam.create(parseControllerMeta.getServicePath(), parseControllerMeta.getOperation(),
						paramMap);
			}
			ESFHelper.callRestfulService(rcx, cp);
			jsonResult = cp.getResult();
		} catch (Throwable te) {
			jsonResult = null;
			try {
				Throwable e = te;
				if (te != null && !(te instanceof RemoteExecuteException) && (te instanceof InvocationTargetException
						|| te.getCause() instanceof InvocationTargetException)) {
					e = ExceptionUtils.unwrapThrowable(te.getCause());
				}
				if (e instanceof RemoteExecuteException) {
					RemoteExecuteException re = (RemoteExecuteException) e;
					if (logger.isWarnEnabled()) {
						logger.warn("[req-uri=" + req.uri() + ",act=" + acceptTime + "]请求执行失败：errno=" + re.getCode()
								+ ",errmsg=" + re.getMessage());
					}
					result.setSuccess(false);
					result.setCode(re.getCode());
					result.setMsg(re.getMessage());
				} else {
					logger.error("[req-uri=" + req.uri() + ",act=" + acceptTime + "]请求执行异常：" + e.getMessage(), e);
					result.setSuccess(false);
					result.setCode(sys_err);
					result.setMsg(e.getMessage());
				}
			} catch (Exception tempe) {
				if (logger.isWarnEnabled()) {
					logger.warn("http异常结果解析异常：" + tempe);
				}
				result.setSuccess(false);
				result.setCode(sys_err);
				result.setMsg(te.getMessage());
			}
		} finally {
			if (jsonResult == null) {
				jsonResult = result.toJson();
			}
			response = new DefaultFullHttpResponse(HTTP_1_1, OK,
					Unpooled.copiedBuffer(jsonResult + "\r\n", CharsetUtil.UTF_8));
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
			String controlAllowOrigin = getAccessControlAllowOrigin();
			if (!StringKit.isEmpty(controlAllowOrigin)) {
				response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, controlAllowOrigin);
			}
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}
	}

	private String getClientIp(ChannelHandlerContext ctx, FullHttpRequest request) {
		String ip = "";
		try {
			HttpHeaders headers = request.headers();
			ip = headers.get("x-forwarded-for");
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = headers.get("Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = headers.get("WL-Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
				if (remoteAddress != null) {
					ip = remoteAddress.getAddress().getHostAddress();
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("rest client ip is : " + ip);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
	}

	private String getAccessControlAllowOrigin() {
		if (cfgInit == false) {
			cfgInit = true;
			accessControlAllowOrigin = ConfigFactory.getStringConfigWithTrim("platform.access-control-allow-origin");
			System.out.println("Init access-control-allow-arigin = " + accessControlAllowOrigin);
		}
		return accessControlAllowOrigin;
	}

	public void dispatchException(ChannelHandlerContext ctx, Throwable cause) {
		httpWorkers.execute(() -> processException(ctx, cause));
	}

	private void processException(ChannelHandlerContext ctx, Throwable cause) {
		Channel channel = ctx.channel();
		logger.error("Http Channel=[" + channel + "]发生异常, msg : " + cause.getMessage(), cause);
	}
}
*/
