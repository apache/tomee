package org.apache.openejb.assembler.classic.event;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.observer.Event;

@Event
public class AssemblerBeforeApplicationDestroyed {
    private final AppInfo app;

    public AssemblerBeforeApplicationDestroyed(final AppInfo appInfo) {
        app = appInfo;
    }

    public AppInfo getApp() {
        return app;
    }

    @Override
    public String toString() {
        return "AssemblerBeforeApplicationDestroyed{" +
                "app=" + app.appId +
            '}';
    }
}
