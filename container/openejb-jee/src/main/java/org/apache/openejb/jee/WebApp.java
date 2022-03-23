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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * web-common_3_0.xsd
 *
 * <p>Java class for web-appType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="web-appType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="module-name" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}web-commonType"/&gt;
 *         &lt;element name="absolute-ordering" type="{http://java.sun.com/xml/ns/javaee}absoluteOrderingType"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attGroup ref="{http://java.sun.com/xml/ns/javaee}web-common-attributes"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */

@XmlRootElement(name = "web-app")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "web-appType", propOrder = {
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
    "defaultContextPath",
    "sessionConfig",
    "mimeMapping",
    "welcomeFileList",
    "errorPage",
    //In web-app-2.3.dtd
    "taglib",
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
    "absoluteOrdering",
    "dataSource",
    "jmsConnectionFactories",
    "jmsDestinations",
    "moduleName"

})
public class WebApp implements WebCommon, Lifecycle, NamedModule {
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
    //in web-app-2.3.dtd, not in any schema
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
    @XmlElement(name = "data-source", required = true)
    protected KeyedCollection<String, DataSource> dataSource;
    @XmlElement(name = "jms-connection-factory", required = true)
    protected KeyedCollection<String, JMSConnectionFactory> jmsConnectionFactories;
    @XmlElement(name = "jms-destination")
    protected KeyedCollection<String, JMSDestination> jmsDestinations;
    @XmlElement(name = "post-construct", required = true)
    protected List<LifecycleCallback> postConstruct;
    @XmlElement(name = "pre-destroy", required = true)
    protected List<LifecycleCallback> preDestroy;

    @XmlElement(name = "message-destination", required = true)
    protected List<MessageDestination> messageDestination;

    @XmlElement(name = "module-name")
    protected String moduleName;
    @XmlElement(name = "default-context-path")
    protected String defaultContextPath;
    @XmlElement(name = "absolute-ordering")
    protected AbsoluteOrdering absoluteOrdering;


    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlAttribute(name = "metadata-complete")
    protected Boolean metadataComplete;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String version = "3.0";

    @Override
    public String getJndiConsumerName() {
        return contextRoot;
    }

    @Override
    public String getContextRoot() {
        return contextRoot;
    }

    @Override
    public void setContextRoot(final String contextRoot) {
        this.contextRoot = contextRoot;
    }

    @Override
    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    @Override
    public void setDescriptions(final Text[] text) {
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
    public void setDisplayNames(final Text[] text) {
        displayName.set(text);
    }

    @Override
    public String getDisplayName() {
        return displayName.get();
    }

    public String getDefaultContextPath() {
        return defaultContextPath;
    }

    public void setDefaultContextPath(final String defaultContextPath) {
        this.defaultContextPath = defaultContextPath;
    }

    @Override
    public Collection<Icon> getIcons() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon;
    }

