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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.apache.openejb.jee.AroundInvoke$JAXB.readAroundInvoke;
import static org.apache.openejb.jee.AroundInvoke$JAXB.writeAroundInvoke;
import static org.apache.openejb.jee.AroundTimeout$JAXB.readAroundTimeout;
import static org.apache.openejb.jee.AroundTimeout$JAXB.writeAroundTimeout;
import static org.apache.openejb.jee.AsyncMethod$JAXB.readAsyncMethod;
import static org.apache.openejb.jee.AsyncMethod$JAXB.writeAsyncMethod;
import static org.apache.openejb.jee.ConcurrencyManagementType$JAXB.parseConcurrencyManagementType;
import static org.apache.openejb.jee.ConcurrencyManagementType$JAXB.toStringConcurrencyManagementType;
import static org.apache.openejb.jee.ConcurrentMethod$JAXB.readConcurrentMethod;
import static org.apache.openejb.jee.ConcurrentMethod$JAXB.writeConcurrentMethod;
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
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.InitMethod$JAXB.readInitMethod;
import static org.apache.openejb.jee.InitMethod$JAXB.writeInitMethod;
import static org.apache.openejb.jee.LifecycleCallback$JAXB.readLifecycleCallback;
import static org.apache.openejb.jee.LifecycleCallback$JAXB.writeLifecycleCallback;
import static org.apache.openejb.jee.MessageDestinationRef$JAXB.readMessageDestinationRef;
import static org.apache.openejb.jee.MessageDestinationRef$JAXB.writeMessageDestinationRef;
import static org.apache.openejb.jee.NamedMethod$JAXB.readNamedMethod;
import static org.apache.openejb.jee.NamedMethod$JAXB.writeNamedMethod;
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.readPersistenceContextRef;
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.writePersistenceContextRef;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.readPersistenceUnitRef;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.writePersistenceUnitRef;
import static org.apache.openejb.jee.RemoveMethod$JAXB.readRemoveMethod;
import static org.apache.openejb.jee.RemoveMethod$JAXB.writeRemoveMethod;
import static org.apache.openejb.jee.ResourceEnvRef$JAXB.readResourceEnvRef;
import static org.apache.openejb.jee.ResourceEnvRef$JAXB.writeResourceEnvRef;
import static org.apache.openejb.jee.ResourceRef$JAXB.readResourceRef;
import static org.apache.openejb.jee.ResourceRef$JAXB.writeResourceRef;
import static org.apache.openejb.jee.SecurityIdentity$JAXB.readSecurityIdentity;
import static org.apache.openejb.jee.SecurityIdentity$JAXB.writeSecurityIdentity;
import static org.apache.openejb.jee.SecurityRoleRef$JAXB.readSecurityRoleRef;
import static org.apache.openejb.jee.SecurityRoleRef$JAXB.writeSecurityRoleRef;
import static org.apache.openejb.jee.ServiceRef$JAXB.readServiceRef;
import static org.apache.openejb.jee.ServiceRef$JAXB.writeServiceRef;
import static org.apache.openejb.jee.SessionType$JAXB.parseSessionType;
import static org.apache.openejb.jee.SessionType$JAXB.toStringSessionType;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;
import static org.apache.openejb.jee.Timeout$JAXB.readTimeout;
import static org.apache.openejb.jee.Timeout$JAXB.writeTimeout;
import static org.apache.openejb.jee.Timer$JAXB.readTimer;
import static org.apache.openejb.jee.Timer$JAXB.writeTimer;
import static org.apache.openejb.jee.TransactionType$JAXB.parseTransactionType;
import static org.apache.openejb.jee.TransactionType$JAXB.toStringTransactionType;

