package net.jplugin.cloud.composedapp;

import net.jplugin.cloud.common.CloudPluginPriority;
import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.PluginAnnotation;

@PluginAnnotation
public class Plugin extends AbstractPlugin {

    @Override
    public int getPrivority() {
        return CloudPluginPriority.COMPOSEDAPP;
    }

    @Override
    public void init() {


    }
}
