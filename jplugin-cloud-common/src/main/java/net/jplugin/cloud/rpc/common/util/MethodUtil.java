package net.jplugin.cloud.rpc.common.util;

import net.jplugin.cloud.rpc.common.config.AbstractConfig;
import net.jplugin.common.kits.ReflactKit;
import net.jplugin.common.kits.StringKit;
import net.jplugin.common.kits.client.ClientInvocationManager;
import net.jplugin.common.kits.client.InvocationParam;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MethodUtil {

	private static final Map<String, Method> methodCacheMap = new ConcurrentHashMap<>();

	public static Method findMethod(Class<?> clazz, String methodName, Class<?>[] argsType)
			throws NoSuchMethodException, SecurityException {
		Method m = null;
		String key = null;
		try {
			key = createCacheKey(clazz, methodName, argsType);
			m = methodCacheMap.get(key);
			if (m == null) {
				m = clazz.getDeclaredMethod(methodName, argsType);
				methodCacheMap.put(key, m);
			}
		} catch (Exception e) {
			m = ReflactKit.findSingeMethodExactly(clazz, methodName);
			if (!StringKit.isEmpty(key) && m != null) {
				methodCacheMap.put(key, m);
			}
		}
		if (m == null) {
			throw new NoSuchMethodException(
					"method=" + methodName + ",clazz=" + clazz + ",args=" + Arrays.toString(argsType));
		}
		return m;
	}

	private static String createCacheKey(Class<?> clazz, String methodName, Class<?>[] argsType) {
		return clazz.getName() + "#" + methodName + "#" + Arrays.toString(argsType);
	}

	private MethodUtil() {
	}

	// private static final ThreadLocal<Long> timeoutLocal = new
	// ThreadLocal<Long>() {
	//
	// @Override
	// protected Long initialValue() {
	// return AbstractConfig.getDefaultTimeoutInMills();
	// }
	// };
	//
	// public static void setTimeout(long timeout) {
	// if (timeout < 0) {
	// throw new IllegalArgumentException("negative timeout=" + timeout);
	// }
	// timeoutLocal.set(timeout);
	// }

	public static long getTimeout() {
		Long timeout = AbstractConfig.getDefaultTimeoutInMills();
		InvocationParam invokeParam = ClientInvocationManager.INSTANCE.getAndClearParam();
		if (invokeParam == null || invokeParam.getServiceTimeOut() <= 0) {
			return timeout;
		}
		timeout = (long) invokeParam.getServiceTimeOut();
		return timeout;
	}

	public static InvocationParam getAndClearParam() {
		return ClientInvocationManager.INSTANCE.getAndClearParam();
	}

	public static InvocationParam getInvocParam() {
		return ClientInvocationManager.INSTANCE.getParam();
	}

}
