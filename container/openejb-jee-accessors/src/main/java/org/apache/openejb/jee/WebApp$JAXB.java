/*
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
package org.apache.openejb.jee;

import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import static org.apache.openejb.jee.AbsoluteOrdering$JAXB.readAbsoluteOrdering;
import static org.apache.openejb.jee.AbsoluteOrdering$JAXB.writeAbsoluteOrdering;
import static org.apache.openejb.jee.DataSource$JAXB.readDataSource;
import static org.apache.openejb.jee.DataSource$JAXB.writeDataSource;
import static org.apache.openejb.jee.EjbLocalRef$JAXB.readEjbLocalRef;
import static org.apache.openejb.jee.EjbLocalRef$JAXB.writeEjbLocalRef;
import static org.apache.openejb.jee.EjbRef$JAXB.readEjbRef;
import static org.apache.openejb.jee.EjbRef$JAXB.writeEjbRef;
import static org.apache.openejb.jee.Empty$JAXB.readEmpty;
import static org.apache.openejb.jee.Empty$JAXB.writeEmpty;
import static org.apache.openejb.jee.EnvEntry$JAXB.readEnvEntry;
import static org.apache.openejb.jee.EnvEntry$JAXB.writeEnvEntry;
import static org.apache.openejb.jee.ErrorPage$JAXB.readErrorPage;
import static org.apache.openejb.jee.ErrorPage$JAXB.writeErrorPage;
import static org.apache.openejb.jee.Filter$JAXB.readFilter;
import static org.apache.openejb.jee.Filter$JAXB.writeFilter;
import static org.apache.openejb.jee.FilterMapping$JAXB.readFilterMapping;
import static org.apache.openejb.jee.FilterMapping$JAXB.writeFilterMapping;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.JMSConnectionFactory$JAXB.readJMSConnectionFactory;
import static org.apache.openejb.jee.JspConfig$JAXB.readJspConfig;
import static org.apache.openejb.jee.JspConfig$JAXB.writeJspConfig;
import static org.apache.openejb.jee.LifecycleCallback$JAXB.readLifecycleCallback;
import static org.apache.openejb.jee.LifecycleCallback$JAXB.writeLifecycleCallback;
import static org.apache.openejb.jee.Listener$JAXB.readListener;
import static org.apache.openejb.jee.Listener$JAXB.writeListener;
import static org.apache.openejb.jee.LocaleEncodingMappingList$JAXB.readLocaleEncodingMappingList;
import static org.apache.openejb.jee.LocaleEncodingMappingList$JAXB.writeLocaleEncodingMappingList;
import static org.apache.openejb.jee.LoginConfig$JAXB.readLoginConfig;
import static org.apache.openejb.jee.LoginConfig$JAXB.writeLoginConfig;
import static org.apache.openejb.jee.MessageDestination$JAXB.readMessageDestination;
import static org.apache.openejb.jee.MessageDestination$JAXB.writeMessageDestination;
import static org.apache.openejb.jee.MessageDestinationRef$JAXB.readMessageDestinationRef;
import static org.apache.openejb.jee.MessageDestinationRef$JAXB.writeMessageDestinationRef;
import static org.apache.openejb.jee.MimeMapping$JAXB.readMimeMapping;
import static org.apache.openejb.jee.MimeMapping$JAXB.writeMimeMapping;
import static org.apache.openejb.jee.ParamValue$JAXB.readParamValue;
import static org.apache.openejb.jee.ParamValue$JAXB.writeParamValue;
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.readPersistenceContextRef;
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.writePersistenceContextRef;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.readPersistenceUnitRef;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.writePersistenceUnitRef;
import static org.apache.openejb.jee.ResourceEnvRef$JAXB.readResourceEnvRef;
import static org.apache.openejb.jee.ResourceEnvRef$JAXB.writeResourceEnvRef;
import static org.apache.openejb.jee.ResourceRef$JAXB.readResourceRef;
import static org.apache.openejb.jee.ResourceRef$JAXB.writeResourceRef;
import static org.apache.openejb.jee.SecurityConstraint$JAXB.readSecurityConstraint;
import static org.apache.openejb.jee.SecurityConstraint$JAXB.writeSecurityConstraint;
import static org.apache.openejb.jee.SecurityRole$JAXB.readSecurityRole;
import static org.apache.openejb.jee.SecurityRole$JAXB.writeSecurityRole;
import static org.apache.openejb.jee.ServiceRef$JAXB.readServiceRef;
import static org.apache.openejb.jee.ServiceRef$JAXB.writeServiceRef;
import static org.apache.openejb.jee.Servlet$JAXB.readServlet;
import static org.apache.openejb.jee.Servlet$JAXB.writeServlet;
import static org.apache.openejb.jee.ServletMapping$JAXB.readServletMapping;
import static org.apache.openejb.jee.ServletMapping$JAXB.writeServletMapping;
import static org.apache.openejb.jee.SessionConfig$JAXB.readSessionConfig;
import static org.apache.openejb.jee.SessionConfig$JAXB.writeSessionConfig;
import static org.apache.openejb.jee.Taglib$JAXB.readTaglib;
import static org.apache.openejb.jee.Taglib$JAXB.writeTaglib;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;
import static org.apache.openejb.jee.WelcomeFileList$JAXB.readWelcomeFileList;
import static org.apache.openejb.jee.WelcomeFileList$JAXB.writeWelcomeFileList;

@SuppressWarnings({
    "StringEquality"
})
public class WebApp$JAXB
    extends JAXBObject<WebApp> {


    public WebApp$JAXB() {
        super(WebApp.class, new QName("http://java.sun.com/xml/ns/javaee".intern(), "web-app".intern()), new QName("http://java.sun.com/xml/ns/javaee".intern(), "web-appType".intern()), Text$JAXB.class, Icon$JAXB.class, Empty$JAXB.class, ParamValue$JAXB.class, Filter$JAXB.class, FilterMapping$JAXB.class, Listener$JAXB.class, Servlet$JAXB.class, ServletMapping$JAXB.class, SessionConfig$JAXB.class, MimeMapping$JAXB.class, WelcomeFileList$JAXB.class, ErrorPage$JAXB.class, Taglib$JAXB.class, JspConfig$JAXB.class, SecurityConstraint$JAXB.class, LoginConfig$JAXB.class, SecurityRole$JAXB.class, LocaleEncodingMappingList$JAXB.class, EnvEntry$JAXB.class, EjbRef$JAXB.class, EjbLocalRef$JAXB.class, ServiceRef$JAXB.class, ResourceRef$JAXB.class, ResourceEnvRef$JAXB.class, MessageDestinationRef$JAXB.class, PersistenceContextRef$JAXB.class, PersistenceUnitRef$JAXB.class, LifecycleCallback$JAXB.class, MessageDestination$JAXB.class, AbsoluteOrdering$JAXB.class, DataSource$JAXB.class);
    }

    public static WebApp readWebApp(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeWebApp(final XoXMLStreamWriter writer, final WebApp webApp, final RuntimeContext context)
        throws Exception {
        _write(writer, webApp, context);
    }

    public void write(final XoXMLStreamWriter writer, final WebApp webApp, final RuntimeContext context)
        throws Exception {
        _write(writer, webApp, context);
    }

    public final static WebApp _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final WebApp webApp = new WebApp();
        context.beforeUnmarshal(webApp, org.metatype.sxc.jaxb.LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<Empty> distributable = null;
        List<ParamValue> contextParam = null;
        List<Filter> filter = null;
        List<FilterMapping> filterMapping = null;
        List<Listener> listener = null;
        List<Servlet> servlet = null;
        List<ServletMapping> servletMapping = null;
        List<SessionConfig> sessionConfig = null;
        List<MimeMapping> mimeMapping = null;
        List<WelcomeFileList> welcomeFileList = null;
        List<ErrorPage> errorPage = null;
        List<JspConfig> jspConfig = null;
        List<SecurityConstraint> securityConstraint = null;
        List<LoginConfig> loginConfig = null;
        List<SecurityRole> securityRole = null;
        List<LocaleEncodingMappingList> localeEncodingMappingList = null;
        KeyedCollection<String, EnvEntry> envEntry = null;
        KeyedCollection<String, EjbRef> ejbRef = null;
        KeyedCollection<String, EjbLocalRef> ejbLocalRef = null;
        KeyedCollection<String, ServiceRef> serviceRef = null;
        KeyedCollection<String, ResourceRef> resourceRef = null;
        KeyedCollection<String, ResourceEnvRef> resourceEnvRef = null;
        KeyedCollection<String, MessageDestinationRef> messageDestinationRef = null;
        KeyedCollection<String, PersistenceContextRef> persistenceContextRef = null;
        KeyedCollection<String, PersistenceUnitRef> persistenceUnitRef = null;
        List<org.apache.openejb.jee.LifecycleCallback> postConstruct = null;
        List<org.apache.openejb.jee.LifecycleCallback> preDestroy = null;
        List<MessageDestination> messageDestination = null;
        KeyedCollection<String, DataSource> dataSource = null;
        KeyedCollection<String, JMSConnectionFactory> jmsConnectionFactories = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("web-appType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, WebApp.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, webApp);
                webApp.id = id;
            } else if (("metadata-complete" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: metadataComplete
                final Boolean metadataComplete = ("1".equals(attribute.getValue()) || "true".equals(attribute.getValue()));
                webApp.metadataComplete = metadataComplete;
            } else if (("version" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: version
                webApp.version = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"), new QName("", "metadata-complete"), new QName("", "version"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                final Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                final Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<Text>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                final Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = webApp.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("distributable" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: distributable
                final Empty distributableItem = readEmpty(elementReader, context);
                if (distributable == null) {
                    distributable = webApp.distributable;
                    if (distributable != null) {
                        distributable.clear();
                    } else {
                        distributable = new ArrayList<Empty>();
                    }
                }
                distributable.add(distributableItem);
            } else if (("context-param" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: contextParam
                final ParamValue contextParamItem = readParamValue(elementReader, context);
                if (contextParam == null) {
                    contextParam = webApp.contextParam;
                    if (contextParam != null) {
                        contextParam.clear();
                    } else {
                        contextParam = new ArrayList<ParamValue>();
                    }
                }
                contextParam.add(contextParamItem);
            } else if (("filter" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: filter
                final Filter filterItem = readFilter(elementReader, context);
                if (filter == null) {
                    filter = webApp.filter;
                    if (filter != null) {
                        filter.clear();
                    } else {
                        filter = new ArrayList<Filter>();
                    }
                }
                filter.add(filterItem);
            } else if (("filter-mapping" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: filterMapping
                final FilterMapping filterMappingItem = readFilterMapping(elementReader, context);
                if (filterMapping == null) {
                    filterMapping = webApp.filterMapping;
                    if (filterMapping != null) {
                        filterMapping.clear();
                    } else {
                        filterMapping = new ArrayList<FilterMapping>();
                    }
                }
                filterMapping.add(filterMappingItem);
            } else if (("listener" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: listener
                final Listener listenerItem = readListener(elementReader, context);
                if (listener == null) {
                    listener = webApp.listener;
                    if (listener != null) {
                        listener.clear();
                    } else {
                        listener = new ArrayList<Listener>();
                    }
                }
                listener.add(listenerItem);
            } else if (("servlet" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: servlet
                final Servlet servletItem = readServlet(elementReader, context);
                if (servlet == null) {
                    servlet = webApp.servlet;
                    if (servlet != null) {
                        servlet.clear();
                    } else {
                        servlet = new ArrayList<Servlet>();
                    }
                }
                servlet.add(servletItem);
            } else if (("servlet-mapping" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: servletMapping
                final ServletMapping servletMappingItem = readServletMapping(elementReader, context);
                if (servletMapping == null) {
                    servletMapping = webApp.servletMapping;
                    if (servletMapping != null) {
                        servletMapping.clear();
                    } else {
                        servletMapping = new ArrayList<ServletMapping>();
                    }
                }
                servletMapping.add(servletMappingItem);
            } else if (("session-config" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: sessionConfig
                final SessionConfig sessionConfigItem = readSessionConfig(elementReader, context);
                if (sessionConfig == null) {
                    sessionConfig = webApp.sessionConfig;
                    if (sessionConfig != null) {
                        sessionConfig.clear();
                    } else {
                        sessionConfig = new ArrayList<SessionConfig>();
                    }
                }
                sessionConfig.add(sessionConfigItem);
            } else if (("mime-mapping" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: mimeMapping
                final MimeMapping mimeMappingItem = readMimeMapping(elementReader, context);
                if (mimeMapping == null) {
                    mimeMapping = webApp.mimeMapping;
                    if (mimeMapping != null) {
                        mimeMapping.clear();
                    } else {
                        mimeMapping = new ArrayList<MimeMapping>();
                    }
                }
                mimeMapping.add(mimeMappingItem);
            } else if (("welcome-file-list" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: welcomeFileList
                final WelcomeFileList welcomeFileListItem = readWelcomeFileList(elementReader, context);
                if (welcomeFileList == null) {
                    welcomeFileList = webApp.welcomeFileList;
                    if (welcomeFileList != null) {
                        welcomeFileList.clear();
                    } else {
                        welcomeFileList = new ArrayList<WelcomeFileList>();
                    }
                }
                welcomeFileList.add(welcomeFileListItem);
            } else if (("error-page" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: errorPage
                final ErrorPage errorPageItem = readErrorPage(elementReader, context);
                if (errorPage == null) {
                    errorPage = webApp.errorPage;
                    if (errorPage != null) {
                        errorPage.clear();
                    } else {
                        errorPage = new ArrayList<ErrorPage>();
                    }
                }
                errorPage.add(errorPageItem);
            } else if (("taglib" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: taglib
                final Taglib taglib = readTaglib(elementReader, context);
                try {
                    webApp.setTaglib(taglib);
                } catch (final Exception e) {
                    context.setterError(reader, WebApp.class, "setTaglib", Taglib.class, e);
                }
            } else if (("jsp-config" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: jspConfig
                final JspConfig jspConfigItem = readJspConfig(elementReader, context);
                if (jspConfig == null) {
                    jspConfig = webApp.jspConfig;
                    if (jspConfig != null) {
                        jspConfig.clear();
                    } else {
                        jspConfig = new ArrayList<JspConfig>();
                    }
                }
                jspConfig.add(jspConfigItem);
            } else if (("security-constraint" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: securityConstraint
                final SecurityConstraint securityConstraintItem = readSecurityConstraint(elementReader, context);
                if (securityConstraint == null) {
                    securityConstraint = webApp.securityConstraint;
                    if (securityConstraint != null) {
                        securityConstraint.clear();
                    } else {
                        securityConstraint = new ArrayList<SecurityConstraint>();
                    }
                }
                securityConstraint.add(securityConstraintItem);
            } else if (("login-config" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: loginConfig
                final LoginConfig loginConfigItem = readLoginConfig(elementReader, context);
                if (loginConfig == null) {
                    loginConfig = webApp.loginConfig;
                    if (loginConfig != null) {
                        loginConfig.clear();
                    } else {
                        loginConfig = new ArrayList<LoginConfig>();
                    }
                }
                loginConfig.add(loginConfigItem);
            } else if (("security-role" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: securityRole
                final SecurityRole securityRoleItem = readSecurityRole(elementReader, context);
                if (securityRole == null) {
                    securityRole = webApp.securityRole;
                    if (securityRole != null) {
                        securityRole.clear();
                    } else {
                        securityRole = new ArrayList<SecurityRole>();
                    }
                }
                securityRole.add(securityRoleItem);
            } else if (("locale-encoding-mapping-list" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: localeEncodingMappingList
                final LocaleEncodingMappingList localeEncodingMappingListItem = readLocaleEncodingMappingList(elementReader, context);
                if (localeEncodingMappingList == null) {
                    localeEncodingMappingList = webApp.localeEncodingMappingList;
                    if (localeEncodingMappingList != null) {
                        localeEncodingMappingList.clear();
                    } else {
                        localeEncodingMappingList = new ArrayList<LocaleEncodingMappingList>();
                    }
                }
                localeEncodingMappingList.add(localeEncodingMappingListItem);
            } else if (("env-entry" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: envEntry
                final EnvEntry envEntryItem = readEnvEntry(elementReader, context);
                if (envEntry == null) {
                    envEntry = webApp.envEntry;
                    if (envEntry != null) {
                        envEntry.clear();
                    } else {
                        envEntry = new KeyedCollection<String, EnvEntry>();
                    }
                }
                envEntry.add(envEntryItem);
            } else if (("ejb-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbRef
                final EjbRef ejbRefItem = readEjbRef(elementReader, context);
                if (ejbRef == null) {
                    ejbRef = webApp.ejbRef;
                    if (ejbRef != null) {
                        ejbRef.clear();
                    } else {
                        ejbRef = new KeyedCollection<String, EjbRef>();
                    }
                }
                ejbRef.add(ejbRefItem);
            } else if (("ejb-local-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbLocalRef
                final EjbLocalRef ejbLocalRefItem = readEjbLocalRef(elementReader, context);
                if (ejbLocalRef == null) {
                    ejbLocalRef = webApp.ejbLocalRef;
                    if (ejbLocalRef != null) {
                        ejbLocalRef.clear();
                    } else {
                        ejbLocalRef = new KeyedCollection<String, EjbLocalRef>();
                    }
                }
                ejbLocalRef.add(ejbLocalRefItem);
            } else if (("service-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceRef
                final ServiceRef serviceRefItem = readServiceRef(elementReader, context);
                if (serviceRef == null) {
                    serviceRef = webApp.serviceRef;
                    if (serviceRef != null) {
                        serviceRef.clear();
                    } else {
                        serviceRef = new KeyedCollection<String, ServiceRef>();
                    }
                }
                serviceRef.add(serviceRefItem);
            } else if (("resource-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceRef
                final ResourceRef resourceRefItem = readResourceRef(elementReader, context);
                if (resourceRef == null) {
                    resourceRef = webApp.resourceRef;
                    if (resourceRef != null) {
                        resourceRef.clear();
                    } else {
                        resourceRef = new KeyedCollection<String, ResourceRef>();
                    }
                }
                resourceRef.add(resourceRefItem);
            } else if (("resource-env-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceEnvRef
                final ResourceEnvRef resourceEnvRefItem = readResourceEnvRef(elementReader, context);
                if (resourceEnvRef == null) {
                    resourceEnvRef = webApp.resourceEnvRef;
                    if (resourceEnvRef != null) {
                        resourceEnvRef.clear();
                    } else {
                        resourceEnvRef = new KeyedCollection<String, ResourceEnvRef>();
                    }
                }
                resourceEnvRef.add(resourceEnvRefItem);
            } else if (("message-destination-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageDestinationRef
                final MessageDestinationRef messageDestinationRefItem = readMessageDestinationRef(elementReader, context);
                if (messageDestinationRef == null) {
                    messageDestinationRef = webApp.messageDestinationRef;
                    if (messageDestinationRef != null) {
                        messageDestinationRef.clear();
                    } else {
                        messageDestinationRef = new KeyedCollection<String, MessageDestinationRef>();
                    }
                }
                messageDestinationRef.add(messageDestinationRefItem);
            } else if (("persistence-context-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: persistenceContextRef
                final PersistenceContextRef persistenceContextRefItem = readPersistenceContextRef(elementReader, context);
                if (persistenceContextRef == null) {
                    persistenceContextRef = webApp.persistenceContextRef;
                    if (persistenceContextRef != null) {
                        persistenceContextRef.clear();
                    } else {
                        persistenceContextRef = new KeyedCollection<String, PersistenceContextRef>();
                    }
                }
                persistenceContextRef.add(persistenceContextRefItem);
            } else if (("persistence-unit-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: persistenceUnitRef
                final PersistenceUnitRef persistenceUnitRefItem = readPersistenceUnitRef(elementReader, context);
                if (persistenceUnitRef == null) {
                    persistenceUnitRef = webApp.persistenceUnitRef;
                    if (persistenceUnitRef != null) {
                        persistenceUnitRef.clear();
                    } else {
                        persistenceUnitRef = new KeyedCollection<String, PersistenceUnitRef>();
                    }
                }
                persistenceUnitRef.add(persistenceUnitRefItem);
            } else if (("post-construct" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: postConstruct
                final org.apache.openejb.jee.LifecycleCallback postConstructItem = readLifecycleCallback(elementReader, context);
                if (postConstruct == null) {
                    postConstruct = webApp.postConstruct;
                    if (postConstruct != null) {
                        postConstruct.clear();
                    } else {
                        postConstruct = new ArrayList<org.apache.openejb.jee.LifecycleCallback>();
                    }
                }
                postConstruct.add(postConstructItem);
            } else if (("pre-destroy" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: preDestroy
                final org.apache.openejb.jee.LifecycleCallback preDestroyItem = readLifecycleCallback(elementReader, context);
                if (preDestroy == null) {
                    preDestroy = webApp.preDestroy;
                    if (preDestroy != null) {
                        preDestroy.clear();
                    } else {
                        preDestroy = new ArrayList<org.apache.openejb.jee.LifecycleCallback>();
                    }
                }
                preDestroy.add(preDestroyItem);
            } else if (("message-destination" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageDestination
                final MessageDestination messageDestinationItem = readMessageDestination(elementReader, context);
                if (messageDestination == null) {
                    messageDestination = webApp.messageDestination;
                    if (messageDestination != null) {
                        messageDestination.clear();
                    } else {
                        messageDestination = new ArrayList<MessageDestination>();
                    }
                }
                messageDestination.add(messageDestinationItem);
            } else if (("absolute-ordering" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: absoluteOrdering
                final AbsoluteOrdering absoluteOrdering = readAbsoluteOrdering(elementReader, context);
                webApp.absoluteOrdering = absoluteOrdering;
            } else if (("data-source" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: dataSource
                final DataSource dataSourceItem = readDataSource(elementReader, context);
                if (dataSource == null) {
                    dataSource = webApp.dataSource;
                    if (dataSource != null) {
                        dataSource.clear();
                    } else {
                        dataSource = new KeyedCollection<String, DataSource>();
                    }
                }
                dataSource.add(dataSourceItem);
            } else if (("jms-connection-factory" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: dataSource
                final JMSConnectionFactory connectionFactory = readJMSConnectionFactory(elementReader, context);
                if (jmsConnectionFactories == null) {
                    jmsConnectionFactories = webApp.jmsConnectionFactories;
                    if (jmsConnectionFactories != null) {
                        jmsConnectionFactories.clear();
                    } else {
                        jmsConnectionFactories = new KeyedCollection<>();
                    }
                }
                jmsConnectionFactories.add(connectionFactory);
            } else if (("module-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: moduleName
                final String moduleNameRaw = elementReader.getElementAsString();

                final String moduleName;
                try {
                    moduleName = Adapters.collapsedStringAdapterAdapter.unmarshal(moduleNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                webApp.moduleName = moduleName;
            } else if (("default-context-path" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: defaultContextPath
                final String defaultContextPathRaw = elementReader.getElementAsString();

                final String defaultContextPath;
                try {
                    defaultContextPath = Adapters.collapsedStringAdapterAdapter.unmarshal(defaultContextPathRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                webApp.defaultContextPath = defaultContextPath;
            } /*
            else not yet used so ignored, here will come servlet 3.1 elements we don't parse cause don't need yet
            */
        }
        if (descriptions != null) {
            try {
                webApp.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, WebApp.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                webApp.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, WebApp.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            webApp.icon = icon;
        }
        if (distributable != null) {
            webApp.distributable = distributable;
        }
        if (contextParam != null) {
            webApp.contextParam = contextParam;
        }
        if (filter != null) {
            webApp.filter = filter;
        }
        if (filterMapping != null) {
            webApp.filterMapping = filterMapping;
        }
        if (listener != null) {
            webApp.listener = listener;
        }
        if (servlet != null) {
            webApp.servlet = servlet;
        }
        if (servletMapping != null) {
            webApp.servletMapping = servletMapping;
        }
        if (sessionConfig != null) {
            webApp.sessionConfig = sessionConfig;
        }
        if (mimeMapping != null) {
            webApp.mimeMapping = mimeMapping;
        }
        if (welcomeFileList != null) {
            webApp.welcomeFileList = welcomeFileList;
        }
        if (errorPage != null) {
            webApp.errorPage = errorPage;
        }
        if (jspConfig != null) {
            webApp.jspConfig = jspConfig;
        }
        if (securityConstraint != null) {
            webApp.securityConstraint = securityConstraint;
        }
        if (loginConfig != null) {
            webApp.loginConfig = loginConfig;
        }
        if (securityRole != null) {
            webApp.securityRole = securityRole;
        }
        if (localeEncodingMappingList != null) {
            webApp.localeEncodingMappingList = localeEncodingMappingList;
        }
        if (envEntry != null) {
            webApp.envEntry = envEntry;
        }
        if (ejbRef != null) {
            webApp.ejbRef = ejbRef;
        }
        if (ejbLocalRef != null) {
            webApp.ejbLocalRef = ejbLocalRef;
        }
        if (serviceRef != null) {
            webApp.serviceRef = serviceRef;
        }
        if (resourceRef != null) {
            webApp.resourceRef = resourceRef;
        }
        if (resourceEnvRef != null) {
            webApp.resourceEnvRef = resourceEnvRef;
        }
        if (messageDestinationRef != null) {
            webApp.messageDestinationRef = messageDestinationRef;
        }
        if (persistenceContextRef != null) {
            webApp.persistenceContextRef = persistenceContextRef;
        }
        if (persistenceUnitRef != null) {
            webApp.persistenceUnitRef = persistenceUnitRef;
        }
        if (postConstruct != null) {
            webApp.postConstruct = postConstruct;
        }
        if (preDestroy != null) {
            webApp.preDestroy = preDestroy;
        }
        if (messageDestination != null) {
            webApp.messageDestination = messageDestination;
        }
        if (dataSource != null) {
            webApp.dataSource = dataSource;
        }

        context.afterUnmarshal(webApp, org.metatype.sxc.jaxb.LifecycleCallback.NONE);

        return webApp;
    }

    public final WebApp read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final WebApp webApp, RuntimeContext context)
        throws Exception {
        if (webApp == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (WebApp.class != webApp.getClass()) {
            context.unexpectedSubclass(writer, webApp, WebApp.class);
            return;
        }

        context.beforeMarshal(webApp, org.metatype.sxc.jaxb.LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = webApp.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(webApp, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ATTRIBUTE: metadataComplete
        final Boolean metadataComplete = webApp.metadataComplete;
        if (metadataComplete != null) {
            writer.writeAttribute("", "", "metadata-complete", Boolean.toString(metadataComplete));
        }

        // ATTRIBUTE: version
        final String versionRaw = webApp.version;
        if (versionRaw != null) {
            String version = null;
            try {
                version = Adapters.collapsedStringAdapterAdapter.marshal(versionRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(webApp, "version", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "version", version);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = webApp.getDescriptions();
        } catch (final Exception e) {
            context.getterError(webApp, "descriptions", WebApp.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = webApp.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(webApp, "displayNames", WebApp.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = webApp.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "icon");
                }
            }
        }

        // ELEMENT: distributable
        final List<Empty> distributable = webApp.distributable;
        if (distributable != null) {
            for (final Empty distributableItem : distributable) {
                writer.writeStartElement(prefix, "distributable", "http://java.sun.com/xml/ns/javaee");
                if (distributableItem != null) {
                    writeEmpty(writer, distributableItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: contextParam
        final List<ParamValue> contextParam = webApp.contextParam;
        if (contextParam != null) {
            for (final ParamValue contextParamItem : contextParam) {
                if (contextParamItem != null) {
                    writer.writeStartElement(prefix, "context-param", "http://java.sun.com/xml/ns/javaee");
                    writeParamValue(writer, contextParamItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: filter
        final List<Filter> filter = webApp.filter;
        if (filter != null) {
            for (final Filter filterItem : filter) {
                writer.writeStartElement(prefix, "filter", "http://java.sun.com/xml/ns/javaee");
                if (filterItem != null) {
                    writeFilter(writer, filterItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: filterMapping
        final List<FilterMapping> filterMapping = webApp.filterMapping;
        if (filterMapping != null) {
            for (final FilterMapping filterMappingItem : filterMapping) {
                if (filterMappingItem != null) {
                    writer.writeStartElement(prefix, "filter-mapping", "http://java.sun.com/xml/ns/javaee");
                    writeFilterMapping(writer, filterMappingItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: listener
        final List<Listener> listener = webApp.listener;
        if (listener != null) {
            for (final Listener listenerItem : listener) {
                writer.writeStartElement(prefix, "listener", "http://java.sun.com/xml/ns/javaee");
                if (listenerItem != null) {
                    writeListener(writer, listenerItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: servlet
        final List<Servlet> servlet = webApp.servlet;
        if (servlet != null) {
            for (final Servlet servletItem : servlet) {
                writer.writeStartElement(prefix, "servlet", "http://java.sun.com/xml/ns/javaee");
                if (servletItem != null) {
                    writeServlet(writer, servletItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: servletMapping
        final List<ServletMapping> servletMapping = webApp.servletMapping;
        if (servletMapping != null) {
            for (final ServletMapping servletMappingItem : servletMapping) {
                if (servletMappingItem != null) {
                    writer.writeStartElement(prefix, "servlet-mapping", "http://java.sun.com/xml/ns/javaee");
                    writeServletMapping(writer, servletMappingItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: sessionConfig
        final List<SessionConfig> sessionConfig = webApp.sessionConfig;
        if (sessionConfig != null) {
            for (final SessionConfig sessionConfigItem : sessionConfig) {
                if (sessionConfigItem != null) {
                    writer.writeStartElement(prefix, "session-config", "http://java.sun.com/xml/ns/javaee");
                    writeSessionConfig(writer, sessionConfigItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: mimeMapping
        final List<MimeMapping> mimeMapping = webApp.mimeMapping;
        if (mimeMapping != null) {
            for (final MimeMapping mimeMappingItem : mimeMapping) {
                if (mimeMappingItem != null) {
                    writer.writeStartElement(prefix, "mime-mapping", "http://java.sun.com/xml/ns/javaee");
                    writeMimeMapping(writer, mimeMappingItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: welcomeFileList
        final List<WelcomeFileList> welcomeFileList = webApp.welcomeFileList;
        if (welcomeFileList != null) {
            for (final WelcomeFileList welcomeFileListItem : welcomeFileList) {
                if (welcomeFileListItem != null) {
                    writer.writeStartElement(prefix, "welcome-file-list", "http://java.sun.com/xml/ns/javaee");
                    writeWelcomeFileList(writer, welcomeFileListItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: errorPage
        final List<ErrorPage> errorPage = webApp.errorPage;
        if (errorPage != null) {
            for (final ErrorPage errorPageItem : errorPage) {
                if (errorPageItem != null) {
                    writer.writeStartElement(prefix, "error-page", "http://java.sun.com/xml/ns/javaee");
                    writeErrorPage(writer, errorPageItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: taglib
        Taglib taglib = null;
        try {
            taglib = webApp.getTaglib();
        } catch (final Exception e) {
            context.getterError(webApp, "taglib", WebApp.class, "getTaglib", e);
        }
        if (taglib != null) {
            writer.writeStartElement(prefix, "taglib", "http://java.sun.com/xml/ns/javaee");
            writeTaglib(writer, taglib, context);
            writer.writeEndElement();
        }

        // ELEMENT: jspConfig
        final List<JspConfig> jspConfig = webApp.jspConfig;
        if (jspConfig != null) {
            for (final JspConfig jspConfigItem : jspConfig) {
                if (jspConfigItem != null) {
                    writer.writeStartElement(prefix, "jsp-config", "http://java.sun.com/xml/ns/javaee");
                    writeJspConfig(writer, jspConfigItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: securityConstraint
        final List<SecurityConstraint> securityConstraint = webApp.securityConstraint;
        if (securityConstraint != null) {
            for (final SecurityConstraint securityConstraintItem : securityConstraint) {
                if (securityConstraintItem != null) {
                    writer.writeStartElement(prefix, "security-constraint", "http://java.sun.com/xml/ns/javaee");
                    writeSecurityConstraint(writer, securityConstraintItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: loginConfig
        final List<LoginConfig> loginConfig = webApp.loginConfig;
        if (loginConfig != null) {
            for (final LoginConfig loginConfigItem : loginConfig) {
                if (loginConfigItem != null) {
                    writer.writeStartElement(prefix, "login-config", "http://java.sun.com/xml/ns/javaee");
                    writeLoginConfig(writer, loginConfigItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: securityRole
        final List<SecurityRole> securityRole = webApp.securityRole;
        if (securityRole != null) {
            for (final SecurityRole securityRoleItem : securityRole) {
                if (securityRoleItem != null) {
                    writer.writeStartElement(prefix, "security-role", "http://java.sun.com/xml/ns/javaee");
                    writeSecurityRole(writer, securityRoleItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: localeEncodingMappingList
        final List<LocaleEncodingMappingList> localeEncodingMappingList = webApp.localeEncodingMappingList;
        if (localeEncodingMappingList != null) {
            for (final LocaleEncodingMappingList localeEncodingMappingListItem : localeEncodingMappingList) {
                if (localeEncodingMappingListItem != null) {
                    writer.writeStartElement(prefix, "locale-encoding-mapping-list", "http://java.sun.com/xml/ns/javaee");
                    writeLocaleEncodingMappingList(writer, localeEncodingMappingListItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: envEntry
        final KeyedCollection<String, EnvEntry> envEntry = webApp.envEntry;
        if (envEntry != null) {
            for (final EnvEntry envEntryItem : envEntry) {
                if (envEntryItem != null) {
                    writer.writeStartElement(prefix, "env-entry", "http://java.sun.com/xml/ns/javaee");
                    writeEnvEntry(writer, envEntryItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "envEntry");
                }
            }
        }

        // ELEMENT: ejbRef
        final KeyedCollection<String, EjbRef> ejbRef = webApp.ejbRef;
        if (ejbRef != null) {
            for (final EjbRef ejbRefItem : ejbRef) {
                if (ejbRefItem != null) {
                    writer.writeStartElement(prefix, "ejb-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbRef(writer, ejbRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "ejbRef");
                }
            }
        }

        // ELEMENT: ejbLocalRef
        final KeyedCollection<String, EjbLocalRef> ejbLocalRef = webApp.ejbLocalRef;
        if (ejbLocalRef != null) {
            for (final EjbLocalRef ejbLocalRefItem : ejbLocalRef) {
                if (ejbLocalRefItem != null) {
                    writer.writeStartElement(prefix, "ejb-local-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbLocalRef(writer, ejbLocalRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "ejbLocalRef");
                }
            }
        }

        // ELEMENT: serviceRef
        final KeyedCollection<String, ServiceRef> serviceRef = webApp.serviceRef;
        if (serviceRef != null) {
            for (final ServiceRef serviceRefItem : serviceRef) {
                if (serviceRefItem != null) {
                    writer.writeStartElement(prefix, "service-ref", "http://java.sun.com/xml/ns/javaee");
                    writeServiceRef(writer, serviceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "serviceRef");
                }
            }
        }

        // ELEMENT: resourceRef
        final KeyedCollection<String, ResourceRef> resourceRef = webApp.resourceRef;
        if (resourceRef != null) {
            for (final ResourceRef resourceRefItem : resourceRef) {
                if (resourceRefItem != null) {
                    writer.writeStartElement(prefix, "resource-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceRef(writer, resourceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "resourceRef");
                }
            }
        }

        // ELEMENT: resourceEnvRef
        final KeyedCollection<String, ResourceEnvRef> resourceEnvRef = webApp.resourceEnvRef;
        if (resourceEnvRef != null) {
            for (final ResourceEnvRef resourceEnvRefItem : resourceEnvRef) {
                if (resourceEnvRefItem != null) {
                    writer.writeStartElement(prefix, "resource-env-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceEnvRef(writer, resourceEnvRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "resourceEnvRef");
                }
            }
        }

        // ELEMENT: messageDestinationRef
        final KeyedCollection<String, MessageDestinationRef> messageDestinationRef = webApp.messageDestinationRef;
        if (messageDestinationRef != null) {
            for (final MessageDestinationRef messageDestinationRefItem : messageDestinationRef) {
                if (messageDestinationRefItem != null) {
                    writer.writeStartElement(prefix, "message-destination-ref", "http://java.sun.com/xml/ns/javaee");
                    writeMessageDestinationRef(writer, messageDestinationRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "messageDestinationRef");
                }
            }
        }

        // ELEMENT: persistenceContextRef
        final KeyedCollection<String, PersistenceContextRef> persistenceContextRef = webApp.persistenceContextRef;
        if (persistenceContextRef != null) {
            for (final PersistenceContextRef persistenceContextRefItem : persistenceContextRef) {
                if (persistenceContextRefItem != null) {
                    writer.writeStartElement(prefix, "persistence-context-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceContextRef(writer, persistenceContextRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "persistenceContextRef");
                }
            }
        }

        // ELEMENT: persistenceUnitRef
        final KeyedCollection<String, PersistenceUnitRef> persistenceUnitRef = webApp.persistenceUnitRef;
        if (persistenceUnitRef != null) {
            for (final PersistenceUnitRef persistenceUnitRefItem : persistenceUnitRef) {
                if (persistenceUnitRefItem != null) {
                    writer.writeStartElement(prefix, "persistence-unit-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceUnitRef(writer, persistenceUnitRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "persistenceUnitRef");
                }
            }
        }

        // ELEMENT: postConstruct
        final List<org.apache.openejb.jee.LifecycleCallback> postConstruct = webApp.postConstruct;
        if (postConstruct != null) {
            for (final org.apache.openejb.jee.LifecycleCallback postConstructItem : postConstruct) {
                if (postConstructItem != null) {
                    writer.writeStartElement(prefix, "post-construct", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, postConstructItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "postConstruct");
                }
            }
        }

        // ELEMENT: preDestroy
        final List<org.apache.openejb.jee.LifecycleCallback> preDestroy = webApp.preDestroy;
        if (preDestroy != null) {
            for (final org.apache.openejb.jee.LifecycleCallback preDestroyItem : preDestroy) {
                if (preDestroyItem != null) {
                    writer.writeStartElement(prefix, "pre-destroy", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, preDestroyItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "preDestroy");
                }
            }
        }

        // ELEMENT: messageDestination
        final List<MessageDestination> messageDestination = webApp.messageDestination;
        if (messageDestination != null) {
            for (final MessageDestination messageDestinationItem : messageDestination) {
                if (messageDestinationItem != null) {
                    writer.writeStartElement(prefix, "message-destination", "http://java.sun.com/xml/ns/javaee");
                    writeMessageDestination(writer, messageDestinationItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "messageDestination");
                }
            }
        }

        // ELEMENT: absoluteOrdering
        final AbsoluteOrdering absoluteOrdering = webApp.absoluteOrdering;
        if (absoluteOrdering != null) {
            writer.writeStartElement(prefix, "absolute-ordering", "http://java.sun.com/xml/ns/javaee");
            writeAbsoluteOrdering(writer, absoluteOrdering, context);
            writer.writeEndElement();
        }

        // ELEMENT: dataSource
        final KeyedCollection<String, DataSource> dataSource = webApp.dataSource;
        if (dataSource != null) {
            for (final DataSource dataSourceItem : dataSource) {
                if (dataSourceItem != null) {
                    writer.writeStartElement(prefix, "data-source", "http://java.sun.com/xml/ns/javaee");
                    writeDataSource(writer, dataSourceItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webApp, "dataSource");
                }
            }
        }

        // ELEMENT: moduleName
        final String moduleNameRaw = webApp.moduleName;
        String moduleName = null;
        try {
            moduleName = Adapters.collapsedStringAdapterAdapter.marshal(moduleNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(webApp, "moduleName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (moduleName != null) {
            writer.writeStartElement(prefix, "module-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(moduleName);
            writer.writeEndElement();
        }

        context.afterMarshal(webApp, org.metatype.sxc.jaxb.LifecycleCallback.NONE);
    }

}
