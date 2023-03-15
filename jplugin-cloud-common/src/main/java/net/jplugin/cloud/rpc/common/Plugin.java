package net.jplugin.cloud.rpc.common;

import net.jplugin.cloud.rpc.common.filter.ESFRpcClientFilter;
import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.ExtensionPoint;
import net.jplugin.core.kernel.api.PluginAnnotation;

@PluginAnnotation
public class Plugin extends AbstractPlugin {

//	public static final String EP_RPC_CLIENT_FILTER = "EP_RPC_CLIENT_FILTER";

	public Plugin() {
//		this.addExtensionPoint(ExtensionPoint.createList(EP_RPC_CLIENT_FILTER, ESFRpcClientFilter.class));
	}

	@Override
	public void init() {
	}

	@Override
	public int getPrivority() {
		return -482;
	}

}
