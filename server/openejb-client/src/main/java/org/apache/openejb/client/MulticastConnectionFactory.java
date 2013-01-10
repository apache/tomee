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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class MulticastConnectionFactory implements ConnectionFactory {

    private final Set<String> defaultSchemes = new HashSet<String>(Arrays.asList("ejbd", "ejbds", "http", "https"));

    protected Set<String> getDefaultSchemes() {
        return defaultSchemes;
    }

    @Override
    public Connection getConnection(final URI uri) throws IOException {
        final Map<String, String> params;
        try {
            params = URIs.parseParamters(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid multicast uri " + uri.toString(), e);
        }

        final Set<String> schemes = getSet(params, "schemes", defaultSchemes);
        final String group = getString(params, "group", "default");
        final long timeout = getLong(params, "timeout", 1500);

        final MulticastSearch search = new MulticastSearch(uri.getHost(), uri.getPort());

        URI serviceURI = null;
        try {
            serviceURI = search.search(new Filter(group, schemes), timeout, TimeUnit.MILLISECONDS);
        } finally {
            search.close();
        }

        if (serviceURI == null) {
            throw new IllegalArgumentException("Unable to find a public ejb server via the multicast URI: " + uri);
        }

        try {
            serviceURI = unwrap(serviceURI); // cut group:
            serviceURI = unwrap(serviceURI); // cut ejb:
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid ejb service uri " + serviceURI.toString(), e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ejb service uri " + serviceURI.toString(), e);
        }

        return ConnectionManager.getConnection(serviceURI);
    }

    public static String getString(final Map<String, String> params, final String param, final String defaultValue) {
        String value = params.get(param);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public static long getLong(final Map<String, String> params, final String param, final long defaultValue) {
        final String value = params.get(param);
        if (value == null) {
            return defaultValue;
        }
        return new Long(value);
    }

    public static Set<String> getSet(final Map<String, String> params, final String param, final Set<String> defaultSet) {
        Set<String> set = new LinkedHashSet<String>();
        if (params.containsKey(param)) {
            final String value = params.get(param);
            if (value != null) {
                final String[] strings = value.split(",");
                Collections.addAll(set, strings);
            }
        } else {
            set = defaultSet;
        }
        return set;
    }

    public static class URIs {

        public static Map<String, String> parseQuery(final String uri) throws URISyntaxException {
            try {
                final Map<String, String> rc = new LinkedHashMap<String, String>();
                if (uri != null) {
                    final String[] parameters = uri.split("&");
                    for (final String parameter : parameters) {
                        final int p = parameter.indexOf("=");
                        if (p >= 0) {
                            final String name = URLDecoder.decode(parameter.substring(0, p), "UTF-8");
                            final String value = URLDecoder.decode(parameter.substring(p + 1), "UTF-8");
                            rc.put(name, value);
                        } else {
                            rc.put(parameter, null);
                        }
                    }
                }
                return rc;
            } catch (UnsupportedEncodingException e) {
                throw (URISyntaxException) new URISyntaxException(e.toString(), "Invalid encoding").initCause(e);
            }
        }

        public static Map<String, String> parseParamters(final URI uri) throws URISyntaxException {
            return uri.getQuery() == null ? new HashMap<String, String>(0) : parseQuery(stripPrefix(uri.getQuery(), "?"));
        }

        public static String stripPrefix(final String value, final String prefix) {
            if (value.startsWith(prefix))
                return value.substring(prefix.length());
            return value;
        }
    }

    protected static URI unwrap(final URI uri) throws URISyntaxException {
        return new URI(uri.getSchemeSpecificPart());
    }

    protected static class Filter implements MulticastSearch.Filter {

        private final Set<String> schemes;
        private final String group;

        public Filter(final String group, final Set<String> schemes) {
            this.group = group;
            this.schemes = schemes;
        }

        @Override
        public boolean accept(URI service) {
            try {
                if (!group.equals(service.getScheme()))
                    return false;
                service = unwrap(service);

                if (!"ejb".equals(service.getScheme()))
                    return false;
                service = unwrap(service);

                if (schemes.contains(service.getScheme()))
                    return true;
            } catch (URISyntaxException e) {
                // not the uri we're looking for.
            }
            return false;
        }

    }
}
