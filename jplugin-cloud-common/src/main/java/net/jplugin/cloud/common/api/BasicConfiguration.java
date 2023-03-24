package net.jplugin.cloud.common.api;

import net.jplugin.common.kits.FileKit;
import net.jplugin.common.kits.PropertiesKit;
import net.jplugin.common.kits.StringKit;
import net.jplugin.core.config.api.CloudEnvironment;
import net.jplugin.core.kernel.api.PluginEnvirement;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.Properties;

public class BasicConfiguration {
	String appCenterUrl;
	String esfPort;
//	String esfPortHttp;
	String appSign;
	String appCode;
	String moduleCode;

	private BasicConfiguration() {
	}

	public String getAppCenterUrl() {
		return appCenterUrl;
	}

	public String getAppCode() {
		return appCode;
	}

	public String getAppSign() {
		return appSign;
	}

	public String getEsfPort() {
		return esfPort;
	}

	public String getModuleCode(){
		return moduleCode;
	}

	@Deprecated
	public String getEsfPortHttp(){
//		return esfPortHttp;
		return "0";
	}

	public void _setESFPort(String ep){
		this.esfPort = ep;
	}
	
	static BasicConfiguration create() {
//		String cfg = PluginEnvirement.getInstance().getConfigDir();
//		String cfgName = KernelKit.getConfigFilePath("basic-config.properties");
		BasicConfiguration appConfiguration = null;
		if (useJvmConfiguration()){
			PluginEnvirement.INSTANCE.getStartLogger().log("Using basic configuration from JVM");
			appConfiguration = createFromJvmParam();
		}else{
			PluginEnvirement.INSTANCE.getStartLogger().log("Using basic configuration from file");
			appConfiguration = createFromConfigFile();
		}

		if (StringKit.isNull(appConfiguration.appCenterUrl)) {
			throw new RuntimeException("Basic config error:app-center-url have null value");
		}
		if (StringKit.isNull(appConfiguration.appCode)) {
			throw new RuntimeException("Basic config error:app-code have null value");
		}
		if (StringKit.isNull(appConfiguration.appSign)) {
			throw new RuntimeException("Basic config error:app-sign have null value");
		}
		if (!checkServiceCode(appConfiguration.moduleCode)) {
			throw new RuntimeException("Service code format error:  can only contain letters");
		}

		if (StringKit.isNull(appConfiguration.esfPort)) {
			//初始化一下tomcatPort
			String tomcatPort = TomcatPortHelper.computeTomcatPort();

			if (StringKit.isNotNull(tomcatPort)) {
				//如果能够获取到，说明是容器Tocmat
				appConfiguration.esfPort = Integer.parseInt(tomcatPort.trim())+100 +"";
			} else{
				//2020 -12月修改：如果是嵌入式Tomcat，则延迟设置esf-port，否则抛出异常
				if (TomcatPortHelper.isEmbedTomcat()){
					appConfiguration.esfPort = CloudEnvironment.RPC_PORT_HINT_EMBED_TOMCAT;
				}else{
					throw new RuntimeException("Basic config error:esf-port have null value");
				}
			}
			PluginEnvirement.INSTANCE.getStartLogger().log("Set esfPort as "+appConfiguration.esfPort);
		}
		
//		PluginEnvirement.INSTANCE.getStartLogger().log("$$$ AppCenterUrl=" + appConfiguration.appCenterUrl + "  AppCode=" + appConfiguration.appCode
//				+ "  ESFPort=" + appConfiguration.esfPort + "  ESFPortHttp="+appConfiguration.esfPortHttp);
		PluginEnvirement.INSTANCE.getStartLogger().log("AppCenterUrl=" + appConfiguration.appCenterUrl + "  AppCode=" + appConfiguration.appCode
				+ "  ESFPort=" + appConfiguration.esfPort );
		return appConfiguration;
	}

	private static boolean checkServiceCode(String serviceCode) {
		if (StringKit.isNull(serviceCode)){
			return true;
		}
		for (char c:serviceCode.toCharArray()){
			if (!( (c>='a' && c<='z') || (c>='A' && c<='Z') || c=='_' ||c=='-' || (c>='0' && c<='9'))){
				return false;
			}
		}
		return true;
	}
//	
//	private static boolean handledEmbbedTomcat() {
//		try {
//			// 判断是嵌入式Tocmat
//			Class.forName("net.jplugin.extension.embed_tomcat.Plugin");
//			
//			PluginEnvirement.INSTANCE.getStartLogger()
//			.log("$$$ Waiting embed-tomcat activate esf-port setting" );
//
//			// 增加监听器
//			PluginEventListenerManager.addListener(new IPluginEventListener() {
//				@Override
//				public String getIntrestedPluginName() {
//					return "net.jplugin.extension.embed_tomcat.Plugin";
//				}
//
//				@Override
//				public void afterCreateServices(String pluginName) {
//					Integer tomcatPort = ConfigFactory.getIntConfig("embed-tomcat.context-port", 8080);
//					
//					//设置缓存一下，因为配置中心会使用这里Tomcat端口。
//					TomcatPortHelper._setEmbedTomcatPort(tomcatPort+"");
//					
//					String esfport = (tomcatPort + 100) + "";
//					AppEnvirement.INSTANCE.getBasicConfiguration().esfPort = esfport;
//					PluginEnvirement.INSTANCE.getStartLogger()
//							.log("$$$ Setting esf-port from embed-tomcat, " + esfport);
//				}
//			});
//			
//			return true;
//
//		} catch (Exception e) {
//			return false;
//		}
//	}

