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

import org.apache.openejb.server.httpd.BasicAuthHttpListenerWrapper;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.OpenEJBHttpRegistry;

import java.util.List;

public class OpenEJBHttpWsRegistry extends OpenEJBHttpRegistry implements WsRegistry {
    public List<String> setWsContainer(String virtualHost, String contextRoot, String servletName, HttpListener wsContainer) throws Exception {
        throw new UnsupportedOperationException("OpenEJB http server does not support POJO webservices");
    }

    public void clearWsContainer(String virtualHost, String contextRoot, String servletName) {
    }

    public List<String> addWsContainer(String context, String path, HttpListener httpListener, String virtualHost, // ignored
            String realmName, // ignored
            String transportGuarantee, // ignored
            String authMethod, // ignored
            ClassLoader classLoader) throws Exception {

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
