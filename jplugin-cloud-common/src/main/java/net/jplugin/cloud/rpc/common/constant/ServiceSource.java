package net.jplugin.cloud.rpc.common.constant;

public enum ServiceSource {

	SERVICE_EXPORT(1), CONTROLLER(3);

	private Integer sourceType;

	private ServiceSource(Integer type) {
		this.sourceType = type;
	}

	public Byte getSourceType() {
		return sourceType.byteValue();
	}

	public static ServiceSource get(Byte type) {
		if (type == 1) {
			return ServiceSource.SERVICE_EXPORT;
		} else if (type == 3) {
			return ServiceSource.CONTROLLER;
		}
		return ServiceSource.SERVICE_EXPORT;
	}
}
