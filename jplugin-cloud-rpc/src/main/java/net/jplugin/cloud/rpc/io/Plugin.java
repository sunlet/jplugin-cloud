package net.jplugin.cloud.rpc.io;

import net.jplugin.cloud.common.CloudPluginPriority;
import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.PluginAnnotation;

@PluginAnnotation
public class Plugin extends AbstractPlugin {
    @Override
    public int getPrivority() {
        return CloudPluginPriority.BASE;
    }

    @Override
    public void init() {

    }
}
