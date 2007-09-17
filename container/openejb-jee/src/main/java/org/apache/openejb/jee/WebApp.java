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
import java.util.List;

@XmlRootElement(name = "web-app")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "web-appType", propOrder = {
        "descriptions",
        "displayNames",
        "icons",
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
        "messageDestination"
})
public class WebApp implements JndiConsumer {
    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlTransient
    protected LocalList<String, Icon> icon = new LocalList<String, Icon>(Icon.class);

    protected List<EmptyType> distributable;
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
    @XmlElement(name = "error-page")
    protected List<ErrorPage> errorPage;
    @XmlElement(name = "jsp-config")
    protected List<JspConfig> jspConfig;
    @XmlElement(name = "security-constraint")
    protected List<SecurityConstraint> securityConstraint;
    @XmlElement(name = "login-config")
    protected List<LoginConfigType> loginConfig;
    @XmlElement(name = "security-role")
    protected List<SecurityRole> securityRole;
    @XmlElement(name = "locale-encoding-mapping-list")
    protected List<LocaleEncodingMappingList> localeEncodingMappingList;

    @XmlElement(name = "env-entry", required = true)
    protected List<EnvEntry> envEntry;
    @XmlElement(name = "ejb-ref", required = true)
    protected List<EjbRef> ejbRef;
    @XmlElement(name = "ejb-local-ref", required = true)
    protected List<EjbLocalRef> ejbLocalRef;
    @XmlElement(name = "service-ref", required = true)
    protected List<ServiceRef> serviceRef;
    @XmlElement(name = "resource-ref", required = true)
    protected List<ResourceRef> resourceRef;
    @XmlElement(name = "resource-env-ref", required = true)
    protected List<ResourceEnvRef> resourceEnvRef;
    @XmlElement(name = "message-destination-ref", required = true)
    protected List<MessageDestinationRef> messageDestinationRef;
    @XmlElement(name = "persistence-context-ref", required = true)
    protected List<PersistenceContextRef> persistenceContextRef;
    @XmlElement(name = "persistence-unit-ref", required = true)
    protected List<PersistenceUnitRef> persistenceUnitRef;
    @XmlElement(name = "post-construct", required = true)
    protected List<LifecycleCallback> postConstruct;
    @XmlElement(name = "pre-destroy", required = true)
    protected List<LifecycleCallback> preDestroy;

    @XmlElement(name = "message-destination", required = true)
    protected List<MessageDestination> messageDestination;


    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlAttribute(name = "metadata-complete")
    protected Boolean metadataComplete;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String version;

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

    @XmlElement(name = "icon", required = true)
    public Icon[] getIcons() {
        return icon.toArray();
    }

    public void setIcons(Icon[] text) {
        icon.set(text);
    }

    public Icon getIcon() {
        return icon.getLocal();
    }

    public List<EmptyType> getDistributable() {
        if (distributable == null) {
            distributable = new ArrayList<EmptyType>();
        }
        return this.distributable;
    }

    public List<ParamValue> getContextParam() {
        if (contextParam == null) {
            contextParam = new ArrayList<ParamValue>();
        }
        return this.contextParam;
    }

    public List<Filter> getFilter() {
        if (filter == null) {
            filter = new ArrayList<Filter>();
        }
        return this.filter;
    }

    public List<FilterMapping> getFilterMapping() {
        if (filterMapping == null) {
            filterMapping = new ArrayList<FilterMapping>();
        }
        return this.filterMapping;
    }

    public List<Listener> getListener() {
        if (listener == null) {
            listener = new ArrayList<Listener>();
        }
        return this.listener;
    }

    public List<Servlet> getServlet() {
        if (servlet == null) {
            servlet = new ArrayList<Servlet>();
        }
        return this.servlet;
    }

    public List<ServletMapping> getServletMapping() {
        if (servletMapping == null) {
            servletMapping = new ArrayList<ServletMapping>();
        }
        return this.servletMapping;
    }

    public List<SessionConfig> getSessionConfig() {
        if (sessionConfig == null) {
            sessionConfig = new ArrayList<SessionConfig>();
        }
        return this.sessionConfig;
    }

    public List<MimeMapping> getMimeMapping() {
        if (mimeMapping == null) {
            mimeMapping = new ArrayList<MimeMapping>();
        }
        return this.mimeMapping;
    }

    public List<WelcomeFileList> getWelcomeFileList() {
        if (welcomeFileList == null) {
            welcomeFileList = new ArrayList<WelcomeFileList>();
        }
        return this.welcomeFileList;
    }

    public List<ErrorPage> getErrorPage() {
        if (errorPage == null) {
            errorPage = new ArrayList<ErrorPage>();
        }
        return this.errorPage;
    }

    public List<JspConfig> getJspConfig() {
        if (jspConfig == null) {
            jspConfig = new ArrayList<JspConfig>();
        }
        return this.jspConfig;
    }

    public List<SecurityConstraint> getSecurityConstraint() {
        if (securityConstraint == null) {
            securityConstraint = new ArrayList<SecurityConstraint>();
        }
        return this.securityConstraint;
    }

    public List<LoginConfigType> getLoginConfig() {
        if (loginConfig == null) {
            loginConfig = new ArrayList<LoginConfigType>();
        }
        return this.loginConfig;
    }

    public List<SecurityRole> getSecurityRole() {
        if (securityRole == null) {
            securityRole = new ArrayList<SecurityRole>();
        }
        return this.securityRole;
    }

    public List<LocaleEncodingMappingList> getLocaleEncodingMappingList() {
        if (localeEncodingMappingList == null) {
            localeEncodingMappingList = new ArrayList<LocaleEncodingMappingList>();
        }
        return this.localeEncodingMappingList;
    }

    public List<EnvEntry> getEnvEntry() {
        if (envEntry == null) {
            envEntry = new ArrayList<EnvEntry>();
        }
        return this.envEntry;
    }

    public List<EjbRef> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new ArrayList<EjbRef>();
        }
        return this.ejbRef;
    }

    public List<EjbLocalRef> getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new ArrayList<EjbLocalRef>();
        }
        return this.ejbLocalRef;
    }

    public List<ServiceRef> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new ArrayList<ServiceRef>();
        }
        return this.serviceRef;
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

    public List<MessageDestinationRef> getMessageDestinationRef() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new ArrayList<MessageDestinationRef>();
        }
        return this.messageDestinationRef;
    }

    public List<PersistenceContextRef> getPersistenceContextRef() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new ArrayList<PersistenceContextRef>();
        }
        return this.persistenceContextRef;
    }

    public List<PersistenceUnitRef> getPersistenceUnitRef() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new ArrayList<PersistenceUnitRef>();
        }
        return this.persistenceUnitRef;
    }

    public List<LifecycleCallback> getPostConstruct() {
        if (postConstruct == null) {
            postConstruct = new ArrayList<LifecycleCallback>();
        }
        return this.postConstruct;
    }

    public List<LifecycleCallback> getPreDestroy() {
        if (preDestroy == null) {
            preDestroy = new ArrayList<LifecycleCallback>();
        }
        return this.preDestroy;
    }

    public List<MessageDestination> getMessageDestination() {
        if (messageDestination == null) {
            messageDestination = new ArrayList<MessageDestination>();
        }
        return this.messageDestination;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public Boolean isMetadataComplete() {
        return metadataComplete != null && metadataComplete;
    }

    public void setMetadataComplete(Boolean value) {
        this.metadataComplete = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String value) {
        this.version = value;
    }

}
