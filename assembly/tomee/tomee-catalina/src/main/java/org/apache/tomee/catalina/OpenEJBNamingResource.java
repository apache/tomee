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

public class OpenEJBNamingResource extends NamingResources {

    @Override
    public void addEnvironment(ContextEnvironment environment) {
        if (environment.getType() == null) {
            normalize(environment);
        }
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
     * These occur when the reference is decalred using a 'lookup' attribute These do not have a type associated
     *
     * @param ref
     */
    private void normalize(ResourceBase ref) {
        if (ref.getType() == null) {
            ref.setType("");
        }
    }
}