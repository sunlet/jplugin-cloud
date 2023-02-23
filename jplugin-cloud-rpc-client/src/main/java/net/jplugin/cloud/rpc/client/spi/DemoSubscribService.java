package net.jplugin.cloud.rpc.client.spi;

import net.jplugin.core.kernel.api.BindExtension;

import java.util.HashSet;
import java.util.Set;

@BindExtension

public class DemoSubscribService implements IClientSubscribeService {
    private Set<String> set;

    @Override
    public void initSubscribCodeList(Set<String> appCodes) {
        this.set = appCodes;
    }

    @Override
    public Set<String> getServiceNodesList(String appCode) {
        String s =  "127.0.0.1:9090";
        HashSet<String> temp = new HashSet<String>();;
        temp.add(s);
        return temp;
    }

    @Override
    public void addServiceNodesChangeListener(IServiceNodeChangeListener listener) {

    }
}
