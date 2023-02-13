package net.jplugin.cloud.rpc.common.bean;

import net.jplugin.cloud.rpc.common.config.AbstractConfig;
import net.jplugin.cloud.rpc.common.constant.State;
import net.jplugin.cloud.rpc.common.util.NetUtils;

public class AbstractAppBean extends AbstractContextAttachBean {

	private static final long serialVersionUID = -5244315561475944825L;

	private String appCode;

	private String ip;

	private Integer rpcPort;

	private Integer httpPort;

	private Boolean reportheartbeat;

	private Byte state;

	private long timestamp;

	private String clusterNodeId;

	private Integer tomcatPort = -1;

	// 序列化类型，用于rpc模式通信，支持json和kryo
	private int st;

	// 是否灰度节点
	private Boolean grayNode;

	public AbstractAppBean() {
		this.appCode = AbstractConfig.getAppcode();
		this.rpcPort = AbstractConfig.getRpcPort();
//		this.httpPort = AbstractConfig.getHttpPort();
		this.httpPort = null;
		this.grayNode = AbstractConfig.isGrayNode();
		this.reportheartbeat = true;
		this.state = State.online;
		this.ip = NetUtils.getLocalHost();
		this.timestamp = System.currentTimeMillis();
	}

	public String getAppCode() {
		return appCode;
	}

	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getRpcPort() {
		return rpcPort;
	}

	public void setRpcPort(Integer rpcPort) {
		this.rpcPort = rpcPort;
	}

	public Integer getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(Integer httpPort) {
		this.httpPort = httpPort;
	}

	public Boolean getReportheartbeat() {
		return reportheartbeat;
	}

	public void setReportheartbeat(Boolean reportheartbeat) {
		this.reportheartbeat = reportheartbeat;
	}

	public Byte getState() {
		return state;
	}

	public void setState(Byte state) {
		this.state = state;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getClusterNodeId() {
		return clusterNodeId;
	}

	public void setClusterNodeId(String clusterNodeId) {
		this.clusterNodeId = clusterNodeId;
	}

	public Integer getTomcatPort() {
		return tomcatPort;
	}

	public void setTomcatPort(Integer tomcatPort) {
		this.tomcatPort = tomcatPort;
	}

	public int getSt() {
		return st;
	}

	public void setSt(int st) {
		this.st = st;
	}

	public Boolean getGrayNode() {
		return grayNode == null ? false : grayNode;
	}

	public void setGrayNode(Boolean grayNode) {
		this.grayNode = grayNode;
	}

}
