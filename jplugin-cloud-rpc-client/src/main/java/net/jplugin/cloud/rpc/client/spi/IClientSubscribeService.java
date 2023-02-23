package net.jplugin.cloud.rpc.client.spi;

import net.jplugin.core.kernel.api.BindExtensionPoint;
import net.jplugin.core.kernel.api.PointType;

import java.util.List;
import java.util.Set;

@BindExtensionPoint(type = PointType.UNIQUE)
public interface IClientSubscribeService {
    /**
     * 设置需要订阅的appcodes
     * @param appCodes
     */
    public void initSubscribCodeList(Set<String> appCodes);

    /**
     * 获取某个appCode的 服务列表
     * @param appCode
     */
    public Set<String> getServiceNodesList(String appCode);

    /**
     * 设置变更通知
     * @param listener
     */
    public void addServiceNodesChangeListener(IServiceNodeChangeListener listener);

}
