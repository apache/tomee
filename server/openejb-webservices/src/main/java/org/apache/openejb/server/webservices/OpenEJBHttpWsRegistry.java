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
    @Override
    public List<String> setWsContainer(final HttpListener httpListener,
                                       final ClassLoader classLoader,
                                       final String context, final String virtualHost, final ServletInfo servletInfo,
                                       final String realmName, final String transportGuarantee, final String authMethod,
                                       final String moduleId) throws Exception {

        final String path = servletInfo.mappings.iterator().next();
        return addWsContainer(httpListener, classLoader, context, virtualHost, path, realmName, transportGuarantee, authMethod, moduleId);
    }

    @Override
    public void clearWsContainer(final String context, final String virtualHost, final ServletInfo servletInfo, final String moduleId) {
        final String path = servletInfo.mappings.iterator().next();
        removeWsContainer(path, moduleId);
    }

    @Override
    public List<String> addWsContainer(final HttpListener inputListener,
                                       final ClassLoader classLoader,
                                       final String context,
                                       final String virtualHost,
                                       final String path,
                                       final String realmName,
                                       final String transportGuarantee, // ignored
                                       final String authMethod,
                                       final String moduleId) throws Exception {

        if (path == null) throw new NullPointerException("contextRoot is null");

        HttpListener httpListener = inputListener;
        if (httpListener == null) throw new NullPointerException("httpListener is null");

        if ("BASIC".equals(authMethod)) {
            httpListener = new BasicAuthHttpListenerWrapper(httpListener, realmName);
        }

        final StringBuilder deployedPath = new StringBuilder("");
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

    @Override
    public void removeWsContainer(final String path, final String moduleId) {
        registry.removeHttpListener(path);
    }
}
