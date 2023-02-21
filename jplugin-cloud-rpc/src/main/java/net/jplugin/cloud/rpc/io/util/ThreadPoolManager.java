package net.jplugin.cloud.rpc.io.util;

import net.jplugin.cloud.rpc.common.config.AbstractConfig;
import net.jplugin.common.kits.ThreadFactoryBuilder;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {
	private static ThreadPoolExecutor  rpcWorkers;

	private static ThreadPoolExecutor  respWorkers;

	private static ThreadPoolExecutor  sendHeartWorkers;
	
	private static final Integer maxSize = 10000;
	
	public static volatile ThreadPoolManager INSTANCE = new ThreadPoolManager(); 
	
	private  ThreadPoolManager(){
	}
	
	public synchronized ThreadPoolExecutor getServerWorkers(){
		if (rpcWorkers==null){
			// 处理请求线程池
			int min = AbstractConfig.getRpcWorkers();
			int max = AbstractConfig.getMaxRpcWorkers();
			rpcWorkers = new ThreadPoolExecutor(min, max, 30, TimeUnit.MINUTES, new LinkedBlockingQueue<>(maxSize),
					new ThreadFactoryBuilder().setDaemon(true).setNameFormat("esf-rpc-executor-%d").build());
		}
		return rpcWorkers;
	}
		
	public synchronized ThreadPoolExecutor getClientWorks(){
		if (respWorkers==null){
				// 处理响应线程池
			int respMin = AbstractConfig.getRpcRespWorkers();
			int respMax = AbstractConfig.getMaxRpcRespWorkers();
			respWorkers = new ThreadPoolExecutor(respMin, respMax, 30, TimeUnit.MINUTES, new LinkedBlockingQueue<>(maxSize),
						new ThreadFactoryBuilder().setDaemon(true).setNameFormat("esf-rpc-responsor-%d").build());
		}
		return respWorkers;
	}
	public synchronized ThreadPoolExecutor  getSendHeartWorkers(){
		if (sendHeartWorkers==null){
			// 处理响应线程池
			int respMin = AbstractConfig.getRpcRespWorkers();
			int respMax = AbstractConfig.getMaxRpcRespWorkers();
			sendHeartWorkers = new ThreadPoolExecutor(respMin, respMax, 30, TimeUnit.MINUTES, new LinkedBlockingQueue<>(maxSize),
					new ThreadFactoryBuilder().setDaemon(true).setNameFormat("esf-sendHeart-executor-%d").build());
		}
		return sendHeartWorkers;
	}

}
