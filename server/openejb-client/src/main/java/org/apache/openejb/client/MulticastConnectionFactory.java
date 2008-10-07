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
import java.util.Collections;
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

    private final Set<String> defaultSchemes = new HashSet<String>();

    {
        defaultSchemes.add("ejbd");
        defaultSchemes.add("ejbds");
        defaultSchemes.add("http");
        defaultSchemes.add("https");
    }

    String defaultGroup = "default";
    private int defaultTimeout = 5000;

    public Connection getConnection(URI uri) throws IOException {
        Map<String, String> params = null;
        try {
            params = URIs.parseParamters(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid multicast uri " + uri.toString(), e);
        }

        Set<String> schemes = getSet(params, "schemes", defaultSchemes);
        String group = getString(params, "group", defaultGroup);
        long timeout = getLong(params, "timeout", defaultTimeout);

        MulticastSearch search = new MulticastSearch(uri.getHost(), uri.getPort());

        URI serviceURI = search.search(new Filter(group, schemes), timeout, TimeUnit.MILLISECONDS);

        if (serviceURI == null) {
            throw new IllegalArgumentException("Unable to find an ejb server via the multicast URI: " + uri);
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

    public static String getString(Map<String, String> params, String param, String defaultValue) {
        String value = params.get(param);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public static long getLong(Map<String, String> params, String param, long defaultValue) {
        String value = params.get(param);
        if (value == null) {
            return defaultValue;
        }
        return new Long(value);
    }

    public static Set<String> getSet(Map<String, String> params, String param, Set<String> defaultSet) {
        Set<String> set = new LinkedHashSet<String>();
        if (params.containsKey(param)) {
            String value = params.get(param);
            if (value != null) {
                String[] strings = value.split(",");
                for (String s : strings) {
                    set.add(s);
                }
            }
        } else {
            set = defaultSet;
        }
        return set;
    }

    public static class URIs {
        public static Map<String, String> parseQuery(String uri) throws URISyntaxException {
            try {
                Map<String, String> rc = new LinkedHashMap<String, String>();
                if (uri != null) {
                    String[] parameters = uri.split("&");
                    for (int i = 0; i < parameters.length; i++) {
                        int p = parameters[i].indexOf("=");
                        if (p >= 0) {
                            String name = URLDecoder.decode(parameters[i].substring(0, p), "UTF-8");
                            String value = URLDecoder.decode(parameters[i].substring(p + 1), "UTF-8");
                            rc.put(name, value);
                        } else {
                            rc.put(parameters[i], null);
                        }
                    }
                }
                return rc;
            } catch (UnsupportedEncodingException e) {
                throw (URISyntaxException) new URISyntaxException(e.toString(), "Invalid encoding").initCause(e);
            }
        }

        public static Map<String, String> parseParamters(URI uri) throws URISyntaxException {
            return uri.getQuery() == null ? Collections.EMPTY_MAP : parseQuery(stripPrefix(uri.getQuery(), "?"));
        }

        public static String stripPrefix(String value, String prefix) {
            if (value.startsWith(prefix))
                return value.substring(prefix.length());
            return value;
        }
    }

    private static URI unwrap(URI uri) throws URISyntaxException {
        return new URI(uri.getSchemeSpecificPart());
    }

    private static class Filter implements MulticastSearch.Filter {
        private final Set<String> schemes;
        private final String group;

        public Filter(String group, Set<String> schemes) {
            this.group = group;
            this.schemes = schemes;
        }

        public boolean accept(URI service) {
            try {
                if (!group.equals(service.getScheme())) return false;
                service = unwrap(service);

                if (!"ejb".equals(service.getScheme())) return false;
                service = unwrap(service);

                if (schemes.contains(service.getScheme())) return true;
            } catch (URISyntaxException e) {
                // not the uri we're looking for.
            }
            return false;
        }

    }
}
