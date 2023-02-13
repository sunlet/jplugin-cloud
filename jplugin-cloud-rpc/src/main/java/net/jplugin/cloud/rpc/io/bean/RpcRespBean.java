package net.jplugin.cloud.rpc.io.bean;

import net.jplugin.cloud.rpc.common.bean.AbstractAppBean;
import net.jplugin.cloud.rpc.common.constant.SerializerCategory;
import net.jplugin.cloud.rpc.common.util.StringUtils;
import net.jplugin.common.kits.JsonKit;

public class RpcRespBean extends AbstractAppBean {

	private static final long serialVersionUID = -5271846335261105704L;

	// 返回的结果
	private Object val;

	// 异常原因
	private Throwable cause;

	public RpcRespBean createResponse(Object val, Throwable cause) {
		this.val = val;
		this.cause = cause;
		return this;
	}

	public Object getVal() {
		return val;
	}

	public void setVal(Object val) {
		if (super.getSt() == SerializerCategory.json) {
			// json序列化
			if (StringUtils.nullOrEmptyStr(val)) {
				this.val = val;
			} else {
				this.val = JsonKit.object2Json(val);
			}
		} else {
			this.val = val;
		}
	}

	public Throwable getCause() {
		return cause;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
	}

	public boolean hasError() {
		return this.cause != null;
	}

}
