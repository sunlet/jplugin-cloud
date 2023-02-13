package net.jplugin.cloud.rpc.common.bean;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ServiceProviderBean extends ServerProviderBean {

	private static final long serialVersionUID = -5427509045482425589L;

	private Set<ServiceBean> serviceSet = new HashSet<>();

	private String methodJson;

	private Byte serviceType;

	private Byte category;

	public Set<ServiceBean> getServiceSet() {
		return serviceSet;
	}

	public void setServiceSet(Set<ServiceBean> serviceSet) {
		this.serviceSet = serviceSet;
	}

	public void addServiceBean(ServiceBean sb) {
		getServiceSet().add(sb);
	}

	public Byte getServiceType() {
		return serviceType;
	}

	public void setServiceType(Byte serviceType) {
		this.serviceType = serviceType;
	}

	public Byte getCategory() {
		return category;
	}

	public void setCategory(Byte category) {
		this.category = category;
	}

	public String getMethodJson() {
		return methodJson;
	}

	public void setMethodJson(String methodJson) {
		this.methodJson = methodJson;
	}

	public String toString() {
		ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) {
			@Override
			protected boolean accept(Field field) {
				return !field.getName().equals("serviceSet");
			}
		};
		return builder.toString();
	}
}
