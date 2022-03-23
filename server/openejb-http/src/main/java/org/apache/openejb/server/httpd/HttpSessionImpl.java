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
package org.apache.openejb.server.httpd;

import org.apache.openejb.client.ArrayEnumeration;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.session.SessionManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSessionContext;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

public class HttpSessionImpl implements HttpSession {
    private Collection<HttpSessionListener> listeners;
    private String sessionId = UUID.randomUUID().toString();
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private final long created = System.currentTimeMillis();
    private volatile long timeout;
    private volatile long lastAccessed = created;
    private volatile boolean valid = true;

    public HttpSessionImpl(final String contextPath, final long timeout) {
        this.timeout = timeout;
        if (contextPath == null) {
            return;
        }

        this.listeners = LightweightWebAppBuilderListenerExtractor.findByTypeForContext(contextPath, HttpSessionListener.class);
    }

    public void callListeners() {
        if (!this.listeners.isEmpty()) {
            final HttpSessionEvent event = new HttpSessionEvent(this);
            for (final HttpSessionListener o : this.listeners) {
                HttpSessionListener.class.cast(o).sessionCreated(event);
            }
        }
    }

    public HttpSessionImpl() {
        this(null, 30000);
    }

    public void newSessionId() {
        this.sessionId = UUID.randomUUID().toString();
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
        touch();
    }

    private void touch() {
        lastAccessed = System.currentTimeMillis();
    }

    @Override
    public void removeValue(final String s) {
        Iterator<String> it = attributes.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (attributes.get(key).equals(s)) {
                attributes.remove(key);
            }
        }
        touch();
    }

    @Override
    public void invalidate() {
        if (!valid) {
            return;
        }

        synchronized (this) {
            if (!valid) {
                return;
            }

            if (!listeners.isEmpty()) {
                final HttpSessionEvent event = new HttpSessionEvent(this);
                for (final HttpSessionListener o : listeners) {
                    try {
                        HttpSessionListener.class.cast(o).sessionDestroyed(event);
                    } catch (final Throwable th) {
                        // ignore, may be undeployed
                    }
                }
            }

            final SessionManager sessionManager = SystemInstance.get().getComponent(SessionManager.class);
            if (sessionManager != null) {
                sessionManager.removeSession(sessionId);
            }
            valid = false;
        }
    }

    @Override
    public boolean isNew() {
        return false;
    }

    @Override
    public Object getAttribute(String name) {
        touch();
        return attributes.get(name);
    }

    @Override
    public Object getValue(String s) {
        touch();
        return attributes.get(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        touch();
        return new ArrayEnumeration(new ArrayList(attributes.keySet()));
    }

    @Override
    public String[] getValueNames() {
        touch();
        return attributes.keySet().toArray(new String[attributes.size()]);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
        touch();
    }

    @Override
    public void putValue(String s, Object o) {
        setAttribute(s, o);
    }

    @Override
    public long getCreationTime() {
        return created;
    }

    @Override
    public String getId() {
        return sessionId;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessed;
    }

    @Override
    public ServletContext getServletContext() {
        touch();
        return SystemInstance.get().getComponent(ServletContext.class);
    }

    @Override
    public void setMaxInactiveInterval(int i) {
        touch();
        timeout = (long) i;
    }

    @Override
    public int getMaxInactiveInterval() {
        // touch(); // TODO: dont use it internally
        return (int) timeout;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        touch();
        final SessionManager component = SystemInstance.get().getComponent(SessionManager.class);
        return new HttpSessionContext() {
            @Override
            public jakarta.servlet.http.HttpSession getSession(final String sessionId) {
                final HttpSessionEvent event = component.findSession(sessionId);
                return event == null ? null : event.getSession();
            }

            @Override
            public Enumeration<String> getIds() {
                return Collections.enumeration(component.findSessionIds());
            }
        };
    }
}
