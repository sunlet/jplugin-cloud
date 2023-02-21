package net.jplugin.cloud.rpc.io.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;

public class KryoSerializer {
	private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

	public static void serialize(Object object, ByteBufOutputStream byteOutputStream) {
		Kryo kryo = KryoHolder.get();
//		int startIdx = byteBuf.writerIndex();
//		ByteBufOutputStream byteOutputStream = new ByteBufOutputStream(byteBuf);
//		try {
//			byteOutputStream.write(LENGTH_PLACEHOLDER);
			Output output = new Output(1024 * 4, -1);
			output.setOutputStream(byteOutputStream);
			kryo.writeClass(output, object.getClass());
			kryo.writeObject(output, object);

			output.flush();
			output.close();

//			int endIdx = byteBuf.writerIndex();

//			byteBuf.setInt(startIdx, endIdx - startIdx - 4);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	@SuppressWarnings("unchecked")
	public static Object deserialize(ByteBufInputStream stream) {
//		if (byteBuf == null)
//			return null;

		Input input = new Input(stream);
		Kryo kryo = KryoHolder.get();
		Registration rt = kryo.readClass(input);
		return kryo.readObject(input, rt.getType());
	}
}