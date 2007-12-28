/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.tomcat;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.OpenEjbVersion;

import javax.servlet.ServletException;
import java.io.IOException;

public class OpenEJBValve extends ValveBase {
    protected TomcatSecurityService securityService;

    public OpenEJBValve() {
        info = getClass().getName() + "/" + OpenEjbVersion.get().getVersion();
        securityService = getSecurityService();
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {
        Object oldState = null;
        if (securityService != null && request.getWrapper() != null) {
            oldState = securityService.enterWebApp(request.getWrapper().getRealm(), request.getUserPrincipal(), request.getWrapper().getRunAs());
        }

        try {
            getNext().invoke(request, response);
        } finally {
            if (securityService != null) {
                securityService.exitWebApp(oldState);
            }
        }
    }

    private TomcatSecurityService getSecurityService() {
        SecurityService securityService =  SystemInstance.get().getComponent(SecurityService.class);
        if (securityService instanceof TomcatSecurityService) {
            return (TomcatSecurityService) securityService;
        }
        return null;
    }
}
