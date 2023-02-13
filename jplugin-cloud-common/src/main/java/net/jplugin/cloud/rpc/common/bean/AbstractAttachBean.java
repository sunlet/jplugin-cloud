package net.jplugin.cloud.rpc.common.bean;

import net.jplugin.cloud.rpc.common.api.IAttach;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAttachBean extends AbstractSerializeBean implements IAttach {

	private static final long serialVersionUID = 53546724343681255L;

	private Map<String, Object> attachments;

	@Override
	public Map<String, Object> getAttachments() {
		return attachments;
	}

	public void setAttachments(Map<String, Object> attachments) {
		this.attachments = attachments == null ? new HashMap<String, Object>() : attachments;
	}

	public void setAttachment(String key, Object value) {
		if (attachments == null) {
			attachments = new HashMap<String, Object>();
		}
		attachments.put(key, value);
	}

	public void setAttachmentIfAbsent(String key, Object value) {
		if (attachments == null) {
			attachments = new HashMap<String, Object>();
		}
		if (!attachments.containsKey(key)) {
			attachments.put(key, value);
		}
	}

	public void addAttachments(Map<String, Object> attachments) {
		if (attachments == null) {
			return;
		}
		if (this.attachments == null) {
			this.attachments = new HashMap<String, Object>();
		}
		this.attachments.putAll(attachments);
	}

	public void addAttachmentsIfAbsent(Map<String, String> attachments) {
		if (attachments == null) {
			return;
		}
		for (Map.Entry<String, String> entry : attachments.entrySet()) {
			setAttachmentIfAbsent(entry.getKey(), entry.getValue());
		}
	}

	public Object getAttachment(String key) {
		if (attachments == null) {
			return null;
		}
		return attachments.get(key);
	}

	public Object getAttachment(String key, Object defaultValue) {
		if (attachments == null) {
			return defaultValue;
		}
		Object value = attachments.get(key);
		if (null == value) {
			return defaultValue;
		}
		return value;
	}

}
