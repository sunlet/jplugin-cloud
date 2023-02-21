package net.jplugin.cloud.rpc.io.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import de.javakaffee.kryoserializers.*;
import org.objenesis.strategy.SerializingInstantiatorStrategy;

import java.lang.reflect.InvocationHandler;
import java.net.URI;
import java.util.Arrays;
import java.util.BitSet;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.regex.Pattern;

public class KryoHolder {
	private static final ThreadLocal<Kryo> kryoLocal = new ThreadLocal<Kryo>() {
		protected Kryo initialValue() {
			Kryo kryo = new Kryo(new CompatibleMapReferenceResolver());
			kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
			kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new SerializingInstantiatorStrategy()));
			kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
			kryo.register(Pattern.class, new RegexSerializer());
			kryo.register(BitSet.class, new BitSetSerializer());
			kryo.register(URI.class, new URISerializer());
			kryo.register(UUID.class, new UUIDSerializer());
			kryo.register(GregorianCalendar.class, new GregorianCalendarSerializer());
			kryo.register(InvocationHandler.class, new JdkProxySerializer());
			UnmodifiableCollectionsSerializer.registerSerializers(kryo);
			SynchronizedCollectionsSerializer.registerSerializers(kryo);
			return kryo;
		};
	};

	public static Kryo get() {
		return kryoLocal.get();
	}
}