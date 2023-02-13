//package net.jplugin.cloud.rpc.common.util;
//
//import net.jplugin.cloud.rpc.common.config.AbstractConfig;
//import net.jplugin.cloud.rpc.common.config.HostConfig;
//import net.jplugin.cloud.rpc.common.config.RegistryConfig;
//import net.jplugin.cloud.rpc.common.constant.AppConstants;
//import net.jplugin.cloud.rpc.common.constant.SymbolConstant;
//import net.jplugin.common.kits.JsonKit;
//import net.jplugin.common.kits.StringKit;
//import net.jplugin.common.kits.http.HttpKit;
//import net.jplugin.common.kits.http.HttpStatusException;
//import net.jplugin.core.config.api.ConfigFactory;
//import net.jplugin.core.rclient.handler.JsonResult4Client;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicLong;
//
//public class RegistryHttpUtils {
//
//	private static final Logger logger = LoggerFactory.getLogger(RegistryHttpUtils.class);
//
//	public interface OldRegisterCenter {
//		public static final String REGISTERCENTER_SERVICE = "/IRegisterCenterHttpProxy";
//
//		public static final String METHOD_REGISTER = "register";
//
//		public static final String METHOD_DELETESERVER = "deleteServer";
//
//		public static final String m_getAppService = "getAppService";
//
//		public static final String m_getAllServer = "getAllServer";
//
//	}
//
//	public interface Registry {
//		public static final String m_registerById = "registerById";
//		public static final String m_unregister = "unregister";
//		public static final String m_getMethodsByAppcode = "getMethodsByAppcode";
//	}
//
//	private static List<String> oldUrls = new ArrayList<>();
//
//	private static List<String> newUrls = new ArrayList<>();
//
//	private static AtomicLong sequences = new AtomicLong();
//
//	private RegistryHttpUtils() {
//	}
//
//	public static int getOldSize() {
//		return oldUrls.size();
//	}
//
//	static {
//		init();
//	}
//
//	// 该init在启动时已经初始化
//	public static void init() {
//		String urls = AbstractConfig.getRegistryUrl();
//		List<HostConfig> nodeList = UrlUtils.parseUrl(urls);
//		for (HostConfig host : nodeList) {
//			String httpUrl = "http://" + host.getHostIp() + ":" + (host.getPort() + SymbolConstant.HTTP_PORT_OFFSET)
//					+ RegistryConfig.getRegistryservice() + "/";
//			newUrls.add(httpUrl);
//		}
//		urls = AbstractConfig.getRegisterCenterUrl();
//		//此处孩子王不需要依赖老的注册中心，可以下线
//		int openSwitch= ConfigFactory.getIntConfig("common.open.old.register",0);
//		if(openSwitch==1){
//			nodeList = UrlUtils.parseUrl(urls);
//			for (HostConfig host : nodeList) {
//				String httpUrl = "http://" + host.getHostIp() + ":" + (host.getPort() + SymbolConstant.HTTP_PORT_OFFSET)
//						+ OldRegisterCenter.REGISTERCENTER_SERVICE + "/";
//				oldUrls.add(httpUrl);
//			}
//		}
//	}
//
//	public static String createRequestUrl(String method, boolean isNew) {
//		Integer index = 0;
//		String url = "";
//		if (isNew) {
//			index = (int) Math.floorMod(sequences.getAndIncrement(), newUrls.size());
//			url = newUrls.get(index) + method;
//		} else {
//			if(oldUrls.size()>0){
//				index = (int) Math.floorMod(sequences.getAndIncrement(), oldUrls.size());
//				url = oldUrls.get(index) + method;
//			}
//		}
//		return url;
//	}
//
//	@SuppressWarnings("unchecked")
//	public static String getAppServiceFromOld(String appCode) {
//		String result = "";
//		for (int i = 0; i < getOldSize(); i++) {
//			try {
//				String url = createRequestUrl(OldRegisterCenter.m_getAppService, false);
//				url += "?appcode=" + appCode;
//				result = HttpKit.get(url);
//				if (StringKit.isEmpty(result)) {
//					continue;
//				}
//				break;
//			} catch (Exception e) {
//				logger.error(e.getMessage(), e);
//			}
//		}
//		if (logger.isDebugEnabled()) {
//			logger.debug("getAppService from old registry result=" + result);
//		}
//		if (!StringKit.isEmpty(result)) {
//			JsonResult4Client jsonResult4Client = JsonKit.json2Object(result, JsonResult4Client.class);
//			if (jsonResult4Client.isSuccess()) {
//				Map<String, String> map = (Map<String, String>) jsonResult4Client.getContent();
//				if (map != null) {
//					return map.get(AppConstants._result);
//				}
//			}
//		}
//		return result;
//	}
//
//	@SuppressWarnings("unchecked")
//	public static String getAppService(String appCode) throws IOException, HttpStatusException {
//		String url = createRequestUrl(Registry.m_getMethodsByAppcode, true);
//		url += "?appCode=" + appCode;
//		String result = HttpKit.get(url);
//		if (logger.isDebugEnabled()) {
//			logger.debug("getAppService from registry result=" + result);
//		}
//		if (!StringKit.isEmpty(result)) {
//			JsonResult4Client jsonResult4Client = JsonKit.json2Object(result, JsonResult4Client.class);
//			if (jsonResult4Client.isSuccess()) {
//				Map<String, String> map = (Map<String, String>) jsonResult4Client.getContent();
//				if (map != null) {
//					return map.get(AppConstants._result);
//				}
//			}
//		}
//		return result;
//	}
//
//}
