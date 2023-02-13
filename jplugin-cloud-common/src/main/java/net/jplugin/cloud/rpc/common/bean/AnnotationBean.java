package net.jplugin.cloud.rpc.common.bean;

public class AnnotationBean extends AbstractAttachBean {
	private static final long serialVersionUID = -7108122439753817380L;

	private String annoClazz;

	private String name;

	private String callerType;

	private String restrictLevel;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAnnoClazz() {
		return annoClazz;
	}

	public void setAnnoClazz(String annoClazz) {
		this.annoClazz = annoClazz;
	}

	public String getCallerType() {
		return callerType;
	}

	public void setCallerType(String callerType) {
		this.callerType = callerType;
	}

	public String getRestrictLevel() {
		return restrictLevel;
	}

	public void setRestrictLevel(String restrictLevel) {
		this.restrictLevel = restrictLevel;
	}

}