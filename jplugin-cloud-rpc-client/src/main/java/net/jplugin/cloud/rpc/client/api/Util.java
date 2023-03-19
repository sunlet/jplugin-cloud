package net.jplugin.cloud.rpc.client.api;

import net.jplugin.common.kits.AssertKit;

import java.lang.reflect.Type;

public class Util {
    static Type[] getTypes(Object[] args) {
        Type[] types = new Type[args.length];

        for (int i = 0; i < types.length; i++) {
            AssertKit.assertNotNull(args[i], "arg");
            types[i] = args[i].getClass();
        }
        return types;
    }
}
