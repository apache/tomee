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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class HttpListenerRegistry implements HttpListener {
    private final Map<String, HttpListener> registry = new LinkedHashMap<String, HttpListener>();
    private final Map<String, Collection<HttpListener>> filterRegistry = new LinkedHashMap<String, Collection<HttpListener>>();
    private final ThreadLocal<FilterListener> currentFilterListener = new ThreadLocal<FilterListener>();

    public HttpListenerRegistry() {
    }

    public void onMessage(HttpRequest request, HttpResponse response) throws Exception {
        final String path = request.getURI().getPath();
        final FilterListener currentFL = currentFilterListener.get();

        // first look filters
        Map<String, Collection<HttpListener>> filters;
        synchronized (filterRegistry) {
            filters = new HashMap<String, Collection<HttpListener>>(filterRegistry);
        }

        try {
            boolean lastWasCurrent = false;
            for (Map.Entry<String, Collection<HttpListener>> entry : filters.entrySet()) {
                String pattern = entry.getKey();
                for (HttpListener listener : entry.getValue()) {
                    if ((lastWasCurrent || currentFL == null) && path.matches(pattern)) {
                        listener.onMessage(request, response);
                        return;
                    }
                    lastWasCurrent = listener == currentFL;
                }
            }


            // then others
            Map<String, HttpListener> listeners;
            synchronized (registry) {
                listeners = new HashMap<String, HttpListener>(registry);
            }

            for (Map.Entry<String, HttpListener> entry : listeners.entrySet()) {
                String pattern = entry.getKey();
                if (path.matches(pattern)) {
                    entry.getValue().onMessage(request, response);
                    break;
                }
            }
        } finally {
            if (currentFL == null) {
                currentFilterListener.remove();
            }
        }
    }

    public void addHttpListener(HttpListener listener, String regex) {
        synchronized (registry) {
            registry.put(regex, listener);
        }
    }

    public HttpListener removeHttpListener(String regex) {
        HttpListener listener;
        synchronized (registry) {
            listener = registry.remove(regex);
        }
        return listener;
    }

    public void addHttpFilter(HttpListener listener, String regex) {
        synchronized (filterRegistry) {
            if (!filterRegistry.containsKey(regex)) {
                filterRegistry.put(regex, new ArrayList<HttpListener>());
            }
            filterRegistry.get(regex).add(listener);
        }
    }

    public Collection<HttpListener> removeHttpFilter(String regex) {
        synchronized (filterRegistry) {
            return filterRegistry.remove(regex);
        }
    }

    public void setOrigin(final FilterListener origin) {
        if (origin == null) {
            currentFilterListener.remove();
        } else {
            currentFilterListener.set(origin);
        }
    }
}
