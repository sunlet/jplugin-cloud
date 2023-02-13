package net.jplugin.cloud.rpc.common.bean;

//import net.jplugin.cloud.rpc.common.util.JSONUtil;
import net.jplugin.common.kits.JsonKit;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public abstract class AbstractSerializeBean implements Serializable {

	private static final long serialVersionUID = -1155550396888681152L;

	private String version = Version.getVersion();

//	public String toJsonString() {
//		return JSONUtil.toJson(this);
//	}
	public String toJsonString(){
		return JsonKit.object2Json(this);
	}


	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
