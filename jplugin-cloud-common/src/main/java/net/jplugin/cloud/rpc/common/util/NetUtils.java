package net.jplugin.cloud.rpc.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class NetUtils {

	private static final Logger logger = LoggerFactory.getLogger(NetUtils.class);

	public static final String LOCALHOST = "127.0.0.1";

	public static final String ANYHOST = "0.0.0.0";

	private static final Pattern ADDRESS_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}\\:\\d{1,5}$");

	public static boolean isValidAddress(String address) {
		return ADDRESS_PATTERN.matcher(address).matches();
	}

	private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");

	public static boolean isLocalHost(String host) {
		return host != null && (LOCAL_IP_PATTERN.matcher(host).matches() || host.equalsIgnoreCase("localhost"));
	}

	public static boolean isAnyHost(String host) {
		return "0.0.0.0".equals(host);
	}

	public static boolean isInvalidLocalHost(String host) {
		return host == null || host.length() == 0 || host.equalsIgnoreCase("localhost") || host.equals("0.0.0.0")
				|| (LOCAL_IP_PATTERN.matcher(host).matches());
	}

	public static boolean isValidLocalHost(String host) {
		return !isInvalidLocalHost(host);
	}

	public static InetSocketAddress getLocalSocketAddress(String host, int port) {
		return isInvalidLocalHost(host) ? new InetSocketAddress(port) : new InetSocketAddress(host, port);
	}

	private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

	private static boolean isValidAddress(InetAddress address) {
		if (address == null || address.isLoopbackAddress())
			return false;
		String name = address.getHostAddress();
		return (name != null && !ANYHOST.equals(name) && !LOCALHOST.equals(name) && IP_PATTERN.matcher(name).matches());
	}

	public static String getLocalHost() {
		InetAddress address = getLocalAddress();
		return address == null ? LOCALHOST : address.getHostAddress();
	}

	private static volatile InetAddress LOCAL_ADDRESS = null;

	/**
	 * 遍历本地网卡，返回第一个合理的IP。
	 * 
	 * @return 本地网卡IP
	 */
	public static InetAddress getLocalAddress() {
		if (LOCAL_ADDRESS != null)
			return LOCAL_ADDRESS;
		InetAddress localAddress = getLocalAddress0();
		LOCAL_ADDRESS = localAddress;
		return localAddress;
	}

	private static InetAddress getLocalAddress0() {
		InetAddress localAddress = null;
		try {
			localAddress = InetAddress.getLocalHost();
			if (isValidAddress(localAddress)) {
				return localAddress;
			}
		} catch (Throwable e) {
			logger.warn("Failed to retriving ip address, " + e);
		}
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			if (interfaces != null) {
				while (interfaces.hasMoreElements()) {
					try {
						NetworkInterface network = interfaces.nextElement();
						Enumeration<InetAddress> addresses = network.getInetAddresses();
						if (addresses != null) {
							while (addresses.hasMoreElements()) {
								try {
									InetAddress address = addresses.nextElement();
									if (isValidAddress(address)) {
										return address;
									}
								} catch (Throwable e) {
									logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
								}
							}
						}
					} catch (Throwable e) {
						logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
					}
				}
			}
		} catch (Throwable e) {
			logger.warn("Failed to retriving ip address, " + e.getMessage());
		}
		logger.warn("Could not get local host ip address, will use 127.0.0.1 instead.");
		return localAddress;
	}

	/**
	 * 获取本地外网IP地址
	 * 
	 * @return
	 */
	public static String getLocalIp() {
		StringBuilder ip = new StringBuilder();
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()
							&& inetAddress.isSiteLocalAddress()) {
						if (intf.getDisplayName().indexOf("Virtual") < 0) {
							ip.append(inetAddress.getHostAddress() + ",");
						}
					}
				}
			}
		} catch (SocketException ex) {
			System.out.println("kmonitor get ip exception: " + ex.getMessage());
		}
		String gotIp = ip.toString();
		if (gotIp.length() > 0) {
			return gotIp.substring(0, gotIp.lastIndexOf(","));
		}
		return null;
	}

	public static String getHostIp() {
		String hostname = "UNKNOW";
		String ip = null;
		try {
			String[] str = ManagementFactory.getRuntimeMXBean().getName().split("@");
			hostname = str[1];
			hostname = hostname.replaceAll(":", "_");
		} catch (Exception e) {
		}
		// 获取ip
		Runtime run = Runtime.getRuntime();
		BufferedReader br = null;
		Process p = null;
		try {
			p = run.exec(new String[] { "/bin/bash", "-c",
					"ifconfig -a|grep inet|grep -v inet6|grep -v 127.0.0.1|grep -v localhost|grep -v 0.0.0.0" });
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String serverId = br.readLine();
			System.out.println("kmonitor, get local ip str: " + serverId);
			ip = serverId.trim().split("\\s+|:")[2];
			ip = ip.replaceAll(":", "_");
			if (ip.indexOf('.') < 0) {
				ip = serverId.trim().split("\\s+|:")[1];
			}
			System.out.println("kmonitor got local ip: " + ip);
			p.waitFor();
		} catch (Exception e) {
			ip = getLocalIp();
			System.out.println("kmonitor win get local ip: " + ip);
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
				}
			if (p != null)
				p.destroy();
		}
		if (ip == null || "".equals(ip.trim())) {
			ip = hostname;
		}
		return ip;
	}
}