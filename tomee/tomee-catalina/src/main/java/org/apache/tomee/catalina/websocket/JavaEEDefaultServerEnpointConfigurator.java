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
package org.apache.tomee.catalina.websocket;

import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.websocket.BackgroundProcess;
import org.apache.tomcat.websocket.BackgroundProcessManager;
import org.apache.tomcat.websocket.WsWebSocketContainer;
import org.apache.tomcat.websocket.pojo.PojoEndpointBase;
import org.apache.tomcat.websocket.server.Constants;
import org.apache.tomcat.websocket.server.DefaultServerEndpointConfigurator;
import org.apache.tomee.catalina.JavaeeInstanceManager;
import org.apache.tomee.catalina.TomcatWebAppBuilder;

import javax.servlet.ServletContext;
import javax.websocket.Endpoint;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class JavaEEDefaultServerEnpointConfigurator extends DefaultServerEndpointConfigurator {
    private static final String BG_PROCESSES_LIST = JavaEEDefaultServerEnpointConfigurator.class.getName() + ".bgProcesses";

    // for websocket eviction
    private static final Field END_POINT_SESSION_MAP_LOCK;
    private static final Field ENDPOINT_SESSION_MAP;
    private static final Method GET_POJO;
    static {
        try {
            ENDPOINT_SESSION_MAP = WsWebSocketContainer.class.getDeclaredField("endpointSessionMap");
            ENDPOINT_SESSION_MAP.setAccessible(true);
            END_POINT_SESSION_MAP_LOCK = WsWebSocketContainer.class.getDeclaredField("endPointSessionMapLock");
            END_POINT_SESSION_MAP_LOCK.setAccessible(true);

            GET_POJO = PojoEndpointBase.class.getDeclaredMethod("getPojo");
            GET_POJO.setAccessible(true);
        } catch (final Exception e) {
            throw new IllegalStateException("Toncat not compatible with tomee", e);
        }
    }

    private final Map<ClassLoader, InstanceManager> instanceManagers;

    public JavaEEDefaultServerEnpointConfigurator() {
        this.instanceManagers = SystemInstance.get().getComponent(TomcatWebAppBuilder.class).getInstanceManagers();
    }

    @Override
    public <T> T getEndpointInstance(final Class<T> clazz) throws InstantiationException {
        final ClassLoader classLoader = clazz.getClassLoader();
        InstanceManager instanceManager = instanceManagers.get(classLoader);

        if (instanceManager == null) {
            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            if (tccl != null) {
                instanceManager = instanceManagers.get(tccl);
            }
        }
        // if we have a single app fallback otherwise we don't have enough contextual information here
        if (instanceManager == null && instanceManagers.size() == 1) {
            instanceManager = instanceManagers.values().iterator().next();
        }
        if (instanceManager == null) {
            return super.getEndpointInstance(clazz);
        }

        try {
            final Object instance;
            if (JavaeeInstanceManager.class.isInstance(instanceManager)) {
                final JavaeeInstanceManager javaeeInstanceManager = JavaeeInstanceManager.class.cast(instanceManager);
                final WebContext.Instance cdiInstance = javaeeInstanceManager.newWeakableInstance(clazz);
                instance = cdiInstance.getValue();
                if (cdiInstance.getCreationalContext() != null) { // TODO: if we manage to have better listeners on tomcat we can use it rather than it
                    final ServletContext sc = javaeeInstanceManager.getServletContext();
                    if (sc != null) {
                        Collection<CdiCleanUpBackgroundProcess> processes;
                        synchronized (sc) {
                            processes = (Collection<CdiCleanUpBackgroundProcess>) sc.getAttribute(BG_PROCESSES_LIST);
                            if (processes == null) {
                                processes = new LinkedList<>();
                                sc.setAttribute(BG_PROCESSES_LIST, processes);
                            }
                        }

                        final WebSocketContainer wsc = WebSocketContainer.class.cast(sc.getAttribute(Constants.SERVER_CONTAINER_SERVLET_CONTEXT_ATTRIBUTE));
                        final Object lock = END_POINT_SESSION_MAP_LOCK.get(wsc);
                        if (wsc != null && WsWebSocketContainer.class.isInstance(wsc)) {
                            final CdiCleanUpBackgroundProcess process = new CdiCleanUpBackgroundProcess(wsc, cdiInstance, lock);
                            synchronized (processes) {
                                processes.add(process);
                            }
                            BackgroundProcessManager.getInstance().register(process);
                        }
                    }
                }
            } else {
                instance = instanceManager.newInstance(clazz);
            }
            return clazz.cast(instance);
        } catch (final Exception e) {
            if (InstantiationException.class.isInstance(e)) {
                throw InstantiationException.class.cast(e);
            }
            throw new InstantiationException(e.getMessage());
        }
    }

    public static void unregisterProcesses(final ServletContext sc) { // no sync needed at this point - no more "runtime"
        final Collection<CdiCleanUpBackgroundProcess> processes = (Collection<CdiCleanUpBackgroundProcess>) sc.getAttribute(BG_PROCESSES_LIST);
        if (processes == null) {
            return;
        }
        for (final CdiCleanUpBackgroundProcess p : processes) {
            try {
                p.stop();
            } catch (final RuntimeException e) {
                // no-op
            }
        }
    }

    private static class CdiCleanUpBackgroundProcess implements BackgroundProcess {
        private volatile int period = 1; // 1s by default
        private volatile int acceptRetries = 3; // in case there is latency between this call and registerSession()
        private volatile Set<Session> sessions;
        private volatile boolean stopped;

        private final WebSocketContainer container;
        private final Object lock;
        private final WebContext.Instance cdiInstance;

        private CdiCleanUpBackgroundProcess(final WebSocketContainer wsc, final WebContext.Instance cdiInstance, final Object lock) {
            this.container = wsc;
            this.cdiInstance = cdiInstance;
            this.lock = lock;
        }

        @Override
        public void backgroundProcess() {
            if (!hasSession() && --acceptRetries > 0) {
                stop();
            }
        }

        @Override
        public void setProcessPeriod(final int period) {
            this.period = period;
        }

        @Override
        public int getProcessPeriod() {
            return period;
        }

        private boolean hasSession() {
            try {
                if (sessions == null) { // needs to be lazy cause tomcat register sessions after
                    final Map<Endpoint, Set<Session>> sessionsByEndpoint = (Map<Endpoint, Set<Session>>) ENDPOINT_SESSION_MAP.get(container);
                    if (sessionsByEndpoint != null) { // find sessions
                        synchronized (lock) {
                            for (final Map.Entry<Endpoint, Set<Session>> e : sessionsByEndpoint.entrySet()) {
                                if (e.getKey() == cdiInstance.getValue()) {
                                    sessions = e.getValue();
                                    break;
                                }
                                if (PojoEndpointBase.class.isInstance(e.getKey())) {
                                    try {
                                        final Object pojo = GET_POJO.invoke(e.getKey());
                                        if (pojo == cdiInstance.getValue()) {
                                            sessions = e.getValue();
                                            break;
                                        }
                                    } catch (final InvocationTargetException e1) {
                                        // no-op
                                    }
                                }
                            }
                        }
                    }
                }

                synchronized (lock) {
                    return sessions != null && !sessions.isEmpty();
                }
            } catch (final IllegalAccessException e) {
                // no-op
            }
            return false;
        }

        public void stop() {
            if (stopped) {
                return;
            }
            stopped = true;
            try {
                cdiInstance.getCreationalContext().release();
            } finally {
                BackgroundProcessManager.getInstance().unregister(this);
            }
        }
    }
}
