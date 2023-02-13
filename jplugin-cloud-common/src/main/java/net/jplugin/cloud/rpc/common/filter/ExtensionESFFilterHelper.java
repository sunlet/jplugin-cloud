package net.jplugin.cloud.rpc.common.filter;

import net.jplugin.cloud.rpc.common.Plugin;
import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.Extension;

public class ExtensionESFFilterHelper {

	public static void addESFRpcClientFilterExtension(AbstractPlugin p, Class<?> clazz) {
		p.addExtension(Extension.create(Plugin.EP_RPC_CLIENT_FILTER, clazz));
	}
}
