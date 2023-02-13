package net.jplugin.cloud.rpc.common.api;

import java.util.Map;

public interface IAttach {

	Map<String, Object> getAttachments();

	Object getAttachment(String key);

	Object getAttachment(String key, Object defaultVal);
}
