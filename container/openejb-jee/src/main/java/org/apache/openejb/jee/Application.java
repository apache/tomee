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
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * application_6.xsd
 *
 * <p>Java class for applicationType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="applicationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="application-name" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/&gt;
 *         &lt;element name="initialize-in-order" type="{http://java.sun.com/xml/ns/javaee}generic-booleanType" minOccurs="0"/&gt;
 *         &lt;element name="module" type="{http://java.sun.com/xml/ns/javaee}moduleType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="security-role" type="{http://java.sun.com/xml/ns/javaee}security-roleType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="library-directory" type="{http://java.sun.com/xml/ns/javaee}pathType" minOccurs="0"/&gt;
 *         &lt;element name="env-entry" type="{http://java.sun.com/xml/ns/javaee}env-entryType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="ejb-ref" type="{http://java.sun.com/xml/ns/javaee}ejb-refType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="ejb-local-ref" type="{http://java.sun.com/xml/ns/javaee}ejb-local-refType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}service-refGroup"/&gt;
 *         &lt;element name="resource-ref" type="{http://java.sun.com/xml/ns/javaee}resource-refType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="resource-env-ref" type="{http://java.sun.com/xml/ns/javaee}resource-env-refType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="message-destination-ref" type="{http://java.sun.com/xml/ns/javaee}message-destination-refType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="persistence-context-ref" type="{http://java.sun.com/xml/ns/javaee}persistence-context-refType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="persistence-unit-ref" type="{http://java.sun.com/xml/ns/javaee}persistence-unit-refType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="message-destination" type="{http://java.sun.com/xml/ns/javaee}message-destinationType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="data-source" type="{http://java.sun.com/xml/ns/javaee}data-sourceType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="version" use="required" type="{http://java.sun.com/xml/ns/javaee}dewey-versionType" fixed="6" /&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
    "dataSource",
    "jmsConnectionFactories",
    "jmsDestinations"
})
public class Application implements JndiConsumer, NamedModule {

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
    protected KeyedCollection<String, EnvEntry> envEntry;
    @XmlElement(name = "ejb-ref", required = true)
    protected KeyedCollection<String, EjbRef> ejbRef;
    @XmlElement(name = "ejb-local-ref", required = true)
    protected KeyedCollection<String, EjbLocalRef> ejbLocalRef;
    @XmlElement(name = "service-ref", required = true)
    protected KeyedCollection<String, ServiceRef> serviceRef;
    @XmlElement(name = "resource-ref", required = true)
    protected KeyedCollection<String, ResourceRef> resourceRef;
    @XmlElement(name = "resource-env-ref", required = true)
    protected KeyedCollection<String, ResourceEnvRef> resourceEnvRef;
    @XmlElement(name = "message-destination-ref", required = true)
    protected KeyedCollection<String, MessageDestinationRef> messageDestinationRef;
    @XmlElement(name = "persistence-context-ref", required = true)
    protected KeyedCollection<String, PersistenceContextRef> persistenceContextRef;
    @XmlElement(name = "persistence-unit-ref", required = true)
    protected KeyedCollection<String, PersistenceUnitRef> persistenceUnitRef;
    @XmlElement(name = "message-destination", required = true)
    protected KeyedCollection<String, MessageDestination> messageDestination;
    @XmlElement(name = "data-source")
    protected KeyedCollection<String, DataSource> dataSource;
    @XmlElement(name = "context-service")
    protected KeyedCollection<String, ContextService> contextService;
    @XmlElement(name = "jms-connection-factory", required = true)
    protected KeyedCollection<String, JMSConnectionFactory> jmsConnectionFactories;
    @XmlElement(name = "jms-destination")
    protected KeyedCollection<String, JMSDestination> jmsDestinations;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected java.lang.String version;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    public Application() {
    }

    public Application(final String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(final String value) {
        this.applicationName = value;
    }

    @Override
    public String getModuleName() {
        return getApplicationName();
    }

    @Override
    public void setModuleName(final String name) {
        setApplicationName(name);
    }

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(final Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    @XmlElement(name = "display-name", required = true)
    public Text[] getDisplayNames() {
        return displayName.toArray();
    }

    public void setDisplayNames(final Text[] text) {
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

    public Map<String, Icon> getIconMap() {
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

    public void setInitializeInOrder(final Boolean value) {
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

    public void setLibraryDirectory(final String value) {
        this.libraryDirectory = value;
    }

    @Override
    public String getJndiConsumerName() {
        return applicationName;
    }

    public Collection<EnvEntry> getEnvEntry() {
        if (envEntry == null) {
            envEntry = new KeyedCollection<String, EnvEntry>();
        }
        return this.envEntry;
    }

    public Map<String, EnvEntry> getEnvEntryMap() {
        if (envEntry == null) {
            envEntry = new KeyedCollection<String, EnvEntry>();
        }
        return this.envEntry.toMap();
    }

    public Collection<EjbRef> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new KeyedCollection<String, EjbRef>();
        }
        return this.ejbRef;
    }

    public Map<String, EjbRef> getEjbRefMap() {
        if (ejbRef == null) {
            ejbRef = new KeyedCollection<String, EjbRef>();
        }
        return this.ejbRef.toMap();
    }

    public Collection<EjbLocalRef> getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new KeyedCollection<String, EjbLocalRef>();
        }
        return this.ejbLocalRef;
    }

    public Map<String, EjbLocalRef> getEjbLocalRefMap() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new KeyedCollection<String, EjbLocalRef>();
        }
        return this.ejbLocalRef.toMap();
    }

    public Collection<ServiceRef> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new KeyedCollection<String, ServiceRef>();
        }
        return this.serviceRef;
    }

    public Map<String, ServiceRef> getServiceRefMap() {
        if (serviceRef == null) {
            serviceRef = new KeyedCollection<String, ServiceRef>();
        }
        return this.serviceRef.toMap();
    }

    public Collection<ResourceRef> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new KeyedCollection<String, ResourceRef>();
        }
        return this.resourceRef;
    }

    public Map<String, ResourceRef> getResourceRefMap() {
        if (resourceRef == null) {
            resourceRef = new KeyedCollection<String, ResourceRef>();
        }
        return this.resourceRef.toMap();
    }

    public Collection<ResourceEnvRef> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new KeyedCollection<String, ResourceEnvRef>();
        }
        return this.resourceEnvRef;
    }

    public Map<String, ResourceEnvRef> getResourceEnvRefMap() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new KeyedCollection<String, ResourceEnvRef>();
        }
        return this.resourceEnvRef.toMap();
    }

    public Collection<MessageDestinationRef> getMessageDestinationRef() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new KeyedCollection<String, MessageDestinationRef>();
        }
        return this.messageDestinationRef;
    }

    public Map<String, MessageDestinationRef> getMessageDestinationRefMap() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new KeyedCollection<String, MessageDestinationRef>();
        }
        return this.messageDestinationRef.toMap();
    }

    public Collection<PersistenceContextRef> getPersistenceContextRef() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new KeyedCollection<String, PersistenceContextRef>();
        }
        return this.persistenceContextRef;
    }

    public Map<String, PersistenceContextRef> getPersistenceContextRefMap() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new KeyedCollection<String, PersistenceContextRef>();
        }
        return this.persistenceContextRef.toMap();
    }

    public Collection<PersistenceUnitRef> getPersistenceUnitRef() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new KeyedCollection<String, PersistenceUnitRef>();
        }
        return this.persistenceUnitRef;
    }

    public Map<String, PersistenceUnitRef> getPersistenceUnitRefMap() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new KeyedCollection<String, PersistenceUnitRef>();
        }
        return this.persistenceUnitRef.toMap();
    }

    public Collection<MessageDestination> getMessageDestination() {
        if (messageDestination == null) {
            messageDestination = new KeyedCollection<String, MessageDestination>();
        }
        return this.messageDestination;
    }

    public Collection<DataSource> getDataSource() {
        if (dataSource == null) {
            dataSource = new KeyedCollection<String, DataSource>();
        }
        return this.dataSource;
    }

    public Map<String, DataSource> getDataSourceMap() {
        if (dataSource == null) {
            dataSource = new KeyedCollection<String, DataSource>();
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

    public void setVersion(final java.lang.String value) {
        this.version = value;
    }

    public java.lang.String getId() {
        return id;
    }

    public void setId(final java.lang.String value) {
        this.id = value;
    }

    @Override
    public Collection<JMSConnectionFactory> getJMSConnectionFactories() {
        return jmsConnectionFactories == null ? (jmsConnectionFactories = new KeyedCollection<>()) : jmsConnectionFactories;
    }

    @Override
    public Map<String, JMSConnectionFactory> getJMSConnectionFactoriesMap() {
        return KeyedCollection.class.cast(getJMSConnectionFactories()).toMap();
    }

    @Override
    public Collection<JMSDestination> getJMSDestination() {
        return jmsDestinations == null ? (jmsDestinations = new KeyedCollection<>()) : jmsDestinations;
    }

    @Override
    public Map<String, JMSDestination> getJMSDestinationMap() {
        return KeyedCollection.class.cast(getJMSDestination()).toMap();
    }

    @Override
    public Map<String, ContextService> getContextServiceMap() {
        if (contextService == null) {
            contextService = new KeyedCollection<String, ContextService>();
        }
        return this.contextService.toMap();
    }
}
