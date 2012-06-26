package org.apache.openejb.assembler.classic.event;

import org.apache.openejb.assembler.classic.AppInfo;

public class AssemblerBeforeApplicationDestroyed {
    private final AppInfo app;

    public AssemblerBeforeApplicationDestroyed(final AppInfo appInfo) {
        app = appInfo;
    }

    public AppInfo getApp() {
        return app;
    }
}
