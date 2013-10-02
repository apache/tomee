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
package org.apache.openejb.server.webservices;

import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.server.httpd.BasicAuthHttpListenerWrapper;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.OpenEJBHttpRegistry;

import java.util.List;

public class OpenEJBHttpWsRegistry extends OpenEJBHttpRegistry implements WsRegistry {
    public List<String> setWsContainer(HttpListener httpListener,
                                        ClassLoader classLoader,
                                        String context, String virtualHost, ServletInfo servletInfo,
                                        String realmName, String transportGuarantee, String authMethod) throws Exception {

        final String path = servletInfo.mappings.iterator().next();
        return addWsContainer(httpListener, classLoader, context, virtualHost, path, realmName, transportGuarantee, authMethod);
    }

    public void clearWsContainer(String context, String virtualHost, ServletInfo servletInfo) {
        final String path = servletInfo.mappings.iterator().next();
        removeWsContainer(path);
    }

    public List<String> addWsContainer(HttpListener httpListener,
                                        ClassLoader classLoader,
                                        String context,
                                        String virtualHost, // ignored
                                        String path,
                                        String realmName, // ignored
                                        String transportGuarantee, // ignored
                                        String authMethod // ignored
                                        ) throws Exception {

        if (path == null) throw new NullPointerException("contextRoot is null");
        if (httpListener == null) throw new NullPointerException("httpListener is null");

        if ("BASIC".equals(authMethod)) {
            httpListener = new BasicAuthHttpListenerWrapper(httpListener, realmName);
        }

        StringBuilder deployedPath = new StringBuilder("");
        if (context != null) {
            if (!context.startsWith("/")) {
                deployedPath.append("/");
            }
            deployedPath.append(context);
            if (!context.endsWith("/")) {
                deployedPath.append("/");
            }
        } else {
            deployedPath.append("/");
        }
        if (path.startsWith("/") && path.length() > 1) {
            deployedPath.append(path.substring(1));
        } else if (path.length() > 1) {
            deployedPath.append(path);
        }
        addWrappedHttpListener(httpListener, classLoader, deployedPath.toString());

        // register wsdl locations for service-ref resolution
        return getResolvedAddresses(deployedPath.toString());
    }

    public void removeWsContainer(String path) {
        registry.removeHttpListener(path);
    }
}