    @Override
    public Map<String, Icon> getIconMap() {
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

    public void addPostConstruct(final String method) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<LifecycleCallback> getPostConstruct() {
        if (postConstruct == null) {
            postConstruct = new ArrayList<LifecycleCallback>();
        }
        return this.postConstruct;
    }

    public void addPreDestroy(final String method) {
        throw new UnsupportedOperationException();
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
    public void setId(final String value) {
        this.id = value;
    }

    @Override
    public Boolean isMetadataComplete() {
        return metadataComplete != null && metadataComplete;
    }

    @Override
    public void setMetadataComplete(final Boolean value) {
        this.metadataComplete = value;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(final String value) {
        this.version = value;
    }

    public Collection<JMSConnectionFactory> getJMSConnectionFactory() {
        if (jmsConnectionFactories == null) {
            jmsConnectionFactories = new KeyedCollection<>();
        }
        return this.jmsConnectionFactories;
    }

    public Map<String, JMSConnectionFactory> getJMSConnectionFactoryMap() {
        if (jmsConnectionFactories == null) {
            jmsConnectionFactories = new KeyedCollection<>();
        }
        return this.jmsConnectionFactories.toMap();
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

    public AbsoluteOrdering getAbsoluteOrdering() {
        return absoluteOrdering;
    }

    public void setAbsoluteOrdering(final AbsoluteOrdering absoluteOrdering) {
        this.absoluteOrdering = absoluteOrdering;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    //compatibility with web-app-2.3.dtd
    @XmlElement(name = "taglib")
    public Taglib getTaglib() {
        return null;
    }

    public void setTaglib(final Taglib taglib) {
        final List<JspConfig> jspConfigs = getJspConfig();
        if (jspConfigs.isEmpty()) {
            jspConfigs.add(new JspConfig());
        }
        jspConfigs.get(0).getTaglib().add(taglib);
    }

    public Map<String, String> contextParamsAsMap() {
        final Map<String, String> map = new HashMap<String, String>();
        if (contextParam != null) {
            for (final ParamValue pv : contextParam) {
                map.put(pv.getParamName(), pv.getParamValue());
            }
        }
        return map;
    }

    public List<String> getServletMappings(final String servletName) {
        if (servletName == null || servletMapping == null || servletMapping.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> mappings = new ArrayList<>();
        for (final ServletMapping mapping : servletMapping) {
            if (servletName.equals(mapping.getServletName())) {
                mappings.addAll(mapping.getUrlPattern());
            }
        }
        return mappings;
    }

    public List<String> getFilterMappings(final String filterName) {
        if (filterName == null || filterMapping == null || filterMapping.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> mappings = new ArrayList<>();
        for (final FilterMapping mapping : filterMapping) {
            if (filterName.equals(mapping.getFilterName())) {
                mappings.addAll(mapping.getUrlPattern());
            }
        }
        return mappings;
    }

    private Servlet findServlet(final String name) {
        for (final Servlet s : getServlet()) {
            if (name.equals(s.getServletName())) {
                return s;
            }
        }
        return null;
    }

    public WebApp addServlet(final String name, final String clazz, final String... mappings) {
        final Servlet servletToAdd = new Servlet();
        servletToAdd.setServletName(name);
        servletToAdd.setServletClass(clazz);

        if (mappings != null && mappings.length > 0) {
            final ServletMapping sm = new ServletMapping();
            sm.setServletName(name);

            for (final String mapping : mappings) {
                if (servletMapping == null) {
                    servletMapping = new ArrayList<ServletMapping>();
                }

                sm.getUrlPattern().add(mapping);
            }
            servletMapping.add(sm);
        }

        getServlet().add(servletToAdd);

        return this;
    }

    public WebApp addServletMapping(final String servletName, final String mapping) {
        for (final ServletMapping s : getServletMapping()) {
            if (servletName.equals(s.getServletName())) {
                s.getUrlPattern().add(mapping);
                return this;
            }
        }

        final ServletMapping sm = new ServletMapping();
        sm.setServletName(servletName);
        sm.getUrlPattern().add(mapping);
        getServletMapping().add(sm);
        return this;
    }

    public WebApp addInitParam(final String servletName, final String name, final String value) {
        final ParamValue paramValue = new ParamValue();
        paramValue.setParamName(name);
        paramValue.setParamValue(value);

        findServlet(servletName).getInitParam().add(paramValue);

        return this;
    }

    public WebApp addFilter(final String name, final String clazz, final String... mappings) {
        final Filter newFilter = new Filter();
        newFilter.setFilterName(name);
        newFilter.setFilterClass(clazz);

        if (mappings != null && mappings.length > 0) {
            final FilterMapping sm = new FilterMapping();
            sm.setFilterName(name);

            for (final String mapping : mappings) {
                if (filterMapping == null) {
                    filterMapping = new ArrayList<FilterMapping>();
                }

                sm.getUrlPattern().add(mapping);
            }
            filterMapping.add(sm);
        }

        getFilter().add(newFilter);

        return this;
    }

    public WebApp addFilterInitParam(final String filterName, final String name, final String value) {
        final ParamValue paramValue = new ParamValue();
        paramValue.setParamName(name);
        paramValue.setParamValue(value);

        findFilter(filterName).getInitParam().add(paramValue);

        return this;
    }

    private Filter findFilter(final String filterName) {
        for (final Filter s : getFilter()) {
            if (filterName.equals(s.getFilterName())) {
                return s;
            }
        }
        return null;
    }

    public WebApp contextRoot(final String root) {
        setContextRoot(root);
        return this;
    }

    public WebApp defaultContextPath(final String path) {
        setDefaultContextPath(path);
        return this;
    }

    public WebApp addListener(final String classname) {
        final Listener l = new Listener();
        l.setListenerClass(classname);
        getListener().add(l);
        return this;
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
}
