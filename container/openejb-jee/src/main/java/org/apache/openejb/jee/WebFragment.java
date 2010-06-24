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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * web-common_3_0.xsd
 * 
 * <p>Java class for web-fragmentType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="web-fragmentType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="name" type="{http://java.sun.com/xml/ns/javaee}java-identifierType"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}web-commonType"/>
 *         &lt;element name="ordering" type="{http://java.sun.com/xml/ns/javaee}orderingType"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://java.sun.com/xml/ns/javaee}web-common-attributes"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */

@XmlRootElement(name = "web-fragment")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "web-fragmentType", propOrder = {
        "descriptions",
        "displayNames",
        "icon",
        "distributable",
        "contextParam",
        "filter",
        "filterMapping",
        "listener",
        "servlet",
        "servletMapping",
        "sessionConfig",
        "mimeMapping",
        "welcomeFileList",
        "errorPage",
        //TDOD not in schema?
//        "taglib",
        "jspConfig",
        "securityConstraint",
        "loginConfig",
        "securityRole",
        "localeEncodingMappingList",
        "envEntry",
        "ejbRef",
        "ejbLocalRef",
        "serviceRef",
        "resourceRef",
        "resourceEnvRef",
        "messageDestinationRef",
        "persistenceContextRef",
        "persistenceUnitRef",
        "postConstruct",
        "preDestroy",
        "messageDestination",
        "ordering",
        "dataSource",
        "name"

})
public class WebFragment implements WebCommon {
    @XmlTransient
    private String contextRoot;

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();

    protected List<Empty> distributable;
    @XmlElement(name = "context-param")
    protected List<ParamValue> contextParam;
    protected List<Filter> filter;
    @XmlElement(name = "filter-mapping")
    protected List<FilterMapping> filterMapping;
    protected List<Listener> listener;
    protected List<Servlet> servlet;
    @XmlElement(name = "servlet-mapping")
    protected List<ServletMapping> servletMapping;
    @XmlElement(name = "session-config")
    protected List<SessionConfig> sessionConfig;
    @XmlElement(name = "mime-mapping")
    protected List<MimeMapping> mimeMapping;
    @XmlElement(name = "welcome-file-list")
    protected List<WelcomeFileList> welcomeFileList;
    //TODO not in schema
//    @XmlElement(name = "taglib")
//    protected List<Taglib> taglib;
    @XmlElement(name = "error-page")
    protected List<ErrorPage> errorPage;
    @XmlElement(name = "jsp-config")
    protected List<JspConfig> jspConfig;
    @XmlElement(name = "security-constraint")
    protected List<SecurityConstraint> securityConstraint;
    @XmlElement(name = "login-config")
    protected List<LoginConfig> loginConfig;
    @XmlElement(name = "security-role")
    protected List<SecurityRole> securityRole;
    @XmlElement(name = "locale-encoding-mapping-list")
    protected List<LocaleEncodingMappingList> localeEncodingMappingList;

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
    @XmlElement(name = "data-source", required = true)
    protected KeyedCollection<String,DataSource> dataSource;
    @XmlElement(name = "post-construct", required = true)
    protected List<LifecycleCallback> postConstruct;
    @XmlElement(name = "pre-destroy", required = true)
    protected List<LifecycleCallback> preDestroy;

    @XmlElement(name = "message-destination", required = true)
    protected List<MessageDestination> messageDestination;

    @XmlElement(name = "name")
    protected String name;
    @XmlElement(name = "ordering")
    protected Ordering ordering;


    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlAttribute(name = "metadata-complete")
    protected Boolean metadataComplete;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String version;




    @Override
    public String getJndiConsumerName() {
        return contextRoot;
    }

    @Override
    public String getContextRoot() {
        return contextRoot;
    }

    @Override
    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    @Override
    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    @Override
    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    @Override
    public String getDescription() {
        return description.get();
    }

    @Override
    @XmlElement(name = "display-name", required = true)
    public Text[] getDisplayNames() {
        return displayName.toArray();
    }

    @Override
    public void setDisplayNames(Text[] text) {
        displayName.set(text);
    }

    @Override
    public String getDisplayName() {
        return displayName.get();
    }

