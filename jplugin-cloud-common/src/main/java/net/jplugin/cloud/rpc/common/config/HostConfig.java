package net.jplugin.cloud.rpc.common.config;

import net.jplugin.cloud.rpc.common.bean.AbstractSerializeBean;

public class HostConfig extends AbstractSerializeBean {
	private static final long serialVersionUID = 8695187575103157194L;

	private String hostIp;

	private int port;

	private transient int rack = -1;

	public HostConfig(String hostIp, int hostPort) {
		this.hostIp = hostIp;
		this.port = hostPort;
	}

	public String getHostIp() {
		return this.hostIp;
	}

	public int getPort() {
		return port;
	}

//	public int getRack() {
//		if (rack == -1) {
//			rack = AbstractConfig.getRackType(hostIp);
//		}
//		return rack;
//	}
//
//	public boolean sameRack() {
//		return AbstractConfig.getCurrentRack() == getRack();
//	}

}
