package net.jplugin.cloud.rpc.common.bean;

import net.jplugin.cloud.rpc.common.config.AbstractConfig;
import net.jplugin.cloud.rpc.common.constant.DevLanguages;
import net.jplugin.cloud.rpc.common.constant.EnableStatus;
import net.jplugin.cloud.rpc.common.constant.RpcProtocol;
import net.jplugin.cloud.rpc.common.constant.SerializerCategory;
import net.jplugin.common.kits.StringKit;

public class ServerProviderBean extends AbstractAppBean {

	private static final long serialVersionUID = 4644778021965011075L;

	private static final transient String default_TenantId = "-1";

	private Boolean enable = EnableStatus.enable;

	private Byte protocol = RpcProtocol.all;

	private Byte languages = DevLanguages.java;

	@Deprecated
	private Byte serialization = SerializerCategory.json;

	private String platformTeantId;

	private Integer weight = 10;

	// 服务节点的租户灰度策略
	private String grayPolicy;

	// 机房标记
	private transient int rackType = -1;
	
	private Byte serverType; 

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public Byte getProtocol() {
		return protocol;
	}

	public void setProtocol(Byte protocol) {
		this.protocol = protocol;
	}

	public Byte getLanguages() {
		return languages;
	}

	public void setLanguages(Byte languages) {
		this.languages = languages;
	}

	public Byte getSerialization() {
		return serialization;
	}

	public void setSerialization(Byte serialization) {
		this.serialization = serialization;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public boolean supportRpc() {
		return (languages == DevLanguages.java) && ((protocol == RpcProtocol.all) || (protocol == RpcProtocol.rpc));
	}

	public String getPlatformTeantId() {
		return StringKit.isEmpty(platformTeantId) ? default_TenantId : platformTeantId;
	}

	public void setPlatformTeantId(String platformTeantId) {
		this.platformTeantId = platformTeantId;
	}

	public static String getDefaultTenantid() {
		return default_TenantId;
	}

	public String getGrayPolicy() {
		return grayPolicy;
	}

	public void setGrayPolicy(String grayPolicy) {
		this.grayPolicy = grayPolicy;
	}

//	public int getRackType() {
//		if (rackType == -1) {
//			rackType = AbstractConfig.getRackType(this.getIp());
//		}
//		return rackType;
//	}
//
//	public boolean sameRack() {
//		return AbstractConfig.getCurrentRack() == getRackType();
//	}

	public Byte getServerType() {
		return serverType;
	}

	public void setServerType(Byte serverType) {
		this.serverType = serverType;
	}

	

}
