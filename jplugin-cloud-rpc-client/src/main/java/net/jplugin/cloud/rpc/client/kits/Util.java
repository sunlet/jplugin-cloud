package net.jplugin.cloud.rpc.client.kits;

import net.jplugin.common.kits.AssertKit;
import net.jplugin.common.kits.StringKit;
import net.jplugin.common.kits.tuple.Tuple2;

public class Util {

    public static Tuple2<String,Integer> splitAddr(String addr){
        String[] splits = StringKit.splitStr(addr, ":");
        AssertKit.assertEqual(splits.length,2);

        return Tuple2.with(splits[0],Integer.parseInt(splits[1]));
    }
}
