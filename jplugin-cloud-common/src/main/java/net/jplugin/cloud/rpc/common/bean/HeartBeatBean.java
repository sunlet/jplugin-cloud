package net.jplugin.cloud.rpc.common.bean;

import net.jplugin.cloud.rpc.common.constant.ServerNodeRole;

public class HeartBeatBean extends AbstractAttachBean {

	private static final long serialVersionUID = 7879705165072080143L;

	private String objectId;

	private ServerNodeRole nodeRole;

	private Long timestamp;

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public ServerNodeRole getNodeRole() {
		return nodeRole;
	}

	public void setNodeRole(ServerNodeRole nodeRole) {
		this.nodeRole = nodeRole;
	}

}
