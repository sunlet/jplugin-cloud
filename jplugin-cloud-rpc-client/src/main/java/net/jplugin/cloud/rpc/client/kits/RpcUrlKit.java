package net.jplugin.cloud.rpc.client.kits;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.jplugin.common.kits.tuple.Tuple2;
import org.apache.commons.lang3.StringUtils;

import net.jplugin.core.log.api.LogFactory;
import net.jplugin.core.log.api.Logger;

public class RpcUrlKit {

	private static final String SINGLE_SLASH = "/";

	private static Map<String, String[]> cacheMap = new ConcurrentHashMap<>();

	public static final String ESF_SIGNAL = "esf://";

	private static Logger logger = LogFactory.getLogger(RpcUrlKit.class.getName());

	public static String makeEsfUrl(String theme, String serviceName) {
		return ESF_SIGNAL + theme + SINGLE_SLASH + serviceName;
	}

	public static boolean isEsfUrl(String serviceURL) {
		if (StringUtils.isEmpty(serviceURL)) {
			return false;
		}
		return serviceURL.startsWith(ESF_SIGNAL);
	}

	public static Tuple2<String, String> parseEsfUrlInfo(String serviceURL) {
		if (!isEsfUrl(serviceURL))
			throw new RuntimeException("code shoudln't come here");

		String tempUrl = serviceURL.substring(6);
		int pos = tempUrl.indexOf(SINGLE_SLASH);

		return Tuple2.with(tempUrl.substring(0, pos), tempUrl.substring(pos) );
	}

}
