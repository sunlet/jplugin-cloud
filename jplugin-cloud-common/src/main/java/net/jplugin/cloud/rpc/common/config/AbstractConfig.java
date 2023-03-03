package net.jplugin.cloud.rpc.common.config;

import net.jplugin.cloud.rpc.common.constant.ConfigConstants;
import net.jplugin.cloud.rpc.common.util.ExceptionUtils;
import net.jplugin.common.kits.StringKit;
import net.jplugin.core.config.api.ConfigFactory;
import net.jplugin.core.kernel.api.ctx.ThreadLocalContextManager;
import net.jplugin.core.rclient.api.RemoteExecuteException;

public abstract class AbstractConfig{
	private static final long serialVersionUID = -2218369194338181239L;

	private static final int demo_cpu_beishu = 5;// 请求处理线程 默认cpu核数倍数

	private static final int demo_resp_cpu_beishu = 2;// 响应处理线程默认cpu核数倍数

	private static final int demo_max_beishu = 2;// 最小配置的倍数

	private static final int demo_netty_http_worker_cpu_beishu = 2;// http服务netty的worker数量是cpu的倍数

	private static final int demo_http_worker_cpu_beishu = 4;// http服务业务处理线程数是netty-http服务worker数量的倍数

	public static Integer getNettyBoss() {
		return Config4AppCenter.getInteger(ConfigConstants.NETTY_BOSS, 1);
	}

	public static Integer getNettyWorkers() {
		return Config4AppCenter.getInteger(ConfigConstants.NETTY_WORKERS, Config4AppCenter.getDefaultWorkers());
	}

	public static Integer getNettyHttpBoss() {
		return Config4AppCenter.getInteger(ConfigConstants.NETTY_HTTP_BOSS, 1);
	}

	public static Integer getNettyHttpWorkers() {
		return Config4AppCenter.getInteger(ConfigConstants.NETTY_HTTP_WORKERS,
				Config4AppCenter.getDefaultWorkers() * demo_netty_http_worker_cpu_beishu);
	}

	public static Integer getRpcWorkers() {
		return Config4AppCenter.getInteger(ConfigConstants.RPC_WORKERS, getNettyWorkers() * demo_cpu_beishu);
	}

	public static int getMaxRpcWorkers() {
		return Config4AppCenter.getInteger(ConfigConstants.RPC_MAX_WORKERS, getRpcWorkers() * demo_max_beishu);
	}

	public static Integer getRpcRespWorkers() {
		return Config4AppCenter.getInteger(ConfigConstants.RPC_RESP_WORKERS, getNettyWorkers() * demo_resp_cpu_beishu);
	}

	public static int getMaxRpcRespWorkers() {
		return Config4AppCenter.getInteger(ConfigConstants.RPC_RESP_MAX_WORKERS, getRpcRespWorkers() * demo_max_beishu);
	}

	public static Integer getHttpWorkers() {
		return Config4AppCenter.getInteger(ConfigConstants.HTTP_WORKERS,
				getNettyHttpWorkers() * demo_http_worker_cpu_beishu);
	}

	public static Integer getMaxHttpWorkers() {
		return Config4AppCenter.getInteger(ConfigConstants.HTTP_MAX_WORKERS, getHttpWorkers() * demo_max_beishu);
	}

	public static Integer getRpcPort() {
		return Config4AppCenter.getRpcPort();
	}

//	public static Integer getHttpPort() {
//		return Config4AppCenter.getHttpPort();
//	}
//
//	public static Integer getClusterPort() {
//		return Config4AppCenter.getClusterPort();
//	}

	public static String getAppcode() {
		return Config4AppCenter.getAppcode();
	}

//	public static String getRegistryUrl() {
//		return Config4AppCenter.getRegistryUrl();
//	}
//
//	public static String getRegisterCenterUrl() {
//		return Config4AppCenter.getOldRegistryUrl();
//	}
//
//	public static String getTenantDomainUrl() {
//		return Config4AppCenter.getTenantDomainUrl();
//	}
	public static Integer getCppTryNum() {
		return Config4AppCenter.getInteger(ConfigConstants.CPP_TRY, 1);
	}

	// 心跳间隔，单位秒
	public static Integer getHeartInterval() {
		return Config4AppCenter.getInteger(ConfigConstants.HEART_INTERVAL, 15);
	}

	// 有效心跳个数
	public static Integer getSerialHeartNum() {
		return Config4AppCenter.getInteger(ConfigConstants.HEART_CONTINUOUS_NUM, 6);
	}

	public static Integer getConnectionTimeout() {
		return Config4AppCenter.getInteger(ConfigConstants.CONN_TIMEOUT, 3000);
	}

