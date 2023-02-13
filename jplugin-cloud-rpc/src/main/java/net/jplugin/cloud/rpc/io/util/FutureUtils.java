package net.jplugin.cloud.rpc.io.util;

import net.jplugin.cloud.rpc.io.future.CallFuture;
import net.jplugin.common.kits.StringKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FutureUtils {

	private static final Logger logger = LoggerFactory.getLogger(FutureUtils.class);

	private FutureUtils() {
	}

	private static final Map<String, CallFuture<?>> futureMap = new ConcurrentHashMap<String, CallFuture<?>>();

	public static CallFuture<?> removeFutures(String contextId) {
		if (logger.isDebugEnabled()) {
			logger.debug("futureMapSize=" + futureMap.keySet().size() + ",futureMap=" + futureMap);
		}
		if (StringKit.isEmpty(contextId)) {
			return null;
		}
		CallFuture<?> result = futureMap.remove(contextId);
		return result;
	}

	public static void addFuture(CallFuture<?> future) {
		if (future == null || StringKit.isEmpty(future.getContextId())) {
			if (logger.isWarnEnabled()) {
				logger.warn("future or contextId is null");
			}
			return;
		}
		futureMap.put(future.getContextId(), future);
	}
}
