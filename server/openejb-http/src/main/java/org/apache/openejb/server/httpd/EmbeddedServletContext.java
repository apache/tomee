/**
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
package org.apache.openejb.server.httpd;

import org.apache.openejb.util.OpenEjbVersion;
import org.apache.webbeans.web.lifecycle.test.MockServletContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class EmbeddedServletContext extends MockServletContext {
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final Map<String, String> initParameters = new ConcurrentHashMap<>();

    private Collection<ResourceProvider> resourceProviders = new ArrayList<>();

    public EmbeddedServletContext() {
        for (final ResourceProvider rp : ServiceLoader.load(ResourceProvider.class, EmbeddedServletContext.class.getClassLoader())) {
            resourceProviders.add(rp);
        }
    }

    @Override
    public String getInitParameter(final String name) {
        return initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParameters.keySet());
    }

    @Override
    public boolean setInitParameter(final String name, final String value) {
        initParameters.put(name, value);
        return true;
    }

    @Override
    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    @Override
    public URL getResource(final String path) throws MalformedURLException {
        if (resourceProviders.isEmpty()) {
            return super.getResource(path);
        }
        for (final ResourceProvider provider : resourceProviders) {
            final URL resource = provider.getResource(path);
            if (resource != null) {
                return resource;
            }
        }
        return super.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(final String path) {
        if (resourceProviders.isEmpty()) {
            return super.getResourceAsStream(path);
        }
        for (final ResourceProvider provider : resourceProviders) {
            final URL resource = provider.getResource(path);
            if (resource != null) {
                try {
                    return resource.openStream();
                } catch (final IOException e) {
                    // no-op
                }
            }
        }
        return super.getResourceAsStream(path);
    }

    @Override
    public int getMajorVersion() {
        return 3;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 3;
    }

    @Override
    public int getMinorVersion() {
        return 1;
    }

    @Override
    public String getVirtualServerName() {
        return "openejb";
    }

    @Override
    public void setAttribute(final String name, final Object object) {
        attributes.put(name, object);
    }

    @Override
    public Object getAttribute(final String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String getServerInfo() {
        return "OpenEJB/" + OpenEjbVersion.get().getVersion();
    }

    public static interface ResourceProvider {
        URL getResource(String path);
    }
}
