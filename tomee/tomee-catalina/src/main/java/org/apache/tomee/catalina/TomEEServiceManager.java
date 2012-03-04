package org.apache.tomee.catalina;

import org.apache.openejb.server.SimpleServiceManager;

public class TomEEServiceManager extends SimpleServiceManager {
    public TomEEServiceManager() {
        setServiceManager(this);
    }

    @Override
    protected boolean accept(final String serviceName) {
        // managed manually or done in a different way in TomEE
        return !"httpejbd".equals(serviceName)
                && !"ejbd".equals(serviceName)
                && !"ejbds".equals(serviceName);
    }
}