    @Override
    public Collection<Icon> getIcons() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon;
    }

    @Override
    public Map<String,Icon> getIconMap() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon.toMap();
    }

    @Override
    public Icon getIcon() {
        return icon.getLocal();
    }

    @Override
    public List<Empty> getDistributable() {
        if (distributable == null) {
            distributable = new ArrayList<Empty>();
        }
        return this.distributable;
    }

    @Override
    public List<ParamValue> getContextParam() {
        if (contextParam == null) {
            contextParam = new ArrayList<ParamValue>();
        }
        return this.contextParam;
    }

    @Override
    public List<Filter> getFilter() {
        if (filter == null) {
            filter = new ArrayList<Filter>();
        }
        return this.filter;
    }

    @Override
    public List<FilterMapping> getFilterMapping() {
        if (filterMapping == null) {
            filterMapping = new ArrayList<FilterMapping>();
        }
        return this.filterMapping;
    }

    @Override
    public List<Listener> getListener() {
        if (listener == null) {
            listener = new ArrayList<Listener>();
        }
        return this.listener;
    }

    @Override
    public List<Servlet> getServlet() {
        if (servlet == null) {
            servlet = new ArrayList<Servlet>();
        }
        return this.servlet;
    }

    @Override
    public List<ServletMapping> getServletMapping() {
        if (servletMapping == null) {
            servletMapping = new ArrayList<ServletMapping>();
        }
        return this.servletMapping;
    }

    @Override
    public List<SessionConfig> getSessionConfig() {
        if (sessionConfig == null) {
            sessionConfig = new ArrayList<SessionConfig>();
        }
        return this.sessionConfig;
    }

    @Override
    public List<MimeMapping> getMimeMapping() {
        if (mimeMapping == null) {
            mimeMapping = new ArrayList<MimeMapping>();
        }
        return this.mimeMapping;
    }

    @Override
    public List<WelcomeFileList> getWelcomeFileList() {
        if (welcomeFileList == null) {
            welcomeFileList = new ArrayList<WelcomeFileList>();
        }
        return this.welcomeFileList;
    }

    @Override
    public List<ErrorPage> getErrorPage() {
        if (errorPage == null) {
            errorPage = new ArrayList<ErrorPage>();
        }
        return this.errorPage;
    }

    @Override
    public List<JspConfig> getJspConfig() {
        if (jspConfig == null) {
            jspConfig = new ArrayList<JspConfig>();
        }
        return this.jspConfig;
    }

    @Override
    public List<SecurityConstraint> getSecurityConstraint() {
        if (securityConstraint == null) {
            securityConstraint = new ArrayList<SecurityConstraint>();
        }
        return this.securityConstraint;
    }

    @Override
    public List<LoginConfig> getLoginConfig() {
        if (loginConfig == null) {
            loginConfig = new ArrayList<LoginConfig>();
        }
        return this.loginConfig;
    }

    @Override
    public List<SecurityRole> getSecurityRole() {
        if (securityRole == null) {
            securityRole = new ArrayList<SecurityRole>();
        }
        return this.securityRole;
    }

    @Override
    public List<LocaleEncodingMappingList> getLocaleEncodingMappingList() {
        if (localeEncodingMappingList == null) {
            localeEncodingMappingList = new ArrayList<LocaleEncodingMappingList>();
        }
        return this.localeEncodingMappingList;
    }

    @Override
    public Collection<EnvEntry> getEnvEntry() {
        if (envEntry == null) {
            envEntry = new KeyedCollection<String,EnvEntry>();
        }
        return this.envEntry;
    }

    @Override
    public Map<String,EnvEntry> getEnvEntryMap() {
        if (envEntry == null) {
            envEntry = new KeyedCollection<String,EnvEntry>();
        }
        return this.envEntry.toMap();
    }

    @Override
    public Collection<EjbRef> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new KeyedCollection<String,EjbRef>();
        }
        return this.ejbRef;
    }

    @Override
    public Map<String,EjbRef> getEjbRefMap() {
        if (ejbRef == null) {
            ejbRef = new KeyedCollection<String,EjbRef>();
        }
        return this.ejbRef.toMap();
    }

    @Override
    public Collection<EjbLocalRef> getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new KeyedCollection<String,EjbLocalRef>();
        }
        return this.ejbLocalRef;
    }

    @Override
    public Map<String,EjbLocalRef> getEjbLocalRefMap() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new KeyedCollection<String,EjbLocalRef>();
        }
        return this.ejbLocalRef.toMap();
    }

    @Override
    public Collection<ServiceRef> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new KeyedCollection<String,ServiceRef>();
        }
        return this.serviceRef;
    }

    @Override
    public Map<String,ServiceRef> getServiceRefMap() {
        if (serviceRef == null) {
            serviceRef = new KeyedCollection<String,ServiceRef>();
        }
        return this.serviceRef.toMap();
    }

    @Override
    public Collection<ResourceRef> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new KeyedCollection<String,ResourceRef>();
        }
        return this.resourceRef;
    }

    @Override
    public Map<String,ResourceRef> getResourceRefMap() {
        if (resourceRef == null) {
            resourceRef = new KeyedCollection<String,ResourceRef>();
        }
        return this.resourceRef.toMap();
    }

    @Override
    public Collection<ResourceEnvRef> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new KeyedCollection<String,ResourceEnvRef>();
        }
        return this.resourceEnvRef;
    }

    @Override
    public Map<String,ResourceEnvRef> getResourceEnvRefMap() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new KeyedCollection<String,ResourceEnvRef>();
        }
        return this.resourceEnvRef.toMap();
    }

    @Override
    public Collection<MessageDestinationRef> getMessageDestinationRef() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new KeyedCollection<String,MessageDestinationRef>();
        }
        return this.messageDestinationRef;
    }

    @Override
    public Map<String,MessageDestinationRef> getMessageDestinationRefMap() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new KeyedCollection<String,MessageDestinationRef>();
        }
        return this.messageDestinationRef.toMap();
    }

    @Override
    public Collection<PersistenceContextRef> getPersistenceContextRef() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new KeyedCollection<String,PersistenceContextRef>();
        }
        return this.persistenceContextRef;
    }

    @Override
    public Map<String,PersistenceContextRef> getPersistenceContextRefMap() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new KeyedCollection<String,PersistenceContextRef>();
        }
        return this.persistenceContextRef.toMap();
    }

    @Override
    public Collection<PersistenceUnitRef> getPersistenceUnitRef() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new KeyedCollection<String,PersistenceUnitRef>();
        }
        return this.persistenceUnitRef;
    }

    @Override
    public Map<String,PersistenceUnitRef> getPersistenceUnitRefMap() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new KeyedCollection<String,PersistenceUnitRef>();
        }
        return this.persistenceUnitRef.toMap();
    }

    @Override
    public List<LifecycleCallback> getPostConstruct() {
        if (postConstruct == null) {
            postConstruct = new ArrayList<LifecycleCallback>();
        }
        return this.postConstruct;
    }

    @Override
    public List<LifecycleCallback> getPreDestroy() {
        if (preDestroy == null) {
            preDestroy = new ArrayList<LifecycleCallback>();
        }
        return this.preDestroy;
    }

    @Override
    public List<MessageDestination> getMessageDestination() {
        if (messageDestination == null) {
            messageDestination = new ArrayList<MessageDestination>();
        }
        return this.messageDestination;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String value) {
        this.id = value;
    }

    @Override
    public Boolean isMetadataComplete() {
        return metadataComplete != null && metadataComplete;
    }

    @Override
    public void setMetadataComplete(Boolean value) {
        this.metadataComplete = value;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String value) {
        this.version = value;
    }

    @Override
    public Collection<DataSource> getDataSource() {
        if (dataSource == null) {
            dataSource = new KeyedCollection<String,DataSource>();
        }
        return this.dataSource;
    }

    @Override
    public Map<String,DataSource> getDataSourceMap() {
        if (dataSource == null) {
            dataSource = new KeyedCollection<String,DataSource>();
        }
        return this.dataSource.toMap();
    }

    public Ordering getOrdering() {
        return ordering;
    }

    public void setOrdering(Ordering ordering) {
        this.ordering = ordering;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}