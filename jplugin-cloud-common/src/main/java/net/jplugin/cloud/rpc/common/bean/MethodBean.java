package net.jplugin.cloud.rpc.common.bean;

import java.util.ArrayList;
import java.util.List;

public class MethodBean extends AbstractContextAttachBean {

	private static final long serialVersionUID = 6739670929567622404L;

	private String methodName;

	private String methodCode;

	private String[] argsType;

	private String returnType;

	private String comment;

	private List<AnnotationBean> annotationList;

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodCode() {
		return methodCode;
	}

	public void setMethodCode(String methodCode) {
		this.methodCode = methodCode;
	}

	public String[] getArgsType() {
		return argsType;
	}

	public void setArgsType(String[] argsType) {
		this.argsType = argsType == null ? new String[0] : argsType;
	}

	public String getReturnType() {
		return returnType == null ? Void.TYPE.getName() : returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<AnnotationBean> getAnnotationList() {
		return annotationList;
	}

	public void setAnnotationList(List<AnnotationBean> annotationList) {
		this.annotationList = annotationList == null ? new ArrayList<>() : annotationList;
	}

	public void addAnnotationBean(AnnotationBean ab) {
		if (this.annotationList == null) {
			this.annotationList = new ArrayList<>();
		}
		this.annotationList.add(ab);
	}

	public void addParameters(Class<?>[] clazz) {
		if (clazz == null || clazz.length == 0) {
			setArgsType(new String[0]);
			return;
		}
		String[] args = new String[clazz.length];
		int i = 0;
		for (Class<?> clz : clazz) {
			args[i++] = clz.getName();
		}
		setArgsType(args);
	}

}
