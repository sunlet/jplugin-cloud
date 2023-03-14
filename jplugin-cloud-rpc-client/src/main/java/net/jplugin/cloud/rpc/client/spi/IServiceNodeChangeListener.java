package net.jplugin.cloud.rpc.client.spi;

import java.util.Set;

public interface IServiceNodeChangeListener {
    void changed(String appCode, Set<String> nodeList);
}
