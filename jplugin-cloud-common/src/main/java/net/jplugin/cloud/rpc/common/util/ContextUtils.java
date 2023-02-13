package net.jplugin.cloud.rpc.common.util;

import net.jplugin.core.kernel.api.ctx.ThreadLocalContext;
import net.jplugin.core.kernel.api.ctx.ThreadLocalContextManager;

public class ContextUtils {

	public static void createThreadlocalContext() {
		ThreadLocalContext context = ThreadLocalContextManager.instance.getContext();
		if (context == null) {
			ThreadLocalContextManager.instance.createContext();
		}
	}

	public static void realeaseThreadlocalContext() {
		ThreadLocalContext context = ThreadLocalContextManager.instance.getContext();
		if (context != null) {
			ThreadLocalContextManager.instance.releaseContext();
		}
	}

	public static boolean hasContextRequest() {
		ThreadLocalContext context = ThreadLocalContextManager.instance.getContext();
		return context != null && ThreadLocalContextManager.getRequestInfo() != null;
	}
}
