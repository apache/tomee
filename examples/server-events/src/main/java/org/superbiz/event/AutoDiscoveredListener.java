package org.superbiz.event;

import org.apache.openejb.assembler.classic.event.AssemblerAfterApplicationCreated;
import org.apache.openejb.observer.Observes;

public class AutoDiscoveredListener {
    private static String appName;

    public void appCreated(@Observes final AssemblerAfterApplicationCreated appCreatedEvent) {
        appName = appCreatedEvent.getApp().appId;
    }

    public static String getAppName() {
        return appName;
    }
}
