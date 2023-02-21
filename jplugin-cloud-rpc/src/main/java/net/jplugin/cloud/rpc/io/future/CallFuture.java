package net.jplugin.cloud.rpc.io.future;


import net.jplugin.cloud.rpc.common.bean.AbstractContextAttachBean;
import net.jplugin.cloud.rpc.common.config.AbstractConfig;
import net.jplugin.common.kits.ThreadFactoryBuilder;
import net.jplugin.common.kits.client.ICallback;
import net.jplugin.core.rclient.api.RemoteExecuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.net.SocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CallFuture<T> extends AbstractContextAttachBean {

	private static final long serialVersionUID = -8250017494915963821L;

	private static final Logger logger = LoggerFactory.getLogger(CallFuture.class);

	private T val;

	private Semaphore semaphore = new Semaphore(0);

	private AtomicBoolean done = new AtomicBoolean(false);

	private long timeout = AbstractConfig.getDefaultTimeoutInMills();

	private Throwable exception;

	private long startTime = System.currentTimeMillis();

	private ICallback callback;

	private boolean async = false;

	private static final ExecutorService ayncWorkers = Executors.newCachedThreadPool(
			new ThreadFactoryBuilder().setDaemon(true).setNameFormat("esf-rpc-async-callback-%d").build());

	/**
	 * 调用的服务方信息
	 */
	private String serverInfo;

	/**
	 * 方法返回结果类型
	 */
	private Type rtnclz;

	public CallFuture(SocketAddress remoteAddress) {
		this.serverInfo = (remoteAddress == null ? null : remoteAddress.toString());
		this.setStartTime(System.currentTimeMillis());
	}

	public T getVal() throws Exception {
		boolean done = isDone();
		try {
			if (!done) {
				done = semaphore.tryAcquire(this.timeout, TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		setDone(true);
		if (done) {// 合理时间内结束
			if (hasError()) {
				if (exception instanceof RuntimeException) {
					throw (RuntimeException) exception;
				}
				throw new RuntimeException(exception.getMessage(), exception);
			}
			return val;
		}
		// 超时结束
		throw new TimeoutException("rpc服务请求超时,startTime(ms)=" + startTime + ",contextId=" + getContextId()
				+ ",serverInfo=" + this.serverInfo);
	}

	@SuppressWarnings("unchecked")
	public void setVal(Object val) {
		this.val = (T) val;
		setDone(true);
	}

	public boolean isDone() {
		return done.get();
	}

	public void setDone(boolean done) {
		this.done.compareAndSet(false, done);
		this.semaphore.release();
		if (logger.isDebugEnabled()) {
			logger.debug("cid=" + getContextId() + ",cost(ms)=" + (System.currentTimeMillis() - startTime));
		}
		if (async && callback != null) {
			// 异步调用
			try {
				ayncWorkers.execute(() -> callback.callback(hasError() ? exception : val));
			} catch (Exception e) {
				logger.error("异步callBack回调执行异常：" + e.getMessage(), e);
			}
		}
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception, SocketAddress serverAddress) {
		this.exception = exception;
		setDone(true);
		if (exception instanceof RemoteExecuteException) {
			RemoteExecuteException re = (RemoteExecuteException) exception;
			if (logger.isWarnEnabled()) {
				logger.warn("[cid=" + getContextId() + ",srv=" + serverAddress + "]请求失败：errno=" + re.getCode()
						+ ",errmsg=" + re.getMessage());
			}
		} else {
			logger.error("[cid=" + getContextId() + ",srv=" + serverAddress + "]请求异常：" + exception.getMessage(),
					exception);
		}
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public boolean hasError() {
		return this.exception != null;
	}

	// 超时设置，单位毫秒
	public void setTimeout(long timeoutInmills) {
		if (timeoutInmills < 0) {
			throw new IllegalArgumentException("timeoutInmills negative!");
		}
		if (timeoutInmills == 0) {
			// 永不超时
			this.timeout = Long.MAX_VALUE;
			return;
		}
		this.timeout = timeoutInmills;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public ICallback getCallback() {
		return callback;
	}

	public void setCallback(ICallback callback) {
		this.callback = callback;
	}

	public String getServerInfo() {
		return serverInfo;
	}

	public void setServerInfo(String serverInfo) {
		this.serverInfo = serverInfo;
	}

	public Type getRtnclz() {
		return rtnclz;
	}

	public void setRtnclz(Type rtnclz) {
		this.rtnclz = rtnclz;
	}

}
