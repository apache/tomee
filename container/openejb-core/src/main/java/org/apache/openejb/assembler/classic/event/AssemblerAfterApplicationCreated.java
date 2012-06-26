package org.apache.openejb.assembler.classic.event;

import org.apache.openejb.assembler.classic.AppInfo;

public class AssemblerAfterApplicationCreated {
    private final AppInfo app;

    public AssemblerAfterApplicationCreated(final AppInfo appInfo) {
        app = appInfo;
    }

    public AppInfo getApp() {
        return app;
    }
}
