/**
 *
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
package org.apache.openejb.tomcat;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Service;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class GlobalListenerSupport implements PropertyChangeListener, LifecycleListener {
    private final StandardServer standardServer;
    private final ContextListener contextListener;

    public GlobalListenerSupport(StandardServer standardServer, ContextListener contextListener) {
        if (standardServer == null) throw new NullPointerException("standardServer is null");
        if (contextListener == null) throw new NullPointerException("contextListener is null");
        this.standardServer = standardServer;
        this.contextListener = contextListener;
    }

    public void lifecycleEvent(LifecycleEvent event) {
        Object source = event.getSource();
        if (source instanceof StandardContext) {
            StandardContext standardContext = (StandardContext) source;
            String type = event.getType();
            if (Lifecycle.INIT_EVENT.equals(type)) {
                contextListener.init(standardContext);
            } else if (Lifecycle.BEFORE_START_EVENT.equals(type)) {
                contextListener.beforeStart(standardContext);
            } else if (Lifecycle.START_EVENT.equals(type)) {
                contextListener.start(standardContext);
            } else if (Lifecycle.AFTER_START_EVENT.equals(type)) {
                contextListener.afterStart(standardContext);
            } else if (Lifecycle.BEFORE_STOP_EVENT.equals(type)) {
                contextListener.beforeStop(standardContext);
            } else if (Lifecycle.STOP_EVENT.equals(type)) {
                contextListener.stop(standardContext);
            } else if (Lifecycle.AFTER_STOP_EVENT.equals(type)) {
                contextListener.afterStop(standardContext);
            } else if (Lifecycle.DESTROY_EVENT.equals(type)) {
                contextListener.destroy(standardContext);
            }
        }
    }

    public void start() {
        // hook the hosts so we get notified before contexts are started
        standardServer.addPropertyChangeListener(this);
        for (Service service : standardServer.findServices()) {
            serviceAdded(service);
        }
    }

    public void stop() {
        standardServer.removePropertyChangeListener(this);
    }

    private void serviceAdded(Service service) {
        Container container = service.getContainer();
        if (container instanceof StandardEngine) {
            StandardEngine engine = (StandardEngine) container;
            engineAdded(engine);
        }
    }

    private void serviceRemoved(Service service) {
        Container container = service.getContainer();
        if (container instanceof StandardEngine) {
            StandardEngine engine = (StandardEngine) container;
            engineRemoved(engine);
        }
    }

    private void engineAdded(StandardEngine engine) {
        addContextListener(engine);
        for (Container child : engine.findChildren()) {
            if (child instanceof StandardHost) {
                StandardHost host = (StandardHost) child;
                hostAdded(host);
            }
        }
    }

    private void engineRemoved(StandardEngine engine) {
        for (Container child : engine.findChildren()) {
            if (child instanceof StandardHost) {
                StandardHost host = (StandardHost) child;
                hostRemoved(host);
            }
        }
    }

    private void hostAdded(StandardHost host) {
        addContextListener(host);
        for (Container child : host.findChildren()) {
            if (child instanceof StandardContext) {
                StandardContext context = (StandardContext) child;
                contextAdded(context);
            }
        }
    }

    private void hostRemoved(StandardHost host) {
        for (Container child : host.findChildren()) {
            if (child instanceof StandardContext) {
                StandardContext context = (StandardContext) child;
                contextRemoved(context);
            }
        }
    }

    private void contextAdded(StandardContext context) {
        // put this class as the first listener so we can process the application before any classes are loaded
        forceFirstLifecycleListener(context);
    }

    private void forceFirstLifecycleListener(StandardContext context) {
        LifecycleListener[] listeners = context.findLifecycleListeners();

        // if we are already first return
        if (listeners.length > 0 && listeners[0] == this) {
            return;
        }

        // remove all of the current listeners
        for (LifecycleListener listener : listeners) {
            context.removeLifecycleListener(listener);
        }

        // add this class (as first)
        context.addLifecycleListener(this);

        // add back all listeners
        for (LifecycleListener listener : listeners) {
            if (listener != this) {
                context.addLifecycleListener(listener);
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void contextRemoved(StandardContext context) {
    }

    public void propertyChange(PropertyChangeEvent event) {
        if ("service".equals(event.getPropertyName())) {
            Object oldValue = event.getOldValue();
            Object newValue = event.getNewValue();
            if (oldValue == null && newValue instanceof Service) {
                serviceAdded((Service) newValue);
            }
            if (oldValue instanceof Service && newValue == null) {
                serviceRemoved((Service) oldValue);
            }
        }
        if ("children".equals(event.getPropertyName())) {
            Object source = event.getSource();
            Object oldValue = event.getOldValue();
            Object newValue = event.getNewValue();
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

    private void addContextListener(ContainerBase containerBase) {
        try {
            Field field = ContainerBase.class.getDeclaredField("children");
            field.setAccessible(true);
            Map children = (Map) field.get(containerBase);
            if (children instanceof GlobalListenerSupport.MoniterableHashMap) {
                return;
            }
            children = new GlobalListenerSupport.MoniterableHashMap(children, containerBase, "children", this);
            field.set(containerBase, children);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings({"unchecked"})
    public static class MoniterableHashMap extends HashMap {
        private final Object source;
        private final String propertyName;
        private final PropertyChangeListener listener;

        public MoniterableHashMap(Map m, Object source, String propertyName, PropertyChangeListener listener) {
            super(m);
            this.source = source;
            this.propertyName = propertyName;
            this.listener = listener;
        }

        public Object put(Object key, Object value) {
            Object oldValue = super.put(key, value);
            PropertyChangeEvent event = new PropertyChangeEvent(source, propertyName, null, value);
            listener.propertyChange(event);
            return oldValue;
        }

        public Object remove(Object key) {
            Object value = super.remove(key);
            PropertyChangeEvent event = new PropertyChangeEvent(source, propertyName, value, null);
            listener.propertyChange(event);
            return value;
        }
    }
}
