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

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.deploy.ContextEjb;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextLocalEjb;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceEnvRef;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.NamingResources;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.dynamic.PassthroughFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.Context;
import javax.naming.NamingException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class OpenEJBNamingContextListener implements LifecycleListener, PropertyChangeListener {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("tomcat"), "org.apache.openejb.util.resources");

    /**
     * Associated standardServer.
     */
    private final StandardServer standardServer;

    /**
     * Has the listener been started?
     */
    private boolean running = false;

    /**
     * Associated naming resources.
     */
    private final NamingResources namingResources;

    public OpenEJBNamingContextListener(StandardServer standardServer) {
        this.standardServer = standardServer;
        namingResources = standardServer.getGlobalNamingResources();
    }

    public void lifecycleEvent(LifecycleEvent event) {
        if (event.getLifecycle() != standardServer) {
            return;
        }

        if (event.getType() == Lifecycle.START_EVENT) {
            start();

        } else if (event.getType() == Lifecycle.STOP_EVENT) {

            stop();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void start() {
        if (running) return;

        namingResources.addPropertyChangeListener(this);
        processInitialNamingResources();


        running = true;
    }

    public void stop() {
        if (!running) return;

        namingResources.removePropertyChangeListener(this);

        running = false;
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (!running) return;

        Object source = event.getSource();
        if (source == namingResources) {
            processGlobalResourcesChange(event.getPropertyName(), event.getOldValue(), event.getNewValue());
        }
    }

    /**
     * Process a property change on the global naming resources, by making the
     * corresponding addition or removal to OpenEJB.
     *
     * @param name     Property name of the change to be processed
     * @param oldValue The old value (or <code>null</code> if adding)
     * @param newValue The new value (or <code>null</code> if removing)
     */
    private void processGlobalResourcesChange(String name, Object oldValue, Object newValue) {

        // NOTE - It seems that the Context for global JNDI resources
        // is left in read-write mode, so we do not have to change it here

        if (name.equals("ejb")) {
            if (oldValue != null) {
                ContextEjb ejb = (ContextEjb) oldValue;
                if (ejb.getName() != null) {
                    removeEjb(ejb.getName());
                }
            }
            if (newValue != null) {
                ContextEjb ejb = (ContextEjb) newValue;
                if (ejb.getName() != null) {
                    addEjb(ejb);
                }
            }
        } else if (name.equals("environment")) {
            if (oldValue != null) {
                ContextEnvironment env = (ContextEnvironment) oldValue;
                if (env.getName() != null) {
                    removeEnvironment(env.getName());
                }
            }
            if (newValue != null) {
                ContextEnvironment env = (ContextEnvironment) newValue;
                if (env.getName() != null) {
                    addEnvironment(env);
                }
            }
        } else if (name.equals("localEjb")) {
            if (oldValue != null) {
                ContextLocalEjb ejb = (ContextLocalEjb) oldValue;
                if (ejb.getName() != null) {
                    removeLocalEjb(ejb.getName());
                }
            }
            if (newValue != null) {
                ContextLocalEjb ejb = (ContextLocalEjb) newValue;
                if (ejb.getName() != null) {
                    addLocalEjb(ejb);
                }
            }
        } else if (name.equals("resource")) {
            if (oldValue != null) {
                ContextResource resource = (ContextResource) oldValue;
                if (resource.getName() != null) {
                    removeResource(resource.getName());
                }
            }
            if (newValue != null) {
                ContextResource resource = (ContextResource) newValue;
                if (resource.getName() != null) {
                    addResource(resource);
                }
            }
        } else if (name.equals("resourceEnvRef")) {
            if (oldValue != null) {
                ContextResourceEnvRef resourceEnvRef = (ContextResourceEnvRef) oldValue;
                if (resourceEnvRef.getName() != null) {
                    removeResourceEnvRef(resourceEnvRef.getName());
                }
            }
            if (newValue != null) {
                ContextResourceEnvRef resourceEnvRef = (ContextResourceEnvRef) newValue;
                if (resourceEnvRef.getName() != null) {
                    addResourceEnvRef(resourceEnvRef);
                }
            }
        } else if (name.equals("resourceLink")) {
            if (oldValue != null) {
                ContextResourceLink rl = (ContextResourceLink) oldValue;
                if (rl.getName() != null) {
                    removeResourceLink(rl.getName());
                }
            }
            if (newValue != null) {
                ContextResourceLink rl = (ContextResourceLink) newValue;
                if (rl.getName() != null) {
                    addResourceLink(rl);
                }
            }
        }
    }


    private void processInitialNamingResources() {
        // Resource links
        ContextResourceLink[] resourceLinks = namingResources.findResourceLinks();
        for (ContextResourceLink resourceLink : resourceLinks) {
            addResourceLink(resourceLink);
        }

        // Resources
        ContextResource[] resources = namingResources.findResources();
        for (ContextResource resource : resources) {
            addResource(resource);
        }

        // Resources Env
        ContextResourceEnvRef[] resourceEnvRefs = namingResources.findResourceEnvRefs();
        for (ContextResourceEnvRef resourceEnvRef : resourceEnvRefs) {
            addResourceEnvRef(resourceEnvRef);
        }

        // Environment entries
        ContextEnvironment[] contextEnvironments = namingResources.findEnvironments();
        for (ContextEnvironment contextEnvironment : contextEnvironments) {
            addEnvironment(contextEnvironment);
        }

        // EJB references
        ContextEjb[] ejbs = namingResources.findEjbs();
        for (ContextEjb ejb : ejbs) {
            addEjb(ejb);
        }
    }

    public void addEjb(ContextEjb ejb) {
    }

    public void addEnvironment(ContextEnvironment env) {
    }

    public void addLocalEjb(ContextLocalEjb localEjb) {
    }

    public void addResource(ContextResource resource) {
        try {
            Context globalNamingContext = standardServer.getGlobalNamingContext();
            Object value = globalNamingContext.lookup(resource.getName());
            String type = resource.getType();
            bindResource(resource.getName(), value, type);
        } catch (NamingException e) {
            logger.error("Unable to lookup Global Tomcat resource " + resource.getName(), e);
        }
    }

    public void addResourceEnvRef(ContextResourceEnvRef resourceEnvRef) {
        try {
            Context globalNamingContext = standardServer.getGlobalNamingContext();
            Object value = globalNamingContext.lookup(resourceEnvRef.getName());
            String type = resourceEnvRef.getType();
            bindResource(resourceEnvRef.getName(), value, type);
        } catch (NamingException e) {
            logger.error("Unable to lookup Global Tomcat resource " + resourceEnvRef.getName(), e);
        }
    }

    private void bindResource(String name, Object value, String type) {
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.id = name;
        resourceInfo.service = "Resource";
        resourceInfo.types.add(type);
        PassthroughFactory.add(resourceInfo, value);
        Assembler assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);

        try {
            assembler.createResource(resourceInfo);
        } catch (OpenEJBException e) {
            logger.error("Unable to bind Global Tomcat resource " + name + " into OpenEJB", e);
        }
    }

    public void addResourceLink(ContextResourceLink resourceLink) {
    }

    public void removeEjb(String name) {
    }

    public void removeEnvironment(String name) {
    }

    public void removeLocalEjb(String name) {
    }

    public void removeResource(String name) {
        // there isn't any way to remove a resource yet
    }

    public void removeResourceEnvRef(String name) {
        // there isn't any way to remove a resource yet
    }

    public void removeResourceLink(String name) {
    }
}
