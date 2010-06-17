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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * application_6.xsd
 *
 * <p>Java class for applicationType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="applicationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="application-name" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="initialize-in-order" type="{http://java.sun.com/xml/ns/javaee}generic-booleanType" minOccurs="0"/>
 *         &lt;element name="module" type="{http://java.sun.com/xml/ns/javaee}moduleType" maxOccurs="unbounded"/>
 *         &lt;element name="security-role" type="{http://java.sun.com/xml/ns/javaee}security-roleType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="library-directory" type="{http://java.sun.com/xml/ns/javaee}pathType" minOccurs="0"/>
 *         &lt;element name="env-entry" type="{http://java.sun.com/xml/ns/javaee}env-entryType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ejb-ref" type="{http://java.sun.com/xml/ns/javaee}ejb-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ejb-local-ref" type="{http://java.sun.com/xml/ns/javaee}ejb-local-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}service-refGroup"/>
 *         &lt;element name="resource-ref" type="{http://java.sun.com/xml/ns/javaee}resource-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="resource-env-ref" type="{http://java.sun.com/xml/ns/javaee}resource-env-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="message-destination-ref" type="{http://java.sun.com/xml/ns/javaee}message-destination-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="persistence-context-ref" type="{http://java.sun.com/xml/ns/javaee}persistence-context-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="persistence-unit-ref" type="{http://java.sun.com/xml/ns/javaee}persistence-unit-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="message-destination" type="{http://java.sun.com/xml/ns/javaee}message-destinationType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="data-source" type="{http://java.sun.com/xml/ns/javaee}data-sourceType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required" type="{http://java.sun.com/xml/ns/javaee}dewey-versionType" fixed="6" />
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */

@XmlRootElement(name = "application")

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "applicationType", propOrder = {
    "applicationName",
    "descriptions",
    "displayNames",
    "icon",
    "initializeInOrder",
    "module",
    "securityRole",
    "libraryDirectory",
    "envEntry",
    "ejbRef",
    "ejbLocalRef",
    "serviceRef",
    "resourceRef",
    "resourceEnvRef",
    "messageDestinationRef",
    "persistenceContextRef",
    "persistenceUnitRef",
    "messageDestination",
    "dataSource"
})
public class Application implements JndiConsumer {

    @XmlElement(name = "application-name")
    protected String applicationName;

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();

    @XmlElement(name = "initialize-in-order")
    protected Boolean initializeInOrder;
    @XmlElement(required = true)
    protected List<Module> module;
    @XmlElement(name = "security-role")
    protected List<SecurityRole> securityRole;
    @XmlElement(name = "library-directory")
    protected String libraryDirectory;
    @XmlElement(name = "env-entry", required = true)
    protected KeyedCollection<String,EnvEntry> envEntry;
    @XmlElement(name = "ejb-ref", required = true)
    protected KeyedCollection<String,EjbRef> ejbRef;
    @XmlElement(name = "ejb-local-ref", required = true)
    protected KeyedCollection<String,EjbLocalRef> ejbLocalRef;
    @XmlElement(name = "service-ref", required = true)
    protected KeyedCollection<String,ServiceRef> serviceRef;
    @XmlElement(name = "resource-ref", required = true)
    protected KeyedCollection<String,ResourceRef> resourceRef;
    @XmlElement(name = "resource-env-ref", required = true)
    protected KeyedCollection<String,ResourceEnvRef> resourceEnvRef;
    @XmlElement(name = "message-destination-ref", required = true)
    protected KeyedCollection<String,MessageDestinationRef> messageDestinationRef;
    @XmlElement(name = "persistence-context-ref", required = true)
    protected KeyedCollection<String,PersistenceContextRef> persistenceContextRef;
    @XmlElement(name = "persistence-unit-ref", required = true)
    protected KeyedCollection<String,PersistenceUnitRef> persistenceUnitRef;
    @XmlElement(name = "message-destination", required = true)
    protected KeyedCollection<String,MessageDestination> messageDestination;
    @XmlElement(name = "data-source")
    protected KeyedCollection<String,DataSource> dataSource;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected java.lang.String version;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String value) {
        this.applicationName = value;
    }

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    @XmlElement(name = "display-name", required = true)
    public Text[] getDisplayNames() {
        return displayName.toArray();
    }

    public void setDisplayNames(Text[] text) {
        displayName.set(text);
    }

    public String getDisplayName() {
        return displayName.get();
    }

    public Collection<Icon> getIcons() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon;
    }

    public Map<String,Icon> getIconMap() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon.toMap();
    }

    public Icon getIcon() {
        return icon.getLocal();
    }

    public Boolean getInitializeInOrder() {
        return initializeInOrder;
    }

    public void setInitializeInOrder(Boolean value) {
        this.initializeInOrder = value;
    }

    public List<Module> getModule() {
        if (module == null) {
            module = new ArrayList<Module>();
        }
        return this.module;
    }

    public List<SecurityRole> getSecurityRole() {
        if (securityRole == null) {
            securityRole = new ArrayList<SecurityRole>();
        }
        return this.securityRole;
    }

    public String getLibraryDirectory() {
        return libraryDirectory;
    }

    public void setLibraryDirectory(String value) {
        this.libraryDirectory = value;
    }

    @Override
    public String getJndiConsumerName() {
        return applicationName;
    }

    public Collection<EnvEntry> getEnvEntry() {
        if (envEntry == null) {
            envEntry = new KeyedCollection<String,EnvEntry>();
        }
        return this.envEntry;
    }

    public Map<String,EnvEntry> getEnvEntryMap() {
        if (envEntry == null) {
            envEntry = new KeyedCollection<String,EnvEntry>();
        }
        return this.envEntry.toMap();
    }

    public Collection<EjbRef> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new KeyedCollection<String,EjbRef>();
        }
        return this.ejbRef;
    }

    public Map<String,EjbRef> getEjbRefMap() {
        if (ejbRef == null) {
            ejbRef = new KeyedCollection<String,EjbRef>();
        }
        return this.ejbRef.toMap();
    }

    public Collection<EjbLocalRef> getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new KeyedCollection<String,EjbLocalRef>();
        }
        return this.ejbLocalRef;
    }

    public Map<String,EjbLocalRef> getEjbLocalRefMap() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new KeyedCollection<String,EjbLocalRef>();
        }
        return this.ejbLocalRef.toMap();
    }

    public Collection<ServiceRef> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new KeyedCollection<String,ServiceRef>();
        }
        return this.serviceRef;
    }

    public Map<String,ServiceRef> getServiceRefMap() {
        if (serviceRef == null) {
            serviceRef = new KeyedCollection<String,ServiceRef>();
        }
        return this.serviceRef.toMap();
    }

    public Collection<ResourceRef> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new KeyedCollection<String,ResourceRef>();
        }
        return this.resourceRef;
    }

    public Map<String,ResourceRef> getResourceRefMap() {
        if (resourceRef == null) {
            resourceRef = new KeyedCollection<String,ResourceRef>();
        }
        return this.resourceRef.toMap();
    }

    public Collection<ResourceEnvRef> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new KeyedCollection<String,ResourceEnvRef>();
        }
        return this.resourceEnvRef;
    }

    public Map<String,ResourceEnvRef> getResourceEnvRefMap() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new KeyedCollection<String,ResourceEnvRef>();
        }
        return this.resourceEnvRef.toMap();
    }

    public Collection<MessageDestinationRef> getMessageDestinationRef() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new KeyedCollection<String,MessageDestinationRef>();
        }
        return this.messageDestinationRef;
    }

    public Map<String,MessageDestinationRef> getMessageDestinationRefMap() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new KeyedCollection<String,MessageDestinationRef>();
        }
        return this.messageDestinationRef.toMap();
    }

    public Collection<PersistenceContextRef> getPersistenceContextRef() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new KeyedCollection<String,PersistenceContextRef>();
        }
        return this.persistenceContextRef;
    }

    public Map<String,PersistenceContextRef> getPersistenceContextRefMap() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new KeyedCollection<String,PersistenceContextRef>();
        }
        return this.persistenceContextRef.toMap();
    }

    public Collection<PersistenceUnitRef> getPersistenceUnitRef() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new KeyedCollection<String,PersistenceUnitRef>();
        }
        return this.persistenceUnitRef;
    }

    public Map<String,PersistenceUnitRef> getPersistenceUnitRefMap() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new KeyedCollection<String,PersistenceUnitRef>();
        }
        return this.persistenceUnitRef.toMap();
    }

    public Collection<MessageDestination> getMessageDestination() {
        if (messageDestination == null) {
            messageDestination = new KeyedCollection<String,MessageDestination>();
        }
        return this.messageDestination;
    }

    public Collection<DataSource> getDataSource() {
        if (dataSource == null) {
            dataSource = new KeyedCollection<String,DataSource>();
        }
        return this.dataSource;
    }

    public Map<String,DataSource> getDataSourceMap() {
        if (dataSource == null) {
            dataSource = new KeyedCollection<String,DataSource>();
        }
        return this.dataSource.toMap();
    }

    public java.lang.String getVersion() {
        if (version == null) {
            return "6";
        } else {
            return version;
        }
    }

    public void setVersion(java.lang.String value) {
        this.version = value;
    }

    public java.lang.String getId() {
        return id;
    }

    public void setId(java.lang.String value) {
        this.id = value;
    }

}
