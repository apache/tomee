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

import org.openejb.jee.common.EjbLocalRef;
import org.openejb.jee.common.EjbRef;
import org.openejb.jee.common.EnvEntry;
import org.openejb.jee.common.Icon;
import org.openejb.jee.common.JndiEnvironmentRef;
import org.openejb.jee.common.MessageDestinationRef;
import org.openejb.jee.common.PersistenceUnitRef;
import org.openejb.jee.common.PersistenceContextRef;
import org.openejb.jee.common.PostConstruct;
import org.openejb.jee.common.PreDestroy;
import org.openejb.jee.common.ResourceEnvRef;
import org.openejb.jee.common.ResourceRef;
import org.openejb.jee.common.SecurityRoleRef;
import org.openejb.jee.webservice.ServiceRef;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class EnterpriseBean {
    private String id;
    private List<String> description = new ArrayList<String>();
    private List<String> displayName = new ArrayList<String>();
    private List<Icon> icons = new ArrayList<Icon>();
    private String ejbName;
    private String mappedName;
    private String home;
    private String remote;
    private String localHome;
    private String local;
    private String ejbClass;
    private List<EnvEntry> envEntries = new ArrayList<EnvEntry>();
    private List<EjbRef> ejbRefs = new ArrayList<EjbRef>();
    private List<EjbLocalRef> ejbLocalRefs = new ArrayList<EjbLocalRef>();
    private List<ServiceRef> serviceRefs = new ArrayList<ServiceRef>();
    private List<ResourceEnvRef> resourceEnvRefs = new ArrayList<ResourceEnvRef>();
    private List<ResourceRef> resourceRefs = new ArrayList<ResourceRef>();
    private List<MessageDestinationRef> messageDestinationRefs = new ArrayList<MessageDestinationRef>();
    private List<PersistenceContextRef> persistenceContextRefs = new ArrayList<PersistenceContextRef>();
    private List<PersistenceUnitRef> persistenceUnitRefs = new ArrayList<PersistenceUnitRef>();

    private List<PostConstruct> postConstructs = new ArrayList<PostConstruct>();
    private List<PreDestroy> preDestroys = new ArrayList<PreDestroy>();

    private List<SecurityRoleRef> securityRoleRefs = new ArrayList<SecurityRoleRef>();
    private SecurityIdentity securityIdentity;

    public EnterpriseBean() {
    }

    public EnterpriseBean(String ejbName, String ejbClass) {
        this.ejbName = ejbName;
        this.ejbClass = ejbClass;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public List<String> getDisplayName() {
        return displayName;
    }

    public void setDisplayName(List<String> displayName) {
        this.displayName = displayName;
    }

    public List<Icon> getIcons() {
        return icons;
    }

    public void setIcons(List<Icon> icons) {
        this.icons = icons;
    }

    public String getEjbName() {
        return ejbName;
    }

    public void setEjbName(String ejbName) {
        this.ejbName = ejbName;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public String getLocalHome() {
        return localHome;
    }

    public void setLocalHome(String localHome) {
        this.localHome = localHome;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getEjbClass() {
        return ejbClass;
    }

    public void setEjbClass(String ejbClass) {
        this.ejbClass = ejbClass;
    }

    public List<EnvEntry> getEnvEntries() {
        return envEntries;
    }

    public void setEnvEntries(List<EnvEntry> envEntries) {
        this.envEntries = envEntries;
    }

    public List<EjbRef> getEjbRefs() {
        return ejbRefs;
    }

    public void setEjbRefs(List<EjbRef> ejbRefs) {
        this.ejbRefs = ejbRefs;
    }

    public List<EjbLocalRef> getEjbLocalRefs() {
        return ejbLocalRefs;
    }

    public void setEjbLocalRefs(List<EjbLocalRef> ejbLocalRefs) {
        this.ejbLocalRefs = ejbLocalRefs;
    }

    public List<ServiceRef> getServiceRefs() {
        return serviceRefs;
    }

    public void setServiceRefs(List<ServiceRef> serviceRefs) {
        this.serviceRefs = serviceRefs;
    }

    public List<ResourceEnvRef> getResourceEnvRefs() {
        return resourceEnvRefs;
    }

    public void setResourceEnvRefs(List<ResourceEnvRef> resourceEnvRefs) {
        this.resourceEnvRefs = resourceEnvRefs;
    }

    public List<ResourceRef> getResourceRefs() {
        return resourceRefs;
    }

    public void setResourceRefs(List<ResourceRef> resourceRefs) {
        this.resourceRefs = resourceRefs;
    }

    public List<MessageDestinationRef> getMessageDestinationRefs() {
        return messageDestinationRefs;
    }

    public void setMessageDestinationRefs(List<MessageDestinationRef> messageDestinationRefs) {
        this.messageDestinationRefs = messageDestinationRefs;
    }

    public List<PersistenceContextRef> getPersistenceContextRefs() {
        return persistenceContextRefs;
    }

    public void setPersistenceContextRefs(List<PersistenceContextRef> persistenceContextRefs) {
        this.persistenceContextRefs = persistenceContextRefs;
    }

    public List<PersistenceUnitRef> getPersistenceUnitRefs() {
        return persistenceUnitRefs;
    }

    public void setPersistenceUnitRefs(List<PersistenceUnitRef> persistenceUnitRefs) {
        this.persistenceUnitRefs = persistenceUnitRefs;
    }

    public List<JndiEnvironmentRef> getJndiEnvironmentRefs() {
        List<JndiEnvironmentRef> jndi = new ArrayList<JndiEnvironmentRef>();
        jndi.addAll(this.ejbLocalRefs);
        jndi.addAll(this.ejbRefs);
        jndi.addAll(this.envEntries);
        jndi.addAll(this.messageDestinationRefs);
        jndi.addAll(this.persistenceContextRefs);
        jndi.addAll(this.persistenceUnitRefs);
        jndi.addAll(this.resourceEnvRefs);
        jndi.addAll(this.resourceRefs);
        jndi.addAll(this.serviceRefs);
        return jndi;
    }

    public List<PostConstruct> getPostConstructs() {
        return postConstructs;
    }

    public void setPostConstructs(List<PostConstruct> postConstructs) {
        this.postConstructs = postConstructs;
    }

    public List<PreDestroy> getPreDestroys() {
        return preDestroys;
    }

    public void setPreDestroys(List<PreDestroy> preDestroys) {
        this.preDestroys = preDestroys;
    }

    public List<SecurityRoleRef> getSecurityRoleRefs() {
        return securityRoleRefs;
    }

    public void setSecurityRoleRefs(List<SecurityRoleRef> securityRoleRefs) {
        this.securityRoleRefs = securityRoleRefs;
    }

    public SecurityIdentity getSecurityIdentity() {
        return securityIdentity;
    }

    public void setSecurityIdentity(SecurityIdentity securityIdentity) {
        this.securityIdentity = securityIdentity;
    }

    public void setRemoteInterfaces(String home, String remote){
        setHome(home);
        setRemote(remote);
    }

    public void setLocalInterfaces(String localHome, String local){
        setLocalHome(localHome);
        setLocal(local);
    }
}
