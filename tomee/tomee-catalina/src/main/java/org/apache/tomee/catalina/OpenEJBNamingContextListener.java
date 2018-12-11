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

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.dynamic.PassthroughFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomcat.util.descriptor.web.ContextEjb;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.ContextLocalEjb;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef;
import org.apache.tomcat.util.descriptor.web.ContextResourceLink;
import org.apache.tomcat.util.descriptor.web.ResourceBase;

import javax.naming.Context;
import javax.naming.NamingException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@SuppressWarnings({"UnusedDeclaration"})
public class OpenEJBNamingContextListener implements LifecycleListener, PropertyChangeListener {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("tomcat"), "org.apache.openejb.util.resources");

    /**
     * Associated standardServer.
     */
    private final StandardServer standardServer;

    /**
     * Has the listener been started?
     */
    private boolean running;

    /**
     * Associated naming resources.
     */
    private final NamingResourcesImpl namingResources;

    public OpenEJBNamingContextListener(final StandardServer standardServer) {
        this.standardServer = standardServer;
        namingResources = standardServer.getGlobalNamingResources();
    }

    @Override
    public void lifecycleEvent(final LifecycleEvent event) {
        if (event.getLifecycle() != standardServer) {
            return;
        }

        if (Lifecycle.START_EVENT.equals(event.getType())) {
            start();

        } else if (Lifecycle.STOP_EVENT.equals(event.getType())) {

            stop();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void start() {
        if (running) {
            return;
        }

        namingResources.addPropertyChangeListener(this);
        processInitialNamingResources();

        running = true;
    }

    public void stop() {
        if (!running) {
            return;
        }

        namingResources.removePropertyChangeListener(this);

        running = false;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (!running) {
            return;
        }

        final Object source = event.getSource();
        if (source == namingResources) {
            processGlobalResourcesChange(event.getPropertyName(), event.getOldValue(), event.getNewValue());
        }
    }

    /**
     * Process a property change on the global naming resources, by making the
     * corresponding addition or removal to OpenEJB.
     *
     * @param name     Property name of the change to be processed
     * @param oldValue The old value (or {@code null} if adding)
     * @param newValue The new value (or {@code null} if removing)
     */
    private void processGlobalResourcesChange(final String name, final Object oldValue, final Object newValue) {

        // NOTE - It seems that the Context for global JNDI resources
        // is left in read-write mode, so we do not have to change it here

        if (name.equals("ejb")) {
            if (oldValue != null) {
                final ContextEjb ejb = (ContextEjb) oldValue;
                if (ejb.getName() != null) {
                    removeEjb(ejb.getName());
                }
            }
            if (newValue != null) {
                final ContextEjb ejb = (ContextEjb) newValue;
                if (ejb.getName() != null) {
                    addEjb(ejb);
                }
            }
        } else if (name.equals("environment")) {
            if (oldValue != null) {
                final ContextEnvironment env = (ContextEnvironment) oldValue;
                if (env.getName() != null) {
                    removeEnvironment(env.getName());
                }
            }
            if (newValue != null) {
                final ContextEnvironment env = (ContextEnvironment) newValue;
                if (env.getName() != null) {
                    addEnvironment(env);
                }
            }
        } else if (name.equals("localEjb")) {
            if (oldValue != null) {
                final ContextLocalEjb ejb = (ContextLocalEjb) oldValue;
                if (ejb.getName() != null) {
                    removeLocalEjb(ejb.getName());
                }
            }
            if (newValue != null) {
                final ContextLocalEjb ejb = (ContextLocalEjb) newValue;
                if (ejb.getName() != null) {
                    addLocalEjb(ejb);
                }
            }
        } else if (name.equals("resource")) {
            if (oldValue != null) {
                final ContextResource resource = (ContextResource) oldValue;
                if (resource.getName() != null) {
                    removeResource(resource.getName());
                }
            }
            if (newValue != null) {
                final ContextResource resource = (ContextResource) newValue;
                if (resource.getName() != null) {
                    addResource(resource);
                }
            }
        } else if (name.equals("resourceEnvRef")) {
            if (oldValue != null) {
                final ContextResourceEnvRef resourceEnvRef = (ContextResourceEnvRef) oldValue;
                if (resourceEnvRef.getName() != null) {
                    removeResourceEnvRef(resourceEnvRef.getName());
                }
            }
            if (newValue != null) {
                final ContextResourceEnvRef resourceEnvRef = (ContextResourceEnvRef) newValue;
                if (resourceEnvRef.getName() != null) {
                    addResourceEnvRef(resourceEnvRef);
                }
            }
        } else if (name.equals("resourceLink")) {
            if (oldValue != null) {
                final ContextResourceLink rl = (ContextResourceLink) oldValue;
                if (rl.getName() != null) {
                    removeResourceLink(rl.getName());
                }
            }
            if (newValue != null) {
                final ContextResourceLink rl = (ContextResourceLink) newValue;
                if (rl.getName() != null) {
                    addResourceLink(rl);
                }
            }
        }
    }


    private void processInitialNamingResources() {
        // Resource links
        final ContextResourceLink[] resourceLinks = namingResources.findResourceLinks();
        for (final ContextResourceLink resourceLink : resourceLinks) {
            addResourceLink(resourceLink);
        }

        // Resources
        final ContextResource[] resources = namingResources.findResources();
        for (final ContextResource resource : resources) {
            addResource(resource);
        }

        // Resources Env
        final ContextResourceEnvRef[] resourceEnvRefs = namingResources.findResourceEnvRefs();
        for (final ContextResourceEnvRef resourceEnvRef : resourceEnvRefs) {
            addResourceEnvRef(resourceEnvRef);
        }

        // Environment entries
        final ContextEnvironment[] contextEnvironments = namingResources.findEnvironments();
        for (final ContextEnvironment contextEnvironment : contextEnvironments) {
            addEnvironment(contextEnvironment);
        }

        // EJB references
        final ContextEjb[] ejbs = namingResources.findEjbs();
        for (final ContextEjb ejb : ejbs) {
            addEjb(ejb);
        }
    }

    public void addEjb(final ContextEjb ejb) {
    }

    public void addEnvironment(final ContextEnvironment env) {
        bindResource(env);
    }

    public void addLocalEjb(final ContextLocalEjb localEjb) {
    }

    public void addResource(final ContextResource resource) {
        bindResource(resource);
    }

    public void addResourceEnvRef(final ContextResourceEnvRef resourceEnvRef) {
        bindResource(resourceEnvRef);
    }

    private void bindResource(final ResourceBase res) {
        try {
            final Context globalNamingContext = standardServer.getGlobalNamingContext();
            final Object value = globalNamingContext.lookup(res.getName());
            final String type = res.getType();
            bindResource(res.getName(), value, type);
        } catch (final NamingException e) {
            logger.error("Unable to lookup Global Tomcat resource " + res.getName(), e);
        }
    }

    private void bindResource(final String name, final Object value, final String type) {
        final Assembler assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
        try {
            assembler.getContainerSystem().getJNDIContext().lookup(Assembler.OPENEJB_RESOURCE_JNDI_PREFIX + name);
            return;
        } catch (final NamingException ne) {
            // no-op: OK
        }

        final ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.id = name;
        resourceInfo.service = "Resource";
        resourceInfo.types.add(type);
        PassthroughFactory.add(resourceInfo, value);

        logger.info("Importing a Tomcat Resource with id '" + resourceInfo.id + "' of type '" + type + "'.");
        try {
            assembler.createResource(null, resourceInfo);
        } catch (final OpenEJBException e) {
            logger.error("Unable to bind Global Tomcat resource " + name + " into OpenEJB", e);
        }
    }

    public void addResourceLink(final ContextResourceLink resourceLink) {
    }

    public void removeEjb(final String name) {
    }

    public void removeEnvironment(final String name) {
    }

    public void removeLocalEjb(final String name) {
    }

    public void removeResource(final String name) {
        // there isn't any way to remove a resource yet
    }

    public void removeResourceEnvRef(final String name) {
        // there isn't any way to remove a resource yet
    }

    public void removeResourceLink(final String name) {
    }
}
