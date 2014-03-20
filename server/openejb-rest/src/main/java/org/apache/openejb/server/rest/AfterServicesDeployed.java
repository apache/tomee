/**
 * Tomitribe Confidential
 *
 * Copyright(c) Tomitribe Corporation. 2014
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 *
 * Author: agumbrecht@tomitribe.com
 * Date: 20.03.14
 * Time: 16:27
 */
package org.apache.openejb.server.rest;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.observer.Event;

@Event
public class AfterServicesDeployed {
    private final AppInfo app;
    private final String s;

    public AfterServicesDeployed(final AppInfo appInfo) {
        app = appInfo;
        this.s = "AfterServicesDeployed{appId=" + app.appId + "}";
    }

    public AppInfo getApp() {
        return app;
    }

    @Override
    public String toString() {
        return s;
    }
}
