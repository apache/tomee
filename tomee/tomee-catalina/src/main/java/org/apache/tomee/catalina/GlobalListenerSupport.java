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
package org.apache.tomee.catalina;

import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Service;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomee.catalina.cluster.TomEEClusterListener;
import org.apache.tomee.catalina.remote.TomEERemoteWebapp;
import org.apache.tomee.loader.TomcatHelper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Observers events from Tomcat to configure
 * web applications etc.
 *
 * @version $Rev$ $Date$
 */
public class GlobalListenerSupport implements PropertyChangeListener, LifecycleListener {
    private static final boolean REMOTE_SUPPORT = SystemInstance.get().getOptions().get("tomee.remote.support", false);

    /**
     * The LifecycleEvent type for the "component init" event.
     * Tomcat 6.0.x only
     * Removed in Tomcat 7
     */
    public static final String INIT_EVENT = "init";

    /**
     * The LifecycleEvent type for the "component destroy" event.
     * Tomcat 6.0.x only
     * Removed in Tomcat 7
     */
    public static final String DESTROY_EVENT = "destroy";

    /**
     * Tomcat server instance
     */
    private final StandardServer standardServer;

    /**
     * Listener for context, host, server operations
     */
    private final ContextListener contextListener;

    /**
     * Creates a new instance.
     *
     * @param standardServer  tomcat server instance
     * @param contextListener context listener instance
     */
    public GlobalListenerSupport(final StandardServer standardServer, final ContextListener contextListener) {
        if (standardServer == null) {
            throw new NullPointerException("standardServer is null");
        }
        if (contextListener == null) {
            throw new NullPointerException("contextListener is null");
        }
        this.standardServer = standardServer;
        this.contextListener = contextListener; // this.contextListener is now an instance of TomcatWebAppBuilder
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void lifecycleEvent(final LifecycleEvent event) {
        final Object source = event.getSource();
        if (source instanceof StandardContext) {
            final StandardContext standardContext = (StandardContext) source;
            if (standardContext instanceof IgnoredStandardContext) {
                return;
            }

            final String type = event.getType();

            switch (type) { // better than if cause it prevent duplicates
                case INIT_EVENT:
                case Lifecycle.BEFORE_INIT_EVENT:
                    contextListener.init(standardContext);
                    break;
                case Lifecycle.BEFORE_START_EVENT:
                    contextListener.beforeStart(standardContext);
                    break;
                case Lifecycle.START_EVENT:
                    standardContext.addParameter("openejb.start.late", "true");
                    contextListener.start(standardContext);
                    break;
                case Lifecycle.AFTER_START_EVENT:
                    contextListener.afterStart(standardContext);
                    standardContext.removeParameter("openejb.start.late");
                    break;
                case Lifecycle.BEFORE_STOP_EVENT:
                    contextListener.beforeStop(standardContext);
                    break;
                case Lifecycle.STOP_EVENT:
                    contextListener.stop(standardContext);
                    break;
                case Lifecycle.AFTER_STOP_EVENT:
                    contextListener.afterStop(standardContext);
                    break;
                case DESTROY_EVENT:
                case Lifecycle.AFTER_DESTROY_EVENT:
                    contextListener.destroy(standardContext);
                    break;
                case Lifecycle.CONFIGURE_START_EVENT:
                    contextListener.configureStart(event, standardContext);
                    break;
                default:
            }
        } else if (StandardHost.class.isInstance(source)) {
            final StandardHost standardHost = (StandardHost) source;
            final String type = event.getType();
            if (Lifecycle.PERIODIC_EVENT.equals(type)) {
                contextListener.checkHost(standardHost);
            } else if (Lifecycle.AFTER_START_EVENT.equals(type) && REMOTE_SUPPORT) {
                final TomEERemoteWebapp child = new TomEERemoteWebapp();
                if (!hasChild(standardHost, child.getName())) {
                    standardHost.addChild(child);
                } // else old tomee webapp surely
            }
        } else if (StandardServer.class.isInstance(source)) {
            final StandardServer standardServer = (StandardServer) source;
            final String type = event.getType();

            if (Lifecycle.START_EVENT.equals(type)) {
                contextListener.start(standardServer);
            }

            if (Lifecycle.BEFORE_STOP_EVENT.equals(type)) {
                TomcatHelper.setStopping(true);
                final TomEEClusterListener tomEEClusterListener = SystemInstance.get().getComponent(TomEEClusterListener.class);
                if (tomEEClusterListener != null) {
                    TomEEClusterListener.stop();
                }
            }

            if (Lifecycle.AFTER_STOP_EVENT.equals(type)) {
                contextListener.afterStop(standardServer);
            }
        }

        // Notify
        SystemInstance.get().fireEvent(event); // here this way we are sure we get it even in embedded mode. TODO: we miss then few boot events, is it an issue.
    }

    private static boolean hasChild(final StandardHost host, final String name) {
        for (final Container child : host.findChildren()) {
            // the TomEERemoteWebapp path = "/" + name
            if (name.equals(child.getName())
                || (StandardContext.class.isInstance(child) && ("/" + name).equals(StandardContext.class.cast(child).getPath()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Starts operation.
     */
    public void start() {
        // hook the hosts so we get notified before contexts are started
        standardServer.addPropertyChangeListener(this);
        standardServer.addLifecycleListener(this);
        for (final Service service : standardServer.findServices()) {
            serviceAdded(service);
        }
    }

    /**
     * Stops operation.
     */
    public void stop() {
        standardServer.removePropertyChangeListener(this);
    }

    /**
     * Service is added.
     *
     * @param service tomcat service
     */
    private void serviceAdded(final Service service) {
        final Container container = service.getContainer();
        if (container instanceof StandardEngine) {
            final StandardEngine engine = (StandardEngine) container;
            engineAdded(engine);
        }
    }

    /**
     * Service removed.
     *
     * @param service tomcat service
     */
    private void serviceRemoved(final Service service) {
        final Container container = service.getContainer();
        if (container instanceof StandardEngine) {
            final StandardEngine engine = (StandardEngine) container;
            engineRemoved(engine);
        }
    }

    /**
     * Engine is added.
     *
     * @param engine tomcat engine
     */
    private void engineAdded(final StandardEngine engine) {
        addContextListener(engine);
        for (final Container child : engine.findChildren()) {
            if (child instanceof StandardHost) {
                final StandardHost host = (StandardHost) child;
                hostAdded(host);
            }
        }
    }

    /**
     * Engine is removed.
     *
     * @param engine tomcat engine
     */
    private void engineRemoved(final StandardEngine engine) {
        for (final Container child : engine.findChildren()) {
            if (child instanceof StandardHost) {
                final StandardHost host = (StandardHost) child;
                hostRemoved(host);
            }
        }
    }

    /**
     * Host is added.
     *
     * @param host tomcat host.
     */
    private void hostAdded(final StandardHost host) {
        addContextListener(host);
        host.addLifecycleListener(this);
        for (final Container child : host.findChildren()) {
            if (child instanceof StandardContext) {
                final StandardContext context = (StandardContext) child;
                contextAdded(context);
            }
        }
    }

    /**
     * Host is removed.
     *
     * @param host tomcat host
     */
    private void hostRemoved(final StandardHost host) {
        for (final Container child : host.findChildren()) {
            if (child instanceof StandardContext) {
                final StandardContext context = (StandardContext) child;
                contextRemoved(context);
            }
        }
    }

    /**
     * New context is added.
     *
     * @param context tomcat context
     */
    private void contextAdded(final StandardContext context) {
        // put this class as the first listener so we can process the application before any classes are loaded
        forceFirstLifecycleListener(context);
    }

    /**
     * Update context lifecycle listeners.
     *
     * @param context tomcat context.
     */
    private void forceFirstLifecycleListener(final StandardContext context) {
        final LifecycleListener[] listeners = context.findLifecycleListeners();

        // if we are already first return
        if (listeners.length > 0 && listeners[0] == this) {
            return;
        }

        // remove all of the current listeners
        for (final LifecycleListener listener : listeners) {
            context.removeLifecycleListener(listener);
        }

        // add this class (as first)
        context.addLifecycleListener(this);

        // add back all listeners
        for (final LifecycleListener listener : listeners) {
            if (listener != this) {
                context.addLifecycleListener(listener);
            }
        }
    }

    /**
     * Context is removed.
     *
     * @param context tomcat context
     */
    @SuppressWarnings({"UnusedDeclaration", "PMD.UnusedFormalParameter"})
    private void contextRemoved(final StandardContext context) {
        // TODO what to do?
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if ("service".equals(event.getPropertyName())) {
            final Object oldValue = event.getOldValue();
            final Object newValue = event.getNewValue();
            if (oldValue == null && newValue instanceof Service) {
                serviceAdded((Service) newValue);
            }
            if (oldValue instanceof Service && newValue == null) {
                serviceRemoved((Service) oldValue);
            }
        }
        if ("children".equals(event.getPropertyName())) {
            final Object source = event.getSource();
            final Object oldValue = event.getOldValue();
            final Object newValue = event.getNewValue();
            if (source instanceof StandardEngine) {
                if (oldValue == null && newValue instanceof StandardHost) {
                    hostAdded((StandardHost) newValue);
                }
                if (oldValue instanceof StandardHost && newValue == null) {
                    hostRemoved((StandardHost) oldValue);
                }
            }
            if (source instanceof StandardHost) {
                if (oldValue == null && newValue instanceof StandardContext) {
                    contextAdded((StandardContext) newValue);
                }
                if (oldValue instanceof StandardContext && newValue == null) {
                    contextRemoved((StandardContext) oldValue);
                }
            }
        }
    }

    /**
     * Setting monitoreable child field.
     *
     * @param containerBase host or engine
     */
    @SuppressWarnings("unchecked")
    private void addContextListener(final ContainerBase containerBase) {
        boolean accessible = false;
        Field field = null;
        try {
            field = ContainerBase.class.getDeclaredField("children");
            accessible = field.isAccessible();
            field.setAccessible(true);
            Map<Object, Object> children = (Map<Object, Object>) field.get(containerBase);
            if (children instanceof GlobalListenerSupport.MoniterableHashMap) {
                return;
            }
            children = new GlobalListenerSupport.MoniterableHashMap(children, containerBase, "children", this);
            field.set(containerBase, children);
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            if (field != null) {
                if (!accessible) {
                    field.setAccessible(false);
                }
            }
        }

    }

    //Hashmap for monitoring children of engine and host, linked because:
    // 1) deterministic, 2) avoid to handle the prop in application.xml
    public static class MoniterableHashMap extends LinkedHashMap<Object, Object> {

        private final Object source;
        private final String propertyName;
        private final PropertyChangeListener listener;

        public MoniterableHashMap(final Map<Object, Object> m, final Object source, final String propertyName, final PropertyChangeListener listener) {
            super(m);

            this.source = source;
            this.propertyName = propertyName;
            this.listener = listener;
        }

        @Override
        public Object put(final Object key, final Object value) {
            final Object oldValue = super.put(key, value);
            final PropertyChangeEvent event = new PropertyChangeEvent(source, propertyName, null, value);
            listener.propertyChange(event);
            return oldValue;
        }

        @Override
        public Object remove(final Object key) {
            final Object value = super.remove(key);
            final PropertyChangeEvent event = new PropertyChangeEvent(source, propertyName, value, null);
            listener.propertyChange(event);
            return value;
        }
    }
}
