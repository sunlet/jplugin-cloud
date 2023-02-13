package net.jplugin.cloud.rpc.io.bean;

import net.jplugin.cloud.rpc.common.bean.AbstractAppBean;
import net.jplugin.cloud.rpc.common.constant.SerializerCategory;
import net.jplugin.cloud.rpc.common.util.StringUtils;
import net.jplugin.common.kits.JsonKit;

import java.lang.reflect.Type;

public class RpcRequestBean extends AbstractAppBean {

	private static final long serialVersionUID = 1813546214156042339L;

	// 服务名
	private String serviceName;

	// 请求方法
	private String methodName;

	// 参数类型
	private Class<?>[] parameterTypes;

	// 方法参数
	private Object[] parameters;

	// 返回方法类型
	private Type rtnclz;

	public RpcRequestBean createRequest(String serviceName, String methodName, Class<?>[] argsType, Object[] args,
			int serializeType) {
		this.setServiceName(serviceName);
		this.setMethodName(methodName);
		super.setSt(serializeType);
		if (super.getSt() == SerializerCategory.json) {
			// json序列化
			this.setParameterTypes(null);
			if (args != null) {
				int len = args.length;
				Object[] jsonObjs = new Object[len];
				for (int i = 0; i < len; i++) {
					Object arg = args[i];
					if (StringUtils.nullOrEmptyStr(arg)) {
						jsonObjs[i] = arg;
					} else {
						jsonObjs[i] = JsonKit.object2Json(arg);
					}
				}
				this.setParameters(jsonObjs);
			} else {
				this.setParameters(args);
			}
		} else {
			// kryo序列化
			this.setParameterTypes(argsType);
			this.setParameters(args);
		}
		this.setContextId(StringUtils.nextUid());
		return this;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters == null ? new Object[0] : parameters;
	}

	public Type getRtnclz() {
		return rtnclz;
	}

	public void setRtnclz(Type rtnclz) {
		this.rtnclz = rtnclz;
	}

}
