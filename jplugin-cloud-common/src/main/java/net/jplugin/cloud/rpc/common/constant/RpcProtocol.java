package net.jplugin.cloud.rpc.common.constant;

public class RpcProtocol {

	public static final Byte all = 0;

	public static final Byte rpc = 1;

	public static final Byte http = 2;

	public static final Byte socket = 3;

	public static final String s_rpc = "rpc";

	public static final String s_http = "http";

	public static final String s_socket = "socket";

	public static Byte transfer(String protocol) {
		if (s_rpc.equals(protocol)) {
			return rpc;
		} else if (s_http.equals(protocol)) {
			return http;
		} else if (s_socket.equals(protocol)) {
			return socket;
		}
		return socket;
	}
}
