package org.openejb.core.ivm;

import org.openejb.util.proxy.ProxyManager;

public class SpecialProxyInfo extends org.openejb.ProxyInfo {

    protected Object proxy;

    public SpecialProxyInfo(Object proxy) {
        super();

        this.proxy = proxy;

        BaseEjbProxyHandler handler = (BaseEjbProxyHandler) ProxyManager.getInvocationHandler(proxy);

        deploymentInfo = handler.deploymentInfo;
        primaryKey = handler.primaryKey;
        beanContainer = handler.container;

        if (handler instanceof EjbHomeProxyHandler)
            type = deploymentInfo.getHomeInterface();
        else
            type = deploymentInfo.getRemoteInterface();

    }

    public Object getProxy() {
        return proxy;
    }
}

