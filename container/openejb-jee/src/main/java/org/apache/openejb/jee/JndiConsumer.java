/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.jee;

import java.util.Collection;
import java.util.Map;

/**
 * Interface for all components that have a java: jndi context or contribute to one such as the application.
 * The get<foo>Map methods return maps keyed by normalized jndi name, that is any old-style <foo> entry is
 * normalized to java:comp/env/<foo> to match entries that are explicitly defined in a comp, module, app, or global
 * context.  The Refs themselves do not have normalized referenceNames.
 *
 * @version $Rev$ $Date$
 */
public interface JndiConsumer {

    String getJndiConsumerName();

    Collection<EnvEntry> getEnvEntry();

    Map<String, EnvEntry> getEnvEntryMap();

    Collection<EjbRef> getEjbRef();

    Map<String, EjbRef> getEjbRefMap();

    Collection<EjbLocalRef> getEjbLocalRef();

    Map<String, EjbLocalRef> getEjbLocalRefMap();

    Collection<ServiceRef> getServiceRef();

    Map<String, ServiceRef> getServiceRefMap();

    Collection<ResourceRef> getResourceRef();

    Map<String, ResourceRef> getResourceRefMap();

    Collection<ResourceEnvRef> getResourceEnvRef();

    Map<String, ResourceEnvRef> getResourceEnvRefMap();

    Collection<MessageDestinationRef> getMessageDestinationRef();

    Map<String, MessageDestinationRef> getMessageDestinationRefMap();

    Collection<PersistenceContextRef> getPersistenceContextRef();

    Map<String, PersistenceContextRef> getPersistenceContextRefMap();

    Collection<PersistenceUnitRef> getPersistenceUnitRef();

    Map<String, PersistenceUnitRef> getPersistenceUnitRefMap();

    Collection<DataSource> getDataSource();

    Map<String, DataSource> getDataSourceMap();

    Collection<JMSConnectionFactory> getJMSConnectionFactories();

    Map<String, JMSConnectionFactory> getJMSConnectionFactoriesMap();

    Collection<JMSDestination> getJMSDestination();

    Map<String, JMSDestination> getJMSDestinationMap();
    Map<String, ContextService> getContextServiceMap();
}
