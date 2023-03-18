package net.jplugin.cloud.rpc.client.api;

import net.jplugin.core.config.api.CloudEnvironment;
import net.jplugin.core.kernel.api.AbstractPlugin;

import net.jplugin.core.rclient.ExtendsionClientHelper;
import net.jplugin.core.rclient.api.Client;

public class ExtensionESFHelper {

	/**
	 * 注册RPC服务调用
	 * 
	 * @param plugin
	 * @param clazz
	 * @param url
	 */
	public static void addRPCProxyExtension(AbstractPlugin plugin, Class<?> clazz, String url) {
		String appCode = CloudEnvironment.INSTANCE._composeAppCode();
		ExtendsionClientHelper.addClientProxyExtension(plugin, clazz, url, Client.PROTOCOL_RPC, appCode);
	}

//	/**
//	 * 注册Rest服务调用
//	 *
//	 * @param plugin
//	 * @param clazz
//	 * @param url
//	 */
//	public static void addRestfulProxyExtension(AbstractPlugin plugin, Class<?> clazz, String url) {
//		String appCode = CloudEnvironment.INSTANCE.getAppCode();
//		ExtendsionClientHelper.addClientProxyExtension(plugin, clazz, url, Client.PROTOCOL_REST, appCode);
//	}

	/**
	 * 注册RPC调用，使用json协议序列化
	 *
	 * @param plugin
	 * @param clazz
	 * @param url
	 */
	public static void addRpcJsonProxyExtension(AbstractPlugin plugin, Class<?> clazz, String url) {
		String appCode = CloudEnvironment.INSTANCE.getAppCode();
		ExtendsionClientHelper.addClientProxyExtension(plugin, clazz, url, Client.PROTOCOL_RPC_JSON, appCode);
	}

	/**
	 * 此方法自动遍历指定包下面的类，如果该类包含BindRemoteServiceProxy 注解，则注册对应的ServiceProxy扩展
	 * 
	 * @param plugin
	 *            Plugin类
	 * @param pkgPath
	 *            基于PLUgin类的相对包路径，比如“.svc" ,可以为null
	 */
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public static void autoBindRemoteServiceProxy(AbstractPlugin plugin, String pkgPath) {
//		for (Class c : plugin.filterContainedClasses(pkgPath, BindRemoteServiceProxy.class)) {
//			BindRemoteServiceProxy anno = (BindRemoteServiceProxy) c.getAnnotation(BindRemoteServiceProxy.class);
//			String url = anno.url();
//			BindRemoteServiceProxy.ProxyProtocol protocol = anno.protocol();
//			if (protocol == null) {
//				throw new NullPointerException("null proxy protocol，url=" + url + ",class=" + c.getName());
//			}
//			switch (protocol) {
////			case rest:
////				addRestfulProxyExtension(plugin, c, url);
////				break;
//			case rpc:
//				addRPCProxyExtension(plugin, c, url);
//				break;
////			case rpc_json:
////				addRpcJsonProxyExtension(plugin, c, url);
////				break;
//			default:
//				throw new IllegalArgumentException(
//						"unknown proxy protocol=" + protocol + ",url=" + url + ",class=" + c.getName());
//			}
//			PluginEnvirement.INSTANCE.getStartLogger().log("$$$ Auto add extension for remote service proxy : protocol="
//					+ protocol + ",url=" + anno.url() + ",class=" + c.getName());
//		}
//	}
}
