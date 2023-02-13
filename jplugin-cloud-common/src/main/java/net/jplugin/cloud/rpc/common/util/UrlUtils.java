package net.jplugin.cloud.rpc.common.util;

import net.jplugin.cloud.rpc.common.config.HostConfig;
import net.jplugin.common.kits.StringKit;

import java.util.ArrayList;
import java.util.List;

public class UrlUtils {

	public static List<HostConfig> parseUrl(String urls) {
		List<HostConfig> tempList = new ArrayList<>();
		if (StringKit.isEmpty(urls)) {
			return tempList;
		}
		String[] nodes = urls.split(",");
		for (String node : nodes) {
			if (StringKit.isEmpty(node)) {
				continue;
			}
			int pos = node.indexOf("://");
			if (pos > 0) {
				node = node.substring(pos + 3);
			}
			if (StringKit.isEmpty(node)) {
				continue;
			}
			String[] host = node.split(":");
			tempList.add(new HostConfig(host[0], Integer.parseInt(host[1])));
		}
		return tempList;
	}

}
