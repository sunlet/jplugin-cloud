package net.jplugin.cloud.rpc.common.bean;

import net.jplugin.cloud.rpc.common.api.IContext;

public abstract class AbstractContextAttachBean extends AbstractAttachBean implements IContext {

	private static final long serialVersionUID = 2595334291653214920L;

	private String contextId;

	@Override
	public String getContextId() {
		return contextId;
	}

	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

}
