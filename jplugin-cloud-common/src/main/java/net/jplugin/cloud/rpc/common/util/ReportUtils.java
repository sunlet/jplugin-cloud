package net.jplugin.cloud.rpc.common.util;

import net.jplugin.cloud.rpc.common.constant.ReportMonitorConstant;

public final class ReportUtils {

	private ReportUtils() {
	}

	/**
	 * 拼接URL
	 * 
	 * @param serviceName
	 * @param methodName
	 * @param args
	 * @return String 返回拼接内容格式：服务名/方法名/参数1类型/参数2类型/.../参数N类型
	 */
	public static String convertURL(String serviceName, String methodName, Object[] args) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(serviceName).append(ReportMonitorConstant.SPLICE_CODE).append(methodName);
		if (args != null && args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				buffer.append(ReportMonitorConstant.SPLICE_CODE);
				if (args[i] == null) {
					buffer.append(ReportMonitorConstant.NULL_CODE);
					continue;
				}
				buffer.append(args[i].getClass().getName());
			}
		}
		return buffer.toString();
	}
}
