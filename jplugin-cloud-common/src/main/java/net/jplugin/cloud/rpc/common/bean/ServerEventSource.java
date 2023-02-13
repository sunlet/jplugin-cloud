package net.jplugin.cloud.rpc.common.bean;

public enum ServerEventSource {

	no(-1), server(1), client(2), cluster(3);

	private Byte type;

	ServerEventSource(Integer type) {
		this.type = type.byteValue();
	}

	public Byte getType() {
		return type;
	}
}
