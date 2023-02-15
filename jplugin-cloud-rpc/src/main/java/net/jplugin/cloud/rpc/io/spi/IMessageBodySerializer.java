package net.jplugin.cloud.rpc.io.spi;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.jplugin.core.kernel.api.BindExtensionPoint;
import net.jplugin.core.kernel.api.PointType;

import java.io.IOException;

@BindExtensionPoint(type = PointType.NAMED)
public interface IMessageBodySerializer {

    Object deSerialBody(ByteBufInputStream input) throws IOException;

    void serialBody(ByteBufOutputStream byteOutputStream, Object body) throws IOException;
}
