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

import org.apache.catalina.deploy.ContextEjb;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextLocalEjb;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceEnvRef;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.ContextService;
import org.apache.catalina.deploy.MessageDestinationRef;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.ResourceBase;

import java.util.ArrayList;
import java.util.Collection;

public class OpenEJBNamingResource extends NamingResources {
    private static final String JAVA_PREFIX = "java:";

    private boolean isTomcatResource = false;
    private final Collection<ResourceBase> tomcatResources = new ArrayList<ResourceBase>();

    public OpenEJBNamingResource() {
        // no-op
    }

    public OpenEJBNamingResource(final NamingResources namingResources) {
        if (namingResources != null) {
            isTomcatResource = true;
            for (ContextResource resource : namingResources.findResources()) {
                addResource(resource);
            }
            for (ContextResourceEnvRef resource : namingResources.findResourceEnvRefs()) {
                addResourceEnvRef(resource);
            }
            for (ContextEjb ejb : namingResources.findEjbs()) {
                addEjb(ejb);
            }
            for (ContextLocalEjb ejb : namingResources.findLocalEjbs()) {
                addLocalEjb(ejb);
            }
            for (ContextResourceLink link : namingResources.findResourceLinks()) {
                addResourceLink(link);
            }
            for (ContextService service : namingResources.findServices()) {
                addService(service);
            }
            for (MessageDestinationRef ref : namingResources.findMessageDestinationRefs()) {
                addMessageDestinationRef(ref);
            }
            for (ContextEnvironment env : namingResources.findEnvironments()) {
                addEnvironment(env);
            }
            isTomcatResource = false;
        }
    }

    @Override
    public void addEnvironment(ContextEnvironment environment) {
        normalize(environment);
        super.addEnvironment(environment);
    }

    @Override
    public void addResourceEnvRef(ContextResourceEnvRef ref) {
        normalize(ref);
        super.addResourceEnvRef(ref);
    }

    @Override
    public void addEjb(ContextEjb ref) {
        normalize(ref);
        super.addEjb(ref);
    }

    @Override
    public void addLocalEjb(ContextLocalEjb ref) {
        normalize(ref);
        super.addLocalEjb(ref);
    }

    @Override
    public void addResource(ContextResource ref) {
        normalize(ref);
        super.addResource(ref);
        if (isTomcatResource) {
            pushResourceToAddInOpenEJB(ref);
        }
    }

    @Override
    public void addMessageDestinationRef(MessageDestinationRef ref) {
        normalize(ref);
        super.addMessageDestinationRef(ref);
    }

    @Override
    public void addService(ContextService ref) {
        normalize(ref);
        super.addService(ref);
    }

    @Override
    public void addResourceLink(ContextResourceLink ref) {
        normalize(ref);
        super.addResourceLink(ref);
    }

    /**
     * tomcat uses a hastable to store entry type, null values are not allowed
     * <p/>
     * These occur when the reference is declared using a 'lookup' attribute These do not have a type associated
     *
     * @param ref
     */
    private void normalize(ResourceBase ref) {
        final String name = ref.getName();
        if (name.startsWith(JAVA_PREFIX)) { // tomcat adds mbeans and a ":" in a mbean is not very cool for the objectname
            ref.setName(name.substring(JAVA_PREFIX.length()));
        }
        if (ref.getType() == null) {
            ref.setType("");
        }
    }

    public void setTomcatResource(final boolean tomcatResource) {
        isTomcatResource = tomcatResource;
    }

    private void pushResourceToAddInOpenEJB(final ContextResource ref) {
        tomcatResources.add(ref);
    }

    public Collection<ResourceBase> getTomcatResources() {
        return tomcatResources;
    }
}