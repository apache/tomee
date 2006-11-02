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
package org.apache.openejb.jee;

import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public interface EnterpriseBean {
//    public List<Text> getDescription();

//    public List<Text> getDisplayName();

//    public List<Icon> getIcon();

    public String getEjbName();

    public void setEjbName(String value);

    public String getMappedName();

    public void setMappedName(String value);

    public String getEjbClass();

    public void setEjbClass(String value);

    public List<AroundInvoke> getAroundInvoke();

    public void addAroundInvoke(String method);

    public List<EnvEntry> getEnvEntry();

    public List<EjbRef> getEjbRef();

    public List<EjbLocalRef> getEjbLocalRef();

    public List<ServiceRef> getServiceRef();

    public List<ResourceRef> getResourceRef();

    public List<ResourceEnvRef> getResourceEnvRef();

    public List<MessageDestinationRef> getMessageDestinationRef();

    public List<PersistenceContextRef> getPersistenceContextRef();

    public List<PersistenceUnitRef> getPersistenceUnitRef();

    public List<LifecycleCallback> getPostConstruct();

    public List<LifecycleCallback> getPreDestroy();

    public void addPostConstruct(String method);

    public void addPreDestroy(String method);

    public SecurityIdentity getSecurityIdentity();

    public void setSecurityIdentity(SecurityIdentity value);

    public String getId();

    public void setId(String value);

    public TransactionType getTransactionType();

    public void setTransactionType(TransactionType type);

}
