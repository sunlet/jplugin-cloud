package net.jplugin.cloud.config;

import net.jplugin.core.config.api.ConfigFactory;
import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.PluginAnnotation;

@PluginAnnotation(prepareSeq = 2)
public class Plugin extends AbstractPlugin {

    public static void prepare() {
        ConfigFactory._setRemoteConfigProvidor(NacosConfigProvidor.me());
    }

    @Override
    public int getPrivority() {
        return 0;
    }

    @Override
    public void init() {
    }
}
