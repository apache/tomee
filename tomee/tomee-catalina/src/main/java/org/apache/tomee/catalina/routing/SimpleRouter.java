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

package org.apache.tomee.catalina.routing;

import org.apache.openejb.api.jmx.ManagedAttribute;
import org.apache.openejb.api.jmx.ManagedOperation;
import org.apache.openejb.monitoring.DynamicMBeanWrapper;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import jakarta.servlet.ServletException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleRouter {

    private static Logger logger = Logger.getInstance(LogCategory.OPENEJB, SimpleRouter.class);

    private static final Pattern PATTERN = Pattern.compile("(.*)->(.*)");

    private String prefix = "";
    private ObjectName objectName;
    private Route[] routes = new Route[0];
    private final Map<String, Route> cache = new ConcurrentHashMap<String, Route>();

    public SimpleRouter readConfiguration(final URL url) {
        if (url == null) {
            return this;
        }

        BufferedReader reader = null;
        try {
            final InputStream is = new BufferedInputStream(url.openStream());
            reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    parseRoute(line);
                }
            }
        } catch (final IOException e) {
            throw new RouterException("can't read " + url.toExternalForm());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.warning(e.getMessage(), e);
                }
            }
        }
        return this;
    }

    private void parseRoute(final String line) {
        final Matcher matcher = PATTERN.matcher(line);
        if (matcher.matches()) {
            final String from = prefix(matcher.group(1).trim());
            final String to = prefix(matcher.group(2).trim());
            addRoute(new Route().from(from).to(to));
        }
    }

    public String route(final String uri) throws IOException, ServletException {
        if (uri == null) {
            return null;
        }

        final Route cachedRoute = cache.get(uri);
        if (cachedRoute != null) {
            cachedRoute.matches(uri);
            return cachedRoute.cleanDestination(prefix);
        }

        for (final Route route : routes) {
            if (route.matches(uri)) {
                if (route.getOrigin().equals(uri)) {
                    cache.put(uri, route);
                }
                return route.cleanDestination(prefix);
            }
        }

        return null;
    }

    public synchronized void addRoute(final Route route) {
        final Route[] newRoutes = new Route[routes.length + 1];
        System.arraycopy(routes, 0, newRoutes, 0, routes.length);
        newRoutes[routes.length] = route;
        routes = newRoutes;
    }

    public void cleanUp() {
        JMXOff();
        routes = null;
        cache.clear();
    }

    public void setPrefix(final String prefix) {
        if (prefix == null || prefix.isEmpty() || prefix.equals("/")) {
            this.prefix = "";
        } else {
            this.prefix = prefix;
        }
    }

    private String prefix(final String value) {
        if (prefix != null) {
            return prefix + value;
        }
        return value;
    }

    public void JMXOn(final String name) {
        final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("J2EEServer", "Router");
        jmxName.set("J2EEApplication", name);
        jmxName.set("Type", "SimpleRouter");

        objectName = jmxName.build();
        try {
            LocalMBeanServer.get().registerMBean(new DynamicMBeanWrapper(this), objectName);
        } catch (final Exception e) {
            objectName = null;
        }
    }

    public void JMXOff() {
        if (objectName != null) {
            try {
                LocalMBeanServer.get().unregisterMBean(objectName);
            } catch (final Exception e) {
                // no-op
            }
        }
    }

    @ManagedAttribute
    public TabularData getActiveRoutes() {
        if (routes.length == 0) {
            return null;
        }

        final OpenType<?>[] types = new OpenType<?>[routes.length];
        final String[] keys = new String[types.length];
        final String[] values = new String[types.length];

        for (int i = 0; i < types.length; i++) {
            types[i] = SimpleType.STRING;
            keys[i] = routes[i].getOrigin().substring(prefix.length());
            values[i] = routes[i].getRawDestination().substring(prefix.length());
        }

        try {
            final CompositeType ct = new CompositeType("routes", "routes", keys, keys, types);
            final TabularType type = new TabularType("router", "routes", ct, keys);
            final TabularDataSupport data = new TabularDataSupport(type);

            final CompositeData line = new CompositeDataSupport(ct, keys, values);
            data.put(line);
            return data;
        } catch (final OpenDataException e) {
            return null;
        }
    }

    @ManagedOperation
    public void addRoute(final String from, final String to) {
        addRoute(new Route().from(prefix(from)).to(prefix(to)));
    }

    @ManagedOperation
    public void removeRoute(final String from, final String to) {
        if (routes.length == 0) {
            return;
        }

        for (int i = 0; i < routes.length; i++) {
            if (routes[i].getOrigin().equals(from) && routes[i].getRawDestination().endsWith(to)) {
                final Route[] newRoutes = new Route[routes.length - 1];
                System.arraycopy(routes, 0, newRoutes, 0, i);
                System.arraycopy(routes, i + 1, newRoutes, i, routes.length - i - 1);
                routes = newRoutes;
            }
        }
    }

    public boolean hasPrefix() {
        return prefix != null && !prefix.isEmpty();
    }

    public String getPrefix() {
        return prefix;
    }
}
