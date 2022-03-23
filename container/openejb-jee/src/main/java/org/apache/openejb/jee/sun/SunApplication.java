/**
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
package org.apache.openejb.jee.sun;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "web",
    "passByReference",
    "uniqueId",
    "securityRoleMapping",
    "realm",
    "ejbRef",
    "resourceRef",
    "resourceEnvRef",
    "serviceRef",
    "messageDestinationRef",
    "messageDestination",
    "archiveName",
    "compatibility"
})
@XmlRootElement(name = "sun-application")
public class SunApplication {
    protected List<Web> web;
    @XmlElement(name = "pass-by-reference")
    protected String passByReference;
    @XmlElement(name = "unique-id")
    protected String uniqueId;
    @XmlElement(name = "security-role-mapping")
    protected List<SecurityRoleMapping> securityRoleMapping;
    protected String realm;
    @XmlElement(name = "ejb-ref")
    protected List<EjbRef> ejbRef;
    @XmlElement(name = "resource-ref")
    protected List<ResourceRef> resourceRef;
    @XmlElement(name = "resource-env-ref")
    protected List<ResourceEnvRef> resourceEnvRef;
    @XmlElement(name = "service-ref")
    protected List<ServiceRef> serviceRef;
    @XmlElement(name = "message-destination-ref")
    protected List<MessageDestinationRef> messageDestinationRef;
    @XmlElement(name = "message-destination")
    protected List<MessageDestination> messageDestination;
    @XmlElement(name = "archive-name")
    protected String archiveName;
    protected String compatibility;

    public List<Web> getWeb() {
        if (web == null) {
            web = new ArrayList<Web>();
        }
        return this.web;
    }

    public String getPassByReference() {
        return passByReference;
    }

    public void setPassByReference(final String value) {
        this.passByReference = value;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(final String value) {
        this.uniqueId = value;
    }

    public List<SecurityRoleMapping> getSecurityRoleMapping() {
        if (securityRoleMapping == null) {
            securityRoleMapping = new ArrayList<SecurityRoleMapping>();
        }
        return this.securityRoleMapping;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(final String value) {
        this.realm = value;
    }

    public List<EjbRef> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new ArrayList<EjbRef>();
        }
        return this.ejbRef;
    }

    public List<ResourceRef> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new ArrayList<ResourceRef>();
        }
        return this.resourceRef;
    }

    public List<ResourceEnvRef> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new ArrayList<ResourceEnvRef>();
        }
        return this.resourceEnvRef;
    }

    public List<ServiceRef> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new ArrayList<ServiceRef>();
        }
        return this.serviceRef;
    }

    public List<MessageDestinationRef> getMessageDestinationRef() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new ArrayList<MessageDestinationRef>();
        }
        return this.messageDestinationRef;
    }

    public List<MessageDestination> getMessageDestination() {
        if (messageDestination == null) {
            messageDestination = new ArrayList<MessageDestination>();
        }
        return this.messageDestination;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(final String archiveName) {
        this.archiveName = archiveName;
    }

    public String getCompatibility() {
        return compatibility;
    }

    public void setCompatibility(final String compatibility) {
        this.compatibility = compatibility;
    }
}
