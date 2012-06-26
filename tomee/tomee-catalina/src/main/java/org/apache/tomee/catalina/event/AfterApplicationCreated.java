package org.apache.tomee.catalina.event;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;

public class AfterApplicationCreated {
    private final AppInfo app;
    private final WebAppInfo web;

    public AfterApplicationCreated(final AppInfo appInfo, final WebAppInfo webApp) {
        app = appInfo;
        web = webApp;
    }

    public AppInfo getApp() {
        return app;
    }

    public WebAppInfo getWeb() {
        return web;
    }
}
