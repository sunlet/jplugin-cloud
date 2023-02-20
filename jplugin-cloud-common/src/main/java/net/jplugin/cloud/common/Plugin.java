package net.jplugin.cloud.common;

import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.PluginAnnotation;

@PluginAnnotation(prepareSeq = 0)
public class Plugin extends AbstractPlugin {
    public static void prepare(){
        //xxxxx

        CloudEnvironment.INSTANCE.loadFromConfig();
    }

    @Override
    public void init() {

    }

    @Override
    public int getPrivority() {
        return CloudPluginPriority.BASE;
    }
}
