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
package org.openejb.jee.app;

import org.openejb.jee.webservice.ServiceRef;
import org.openejb.jee.common.Icon;
import org.openejb.jee.common.EnvEntry;
import org.openejb.jee.common.EjbRef;
import org.openejb.jee.common.ResourceRef;
import org.openejb.jee.common.ResourceEnvRef;
import org.openejb.jee.common.MessageDestinationRef;
import org.openejb.jee.common.PersistenceUnitRef;
import org.openejb.jee.common.PostConstruct;
import org.openejb.jee.common.PreDestroy;
import org.openejb.jee.common.MessageDestination;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class ApplicationClient {
    private String id;
    private String version;
    private boolean metadataComplete;
    private List<String> description = new ArrayList<String>();
    private List<String> displayName = new ArrayList<String>();
    private List<Icon> icons = new ArrayList<Icon>();
    private List<EnvEntry> envEntries = new ArrayList<EnvEntry>();
    private List<EjbRef> ejbRefs = new ArrayList<EjbRef>();
    private List<ServiceRef> serviceRefs = new ArrayList<ServiceRef>();
    private List<ResourceRef> resourceRefs = new ArrayList<ResourceRef>();
    private List<ResourceEnvRef> resourceEnvRefs = new ArrayList<ResourceEnvRef>();
    private List<MessageDestinationRef> messageDestinationRefs = new ArrayList<MessageDestinationRef>();
    private List<PersistenceUnitRef> persistenceUnitRefs = new ArrayList<PersistenceUnitRef>();
    private List<PostConstruct> postConstructs = new ArrayList<PostConstruct>();
    private List<PreDestroy> preDestroys = new ArrayList<PreDestroy>();
    private String callbackHandler;
    private List<MessageDestination> messageDestinations = new ArrayList<MessageDestination>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isMetadataComplete() {
        return metadataComplete;
    }

    public void setMetadataComplete(boolean metadataComplete) {
        this.metadataComplete = metadataComplete;
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

    public List<ServiceRef> getServiceRefs() {
        return serviceRefs;
    }

    public void setServiceRefs(List<ServiceRef> serviceRefs) {
        this.serviceRefs = serviceRefs;
    }

    public List<ResourceRef> getResourceRefs() {
        return resourceRefs;
    }

    public void setResourceRefs(List<ResourceRef> resourceRefs) {
        this.resourceRefs = resourceRefs;
    }

    public List<ResourceEnvRef> getResourceEnvRefs() {
        return resourceEnvRefs;
    }

    public void setResourceEnvRefs(List<ResourceEnvRef> resourceEnvRefs) {
        this.resourceEnvRefs = resourceEnvRefs;
    }

    public List<MessageDestinationRef> getMessageDestinationRefs() {
        return messageDestinationRefs;
    }

    public void setMessageDestinationRefs(List<MessageDestinationRef> messageDestinationRefs) {
        this.messageDestinationRefs = messageDestinationRefs;
    }

    public List<PersistenceUnitRef> getPersistenceUnitRefs() {
        return persistenceUnitRefs;
    }

    public void setPersistenceUnitRefs(List<PersistenceUnitRef> persistenceUnitRefs) {
        this.persistenceUnitRefs = persistenceUnitRefs;
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

    public String getCallbackHandler() {
        return callbackHandler;
    }

    public void setCallbackHandler(String callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    public List<MessageDestination> getMessageDestinations() {
        return messageDestinations;
    }

    public void setMessageDestinations(List<MessageDestination> messageDestinations) {
        this.messageDestinations = messageDestinations;
    }

}
