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
package org.apache.openejb.server.hessian;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.HttpListenerRegistry;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.server.httpd.OpenEJBHttpRegistry;
import org.apache.openejb.server.httpd.util.HttpUtil;

public class HessianRegistryImpl extends OpenEJBHttpRegistry implements HessianRegistry {
    @Override
    public String deploy(final ClassLoader loader, final HessianServer listener, final String host,
                         final String app, final String authMethod, final String transportGuarantee,
                         final String realmName, final String name) {
        final String path = generateEndpointName(app, name);
        addWrappedHttpListener(new HessianListener(listener), loader, path);
        return HttpUtil.selectSingleAddress(getResolvedAddresses(path));
    }

    @Override
    public void undeploy(String host, String app, String name) {
        SystemInstance.get().getComponent(HttpListenerRegistry.class).removeHttpListener(generateEndpointName(app, name));
    }

    private static String generateEndpointName(final String app, final String name) {
        return "/" + app + HESSIAN + name;
    }

    protected static class HessianListener implements HttpListener {
        private final HessianServer delegate;

        protected HessianListener(final HessianServer server) {
            this.delegate = server;
        }

        @Override
        public void onMessage(final HttpRequest request, final HttpResponse response) throws Exception {
            try {
                delegate.invoke(request.getInputStream(), response.getOutputStream());
            } catch (final Throwable throwable) {
                if (Exception.class.isInstance(throwable)) {
                    throw Exception.class.cast(throwable);
                }
                throw new OpenEJBRuntimeException(throwable.getMessage(), throwable);
            }
        }
    }
}
