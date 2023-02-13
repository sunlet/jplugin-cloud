//package net.jplugin.cloud.rpc.common.config;
//
//import net.jplugin.cloud.rpc.common.util.UrlUtils;
//import net.jplugin.common.kits.JsonKit;
//import net.jplugin.common.kits.StringKit;
//import net.jplugin.common.kits.http.HttpKit;
//import net.jplugin.core.rclient.handler.JsonResult4Client;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class RegistryConfig extends AbstractConfig {
//
//	private static final long serialVersionUID = -6940201111590635342L;
//
//	private static final String registryService = "/registryService";
//
//	private static final String registryClusterService = "/registryClusterService";
//
//	private static final String get_registrynode_url = "/esf-web/esfService/getRegistry.do";
//
//	private static final String field_result = "result";
//
//	private List<HostConfig> nodeList = new ArrayList<>();
//
//	private static final Logger logger = LoggerFactory.getLogger(RegistryConfig.class);
//
//	public List<HostConfig> loadNodeList() {
//		throw new RuntimeException("not impl");
////		if (this.nodeList == null || this.nodeList.isEmpty()) {
////			loadRegistryNode();
////		}
////		return this.nodeList;
//	}
//
////	private void loadRegistryNode() {
////		// 首先根据配置的域名获取注册中心节点
////		boolean load = loadNodeByDomainUrl();
////		if (!load) {
////			// 如果通过域名加载失败，则尝试获取已经配置的种子节点
////			loadNodeByEnv();
////		}
////	}
////
////	private boolean loadNodeByDomainUrl() {
////		try {
////			String domainUrl = AbstractConfig.getRegistryDomainUrl();
////			if (StringKit.isEmpty(domainUrl)) {
////				if (logger.isInfoEnabled()) {
////					logger.info("获取注册中心节点域名地址为空，尝试获取已配置的种子节点");
////				}
////				return false;
////			}
////			String result = HttpKit.get(domainUrl.trim() + get_registrynode_url);
////			if (logger.isInfoEnabled()) {
////				logger.info("通过域名获取注册中心节点列表：result=" + result);
////			}
////			if (StringKit.isEmpty(result)) {
////				return false;
////			}
////			JsonResult4Client jsonResult4Client = JsonKit.json2Object(result, JsonResult4Client.class);
////			if (jsonResult4Client == null || !jsonResult4Client.isSuccess()) {
////				return false;
////			}
////			@SuppressWarnings("unchecked")
////			Map<String, String> map = (Map<String, String>) jsonResult4Client.getContent();
////			if (map == null || map.isEmpty()) {
////				return false;
////			}
////			String nodeStrs = map.get(field_result);
////			if (StringKit.isEmpty(nodeStrs)) {
////				return false;
////			}
////			List<HostConfig> tempList = UrlUtils.parseUrl(nodeStrs);
////			if (tempList == null || tempList.isEmpty()) {
////				return false;
////			}
////			nodeList.addAll(tempList);
////			return true;
////		} catch (Exception e) {
////			if (logger.isInfoEnabled()) {
////				logger.info("通过域名获取注册中心节点地址异常：warnmsg = " + e.getMessage() + ",warnexp=" + e);
////			}
////			return false;
////		}
////	}
//
////	private void loadNodeByEnv() {
////		String urls = AbstractConfig.getRegistryUrl();
////		if (StringKit.isEmpty(urls)) {
////			throw new NullPointerException("not found registry url config!");
////		}
////		List<HostConfig> tempList = UrlUtils.parseUrl(urls);
////		if (tempList != null) {
////			nodeList.addAll(tempList);
////		}
//////	}
////
////	public static String getRegistryservice() {
////		return registryService;
////	}
////
////	public static String getRegistryclusterservice() {
////		return registryClusterService;
////	}
//
//}
