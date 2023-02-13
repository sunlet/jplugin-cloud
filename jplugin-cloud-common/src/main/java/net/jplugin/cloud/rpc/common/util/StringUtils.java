package net.jplugin.cloud.rpc.common.util;

import net.jplugin.cloud.rpc.common.bean.AbstractAppBean;
import net.jplugin.cloud.rpc.common.constant.SymbolConstant;
import net.jplugin.common.kits.MD5Kit;
import net.jplugin.common.kits.RequestIdKit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class StringUtils {

	public static String nextUid() {
		return RequestIdKit.newSpanId();
	}

	public static String getUUID() {
		String s = UUID.randomUUID().toString();
		return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18) + s.substring(19, 23) + s.substring(24);
	}

	public static String getServerid(AbstractAppBean sbean) {
		return getIdByAppCodeIpAndPort(sbean.getAppCode(), sbean.getIp(), sbean.getRpcPort());
	}

	public static String getIdByIpAndPort(String hostIp, Integer httpPort) {
		return getIdByAppCodeIpAndPort(null, hostIp, httpPort);

	}

	public static String getIdByAppCodeIpAndPort(String appCode, String ip, Integer port) {
		return MD5Kit.MD5(appCode + SymbolConstant.COLON_SYMBOL + ip + SymbolConstant.COLON_SYMBOL + port);
	}

	public static String getIdByTenantIdAndAppCodeIpAndPort(String tenantId, String appCode, String ip, Integer port) {
		return MD5Kit.MD5(tenantId + SymbolConstant.COLON_SYMBOL + appCode + SymbolConstant.COLON_SYMBOL + ip
				+ SymbolConstant.COLON_SYMBOL + port);
	}

	public static Properties parseBytes2Properties(byte[] content) throws IOException {
		Properties pro = new Properties();
		if (content == null || content.length == 0) {
			return pro;
		}
		ByteArrayInputStream bis = null;
		try {
			bis = new ByteArrayInputStream(content);
			pro.load(bis);
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
				}
			}
		}
		return pro;
	}

	public static boolean nullOrEmptyStr(Object obj) {
		if (obj == null) {
			return true;
		} else if (obj instanceof String) {
			return "".equals(obj);
		}
		return false;
	}

}
