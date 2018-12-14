/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package org.apache.openejb.server.rest;

import org.apache.openejb.server.httpd.BasicAuthHttpListenerWrapper;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.OpenEJBHttpRegistry;
import org.apache.openejb.server.httpd.util.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public class RsRegistryImpl extends OpenEJBHttpRegistry implements RsRegistry {
    private Map<String, String> addresses = new HashMap<String, String>();

    @Override
    public AddressInfo createRsHttpListener(final String appId, final String webContext, 
            final HttpListener listener, final ClassLoader classLoader, final String path, 
            final String virtualHost, final String auth, final String realm) {
        final String address = HttpUtil.selectSingleAddress(getResolvedAddresses(path));

        if ("BASIC".equals(auth)) { // important to wrap with basic wrapper before classloader wrapping
            addWrappedHttpListener(new BasicAuthHttpListenerWrapper(listener, realm), classLoader, path);
        } else {
            addWrappedHttpListener(listener, classLoader, path);
        }

        addresses.put(address, path);
        return new AddressInfo(address, address);
    }

    @Override
    public HttpListener removeListener(final String appId, final String context) {
        String regex = addresses.get(context);
        if (regex != null) {
            HttpListener listener = registry.removeHttpListener(regex);
            if (listener instanceof ClassLoaderHttpListener) {
                return ((ClassLoaderHttpListener) listener).getDelegate();
            }
        }
        return null;
    }
}
