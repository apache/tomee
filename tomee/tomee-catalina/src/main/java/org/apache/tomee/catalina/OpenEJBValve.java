/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.catalina;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.OpenEjbVersion;

import jakarta.servlet.ServletException;
import java.io.IOException;

public class OpenEJBValve extends ValveBase {
    protected TomcatSecurityService securityService;
    protected static final String info = OpenEJBValve.class.getName() + "/" + OpenEjbVersion.get().getVersion();

    public OpenEJBValve() {
        super(true);
        securityService = getSecurityService();
    }

    @Override
    public void invoke(final Request request, final Response response) throws IOException, ServletException {
        final OpenEJBSecurityListener listener = new OpenEJBSecurityListener(securityService, request);

        if (!request.isAsync() || request.getAsyncContextInternal() == null) {
            listener.enter();
            try {
                getNext().invoke(request, response);
            } finally {
                listener.exit();
            }
        } else {
            request.getAsyncContextInternal().addListener(new OpenEJBSecurityListener(securityService, request));

            // finally continue the invocation
            getNext().invoke(request, response);
        }
    }

    private TomcatSecurityService getSecurityService() {
        final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
        if (securityService instanceof TomcatSecurityService) {
            return (TomcatSecurityService) securityService;
        }
        return null;
    }
}
