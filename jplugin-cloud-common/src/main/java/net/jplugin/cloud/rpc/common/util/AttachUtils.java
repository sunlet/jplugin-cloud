package net.jplugin.cloud.rpc.common.util;

//import com.haiziwang.platform.appclient.api.AppEnvirement;
import net.jplugin.cloud.rpc.common.constant.AppConstants;
import net.jplugin.common.kits.RequestIdKit;
import net.jplugin.core.kernel.api.CloudEnvironment;
import net.jplugin.core.kernel.api.ctx.RequesterInfo;
import net.jplugin.core.kernel.api.ctx.ThreadLocalContextManager;

import java.util.HashMap;
import java.util.Map;

public class AttachUtils {

	public static Map<String, Object> createAttachments() {
		Map<String, Object> extParaMap = new HashMap<String, Object>();
//		extParaMap.put(AppConstants._APP_ID, AppEnvirement.INSTANCE.getBasicConfiguration().getAppCode());
//		extParaMap.put(AppConstants._APP_TOKEN, AppEnvirement.INSTANCE.getAppToken());
		extParaMap.put(AppConstants._APP_ID, CloudEnvironment.INSTANCE.getAppCode());
		if (ContextUtils.hasContextRequest()) {
			RequesterInfo requestInfo = ThreadLocalContextManager.getRequestInfo();
			extParaMap.put(AppConstants._TENANT_ID, requestInfo.getCurrentTenantId());
			try {
				String traceId = requestInfo.getTraceId();
				//Span span = GTraceKit.getCurrentSpan();
				String reqId = RequestIdKit.makeReqId(traceId, null);
				extParaMap.put(AppConstants._REQUEST_ID, reqId);
			} catch (Exception e) {
			}
		}
		return extParaMap;
	}
}