	private static boolean useJvmConfiguration() {
		String temp = System.getProperty("app-center-url");
		return temp!=null && temp.trim()!=null;
	}

	private static BasicConfiguration createFromConfigFile() {
		String cfgName = getConfigPath();
		Properties prop = null;
		try {
			prop = PropertiesKit.loadProperties(cfgName);
		} catch (Exception e) {
			throw new RuntimeException("Can't find basic-config.properties in your config dir:" + cfgName);
		}

		BasicConfiguration appConfiguration = new BasicConfiguration();
		appConfiguration.appCenterUrl = prop.getProperty("app-center-url");
		appConfiguration.appSign = prop.getProperty("app-sign");
		appConfiguration.appCode = prop.getProperty("app-code");
		appConfiguration.esfPort = prop.getProperty("esf-port");
		appConfiguration.moduleCode = prop.getProperty("module-code");
//		appConfiguration.esfPortHttp = prop.getProperty("esf-port-http");
		
		return appConfiguration;
	}

	private static BasicConfiguration createFromJvmParam() {
		BasicConfiguration appConfiguration = new BasicConfiguration();
		Properties prop = System.getProperties();
		appConfiguration.appCenterUrl = prop.getProperty("app-center-url");
		appConfiguration.appSign = prop.getProperty("app-sign");
		appConfiguration.appCode = prop.getProperty("app-code");
		appConfiguration.esfPort = prop.getProperty("esf-port");
		appConfiguration.moduleCode = prop.getProperty("module-code");
//		appConfiguration.esfPortHttp = prop.getProperty("esf-port-http");
		
		return appConfiguration;
	}

	public static String getConfigPath(){
		String p = getHomeConfigPath();
		if (FileKit.existsAndIsFile(p)){
			PluginEnvirement.getInstance().getStartLogger().log("Using Home basic-config:"+p);
			return p;
		}else{
			PluginEnvirement.getInstance().getStartLogger().log("Using Built-in basic-config:"+p);
			p = getBuildInConfigPath();
			return p;
		}
	}
	
	private static String getBuildInConfigPath(){
		return PluginEnvirement.INSTANCE.getConfigDir()+"/basic-config.properties";
	}
	private static String getHomeConfigPath() {
		//check catalina.home
		if (System.getProperty("catalina.home")==null){
			return PluginEnvirement.INSTANCE.getWorkDir()+"/basic-config.properties";
		}else{
			return System.getProperty("catalina.home")+"/basic-config.properties";
		}
	}




	static class TomcatPortHelper {
		/**
		 * 如果初始化initTomcatPort以后仍然为空，可能没有tocmat，也可能是嵌入式tomcat
		 */
		public static String computeTomcatPort(){
			//首先从环境变量获取，spring启动时候目前放入环境变量的。
			String tomcatPort = System.getProperty("app.embedded.server.port");
			if (StringKit.isNotNull(tomcatPort)){
				return tomcatPort;
			}

			//从其他方式获取
			String thome = TomcatPortHelper.getTomcatHome();
			if (StringKit.isNotNull(thome)) {
				tomcatPort = TomcatPortHelper.getTomcatPort(new File(thome + "/conf/server.xml"));
				PluginEnvirement.INSTANCE.getStartLogger().log("$$$ Get tomcat port from tomcat container: "+tomcatPort);
				if (StringKit.isNull(tomcatPort)){
					throw new RuntimeException("tomcat port can't be null");
				}else{
					return tomcatPort;
				}
			}else{
				//返回null
				return null;
			}
		}

		// 判断是嵌入式Tocmat
		public static boolean isEmbedTomcat(){
			try{
				Class.forName("net.jplugin.extension.embed_tomcat.Plugin");
				return true;
			}catch(Exception e){
				return false;
			}
		}

		private static String getTomcatPort(File serverXml) {
			try {
				DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
				domFactory.setNamespaceAware(true); // never forget this!
				DocumentBuilder builder = domFactory.newDocumentBuilder();
				Document doc = builder.parse(serverXml);
				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();
				XPathExpression expr = xpath
						.compile("/Server/Service[@name='Catalina']/Connector[count(@scheme)=0]/@port[1]");
				String result = (String) expr.evaluate(doc, XPathConstants.STRING);
				if (StringKit.isNull(result))
					throw new RuntimeException("Can't find tomcat port from file:"+serverXml.getAbsolutePath());
				return result;
			} catch (Exception e) {
				throw new RuntimeException("can't find tomcat port from file"+serverXml,e);
			}
		}

		private static String  getTomcatHome() {
			return System.getProperty("catalina.home");
		}
	}



}
