package net.jplugin.cloud.rpc.io.util;

import net.jplugin.cloud.rpc.io.message.RpcMessage;
import net.jplugin.core.config.api.CloudEnvironment;
import net.jplugin.core.kernel.api.PluginEnvirement;

import java.net.URL;

public class MessageUtil {

    public static final String APP_CODE = "APP_CODE";
    public static final String MODULE_CODE = "MODULE_CODE";
    public static final String TIME = "TIME";
    private static final String VERSION = "VERSION";

    static String version = null;

    public static RpcMessage getClientInfoMessage() {
        RpcMessage msg = RpcMessage.create(RpcMessage.TYPE_CLIENT_INFO).header(APP_CODE, CloudEnvironment.INSTANCE.getAppCode())
                .header(MODULE_CODE, CloudEnvironment.INSTANCE.getModuleCode()).header(TIME, System.currentTimeMillis()+"").header(VERSION, getVersion());
        return msg;
    }

    private static final String VERSION_JAR_NAME="jplugin-cloud-";
    private static String getVersion() {
        if (version==null){
            synchronized (MessageUtil.class){
                URL res = MessageUtil.class.getResource("");

                String path = res.getPath();
                int pos = path.lastIndexOf(VERSION_JAR_NAME);
                if (pos<0) version = "";
                else{
                    int pos2 = path.indexOf("/",pos+VERSION_JAR_NAME.length());
                    if (pos2<0) version = "";
                    else{
                        version = path.substring(pos+VERSION_JAR_NAME.length(),pos2);
                    }
                }
                PluginEnvirement.INSTANCE.getStartLogger().log("cloud version:"+version);
            }
        }
        return version;
    }

    public static RpcMessage getServerInfoMessage() {
        RpcMessage msg = RpcMessage.create(RpcMessage.TYPE_SERVER_INFO).header(APP_CODE, CloudEnvironment.INSTANCE.getAppCode())
                .header(MODULE_CODE, CloudEnvironment.INSTANCE.getModuleCode()).header(TIME, System.currentTimeMillis()+"").header(VERSION,getVersion());
        return msg;
    }
}
