package net.jplugin.cloud.rpc.common.bean;

public class HeartResponseBean extends AbstractAttachBean {

	private static final long serialVersionUID = 5353771757708160963L;

	private boolean notify;

	private ServerEventSource event;

	public boolean isNotify() {
		return notify;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	public ServerEventSource getEvent() {
		return event;
	}

	public void setEvent(ServerEventSource event) {
		this.event = event;
	}

}
