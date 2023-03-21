package net.jplugin.cloud.rpc.client.extension;

import net.jplugin.cloud.rpc.client.annotation.RefRemoteService;
import net.jplugin.core.kernel.api.IAnnoForAttrHandler;
import net.jplugin.core.rclient.proxyfac.ClientProxyFactory;

public class EsfRemoteServiceAnnoHandler implements IAnnoForAttrHandler<RefRemoteService> {

	@Override
	public Class<RefRemoteService> getAnnoClass() {
		return RefRemoteService.class;
	}

	@Override
	public Class<?> getAttrClass() {
		return Object.class;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getValue(Object obj, Class fieldClazz, RefRemoteService anno) {
		// String remoteService = anno.remoteService();
		// if (!StringKit.isEmpty(remoteService)) {
		// return ClientProxyFactory.instance.getClientProxy(remoteService,
		// fieldClazz);
		// }
		return ClientProxyFactory.instance.getClientProxy(fieldClazz);
	}

}
