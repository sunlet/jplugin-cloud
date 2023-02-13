package net.jplugin.cloud.rpc.io.codec.kryo;

import com.esotericsoftware.kryo.util.MapReferenceResolver;

public class CompatibleMapReferenceResolver extends MapReferenceResolver {

	@Override
	public Object getReadObject(@SuppressWarnings("rawtypes") Class type, int id) {
		if (id < super.readObjects.size()) {
			return super.getReadObject(type, id);
		}
		return null;
	}

}
