package net.jplugin.cloud.rpc.msg;

import io.netty.buffer.ByteBuf;
import net.jplugin.core.kernel.api.BindExtensionPoint;
import net.jplugin.core.kernel.api.PointType;

@BindExtensionPoint(type = PointType.NAMED)

public interface IBodySerializer {

    void serialize(Object object, ByteBuf byteBuf);

    Object deserialize(ByteBuf byteBuf);
}
