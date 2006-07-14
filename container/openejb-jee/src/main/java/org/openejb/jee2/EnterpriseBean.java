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
package org.openejb.jee2;

import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public interface EnterpriseBean {
    public List<Text> getDescription();

    public List<Text> getDisplayName();

    public List<IconType> getIcon();

    public String getEjbName();

    public void setEjbName(String value);

    public String getMappedName();

    public void setMappedName(String value);

    public String getEjbClass();

    public void setEjbClass(String value);

    public List<AroundInvokeType> getAroundInvoke();

    public List<EnvEntryType> getEnvEntry();

    public List<EjbRefType> getEjbRef();

    public List<EjbLocalRefType> getEjbLocalRef();

    public List<ServiceRefType> getServiceRef();

    public List<ResourceRefType> getResourceRef();

    public List<ResourceEnvRefType> getResourceEnvRef();

    public List<MessageDestinationRefType> getMessageDestinationRef();

    public List<PersistenceContextRefType> getPersistenceContextRef();

    public List<PersistenceUnitRefType> getPersistenceUnitRef();

    public List<LifecycleCallbackType> getPostConstruct();

    public List<LifecycleCallbackType> getPreDestroy();

    public SecurityIdentityType getSecurityIdentity();

    public void setSecurityIdentity(SecurityIdentityType value);

    public String getId();

    public void setId(String value);

}
