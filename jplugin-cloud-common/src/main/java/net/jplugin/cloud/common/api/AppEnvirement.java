package net.jplugin.cloud.common.api;

import net.jplugin.core.config.api.CloudEnvironment;
import net.jplugin.core.config.api.ConfigFactory;
import net.jplugin.core.config.api.GlobalConfigFactory;
import net.jplugin.core.kernel.api.PluginEnvirement;

import java.util.HashMap;
import java.util.Map;

public class AppEnvirement {
	public static AppEnvirement INSTANCE;
	private BasicConfiguration basicConfig;
	private String appToken;
	private AppEnvirement(){}

	public String getAppToken(){
		return appToken;
	}
	/**
	 * 获取本地基础配置
	 * @return
	 */
	public BasicConfiguration getBasicConfiguration(){
		return basicConfig;
	}
	
	/**
	 * 获取整体环境级配置
	 * @param key
	 * @return
	 */
	public String getEnvConfiguration(String key){
//		return AppcenterServiceIndpendent.getGlobalVar(key);
		return GlobalConfigFactory.getValueInDefaultGroup("DEFAULT_GROUP."+key);
	}

	
	/**
	 * 获取应用级配置
	 * @param key
	 * @return
	 */
	public String getAppConfiguration(String key){
		return ConfigFactory.getStringConfig(key);
	}
//
//	static  Boolean useJpluginMVC;
//	public static boolean useingJPluginMVC(){
//		if (useJpluginMVC==null){
//			try{
//				Class.forName("net.jplugin.ext.webasic.api.AbstractExController");
//				useJpluginMVC = true;
//			}catch(Exception e){
//				useJpluginMVC = false;
//			}
//		}
//		return useJpluginMVC;
//	}

	public void _setAppToken(String t){
		this.appToken = t;
	}
	
	/**
	 * 获取应用级配置
	 * @return
	 */
	public Map<String, String> getAppConfigurationOfGroup(String group){
		return ConfigFactory.getStringConfigInGroup(group);
	}
	
	
	private static boolean init = false;
	public static void init() {
		if (init) {
			PluginEnvirement.INSTANCE.getStartLogger().log("Warnning! call the plugin init a second time!"+AppEnvirement.class.getName());
			return;
		}
		else init = true;

		if (CloudEnvironment.INSTANCE.hasInit()){
			PluginEnvirement.INSTANCE.getStartLogger().log("Already Init by CloudEnvirement! ");
			return;
		}

		INSTANCE = new AppEnvirement();
		INSTANCE.basicConfig = BasicConfiguration.create();


//		AssertKit.assertStringNotNull(map.get(NACOS_URL), NACOS_URL);
//		AssertKit.assertStringNotNull(map.get(APP_CODE), APP_CODE);
//		AssertKit.assertStringNotNull(map.get(SERVICE_CODE), SERVICE_CODE);
//		AssertKit.assertStringNotNull(map.get(RPC_PORT), RPC_PORT);

		Map<String,String> config = new HashMap<>();

		config.put(CloudEnvironment.NACOS_URL,INSTANCE.basicConfig.getAppCenterUrl());
		config.put(CloudEnvironment.APP_CODE,INSTANCE.basicConfig.getAppCode());
		config.put(CloudEnvironment.MODULE_CODE,INSTANCE.basicConfig.getModuleCode());
		config.put(CloudEnvironment.RPC_PORT,INSTANCE.basicConfig.getEsfPort());
		config.put(CloudEnvironment.NACOS_USER,INSTANCE.basicConfig.getAppCode());
		config.put(CloudEnvironment.NACOS_PWD,INSTANCE.basicConfig.getAppSign());

		CloudEnvironment.INSTANCE.init(config);

		//不支持token插一个固定值
		INSTANCE._setAppToken("00000000");

		//验证并获取token
//		String tk = AppTokenFetcher.getToken();
//		if (StringKit.isNull(tk)){
//			throw new RuntimeException("应用验证失败，无法启动!");
//		}else{
//			INSTANCE._setAppToken(tk);
//			AppTokenFetcher.startTimer();
//		}
	}
}
