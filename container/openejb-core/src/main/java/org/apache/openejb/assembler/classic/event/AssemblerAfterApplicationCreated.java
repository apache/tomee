package org.apache.openejb.assembler.classic.event;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.observer.Event;

@Event
public class AssemblerAfterApplicationCreated {
    private final AppInfo app;

    public AssemblerAfterApplicationCreated(final AppInfo appInfo) {
        app = appInfo;
    }

    public AppInfo getApp() {
        return app;
    }

    @Override
    public String toString() {
        return "AssemblerAfterApplicationCreated{" +
                "app=" + app.appId +
            '}';
    }
}
