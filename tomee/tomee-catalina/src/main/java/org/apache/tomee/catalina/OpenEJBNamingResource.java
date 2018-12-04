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

import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.tomcat.util.descriptor.web.ContextEjb;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.ContextLocalEjb;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef;
import org.apache.tomcat.util.descriptor.web.ContextResourceLink;
import org.apache.tomcat.util.descriptor.web.ContextService;
import org.apache.tomcat.util.descriptor.web.MessageDestinationRef;
import org.apache.tomcat.util.descriptor.web.ResourceBase;
import org.apache.tomee.common.NamingUtil;

import java.util.ArrayList;
import java.util.Collection;

public class OpenEJBNamingResource extends NamingResourcesImpl {
    private static final String JAVA_PREFIX = "java:";

    private boolean isTomcatResource;
    private final Collection<ResourceBase> tomcatResources = new ArrayList<>();

    public OpenEJBNamingResource() {
        // no-op
    }

    public OpenEJBNamingResource(final NamingResourcesImpl namingResources) {
        if (namingResources != null) {
            isTomcatResource = true;
            for (final ContextResource resource : namingResources.findResources()) {
                addResource(resource);
            }
            for (final ContextResourceEnvRef resource : namingResources.findResourceEnvRefs()) {
                addResourceEnvRef(resource);
            }
            for (final ContextEjb ejb : namingResources.findEjbs()) {
                addEjb(ejb);
            }
            for (final ContextLocalEjb ejb : namingResources.findLocalEjbs()) {
                addLocalEjb(ejb);
            }
            for (final ContextResourceLink link : namingResources.findResourceLinks()) {
                addResourceLink(link);
            }
            for (final ContextService service : namingResources.findServices()) {
                addService(service);
            }
            for (final MessageDestinationRef ref : namingResources.findMessageDestinationRefs()) {
                addMessageDestinationRef(ref);
            }
            for (final ContextEnvironment env : namingResources.findEnvironments()) {
                addEnvironment(env);
            }
            isTomcatResource = false;
        }
    }

    @Override
    public void addEnvironment(final ContextEnvironment environment) {
        normalize(environment);
        super.addEnvironment(environment);
    }

    @Override
    public void addResourceEnvRef(final ContextResourceEnvRef ref) {
        normalize(ref);
        super.addResourceEnvRef(ref);
    }

    @Override
    public void addEjb(final ContextEjb ref) {
        normalize(ref);
        super.addEjb(ref);
    }

    @Override
    public void addLocalEjb(final ContextLocalEjb ref) {
        normalize(ref);
        super.addLocalEjb(ref);
    }

    @Override
    public void addResource(final ContextResource ref) {
        normalize(ref);
        super.addResource(ref);
        if (isTomcatResource) {
            pushResourceToAddInOpenEJB(ref);
        }
    }

    @Override
    public void addMessageDestinationRef(final MessageDestinationRef ref) {
        normalize(ref);
        super.addMessageDestinationRef(ref);
    }

    @Override
    public void addService(final ContextService ref) {
        normalize(ref);
        super.addService(ref);
    }

    @Override
    public void addResourceLink(final ContextResourceLink ref) {
        normalize(ref);
        super.addResourceLink(ref);
    }

    /**
     * tomcat uses a hastable to store entry type, null values are not allowed
     * <p>
     * These occur when the reference is declared using a 'lookup' attribute These do not have a type associated
     * </p>
     * @param ref
     */
    private void normalize(final ResourceBase ref) {
        final String name = ref.getName();
        if (name.startsWith(JAVA_PREFIX)) { // tomcat adds mbeans and a ":" in a mbean is not very cool for the objectname
            ref.setName(name.substring(JAVA_PREFIX.length()));
        } else if (name.startsWith("openejb/Resource/")) {
            final String id = (String) ref.getProperty(NamingUtil.RESOURCE_ID);
            if (id != null) { // id can be != substring (else) in case of app resource scope
                ref.setProperty(NamingUtil.JNDI_NAME, "openejb:Resource/" + id);
            } else {
                ref.setProperty(NamingUtil.JNDI_NAME, "openejb:" + name.substring("openejb/".length()));
            }
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