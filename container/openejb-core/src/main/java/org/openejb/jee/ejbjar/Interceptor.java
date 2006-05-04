/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.jee.ejbjar;

import org.openejb.jee.javaee.EjbLocalRef;
import org.openejb.jee.javaee.EjbRef;
import org.openejb.jee.javaee.EnvEntry;
import org.openejb.jee.javaee.JndiEnvironmentRef;
import org.openejb.jee.javaee.MessageDestinationRef;
import org.openejb.jee.javaee.PersistenceUnitRef;
import org.openejb.jee.javaee.PersistenceContextRef;
import org.openejb.jee.javaee.PostConstruct;
import org.openejb.jee.javaee.PreDestroy;
import org.openejb.jee.javaee.ResourceEnvRef;
import org.openejb.jee.javaee.ResourceRef;
import org.openejb.jee.webservice.ServiceRef;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class Interceptor {
    private String id;
    private List<String> description = new ArrayList<String>();
    private String interceptorClass;
    private List<AroundInvoke> aroundInvokes = new ArrayList<AroundInvoke>();
    private List<PostActivate> postActivates = new ArrayList<PostActivate>();
    private List<PrePassivate> prePassivates = new ArrayList<PrePassivate>();
    private List<EnvEntry> envEntries = new ArrayList<EnvEntry>();
    private List<EjbRef> ejbRefs = new ArrayList<EjbRef>();
    private List<EjbLocalRef> ejbLocalRefs = new ArrayList<EjbLocalRef>();
    private List<ServiceRef> serviceRefs = new ArrayList<ServiceRef>();
    private List<ResourceEnvRef> resourceEnvRefs = new ArrayList<ResourceEnvRef>();
    private List<ResourceRef> resourceRefs = new ArrayList<ResourceRef>();
    private List<MessageDestinationRef> messageDestinationRefs = new ArrayList<MessageDestinationRef>();
    private List<PersistenceContextRef> persistenceContextRefs = new ArrayList<PersistenceContextRef>();
    private List<PersistenceUnitRef> persistenceUnitRefs = new ArrayList<PersistenceUnitRef>();

    // For aggregation
    private List<JndiEnvironmentRef> jndiEnvironmentRefs = new ArrayList<JndiEnvironmentRef>();

    private List<PostConstruct> postConstructs = new ArrayList<PostConstruct>();
    private List<PreDestroy> preDestroys = new ArrayList<PreDestroy>();
}
