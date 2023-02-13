//package net.jplugin.cloud.rpc.io.util;
//
//import net.jplugin.cloud.rpc.io.channel.IChannel;
//import net.jplugin.common.kits.StringKit;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.net.InetSocketAddress;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CopyOnWriteArrayList;
//import java.util.function.Consumer;
//import java.util.function.Predicate;
//
//public class ClientContextUtil {
//
//	private static final Logger logger = LoggerFactory.getLogger(ClientContextUtil.class);
//
//	private static final Map<String, CopyOnWriteArrayList<RPClientContext>> clientContextMap = new ConcurrentHashMap<>();
//
//	private ClientContextUtil() {
//	}
//
//	public static void addClientInfo(String appcode, String esfPort, IChannel channel) {
//		if (StringKit.isEmpty(appcode) || StringKit.isEmpty(esfPort)) {
//			return;
//		}
//		if (channel == null || !channel.isConnected()) {
//			logger.error("clientAppCode=" + appcode + ",esfPort=" + esfPort + ",Channel=" + channel);
//			return;
//		}
//		long start = System.currentTimeMillis();
//		try {
//			CopyOnWriteArrayList<RPClientContext> list = clientContextMap.get(appcode);
//			if (list == null) {
//				synchronized (clientContextMap) {
//					list = clientContextMap.get(appcode);
//					if (list == null) {
//						list = new CopyOnWriteArrayList<>();
//						clientContextMap.put(appcode, list);
//					}
//				}
//			}
//			String clientIP = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
//			if (logger.isDebugEnabled()) {
//				logger.debug("before=> [appcode=" + appcode + ",clientIp=" + clientIP + ",esfPort=" + esfPort
//						+ ",channelId=" + channel.channelId() + ",connected=" + channel.isConnected() + ",size="
//						+ list.size() + "]");
//			}
//			RPClientContext findFirst = list.parallelStream().filter(new Predicate<RPClientContext>() {
//
//				@Override
//				public boolean test(RPClientContext t) {
//					try {
//						return t.clientAppcode.equals(appcode) && t.esfPort.equals(esfPort)
//								&& t.clientIP.equals(clientIP);
//					} catch (Exception e) {
//						return false;
//					}
//				}
//			}).findFirst().orElse(null);
//			if (logger.isDebugEnabled()) {
//				logger.debug("findFirst=>" + findFirst);
//			}
//			if (findFirst != null && !findFirst.chanelContext.channel.isConnected()) {
//				list.remove(findFirst);
//				findFirst = null;
//			}
//			if (findFirst == null) {
//				RPClientContext context = new RPClientContext(appcode, clientIP, esfPort, channel);
//				list.add(context);
//			}
//			if (logger.isDebugEnabled()) {
//				logger.debug("after=> [appcode=" + appcode + ",clientIp=" + clientIP + ",esfPort=" + esfPort
//						+ ",channelId=" + channel.channelId() + ",connected=" + channel.isConnected() + ",size="
//						+ list.size() + "]");
//			}
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		} finally {
//			if (logger.isDebugEnabled()) {
//				logger.debug("save client info cost time(ms) = " + (System.currentTimeMillis() - start));
//			}
//		}
//	}
//
//	public static void removeClient(String channelID) {
//		if (StringKit.isEmpty(channelID)) {
//			return;
//		}
//		try {
//			Iterator<CopyOnWriteArrayList<RPClientContext>> iterator = clientContextMap.values().iterator();
//			while (iterator.hasNext()) {
//				CopyOnWriteArrayList<RPClientContext> list = iterator.next();
//				list.forEach(new Consumer<RPClientContext>() {
//
//					@Override
//					public void accept(RPClientContext t) {
//						IChannel savedChannel = t.chanelContext.channel;
//						if (channelID.equals(savedChannel.channelId())) {
//							list.remove(t);
//							t = null;
//						}
//					}
//
//				});
//			}
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		}
//	}
//
//	public static List<RPClientContext> getClientContext(String appcode) {
//		List<RPClientContext> list = new ArrayList<>();
//		try {
//			CopyOnWriteArrayList<RPClientContext> resultList = clientContextMap.get(appcode);
//			if (resultList != null) {
//				resultList.forEach(new Consumer<RPClientContext>() {
//
//					@Override
//					public void accept(RPClientContext t) {
//						if (t.chanelContext.channel.isConnected()) {
//							list.add(t);
//						} else {
//							resultList.remove(t);
//						}
//					}
//				});
//			}
//
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		}
//		return Collections.unmodifiableList(list);
//	}
//
//	public static RPClientContext getClientContext(String appcode, String ip) {
//		try {
//			List<RPClientContext> list = getClientContext(appcode);
//			if (list == null || list.isEmpty()) {
//				return null;
//			}
//			for (RPClientContext cc : list) {
//				if (ip.equals(cc.clientIP)) {
//					if (cc.chanelContext.channel.isConnected()) {
//						return cc;
//					}
//				}
//			}
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		}
//		return null;
//	}
//
//	public static RPClientContext getClientContext(String appcode, String ip, String esfPort) {
//		try {
//			List<RPClientContext> list = getClientContext(appcode);
//			if (list == null || list.isEmpty()) {
//				return null;
//			}
//			for (RPClientContext cc : list) {
//				if (ip.equals(cc.clientIP) && esfPort.equals(cc.esfPort)) {
//					if (cc.chanelContext.channel.isConnected()) {
//						return cc;
//					}
//				}
//			}
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		}
//		return null;
//	}
//}
