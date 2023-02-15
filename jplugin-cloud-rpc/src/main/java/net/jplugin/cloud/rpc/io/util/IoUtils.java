package net.jplugin.cloud.rpc.io.util;

import net.jplugin.cloud.rpc.common.config.AbstractConfig;
import net.jplugin.common.kits.client.InvocationParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoUtils {

	private static final Logger logger = LoggerFactory.getLogger(IoUtils.class);

	public static long getRpcTimeout(InvocationParam invokeParam) {
		Long timeout = AbstractConfig.getDefaultTimeoutInMills();
		if (invokeParam == null || invokeParam.getServiceTimeOut() <= 0) {
			return timeout;
		}
		timeout = (long) invokeParam.getServiceTimeOut();
		return timeout;
	}

}
