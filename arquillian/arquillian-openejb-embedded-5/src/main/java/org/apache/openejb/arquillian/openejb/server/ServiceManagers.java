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
package org.apache.openejb.arquillian.openejb.server;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.SimpleServiceManager;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;

// only here to not trigger any loadClass of openejb-server if not mandatory
public final class ServiceManagers {
    private ServiceManagers() {
        // no-op
    }

    public static ProtocolMetaData protocolMetaData(final AppInfo info) {
        final org.apache.openejb.server.ServiceManager smp = org.apache.openejb.server.ServiceManager.get();
        if (smp != null && SimpleServiceManager.class.isInstance(smp)) {
            final ServerService[] daemons = SimpleServiceManager.class.cast(smp).getDaemons();
            for (final ServerService ss : daemons) {
                if ("httpejbd".equals(ss.getName())) {
                    if (info.webApps.size() == 1) {
                        return newHttpProtocolMetaData(ss, info.webApps.iterator().next().contextRoot);
                    }
                    return newHttpProtocolMetaData(ss, info.appId);
                }
            }
        }
        return null;
    }

    private static ProtocolMetaData newHttpProtocolMetaData(final ServerService ss, final String contextRoot) {
        final HTTPContext httpContext = new HTTPContext(ss.getIP(), ss.getPort());
        httpContext.add(new Servlet("ArquillianServletRunner", contextRoot));
        return new ProtocolMetaData().addContext(httpContext);
    }
}
