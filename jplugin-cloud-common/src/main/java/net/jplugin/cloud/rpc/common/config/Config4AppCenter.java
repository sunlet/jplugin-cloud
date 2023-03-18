package net.jplugin.cloud.rpc.common.config;

//import net.jplugin.cloud.rpc.common.BasicConfiguration;
import net.jplugin.cloud.rpc.common.constant.ConfigConstants;
import net.jplugin.core.config.api.CloudEnvironment;
import net.jplugin.core.config.api.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Config4AppCenter {

	private static final Logger logger = LoggerFactory.getLogger(Config4AppCenter.class);

	private static final Map<String, String> group_conf = new HashMap<>();

	private static String appCode;

	private static Integer rpcPort;

//	private static Integer httpPort;

//	private static Integer clusterPort;

	/**
	 * 注册中心地址，配置在应用中心全局配置
	 */
//	private static String registryUrl;

	/**
	 * 老版本的注册中心地址，配置在应用中心全局配置，为了兼容，等所有java项目升级完成可以弃用
	 */
//	private static String oldRegistryUrl;

	/**
	 * 获取注册中心地址的esf域名URL，配置在应用中心全局配置
	 */
//	private static String registryDomainUrl;
	/**
	 * 获取租户信息的域名URL，配置在应用中心全局配置
	 */
//	private static String tenantDomainUrl;

//	private static boolean k8sEnv;
//
//	private static String k8sIp;
//
//	// k8s上报rpc端口
//	private static Integer k8sRpcPort;
//
//	// k8s上报rest端口
//	private static Integer k8sRestPort;
//
//	// k8s监听rpc端口
//	private static Integer k8sRpcListenPort;
//
//	// k8s监听rest端口
//	private static Integer k8sRestListenPort;

	// 标识是否是debug模式
	private static boolean debug;

	// 标识是否是灰度节点
	private static boolean grayNode;
//
//	// 无锡IP地址前缀配置
//	private static final Set<String> wxIpRulePre = new HashSet<>();
//
//	// uc机房IP地址前缀配置
//	private static final Set<String> ucIpRulePre = new HashSet<>();
//
//	private static volatile int current_rack = RackType.RACK_WX;

	//private static volatile String currentDataCenterName;

	static {
		try {
			init();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Config4AppCenter() {
	}

	private static void init() {
//		loadRackIpRule();
//		BasicConfiguration bcfg = AppEnvirement.INSTANCE.getBasicConfiguration();
//		appCode = bcfg.getAppCode();
//		rpcPort = Integer.parseInt(bcfg.getEsfPort());
		appCode = CloudEnvironment.INSTANCE._composeAppCode();
		rpcPort = Integer.parseInt(CloudEnvironment.INSTANCE.getRpcPort());

//		Boolean enableDeployPort = getBoolean(ConfigConstants.ENABLE_DEPLOY_PORT, false);
//		if (enableDeployPort != null && enableDeployPort) {
//			String deployPort = System.getProperty(ConfigConstants.DEPLOY_PORT0);
//			if (StringKit.isEmpty(deployPort)) {
//				deployPort = System.getenv(ConfigConstants.DEPLOY_PORT0);
//			}
//			System.out.println("deploy conf port : " + deployPort);
//			if (!StringKit.isEmpty(deployPort)) {
//				try {
//					rpcPort = Integer.parseInt(deployPort.trim());
//				} catch (Exception e) {
//					if (logger.isWarnEnabled()) {
//						logger.warn("parse deplogy conf port error:" + e);
//					}
//				}
//			}
//		}
//		String configHttpPort = bcfg.getEsfPortHttp();
//		if (StringKit.isEmpty(configHttpPort)) {
//			configHttpPort = "" + (rpcPort + SymbolConstant.HTTP_PORT_OFFSET);
//		}
//		httpPort = Integer.parseInt(configHttpPort);
//		k8sEnv = K8SUtils.isK8sCluster();
//		grayNode = K8SGrayUtils.isK8sGrayApp();
//		System.out.println("k8s env : " + k8sEnv + ",grayNode : " + grayNode);
//		String localIp = NetUtils.getHostIp();
//		if (isK8sEnv()) {
//			EsfIpPortInfo appEsfInfo = K8SUtils.getAppEsfInfo(appCode, NetUtils.getLocalIp());
//			if (appEsfInfo != null) {
//				k8sIp = appEsfInfo.getIp();
//				k8sRpcPort = appEsfInfo.getRpcPort();
//				k8sRestPort = appEsfInfo.getRestPort();
//				k8sRpcListenPort = appEsfInfo.getRpcListenPort();
//				k8sRestListenPort = appEsfInfo.getRestListenPort();
//				localIp = k8sIp;
//				System.out.println("k8s return result: k8sIp=" + k8sIp + ",k8sRpcPort=" + k8sRpcPort + ",k8sRestPort="
//						+ k8sRestPort + ",k8sRpcListenPort=" + k8sRpcListenPort + ",k8sRestListenPort="
//						+ k8sRestListenPort);
//			} else {
//				System.err.println("k8s return appEsfInfo is null!");
//			}
//		}
//		clusterPort = httpPort + SymbolConstant.HTTP_PORT_OFFSET;
//		registryUrl = AppEnvirement.INSTANCE.getEnvConfiguration(ConfigConstants.REGISTRY_URL);
//		oldRegistryUrl = AppEnvirement.INSTANCE.getEnvConfiguration(ConfigConstants.REGISTER_CENTER_SITE);
//		registryDomainUrl = AppEnvirement.INSTANCE.getEnvConfiguration(ConfigConstants.REGISTRY_DOMAIN_URL);
//		tenantDomainUrl = AppEnvirement.INSTANCE.getEnvConfiguration(ConfigConstants.TENANT_DOMAIN_URL);
//		Map<String, String> configGroup = AppEnvirement.INSTANCE.getAppConfigurationOfGroup(ConfigConstants.ESF_GROUP);
//		if (configGroup != null) {
//			group_conf.putAll(configGroup);
//			String configedNewRegistry = group_conf.get(ConfigConstants.REGISTRY_URL);
//			String configedOldRegistry = group_conf.get(ConfigConstants.REGISTER_CENTER_SITE);
//			String configedRegistryDomainUrl = group_conf.get(ConfigConstants.REGISTRY_DOMAIN_URL);
//			String tenantDomainUrl = group_conf.get(ConfigConstants.TENANT_DOMAIN_URL);
//			if (!StringKit.isEmpty(configedNewRegistry)) {
//				registryUrl = configedNewRegistry.trim();
//			}
//			if (!StringKit.isEmpty(configedOldRegistry)) {
//				oldRegistryUrl = configedOldRegistry.trim();
//			}
//			if (!StringKit.isEmpty(configedRegistryDomainUrl)) {
//				registryDomainUrl = configedRegistryDomainUrl.trim();
//			}
//			if (!StringKit.isEmpty(tenantDomainUrl)) {
//				tenantDomainUrl = tenantDomainUrl.trim();
//			}
//		}
//		debug = getBoolean(ConfigConstants.DEBUG_MODE, false);
//		current_rack = getRackType(localIp);
//		//currentDataCenterName = CommonDataCenterUtils.getDataCenterName(localIp);
//		if (logger.isInfoEnabled()) {
//			logger.info("registryUrl=" + registryUrl);
//			logger.info("registerCenterUrl=" + oldRegistryUrl);
//			logger.info("registryDomainUrl=" + registryDomainUrl);
//			logger.info("tenantDomainUrl=" + tenantDomainUrl);
//			logger.info("esf_config_group=" + group_conf);
//			logger.info("debug.mode=" + debug + ",enableDeployPort=" + enableDeployPort + ",wxIpRule=" + wxIpRulePre
//					+ ",ucIpRule=" + ucIpRulePre + ",localIp=" + localIp + ",current_rack=" + current_rack+",currentDataCenterName"+"");
//		}
//	}
//
//	private static void loadRackIpRule() {
//		String wxIpRule = AppEnvirement.INSTANCE.getEnvConfiguration(ConfigConstants.WX_IP_RULE);
//		String ucIpRule = AppEnvirement.INSTANCE.getEnvConfiguration(ConfigConstants.UC_IP_RULE);
//		if (!StringKit.isEmpty(wxIpRule)) {
//			String[] wxIpRules = wxIpRule.split(",");
//			for (String wxIp : wxIpRules) {
//				if (StringKit.isEmpty(wxIp)) {
//					continue;
//				}
//				wxIpRulePre.add(wxIp.trim());
//			}
//		}
//		if (!StringKit.isEmpty(ucIpRule)) {
//			String[] ucIpRules = ucIpRule.split(",");
//			for (String ucIp : ucIpRules) {
//				if (StringKit.isEmpty(ucIp)) {
//					continue;
//				}
//				ucIpRulePre.add(ucIp.trim());
//			}
//		}
	}
//
//	public static String getRegistryUrl() {
//		return registryUrl;
//	}

	public static String getEnv(String envName) {
//		return AppEnvirement.INSTANCE.getEnvConfiguration(envName);
		throw new RuntimeException("not impl yet");
	}

	public static String getAppcode() {
		return appCode;
	}

	public static Integer getStartRpcPort() {
//		if (isK8sEnv() && k8sRpcListenPort != null) {
//			return k8sRpcListenPort;
//		}
		return rpcPort;
	}

//	public static Integer getStartHttpPort() {
//		if (isK8sEnv() && k8sRestListenPort != null) {
//			return k8sRestListenPort;
//		}
//		return httpPort;
//	}

	public static Integer getRpcPort() {
//		if (isK8sEnv() && k8sRpcPort != null) {
//			return k8sRpcPort;
//		}
		return rpcPort;
	}

//	public static Integer getHttpPort() {
//		if (isK8sEnv() && k8sRestPort != null) {
//			return k8sRestPort;
//		}
//		return httpPort;
//	}
//
//	public static Integer getClusterPort() {
//		return clusterPort;
//	}

	public static String getAsign() {
//		return AppEnvirement.INSTANCE.getBasicConfiguration().getAppSign();
		return CloudEnvironment.INSTANCE.getNacosPwd();
	}

	public static String getStringOfGroup(String item) {
		String val = group_conf.get(item);
		if (StringUtils.isEmpty(val)) {
			String localConfig = ConfigFactory.getStringConfig(ConfigConstants.ESF_PREFIX + item);
			group_conf.put(item, localConfig);
			return localConfig;
		}
		return val;
	}

	public static List<String> getConfigAsList(String item) {
		String val = group_conf.get(item);
		if (StringUtils.isEmpty(val)) {
			String localConfig = ConfigFactory.getStringConfig(ConfigConstants.ESF_PREFIX + item);
			group_conf.put(item, localConfig);
			val = localConfig;
		}
		if (!StringUtils.isEmpty(val)) {
			return Arrays.asList(val.split(","));
		}
		return new ArrayList<>(0);
	}

	public static String getString(String item, String defaultVal) {
		String val = getStringOfGroup(item);
		return StringUtils.isEmpty(val) ? defaultVal : val;
	}

	public static Integer getInteger(String item) {
		String val = getStringOfGroup(item);
		if (!StringUtils.isEmpty(val)) {
			return Integer.parseInt(val);
		}
		return null;
	}

	public static Integer getInteger(String item, int defaultValue) {
		Integer val = getInteger(item);
		return val == null ? defaultValue : val;
	}

	public static Long getLong(String item) {
		String val = getStringOfGroup(item);
		if (!StringUtils.isEmpty(val)) {
			return Long.parseLong(val);
		}
		return null;
	}

	public static Long getLong(String item, long defaultValue) {
		Long val = getLong(item);
		return val == null ? defaultValue : val;
	}

	public static Boolean getBoolean(String item) {
		String val = getStringOfGroup(item);
		if (!StringUtils.isEmpty(val)) {
			return Boolean.parseBoolean(val);
		}
		return null;
	}

	public static Boolean getBoolean(String item, boolean defaultValue) {
		Boolean val = getBoolean(item);
		return val == null ? defaultValue : val;
	}

	public static Double getDouble(String item) {
		String val = getStringOfGroup(item);
		if (!StringUtils.isEmpty(val)) {
			return Double.parseDouble(val);
		}
		return null;
	}

	public static Double getDouble(String item, double defaultVal) {
		Double val = getDouble(item);
		return val == null ? defaultVal : val;
	}

	public static Integer getDefaultWorkers() {
		return Runtime.getRuntime().availableProcessors() + 1;
	}

//	public static String getOldRegistryUrl() {
//		return oldRegistryUrl;
//	}
//
//	public static String getK8sIp() {
//		return k8sIp;
//	}
//
//	public static boolean isK8sEnv() {
//		return k8sEnv;
//	}
//
//	public static Integer getK8sRpcPort() {
//		return k8sRpcPort;
//	}
//
//	public static Integer getK8sRestPort() {
//		return k8sRestPort;
//	}
//
//	public static String getRegistryDomainUrl() {
//		return registryDomainUrl;
//	}
//
//	public static String getTenantDomainUrl() {
//		return tenantDomainUrl;
//	}
//
//	public static void setTenantDomainUrl(String tenantDomainUrl) {
//		Config4AppCenter.tenantDomainUrl = tenantDomainUrl;
//	}

	public static boolean isDebug() {
		return debug;
	}

	public static boolean isGrayNode() {
		return grayNode;
	}

//	public static Set<String> getWxiprulepre() {
//		return wxIpRulePre;
//	}
//
//	public static Set<String> getUciprulepre() {
//		return ucIpRulePre;
//	}

//	private static boolean isWxIp(String ip) {
//		if (StringKit.isEmpty(ip)) {
//			return false;
//		}
//		Set<String> wxIpRules = getWxiprulepre();
//		if (wxIpRules.isEmpty()) {
//			return false;
//		}
//		Iterator<String> iter = wxIpRules.iterator();
//		while (iter.hasNext()) {
//			String pre = iter.next();
//			if (ip.startsWith(pre)) {
//				return true;
//			}
//		}
//		return false;
//	}

//	private static boolean isUcIp(String ip) {
//		if (StringKit.isEmpty(ip)) {
//			return false;
//		}
//		Set<String> ucIpRules = getUciprulepre();
//		if (ucIpRules.isEmpty()) {
//			return false;
//		}
//		Iterator<String> iter = ucIpRules.iterator();
//		while (iter.hasNext()) {
//			String pre = iter.next();
//			if (ip.startsWith(pre)) {
//				return true;
//			}
//		}
//		return false;
//	}

//	public static Integer getRackType(String ip) {
//		if (isWxIp(ip)) {
//			return RackType.RACK_WX;
//		} else if (isUcIp(ip)) {
//			return RackType.RACK_UC;
//		}
//		// 默认使用无锡机房归属
//		return RackType.RACK_WX;
//	}
//
//	public static int getCurrent_rack() {
//		return current_rack;
//	}


	/*public static String getCurrentDataCenterName() {
		return currentDataCenterName;
	}

	public static void setCurrentDataCenterName(String currentDataCenterName) {
		Config4AppCenter.currentDataCenterName = currentDataCenterName;
	}*/
}
