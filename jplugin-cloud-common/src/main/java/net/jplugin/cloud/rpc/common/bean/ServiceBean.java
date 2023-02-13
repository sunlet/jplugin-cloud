package net.jplugin.cloud.rpc.common.bean;

import net.jplugin.cloud.rpc.common.constant.ServiceSource;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ServiceBean extends AbstractContextAttachBean {

	private static final long serialVersionUID = 8131487190899864804L;

	private String serviceCode;

	private String serviceClazz;

	private ServiceSource serviceSource;

	private String comment;

	private List<MethodBean> methodList;

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getServiceClazz() {
		return serviceClazz;
	}

	public void setServiceClazz(String serviceClazz) {
		this.serviceClazz = serviceClazz;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public List<MethodBean> getMethodList() {
		return methodList;
	}

	public void setMethodList(List<MethodBean> methodList) {
		this.methodList = methodList == null ? new ArrayList<>() : methodList;
	}

	public ServiceSource getServiceSource() {
		return serviceSource;
	}

	public void setServiceSource(ServiceSource serviceSource) {
		this.serviceSource = serviceSource;
	}

	public String toString() {
		ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) {
			@Override
			protected boolean accept(Field field) {
				return !field.getName().equals("methodList");
			}
		};
		return builder.toString();
	}

}
