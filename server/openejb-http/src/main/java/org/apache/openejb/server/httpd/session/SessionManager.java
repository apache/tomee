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
package org.apache.openejb.server.httpd.session;

import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.BeginWebBeansListener;
import org.apache.openejb.server.httpd.HttpRequestImpl;
import org.apache.openejb.server.httpd.HttpSession;
import org.apache.openejb.util.DaemonThreadFactory;
import org.apache.openejb.util.Duration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpSessionEvent;

public class SessionManager {
    public static final String EJBSESSIONID = "EJBSESSIONID";
    public static final String JSESSIONID = "JSESSIONID";

    private final ConcurrentMap<String, SessionWrapper> sessions = new ConcurrentHashMap<>();

    private static volatile ScheduledExecutorService es;

    public void destroy(final WebContext app) {
        final Iterator<SessionWrapper> iterator = sessions.values().iterator();
        while (iterator.hasNext()) {
            final SessionWrapper next = iterator.next();
            if (next.app == app) {
                next.session.invalidate();
                iterator.remove();
            }
        }
    }

    public void destroy() {
        if (es == null) {
            return;
        }
        es.shutdownNow();
        for (final SessionWrapper rs : sessions.values()) {
            rs.session.invalidate();
        }
        sessions.clear();
    }

    public void initEviction() {
        if (!"true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.http.eviction", "true"))) {
            return;
        }
        final Duration duration = new Duration(SystemInstance.get().getProperty("openejb.http.eviction.duration", "1 minute"));
        es = Executors.newScheduledThreadPool(1, new DaemonThreadFactory(HttpRequestImpl.class));
        es.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                for (final SessionWrapper data : new ArrayList<>(sessions.values())) {
                    final HttpSession session = data.session;
                    if (session.getMaxInactiveInterval() > 0
                            && session.getLastAccessedTime() + TimeUnit.SECONDS.toMillis(session.getMaxInactiveInterval()) < System.currentTimeMillis()) {
                        sessions.remove(session.getId());
                        session.invalidate();

                        if (data.listener != null) {
                            data.listener.sessionDestroyed(new HttpSessionEvent(session));
                        }
                    }
                }
            }
        }, duration.getTime(), duration.getTime(), duration.getUnit());
    }

    public SessionWrapper findSession(final String id) {
        return sessions.get(id);
    }

    public void removeSession(final String sessionId) {
        sessions.remove(sessionId);
    }

    public Collection<String> findSessionIds() {
        return sessions.keySet();
    }

    public int size() {
        return sessions.size();
    }

    public SessionWrapper newSession(final BeginWebBeansListener listener, final HttpSession session, final WebContext app) {
        final SessionWrapper existing = sessions.putIfAbsent(session.getId(), new SessionWrapper(listener, session, app));
        if (existing == null && es == null) {
            synchronized (this) {
                if (es == null) {
                    initEviction();
                }
            }
        }
        return existing;
    }

    public static class SessionWrapper extends HttpSessionEvent {
        public final BeginWebBeansListener listener;
        public final HttpSession session;
        public final WebContext app;

        public SessionWrapper(final BeginWebBeansListener listener, final HttpSession session, final WebContext app) {
            super(session);
            this.listener = listener;
            this.session = session;
            this.app = app;
        }
    }
}