@SuppressWarnings({
    "StringEquality"
})
public class SessionBean$JAXB
    extends JAXBObject<SessionBean> {
	
	private static final Set<String> sessionBeanClasses;

	static {
		sessionBeanClasses = new HashSet<String>();
		sessionBeanClasses.add("org.apache.openejb.jee.SessionBean");
		sessionBeanClasses.add("org.apache.openejb.jee.ManagedBean");
		sessionBeanClasses.add("org.apache.openejb.config.CompManagedBean");
		sessionBeanClasses.add("org.apache.openejb.jee.StatefulBean");
		sessionBeanClasses.add("org.apache.openejb.jee.StatelessBean");
		sessionBeanClasses.add("org.apache.openejb.jee.SingletonBean");
		
	}

    public SessionBean$JAXB() {
        super(SessionBean.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "session-beanType".intern()), Text$JAXB.class, Icon$JAXB.class, Empty$JAXB.class, SessionType$JAXB.class, Timeout$JAXB.class, NamedMethod$JAXB.class, Timer$JAXB.class, ConcurrencyManagementType$JAXB.class, ConcurrentMethod$JAXB.class, InitMethod$JAXB.class, RemoveMethod$JAXB.class, AsyncMethod$JAXB.class, TransactionType$JAXB.class, AroundInvoke$JAXB.class, AroundTimeout$JAXB.class, EnvEntry$JAXB.class, EjbRef$JAXB.class, EjbLocalRef$JAXB.class, ServiceRef$JAXB.class, ResourceRef$JAXB.class, ResourceEnvRef$JAXB.class, MessageDestinationRef$JAXB.class, PersistenceContextRef$JAXB.class, PersistenceUnitRef$JAXB.class, LifecycleCallback$JAXB.class, DataSource$JAXB.class, SecurityRoleRef$JAXB.class, SecurityIdentity$JAXB.class);
    }

    public static SessionBean readSessionBean(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeSessionBean(final XoXMLStreamWriter writer, final SessionBean sessionBean, final RuntimeContext context)
        throws Exception {
        _write(writer, sessionBean, context);
    }

    public void write(final XoXMLStreamWriter writer, final SessionBean sessionBean, final RuntimeContext context)
        throws Exception {
        _write(writer, sessionBean, context);
    }

    public final static SessionBean _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final SessionBean sessionBean = new SessionBean();
        context.beforeUnmarshal(sessionBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        LinkedHashSet<String> businessLocal = null;
        LinkedHashSet<String> businessRemote = null;
        List<Timer> timer = null;
        List<ConcurrentMethod> concurrentMethod = null;
        List<InitMethod> initMethod = null;
        List<RemoveMethod> removeMethod = null;
        List<AsyncMethod> asyncMethod = null;
        List<AroundInvoke> aroundInvoke = null;
        List<AroundTimeout> aroundTimeout = null;
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
        KeyedCollection<String, DataSource> dataSource = null;
        List<org.apache.openejb.jee.LifecycleCallback> postActivate = null;
        List<org.apache.openejb.jee.LifecycleCallback> prePassivate = null;
        List<SecurityRoleRef> securityRoleRef = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("session-beanType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, SessionBean.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, sessionBean);
                sessionBean.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
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
                    icon = sessionBean.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("ejb-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbName
                final String ejbNameRaw = elementReader.getElementAsString();

                final String ejbName;
                try {
                    ejbName = Adapters.collapsedStringAdapterAdapter.unmarshal(ejbNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                sessionBean.ejbName = ejbName;
            } else if (("mapped-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: mappedName
                final String mappedNameRaw = elementReader.getElementAsString();

                final String mappedName;
                try {
                    mappedName = Adapters.collapsedStringAdapterAdapter.unmarshal(mappedNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                sessionBean.mappedName = mappedName;
            } else if (("home" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: home
                final String homeRaw = elementReader.getElementAsString();

                final String home;
                try {
                    home = Adapters.collapsedStringAdapterAdapter.unmarshal(homeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                sessionBean.home = home;
            } else if (("remote" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: remote
                final String remoteRaw = elementReader.getElementAsString();

                final String remote;
                try {
                    remote = Adapters.collapsedStringAdapterAdapter.unmarshal(remoteRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                sessionBean.remote = remote;
            } else if (("local-home" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: localHome
                final String localHomeRaw = elementReader.getElementAsString();

                final String localHome;
                try {
                    localHome = Adapters.collapsedStringAdapterAdapter.unmarshal(localHomeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                sessionBean.localHome = localHome;
            } else if (("local" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: local
                final String localRaw = elementReader.getElementAsString();

                final String local;
                try {
                    local = Adapters.collapsedStringAdapterAdapter.unmarshal(localRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                sessionBean.local = local;
            } else if (("business-local" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: businessLocal
                final String businessLocalItemRaw = elementReader.getElementAsString();

                final String businessLocalItem;
                try {
                    businessLocalItem = Adapters.collapsedStringAdapterAdapter.unmarshal(businessLocalItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (businessLocal == null) {
                    businessLocal = sessionBean.businessLocal;
                    if (businessLocal != null) {
                        businessLocal.clear();
                    } else {
                        businessLocal = new LinkedHashSet<String>();
                    }
                }
                businessLocal.add(businessLocalItem);
            } else if (("business-remote" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: businessRemote
                final String businessRemoteItemRaw = elementReader.getElementAsString();

                final String businessRemoteItem;
                try {
                    businessRemoteItem = Adapters.collapsedStringAdapterAdapter.unmarshal(businessRemoteItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (businessRemote == null) {
                    businessRemote = sessionBean.businessRemote;
                    if (businessRemote != null) {
                        businessRemote.clear();
                    } else {
                        businessRemote = new LinkedHashSet<String>();
                    }
                }
                businessRemote.add(businessRemoteItem);
            } else if (("local-bean" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: localBean
                final Empty localBean = readEmpty(elementReader, context);
                sessionBean.localBean = localBean;
            } else if (("service-endpoint" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceEndpoint
                final String serviceEndpointRaw = elementReader.getElementAsString();

                final String serviceEndpoint;
                try {
                    serviceEndpoint = Adapters.collapsedStringAdapterAdapter.unmarshal(serviceEndpointRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                sessionBean.serviceEndpoint = serviceEndpoint;
            } else if (("ejb-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbClass
                final String ejbClassRaw = elementReader.getElementAsString();

                final String ejbClass;
                try {
                    ejbClass = Adapters.collapsedStringAdapterAdapter.unmarshal(ejbClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                sessionBean.ejbClass = ejbClass;
            } else if (("session-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: sessionType
                final SessionType sessionType = parseSessionType(elementReader, context, elementReader.getElementAsString());
                if (sessionType != null) {
                    sessionBean.sessionType = sessionType;
                }
            } else if (("stateful-timeout" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: statefulTimeout
                final Timeout statefulTimeout = readTimeout(elementReader, context);
                sessionBean.statefulTimeout = statefulTimeout;
            } else if (("timeout-method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: timeoutMethod
                final NamedMethod timeoutMethod = readNamedMethod(elementReader, context);
                sessionBean.timeoutMethod = timeoutMethod;
            } else if (("timer" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: timer
                final Timer timerItem = readTimer(elementReader, context);
                if (timer == null) {
                    timer = sessionBean.timer;
                    if (timer != null) {
                        timer.clear();
                    } else {
                        timer = new ArrayList<Timer>();
                    }
                }
                timer.add(timerItem);
            } else if (("init-on-startup" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: initOnStartup
                final Boolean initOnStartup = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                sessionBean.initOnStartup = initOnStartup;
            } else if (("concurrency-management-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: concurrencyManagementType
                final ConcurrencyManagementType concurrencyManagementType = parseConcurrencyManagementType(elementReader, context, elementReader.getElementAsString());
                if (concurrencyManagementType != null) {
                    sessionBean.concurrencyManagementType = concurrencyManagementType;
                }
            } else if (("concurrent-method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: concurrentMethod
                final ConcurrentMethod concurrentMethodItem = readConcurrentMethod(elementReader, context);
                if (concurrentMethod == null) {
                    concurrentMethod = sessionBean.concurrentMethod;
                    if (concurrentMethod != null) {
                        concurrentMethod.clear();
                    } else {
                        concurrentMethod = new ArrayList<ConcurrentMethod>();
                    }
                }
                concurrentMethod.add(concurrentMethodItem);
            } else if (("depends-on" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT WRAPPER: dependsOn
                _readDependsOn(elementReader, context, sessionBean);
            } else if (("init-method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: initMethod
                final InitMethod initMethodItem = readInitMethod(elementReader, context);
                if (initMethod == null) {
                    initMethod = sessionBean.initMethod;
                    if (initMethod != null) {
                        initMethod.clear();
                    } else {
                        initMethod = new ArrayList<InitMethod>();
                    }
                }
                initMethod.add(initMethodItem);
            } else if (("remove-method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: removeMethod
                final RemoveMethod removeMethodItem = readRemoveMethod(elementReader, context);
                if (removeMethod == null) {
                    removeMethod = sessionBean.removeMethod;
                    if (removeMethod != null) {
                        removeMethod.clear();
                    } else {
                        removeMethod = new ArrayList<RemoveMethod>();
                    }
                }
                removeMethod.add(removeMethodItem);
            } else if (("async-method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: asyncMethod
                final AsyncMethod asyncMethodItem = readAsyncMethod(elementReader, context);
                if (asyncMethod == null) {
                    asyncMethod = sessionBean.asyncMethod;
                    if (asyncMethod != null) {
                        asyncMethod.clear();
                    } else {
                        asyncMethod = new ArrayList<AsyncMethod>();
                    }
                }
                asyncMethod.add(asyncMethodItem);
            } else if (("transaction-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: transactionType
                final TransactionType transactionType = parseTransactionType(elementReader, context, elementReader.getElementAsString());
                if (transactionType != null) {
                    sessionBean.transactionType = transactionType;
                }
            } else if (("after-begin-method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: afterBeginMethod
                final NamedMethod afterBeginMethod = readNamedMethod(elementReader, context);
                try {
                    sessionBean.setAfterBeginMethod(afterBeginMethod);
                } catch (final Exception e) {
                    context.setterError(reader, SessionBean.class, "setAfterBeginMethod", NamedMethod.class, e);
                }
            } else if (("before-completion-method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: beforeCompletionMethod
                final NamedMethod beforeCompletionMethod = readNamedMethod(elementReader, context);
                try {
                    sessionBean.setBeforeCompletionMethod(beforeCompletionMethod);
                } catch (final Exception e) {
                    context.setterError(reader, SessionBean.class, "setBeforeCompletionMethod", NamedMethod.class, e);
                }
            } else if (("after-completion-method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: afterCompletionMethod
                final NamedMethod afterCompletionMethod = readNamedMethod(elementReader, context);
                try {
                    sessionBean.setAfterCompletionMethod(afterCompletionMethod);
                } catch (final Exception e) {
                    context.setterError(reader, SessionBean.class, "setAfterCompletionMethod", NamedMethod.class, e);
                }
            } else if (("around-invoke" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: aroundInvoke
                final AroundInvoke aroundInvokeItem = readAroundInvoke(elementReader, context);
                if (aroundInvoke == null) {
                    aroundInvoke = sessionBean.aroundInvoke;
                    if (aroundInvoke != null) {
                        aroundInvoke.clear();
                    } else {
                        aroundInvoke = new ArrayList<AroundInvoke>();
                    }
                }
                aroundInvoke.add(aroundInvokeItem);
            } else if (("around-timeout" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: aroundTimeout
                final AroundTimeout aroundTimeoutItem = readAroundTimeout(elementReader, context);
                if (aroundTimeout == null) {
                    aroundTimeout = sessionBean.aroundTimeout;
                    if (aroundTimeout != null) {
                        aroundTimeout.clear();
                    } else {
                        aroundTimeout = new ArrayList<AroundTimeout>();
                    }
                }
                aroundTimeout.add(aroundTimeoutItem);
            } else if (("env-entry" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: envEntry
                final EnvEntry envEntryItem = readEnvEntry(elementReader, context);
                if (envEntry == null) {
                    envEntry = sessionBean.envEntry;
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
                    ejbRef = sessionBean.ejbRef;
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
                    ejbLocalRef = sessionBean.ejbLocalRef;
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
                    serviceRef = sessionBean.serviceRef;
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
                    resourceRef = sessionBean.resourceRef;
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
                    resourceEnvRef = sessionBean.resourceEnvRef;
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
                    messageDestinationRef = sessionBean.messageDestinationRef;
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
                    persistenceContextRef = sessionBean.persistenceContextRef;
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
                    persistenceUnitRef = sessionBean.persistenceUnitRef;
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
                    postConstruct = sessionBean.postConstruct;
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
                    preDestroy = sessionBean.preDestroy;
                    if (preDestroy != null) {
                        preDestroy.clear();
                    } else {
                        preDestroy = new ArrayList<org.apache.openejb.jee.LifecycleCallback>();
                    }
                }
                preDestroy.add(preDestroyItem);
            } else if (("data-source" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: dataSource
                final DataSource dataSourceItem = readDataSource(elementReader, context);
                if (dataSource == null) {
                    dataSource = sessionBean.dataSource;
                    if (dataSource != null) {
                        dataSource.clear();
                    } else {
                        dataSource = new KeyedCollection<String, DataSource>();
                    }
                }
                dataSource.add(dataSourceItem);
            } else if (("post-activate" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: postActivate
                final org.apache.openejb.jee.LifecycleCallback postActivateItem = readLifecycleCallback(elementReader, context);
                if (postActivate == null) {
                    postActivate = sessionBean.postActivate;
                    if (postActivate != null) {
                        postActivate.clear();
                    } else {
                        postActivate = new ArrayList<org.apache.openejb.jee.LifecycleCallback>();
                    }
                }
                postActivate.add(postActivateItem);
            } else if (("pre-passivate" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: prePassivate
                final org.apache.openejb.jee.LifecycleCallback prePassivateItem = readLifecycleCallback(elementReader, context);
                if (prePassivate == null) {
                    prePassivate = sessionBean.prePassivate;
                    if (prePassivate != null) {
                        prePassivate.clear();
                    } else {
                        prePassivate = new ArrayList<org.apache.openejb.jee.LifecycleCallback>();
                    }
                }
                prePassivate.add(prePassivateItem);
            } else if (("security-role-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: securityRoleRef
                final SecurityRoleRef securityRoleRefItem = readSecurityRoleRef(elementReader, context);
                if (securityRoleRef == null) {
                    securityRoleRef = sessionBean.securityRoleRef;
                    if (securityRoleRef != null) {
                        securityRoleRef.clear();
                    } else {
                        securityRoleRef = new ArrayList<SecurityRoleRef>();
                    }
                }
                securityRoleRef.add(securityRoleRefItem);
            } else if (("security-identity" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: securityIdentity
                final SecurityIdentity securityIdentity = readSecurityIdentity(elementReader, context);
                sessionBean.securityIdentity = securityIdentity;
            } else if (("passivation-capable" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                sessionBean.setPassivationCapable(Boolean.parseBoolean(elementReader.getElementAsString()));
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "passivation-capable"), new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-name"), new QName("http://java.sun.com/xml/ns/javaee", "mapped-name"), new QName("http://java.sun.com/xml/ns/javaee", "home"), new QName("http://java.sun.com/xml/ns/javaee", "remote"), new QName("http://java.sun.com/xml/ns/javaee", "local-home"), new QName("http://java.sun.com/xml/ns/javaee", "local"), new QName("http://java.sun.com/xml/ns/javaee", "business-local"), new QName("http://java.sun.com/xml/ns/javaee", "business-remote"), new QName("http://java.sun.com/xml/ns/javaee", "local-bean"), new QName("http://java.sun.com/xml/ns/javaee", "service-endpoint"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-class"), new QName("http://java.sun.com/xml/ns/javaee", "session-type"), new QName("http://java.sun.com/xml/ns/javaee", "stateful-timeout"), new QName("http://java.sun.com/xml/ns/javaee", "timeout-method"), new QName("http://java.sun.com/xml/ns/javaee", "timer"), new QName("http://java.sun.com/xml/ns/javaee", "init-on-startup"), new QName("http://java.sun.com/xml/ns/javaee", "concurrency-management-type"), new QName("http://java.sun.com/xml/ns/javaee", "concurrent-method"), new QName("http://java.sun.com/xml/ns/javaee", "depends-on"), new QName("http://java.sun.com/xml/ns/javaee", "init-method"), new QName("http://java.sun.com/xml/ns/javaee", "remove-method"), new QName("http://java.sun.com/xml/ns/javaee", "async-method"), new QName("http://java.sun.com/xml/ns/javaee", "transaction-type"), new QName("http://java.sun.com/xml/ns/javaee", "after-begin-method"), new QName("http://java.sun.com/xml/ns/javaee", "before-completion-method"), new QName("http://java.sun.com/xml/ns/javaee", "after-completion-method"), new QName("http://java.sun.com/xml/ns/javaee", "around-invoke"), new QName("http://java.sun.com/xml/ns/javaee", "around-timeout"), new QName("http://java.sun.com/xml/ns/javaee", "env-entry"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-ref"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-local-ref"), new QName("http://java.sun.com/xml/ns/javaee", "service-ref"), new QName("http://java.sun.com/xml/ns/javaee", "resource-ref"), new QName("http://java.sun.com/xml/ns/javaee", "resource-env-ref"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination-ref"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-context-ref"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-unit-ref"), new QName("http://java.sun.com/xml/ns/javaee", "post-construct"), new QName("http://java.sun.com/xml/ns/javaee", "pre-destroy"), new QName("http://java.sun.com/xml/ns/javaee", "data-source"), new QName("http://java.sun.com/xml/ns/javaee", "post-activate"), new QName("http://java.sun.com/xml/ns/javaee", "pre-passivate"), new QName("http://java.sun.com/xml/ns/javaee", "security-role-ref"), new QName("http://java.sun.com/xml/ns/javaee", "security-identity"));
            }
        }
        if (descriptions != null) {
            try {
                sessionBean.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, SessionBean.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                sessionBean.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, SessionBean.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            sessionBean.icon = icon;
        }
        if (businessLocal != null) {
            sessionBean.businessLocal = businessLocal;
        }
        if (businessRemote != null) {
            sessionBean.businessRemote = businessRemote;
        }
        if (timer != null) {
            sessionBean.timer = timer;
        }
        if (concurrentMethod != null) {
            sessionBean.concurrentMethod = concurrentMethod;
        }
        if (initMethod != null) {
            sessionBean.initMethod = initMethod;
        }
        if (removeMethod != null) {
            sessionBean.removeMethod = removeMethod;
        }
        if (asyncMethod != null) {
            sessionBean.asyncMethod = asyncMethod;
        }
        if (aroundInvoke != null) {
            sessionBean.aroundInvoke = aroundInvoke;
        }
        if (aroundTimeout != null) {
            sessionBean.aroundTimeout = aroundTimeout;
        }
        if (envEntry != null) {
            sessionBean.envEntry = envEntry;
        }
        if (ejbRef != null) {
            sessionBean.ejbRef = ejbRef;
        }
        if (ejbLocalRef != null) {
            sessionBean.ejbLocalRef = ejbLocalRef;
        }
        if (serviceRef != null) {
            sessionBean.serviceRef = serviceRef;
        }
        if (resourceRef != null) {
            sessionBean.resourceRef = resourceRef;
        }
        if (resourceEnvRef != null) {
            sessionBean.resourceEnvRef = resourceEnvRef;
        }
        if (messageDestinationRef != null) {
            sessionBean.messageDestinationRef = messageDestinationRef;
        }
        if (persistenceContextRef != null) {
            sessionBean.persistenceContextRef = persistenceContextRef;
        }
        if (persistenceUnitRef != null) {
            sessionBean.persistenceUnitRef = persistenceUnitRef;
        }
        if (postConstruct != null) {
            sessionBean.postConstruct = postConstruct;
        }
        if (preDestroy != null) {
            sessionBean.preDestroy = preDestroy;
        }
        if (dataSource != null) {
            sessionBean.dataSource = dataSource;
        }
        if (postActivate != null) {
            sessionBean.postActivate = postActivate;
        }
        if (prePassivate != null) {
            sessionBean.prePassivate = prePassivate;
        }
        if (securityRoleRef != null) {
            sessionBean.securityRoleRef = securityRoleRef;
        }

        context.afterUnmarshal(sessionBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);

        return sessionBean;
    }

    public final SessionBean read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _readDependsOn(final XoXMLStreamReader reader, final RuntimeContext context, final SessionBean sessionBean)
        throws Exception {
        List<String> dependsOn = null;

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("ejb-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: dependsOn
                final String dependsOnItemRaw = elementReader.getElementAsString();

                final String dependsOnItem;
                try {
                    dependsOnItem = Adapters.collapsedStringAdapterAdapter.unmarshal(dependsOnItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (dependsOn == null) {
                    dependsOn = sessionBean.dependsOn;
                    if (dependsOn != null) {
                        dependsOn.clear();
                    } else {
                        dependsOn = new ArrayList<String>();
                    }
                }
                dependsOn.add(dependsOnItem);
            }
        }
        if (dependsOn != null) {
            sessionBean.dependsOn = dependsOn;
        }
    }

    public final static void _write(final XoXMLStreamWriter writer, final SessionBean sessionBean, RuntimeContext context)
        throws Exception {
        if (sessionBean == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        
        if (! sessionBeanClasses.contains(sessionBean.getClass().getName())) {
            context.unexpectedSubclass(writer, sessionBean, SessionBean.class);
            return;
        }

        context.beforeMarshal(sessionBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = sessionBean.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(sessionBean, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = sessionBean.getDescriptions();
        } catch (final Exception e) {
            context.getterError(sessionBean, "descriptions", SessionBean.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = sessionBean.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(sessionBean, "displayNames", SessionBean.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = sessionBean.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "icon");
                }
            }
        }

        // ELEMENT: ejbName
        final String ejbNameRaw = sessionBean.ejbName;
        String ejbName = null;
        try {
            ejbName = Adapters.collapsedStringAdapterAdapter.marshal(ejbNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(sessionBean, "ejbName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbName != null) {
            writer.writeStartElement(prefix, "ejb-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(sessionBean, "ejbName");
        }

        // ELEMENT: mappedName
        final String mappedNameRaw = sessionBean.mappedName;
        String mappedName = null;
        try {
            mappedName = Adapters.collapsedStringAdapterAdapter.marshal(mappedNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(sessionBean, "mappedName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (mappedName != null) {
            writer.writeStartElement(prefix, "mapped-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(mappedName);
            writer.writeEndElement();
        }

        // ELEMENT: home
        final String homeRaw = sessionBean.home;
        String home = null;
        try {
            home = Adapters.collapsedStringAdapterAdapter.marshal(homeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(sessionBean, "home", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (home != null) {
            writer.writeStartElement(prefix, "home", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(home);
            writer.writeEndElement();
        }

        // ELEMENT: remote
        final String remoteRaw = sessionBean.remote;
        String remote = null;
        try {
            remote = Adapters.collapsedStringAdapterAdapter.marshal(remoteRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(sessionBean, "remote", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (remote != null) {
            writer.writeStartElement(prefix, "remote", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(remote);
            writer.writeEndElement();
        }

        // ELEMENT: localHome
        final String localHomeRaw = sessionBean.localHome;
        String localHome = null;
        try {
            localHome = Adapters.collapsedStringAdapterAdapter.marshal(localHomeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(sessionBean, "localHome", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (localHome != null) {
            writer.writeStartElement(prefix, "local-home", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(localHome);
            writer.writeEndElement();
        }

        // ELEMENT: local
        final String localRaw = sessionBean.local;
        String local = null;
        try {
            local = Adapters.collapsedStringAdapterAdapter.marshal(localRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(sessionBean, "local", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (local != null) {
            writer.writeStartElement(prefix, "local", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(local);
            writer.writeEndElement();
        }

        // ELEMENT: businessLocal
        final LinkedHashSet<String> businessLocalRaw = sessionBean.businessLocal;
        if (businessLocalRaw != null) {
            for (final String businessLocalItem : businessLocalRaw) {
                String businessLocal = null;
                try {
                    businessLocal = Adapters.collapsedStringAdapterAdapter.marshal(businessLocalItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(sessionBean, "businessLocal", CollapsedStringAdapter.class, LinkedHashSet.class, LinkedHashSet.class, e);
                }
                if (businessLocal != null) {
                    writer.writeStartElement(prefix, "business-local", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(businessLocal);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: businessRemote
        final LinkedHashSet<String> businessRemoteRaw = sessionBean.businessRemote;
        if (businessRemoteRaw != null) {
            for (final String businessRemoteItem : businessRemoteRaw) {
                String businessRemote = null;
                try {
                    businessRemote = Adapters.collapsedStringAdapterAdapter.marshal(businessRemoteItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(sessionBean, "businessRemote", CollapsedStringAdapter.class, LinkedHashSet.class, LinkedHashSet.class, e);
                }
                if (businessRemote != null) {
                    writer.writeStartElement(prefix, "business-remote", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(businessRemote);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: localBean
        final Empty localBean = sessionBean.localBean;
        if (localBean != null) {
            writer.writeStartElement(prefix, "local-bean", "http://java.sun.com/xml/ns/javaee");
            writeEmpty(writer, localBean, context);
            writer.writeEndElement();
        }

        // ELEMENT: serviceEndpoint
        final String serviceEndpointRaw = sessionBean.serviceEndpoint;
        String serviceEndpoint = null;
        try {
            serviceEndpoint = Adapters.collapsedStringAdapterAdapter.marshal(serviceEndpointRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(sessionBean, "serviceEndpoint", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (serviceEndpoint != null) {
            writer.writeStartElement(prefix, "service-endpoint", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(serviceEndpoint);
            writer.writeEndElement();
        }

        // ELEMENT: ejbClass
        final String ejbClassRaw = sessionBean.ejbClass;
        String ejbClass = null;
        try {
            ejbClass = Adapters.collapsedStringAdapterAdapter.marshal(ejbClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(sessionBean, "ejbClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbClass != null) {
            writer.writeStartElement(prefix, "ejb-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbClass);
            writer.writeEndElement();
        }

        // ELEMENT: sessionType
        final SessionType sessionType = sessionBean.sessionType;
        if (sessionType != null) {
            writer.writeStartElement(prefix, "session-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringSessionType(sessionBean, null, context, sessionType));
            writer.writeEndElement();
        }

        // ELEMENT: statefulTimeout
        final Timeout statefulTimeout = sessionBean.statefulTimeout;
        if (statefulTimeout != null) {
            writer.writeStartElement(prefix, "stateful-timeout", "http://java.sun.com/xml/ns/javaee");
            writeTimeout(writer, statefulTimeout, context);
            writer.writeEndElement();
        }

        // ELEMENT: timeoutMethod
        final NamedMethod timeoutMethod = sessionBean.timeoutMethod;
        if (timeoutMethod != null) {
            writer.writeStartElement(prefix, "timeout-method", "http://java.sun.com/xml/ns/javaee");
            writeNamedMethod(writer, timeoutMethod, context);
            writer.writeEndElement();
        }

        // ELEMENT: timer
        final List<Timer> timer = sessionBean.timer;
        if (timer != null) {
            for (final Timer timerItem : timer) {
                if (timerItem != null) {
                    writer.writeStartElement(prefix, "timer", "http://java.sun.com/xml/ns/javaee");
                    writeTimer(writer, timerItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: initOnStartup
        final Boolean initOnStartup = sessionBean.initOnStartup;
        if (initOnStartup != null) {
            writer.writeStartElement(prefix, "init-on-startup", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(initOnStartup));
            writer.writeEndElement();
        }

        // ELEMENT: concurrencyManagementType
        final ConcurrencyManagementType concurrencyManagementType = sessionBean.concurrencyManagementType;
        if (concurrencyManagementType != null) {
            writer.writeStartElement(prefix, "concurrency-management-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringConcurrencyManagementType(sessionBean, null, context, concurrencyManagementType));
            writer.writeEndElement();
        }

        // ELEMENT: concurrentMethod
        final List<ConcurrentMethod> concurrentMethod = sessionBean.concurrentMethod;
        if (concurrentMethod != null) {
            for (final ConcurrentMethod concurrentMethodItem : concurrentMethod) {
                if (concurrentMethodItem != null) {
                    writer.writeStartElement(prefix, "concurrent-method", "http://java.sun.com/xml/ns/javaee");
                    writeConcurrentMethod(writer, concurrentMethodItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: dependsOn
        final List<String> dependsOnRaw = sessionBean.dependsOn;
        if (dependsOnRaw != null) {
            writer.writeStartElement(prefix, "depends-on", "http://java.sun.com/xml/ns/javaee");
            for (final String dependsOnItem : dependsOnRaw) {
                String dependsOn = null;
                try {
                    dependsOn = Adapters.collapsedStringAdapterAdapter.marshal(dependsOnItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(sessionBean, "dependsOn", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (dependsOn != null) {
                    writer.writeStartElement(prefix, "ejb-name", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(dependsOn);
                    writer.writeEndElement();
                }
            }
            writer.writeEndElement();
        }

        // ELEMENT: initMethod
        final List<InitMethod> initMethod = sessionBean.initMethod;
        if (initMethod != null) {
            for (final InitMethod initMethodItem : initMethod) {
                if (initMethodItem != null) {
                    writer.writeStartElement(prefix, "init-method", "http://java.sun.com/xml/ns/javaee");
                    writeInitMethod(writer, initMethodItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: removeMethod
        final List<RemoveMethod> removeMethod = sessionBean.removeMethod;
        if (removeMethod != null) {
            for (final RemoveMethod removeMethodItem : removeMethod) {
                if (removeMethodItem != null) {
                    writer.writeStartElement(prefix, "remove-method", "http://java.sun.com/xml/ns/javaee");
                    writeRemoveMethod(writer, removeMethodItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: asyncMethod
        final List<AsyncMethod> asyncMethod = sessionBean.asyncMethod;
        if (asyncMethod != null) {
            for (final AsyncMethod asyncMethodItem : asyncMethod) {
                if (asyncMethodItem != null) {
                    writer.writeStartElement(prefix, "async-method", "http://java.sun.com/xml/ns/javaee");
                    writeAsyncMethod(writer, asyncMethodItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: transactionType
        final TransactionType transactionType = sessionBean.transactionType;
        if (transactionType != null) {
            writer.writeStartElement(prefix, "transaction-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringTransactionType(sessionBean, null, context, transactionType));
            writer.writeEndElement();
        }

        // ELEMENT: afterBeginMethod
        NamedMethod afterBeginMethod = null;
        try {
            afterBeginMethod = sessionBean.getAfterBeginMethod();
        } catch (final Exception e) {
            context.getterError(sessionBean, "afterBeginMethod", SessionBean.class, "getAfterBeginMethod", e);
        }
        if (afterBeginMethod != null) {
            writer.writeStartElement(prefix, "after-begin-method", "http://java.sun.com/xml/ns/javaee");
            writeNamedMethod(writer, afterBeginMethod, context);
            writer.writeEndElement();
        }

        // ELEMENT: beforeCompletionMethod
        NamedMethod beforeCompletionMethod = null;
        try {
            beforeCompletionMethod = sessionBean.getBeforeCompletionMethod();
        } catch (final Exception e) {
            context.getterError(sessionBean, "beforeCompletionMethod", SessionBean.class, "getBeforeCompletionMethod", e);
        }
        if (beforeCompletionMethod != null) {
            writer.writeStartElement(prefix, "before-completion-method", "http://java.sun.com/xml/ns/javaee");
            writeNamedMethod(writer, beforeCompletionMethod, context);
            writer.writeEndElement();
        }

        // ELEMENT: afterCompletionMethod
        NamedMethod afterCompletionMethod = null;
        try {
            afterCompletionMethod = sessionBean.getAfterCompletionMethod();
        } catch (final Exception e) {
            context.getterError(sessionBean, "afterCompletionMethod", SessionBean.class, "getAfterCompletionMethod", e);
        }
        if (afterCompletionMethod != null) {
            writer.writeStartElement(prefix, "after-completion-method", "http://java.sun.com/xml/ns/javaee");
            writeNamedMethod(writer, afterCompletionMethod, context);
            writer.writeEndElement();
        }

        // ELEMENT: aroundInvoke
        final List<AroundInvoke> aroundInvoke = sessionBean.aroundInvoke;
        if (aroundInvoke != null) {
            for (final AroundInvoke aroundInvokeItem : aroundInvoke) {
                if (aroundInvokeItem != null) {
                    writer.writeStartElement(prefix, "around-invoke", "http://java.sun.com/xml/ns/javaee");
                    writeAroundInvoke(writer, aroundInvokeItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "aroundInvoke");
                }
            }
        }

        // ELEMENT: aroundTimeout
        final List<AroundTimeout> aroundTimeout = sessionBean.aroundTimeout;
        if (aroundTimeout != null) {
            for (final AroundTimeout aroundTimeoutItem : aroundTimeout) {
                if (aroundTimeoutItem != null) {
                    writer.writeStartElement(prefix, "around-timeout", "http://java.sun.com/xml/ns/javaee");
                    writeAroundTimeout(writer, aroundTimeoutItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: envEntry
        final KeyedCollection<String, EnvEntry> envEntry = sessionBean.envEntry;
        if (envEntry != null) {
            for (final EnvEntry envEntryItem : envEntry) {
                if (envEntryItem != null) {
                    writer.writeStartElement(prefix, "env-entry", "http://java.sun.com/xml/ns/javaee");
                    writeEnvEntry(writer, envEntryItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "envEntry");
                }
            }
        }

        // ELEMENT: ejbRef
        final KeyedCollection<String, EjbRef> ejbRef = sessionBean.ejbRef;
        if (ejbRef != null) {
            for (final EjbRef ejbRefItem : ejbRef) {
                if (ejbRefItem != null) {
                    writer.writeStartElement(prefix, "ejb-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbRef(writer, ejbRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "ejbRef");
                }
            }
        }

        // ELEMENT: ejbLocalRef
        final KeyedCollection<String, EjbLocalRef> ejbLocalRef = sessionBean.ejbLocalRef;
        if (ejbLocalRef != null) {
            for (final EjbLocalRef ejbLocalRefItem : ejbLocalRef) {
                if (ejbLocalRefItem != null) {
                    writer.writeStartElement(prefix, "ejb-local-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbLocalRef(writer, ejbLocalRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "ejbLocalRef");
                }
            }
        }

        // ELEMENT: serviceRef
        final KeyedCollection<String, ServiceRef> serviceRef = sessionBean.serviceRef;
        if (serviceRef != null) {
            for (final ServiceRef serviceRefItem : serviceRef) {
                if (serviceRefItem != null) {
                    writer.writeStartElement(prefix, "service-ref", "http://java.sun.com/xml/ns/javaee");
                    writeServiceRef(writer, serviceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "serviceRef");
                }
            }
        }

        // ELEMENT: resourceRef
        final KeyedCollection<String, ResourceRef> resourceRef = sessionBean.resourceRef;
        if (resourceRef != null) {
            for (final ResourceRef resourceRefItem : resourceRef) {
                if (resourceRefItem != null) {
                    writer.writeStartElement(prefix, "resource-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceRef(writer, resourceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "resourceRef");
                }
            }
        }

        // ELEMENT: resourceEnvRef
        final KeyedCollection<String, ResourceEnvRef> resourceEnvRef = sessionBean.resourceEnvRef;
        if (resourceEnvRef != null) {
            for (final ResourceEnvRef resourceEnvRefItem : resourceEnvRef) {
                if (resourceEnvRefItem != null) {
                    writer.writeStartElement(prefix, "resource-env-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceEnvRef(writer, resourceEnvRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "resourceEnvRef");
                }
            }
        }

        // ELEMENT: messageDestinationRef
        final KeyedCollection<String, MessageDestinationRef> messageDestinationRef = sessionBean.messageDestinationRef;
        if (messageDestinationRef != null) {
            for (final MessageDestinationRef messageDestinationRefItem : messageDestinationRef) {
                if (messageDestinationRefItem != null) {
                    writer.writeStartElement(prefix, "message-destination-ref", "http://java.sun.com/xml/ns/javaee");
                    writeMessageDestinationRef(writer, messageDestinationRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "messageDestinationRef");
                }
            }
        }

        // ELEMENT: persistenceContextRef
        final KeyedCollection<String, PersistenceContextRef> persistenceContextRef = sessionBean.persistenceContextRef;
        if (persistenceContextRef != null) {
            for (final PersistenceContextRef persistenceContextRefItem : persistenceContextRef) {
                if (persistenceContextRefItem != null) {
                    writer.writeStartElement(prefix, "persistence-context-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceContextRef(writer, persistenceContextRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "persistenceContextRef");
                }
            }
        }

        // ELEMENT: persistenceUnitRef
        final KeyedCollection<String, PersistenceUnitRef> persistenceUnitRef = sessionBean.persistenceUnitRef;
        if (persistenceUnitRef != null) {
            for (final PersistenceUnitRef persistenceUnitRefItem : persistenceUnitRef) {
                if (persistenceUnitRefItem != null) {
                    writer.writeStartElement(prefix, "persistence-unit-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceUnitRef(writer, persistenceUnitRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "persistenceUnitRef");
                }
            }
        }

        // ELEMENT: postConstruct
        final List<org.apache.openejb.jee.LifecycleCallback> postConstruct = sessionBean.postConstruct;
        if (postConstruct != null) {
            for (final org.apache.openejb.jee.LifecycleCallback postConstructItem : postConstruct) {
                if (postConstructItem != null) {
                    writer.writeStartElement(prefix, "post-construct", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, postConstructItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "postConstruct");
                }
            }
        }

        // ELEMENT: preDestroy
        final List<org.apache.openejb.jee.LifecycleCallback> preDestroy = sessionBean.preDestroy;
        if (preDestroy != null) {
            for (final org.apache.openejb.jee.LifecycleCallback preDestroyItem : preDestroy) {
                if (preDestroyItem != null) {
                    writer.writeStartElement(prefix, "pre-destroy", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, preDestroyItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "preDestroy");
                }
            }
        }

        // ELEMENT: dataSource
        final KeyedCollection<String, DataSource> dataSource = sessionBean.dataSource;
        if (dataSource != null) {
            for (final DataSource dataSourceItem : dataSource) {
                if (dataSourceItem != null) {
                    writer.writeStartElement(prefix, "data-source", "http://java.sun.com/xml/ns/javaee");
                    writeDataSource(writer, dataSourceItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: postActivate
        final List<org.apache.openejb.jee.LifecycleCallback> postActivate = sessionBean.postActivate;
        if (postActivate != null) {
            for (final org.apache.openejb.jee.LifecycleCallback postActivateItem : postActivate) {
                if (postActivateItem != null) {
                    writer.writeStartElement(prefix, "post-activate", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, postActivateItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "postActivate");
                }
            }
        }

        // ELEMENT: prePassivate
        final List<org.apache.openejb.jee.LifecycleCallback> prePassivate = sessionBean.prePassivate;
        if (prePassivate != null) {
            for (final org.apache.openejb.jee.LifecycleCallback prePassivateItem : prePassivate) {
                if (prePassivateItem != null) {
                    writer.writeStartElement(prefix, "pre-passivate", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, prePassivateItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "prePassivate");
                }
            }
        }

        // ELEMENT: securityRoleRef
        final List<SecurityRoleRef> securityRoleRef = sessionBean.securityRoleRef;
        if (securityRoleRef != null) {
            for (final SecurityRoleRef securityRoleRefItem : securityRoleRef) {
                if (securityRoleRefItem != null) {
                    writer.writeStartElement(prefix, "security-role-ref", "http://java.sun.com/xml/ns/javaee");
                    writeSecurityRoleRef(writer, securityRoleRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(sessionBean, "securityRoleRef");
                }
            }
        }

        // ELEMENT: securityIdentity
        final SecurityIdentity securityIdentity = sessionBean.securityIdentity;
        if (securityIdentity != null) {
            writer.writeStartElement(prefix, "security-identity", "http://java.sun.com/xml/ns/javaee");
            writeSecurityIdentity(writer, securityIdentity, context);
            writer.writeEndElement();
        }

        context.afterMarshal(sessionBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);
    }

}