	public static Integer getSoTimeout() {
		return Config4AppCenter.getInteger(ConfigConstants.SO_TIMEOUT, 30000);
	}

	public static String getAuthCenterUrl() {
		return Config4AppCenter.getEnv(ConfigConstants.KAUTH_PROXY_URL);
	}

	public static String getAuthDomainUrl() {
		return Config4AppCenter.getEnv(ConfigConstants.KAUTH_PROXY_DOMAIN_URL);
	}

	public static Long getValidHeartTime() {
		return System.currentTimeMillis() - getHeartInterval() * getSerialHeartNum() * 1000;
	}

	public static Boolean isValidHeart(Long heartTime) {
		return System.currentTimeMillis() <= (heartTime + getHeartInterval() * getSerialHeartNum() * 1000);
	}

	public static long getDefaultTimeoutInMills() {
		return Config4AppCenter.getInteger(ConfigConstants.WAIT_TIMEOUT, 6000);
	}

	public static Long getBlackTime() {
		return Config4AppCenter.getLong(ConfigConstants.BLACK_TIMEOUT, 1000 * 60 * 2);
	}

	public static boolean enableMultiTenants() {
		return "true".equalsIgnoreCase(ConfigFactory.getStringConfig("mtenant.enable"));
	}

	public static String getCurrentTenantId() {
		String currentTenantId = ThreadLocalContextManager.getRequestInfo().getCurrentTenantId();
		if (StringKit.isEmpty(currentTenantId)) {
			throw new NullPointerException("请求租戶ID空，请检查租户ID配置");
		}
		return currentTenantId;
	}

	public static boolean getCppMonitorReport() {
		return Config4AppCenter.getBoolean(ConfigConstants.CPP_MONITOR, true);
	}

	public static boolean getCppSourceLucency() {
		return Config4AppCenter.getBoolean(ConfigConstants.CPP_SOURCE_LUCENCY, false);
	}

	public static boolean getCppMachinekeyLucency() {
		return Config4AppCenter.getBoolean(ConfigConstants.CPP_MACHINEKEY_LUCENCY, true);
	}

	public static boolean getCppTanancyOverride() {
		return Config4AppCenter.getBoolean(ConfigConstants.CPP_TANANCY_OVERRIDE, false);
	}
	public static Boolean getBabyStoreMode() {
		 Integer babyStoreMode= Config4AppCenter.getInteger(ConfigConstants.BABY_STORE_MODE);
		 if(babyStoreMode==null){
			 return null;
		 }else if(babyStoreMode==1){
			 return true;
		 }else if(babyStoreMode==0){
			 return false;
		 }
		 return true;
	}
//	public static boolean isK8sEnv() {
//		return Config4AppCenter.isK8sEnv();
//	}
//
//	public static String getK8SClusterIp() {
//		return Config4AppCenter.getK8sIp();
//	}

	public static int getStartRpcPort() {
		return Config4AppCenter.getStartRpcPort();
	}

//	public static int getStartHttpPort() {
//		return Config4AppCenter.getStartHttpPort();
//	}
//
//	public static String getRegistryDomainUrl() {
//		return Config4AppCenter.getRegistryDomainUrl();
//	}

	public static boolean monitorRemoteExcep() {
		return Config4AppCenter.getBoolean(ConfigConstants.MOINTOR_REMOTEEXCEP, false);
	}

	private static boolean monitorClientRemoteExcep() {
		return Config4AppCenter.getBoolean(ConfigConstants.MOINTOR_CLIENT_REMOTEEXCEP, true);
	}

	public static boolean debugMode() {
		return Config4AppCenter.isDebug();
	}

	public static boolean filterRemoteExcep(Throwable th) {
		if (th == null) {
			return true;
		}
		return !AbstractConfig.monitorRemoteExcep()
				&& (ExceptionUtils.unwrapThrowable(th) instanceof RemoteExecuteException
						|| ExceptionUtils.unwrapThrowable(th.getCause()) instanceof RemoteExecuteException);
	}

	public static boolean filterClientRemoteExcep(Throwable th) {
		if (th == null) {
			return true;
		}
		return !AbstractConfig.monitorClientRemoteExcep()
				&& (ExceptionUtils.unwrapThrowable(th) instanceof RemoteExecuteException
						|| ExceptionUtils.unwrapThrowable(th.getCause()) instanceof RemoteExecuteException);
	}

	public static boolean isGrayNode() {
		return Config4AppCenter.isGrayNode();
	}

//	public static int getRackType(String ip) {
//		return Config4AppCenter.getRackType(ip);
//	}
//
//	public static int getCurrentRack() {
//		return Config4AppCenter.getCurrent_rack();
//	}

}
