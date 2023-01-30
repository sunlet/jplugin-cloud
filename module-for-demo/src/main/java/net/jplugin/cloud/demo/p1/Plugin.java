package net.jplugin.cloud.demo.p1;

import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.PluginAnnotation;

@PluginAnnotation
public class Plugin extends AbstractPlugin {

    @Override
    public void init() {
        System.out.println("initing................");
    }

    @Override
    public int getPrivority() {
        return 0;
    }
}
