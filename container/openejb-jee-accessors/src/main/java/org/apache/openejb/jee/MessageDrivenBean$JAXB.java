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

import static org.apache.openejb.jee.ActivationConfig$JAXB.readActivationConfig;
import static org.apache.openejb.jee.ActivationConfig$JAXB.writeActivationConfig;
import static org.apache.openejb.jee.AroundInvoke$JAXB.readAroundInvoke;
import static org.apache.openejb.jee.AroundInvoke$JAXB.writeAroundInvoke;
import static org.apache.openejb.jee.AroundTimeout$JAXB.readAroundTimeout;
import static org.apache.openejb.jee.AroundTimeout$JAXB.writeAroundTimeout;
import static org.apache.openejb.jee.DataSource$JAXB.readDataSource;
import static org.apache.openejb.jee.DataSource$JAXB.writeDataSource;
import static org.apache.openejb.jee.EjbLocalRef$JAXB.readEjbLocalRef;
import static org.apache.openejb.jee.EjbLocalRef$JAXB.writeEjbLocalRef;
import static org.apache.openejb.jee.EjbRef$JAXB.readEjbRef;
import static org.apache.openejb.jee.EjbRef$JAXB.writeEjbRef;
import static org.apache.openejb.jee.EnvEntry$JAXB.readEnvEntry;
import static org.apache.openejb.jee.EnvEntry$JAXB.writeEnvEntry;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.LifecycleCallback$JAXB.readLifecycleCallback;
import static org.apache.openejb.jee.LifecycleCallback$JAXB.writeLifecycleCallback;
import static org.apache.openejb.jee.MessageDestinationRef$JAXB.readMessageDestinationRef;
import static org.apache.openejb.jee.MessageDestinationRef$JAXB.writeMessageDestinationRef;
import static org.apache.openejb.jee.MessageDrivenDestination$JAXB.readMessageDrivenDestination;
import static org.apache.openejb.jee.MessageDrivenDestination$JAXB.writeMessageDrivenDestination;
import static org.apache.openejb.jee.NamedMethod$JAXB.readNamedMethod;
import static org.apache.openejb.jee.NamedMethod$JAXB.writeNamedMethod;
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.readPersistenceContextRef;
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.writePersistenceContextRef;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.readPersistenceUnitRef;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.writePersistenceUnitRef;
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
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;
import static org.apache.openejb.jee.Timer$JAXB.readTimer;
import static org.apache.openejb.jee.Timer$JAXB.writeTimer;
import static org.apache.openejb.jee.TransactionType$JAXB.parseTransactionType;
import static org.apache.openejb.jee.TransactionType$JAXB.toStringTransactionType;

@SuppressWarnings({
    "StringEquality"
})
public class MessageDrivenBean$JAXB
    extends JAXBObject<MessageDrivenBean> {


    public MessageDrivenBean$JAXB() {
        super(MessageDrivenBean.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "message-driven-beanType".intern()), Text$JAXB.class, Icon$JAXB.class, NamedMethod$JAXB.class, Timer$JAXB.class, TransactionType$JAXB.class, MessageDrivenDestination$JAXB.class, ActivationConfig$JAXB.class, AroundInvoke$JAXB.class, AroundTimeout$JAXB.class, EnvEntry$JAXB.class, EjbRef$JAXB.class, EjbLocalRef$JAXB.class, ServiceRef$JAXB.class, ResourceRef$JAXB.class, ResourceEnvRef$JAXB.class, MessageDestinationRef$JAXB.class, PersistenceContextRef$JAXB.class, PersistenceUnitRef$JAXB.class, LifecycleCallback$JAXB.class, DataSource$JAXB.class, SecurityRoleRef$JAXB.class, SecurityIdentity$JAXB.class);
    }

    public static MessageDrivenBean readMessageDrivenBean(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeMessageDrivenBean(final XoXMLStreamWriter writer, final MessageDrivenBean messageDrivenBean, final RuntimeContext context)
        throws Exception {
        _write(writer, messageDrivenBean, context);
    }

    public void write(final XoXMLStreamWriter writer, final MessageDrivenBean messageDrivenBean, final RuntimeContext context)
        throws Exception {
        _write(writer, messageDrivenBean, context);
    }

    public final static MessageDrivenBean _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final MessageDrivenBean messageDrivenBean = new MessageDrivenBean();
        context.beforeUnmarshal(messageDrivenBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<Timer> timer = null;
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
        List<SecurityRoleRef> securityRoleRef = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("message-driven-beanType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, MessageDrivenBean.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, messageDrivenBean);
                messageDrivenBean.id = id;
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
                    icon = messageDrivenBean.icon;
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

                messageDrivenBean.ejbName = ejbName;
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

                messageDrivenBean.mappedName = mappedName;
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

                messageDrivenBean.ejbClass = ejbClass;
            } else if (("messaging-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messagingType
                final String messagingTypeRaw = elementReader.getElementAsString();

                final String messagingType;
                try {
                    messagingType = Adapters.collapsedStringAdapterAdapter.unmarshal(messagingTypeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                messageDrivenBean.messagingType = messagingType;
            } else if (("timeout-method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: timeoutMethod
                final NamedMethod timeoutMethod = readNamedMethod(elementReader, context);
                messageDrivenBean.timeoutMethod = timeoutMethod;
            } else if (("timer" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: timer
                final Timer timerItem = readTimer(elementReader, context);
                if (timer == null) {
                    timer = messageDrivenBean.timer;
                    if (timer != null) {
                        timer.clear();
                    } else {
                        timer = new ArrayList<Timer>();
                    }
                }
                timer.add(timerItem);
            } else if (("transaction-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: transactionType
                final TransactionType transactionType = parseTransactionType(elementReader, context, elementReader.getElementAsString());
                if (transactionType != null) {
                    messageDrivenBean.transactionType = transactionType;
                }
            } else if (("message-selector" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageSelector
                final String messageSelectorRaw = elementReader.getElementAsString();

                final String messageSelector;
                try {
                    messageSelector = Adapters.collapsedStringAdapterAdapter.unmarshal(messageSelectorRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                try {
                    messageDrivenBean.setMessageSelector(messageSelector);
                } catch (final Exception e) {
                    context.setterError(reader, MessageDrivenBean.class, "setMessageSelector", String.class, e);
                }
            } else if (("acknowledge-mode" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: acknowledgeMode
                final String acknowledgeModeRaw = elementReader.getElementAsString();

                final String acknowledgeMode;
                try {
                    acknowledgeMode = Adapters.collapsedStringAdapterAdapter.unmarshal(acknowledgeModeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                try {
                    messageDrivenBean.setAcknowledgeMode(acknowledgeMode);
                } catch (final Exception e) {
                    context.setterError(reader, MessageDrivenBean.class, "setAcknowledgeMode", String.class, e);
                }
            } else if (("message-driven-destination" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageDrivenDestination
                final MessageDrivenDestination messageDrivenDestination = readMessageDrivenDestination(elementReader, context);
                try {
                    messageDrivenBean.setMessageDrivenDestination(messageDrivenDestination);
                } catch (final Exception e) {
                    context.setterError(reader, MessageDrivenBean.class, "setMessageDrivenDestination", MessageDrivenDestination.class, e);
                }
            } else if (("message-destination-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageDestinationType
                final String messageDestinationTypeRaw = elementReader.getElementAsString();

                final String messageDestinationType;
                try {
                    messageDestinationType = Adapters.collapsedStringAdapterAdapter.unmarshal(messageDestinationTypeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                messageDrivenBean.messageDestinationType = messageDestinationType;
            } else if (("message-destination-link" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageDestinationLink
                final String messageDestinationLinkRaw = elementReader.getElementAsString();

                final String messageDestinationLink;
                try {
                    messageDestinationLink = Adapters.collapsedStringAdapterAdapter.unmarshal(messageDestinationLinkRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                messageDrivenBean.messageDestinationLink = messageDestinationLink;
            } else if (("activation-config" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: activationConfig
                final ActivationConfig activationConfig = readActivationConfig(elementReader, context);
                messageDrivenBean.activationConfig = activationConfig;
            } else if (("around-invoke" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: aroundInvoke
                final AroundInvoke aroundInvokeItem = readAroundInvoke(elementReader, context);
                if (aroundInvoke == null) {
                    aroundInvoke = messageDrivenBean.aroundInvoke;
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
                    aroundTimeout = messageDrivenBean.aroundTimeout;
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
                    envEntry = messageDrivenBean.envEntry;
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
                    ejbRef = messageDrivenBean.ejbRef;
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
                    ejbLocalRef = messageDrivenBean.ejbLocalRef;
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
                    serviceRef = messageDrivenBean.serviceRef;
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
                    resourceRef = messageDrivenBean.resourceRef;
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
                    resourceEnvRef = messageDrivenBean.resourceEnvRef;
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
                    messageDestinationRef = messageDrivenBean.messageDestinationRef;
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
                    persistenceContextRef = messageDrivenBean.persistenceContextRef;
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
                    persistenceUnitRef = messageDrivenBean.persistenceUnitRef;
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
                    postConstruct = messageDrivenBean.postConstruct;
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
                    preDestroy = messageDrivenBean.preDestroy;
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
                    dataSource = messageDrivenBean.dataSource;
                    if (dataSource != null) {
                        dataSource.clear();
                    } else {
                        dataSource = new KeyedCollection<String, DataSource>();
                    }
                }
                dataSource.add(dataSourceItem);
            } else if (("security-role-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: securityRoleRef
                final SecurityRoleRef securityRoleRefItem = readSecurityRoleRef(elementReader, context);
                if (securityRoleRef == null) {
                    securityRoleRef = messageDrivenBean.securityRoleRef;
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
                messageDrivenBean.securityIdentity = securityIdentity;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-name"), new QName("http://java.sun.com/xml/ns/javaee", "mapped-name"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-class"), new QName("http://java.sun.com/xml/ns/javaee", "messaging-type"), new QName("http://java.sun.com/xml/ns/javaee", "timeout-method"), new QName("http://java.sun.com/xml/ns/javaee", "timer"), new QName("http://java.sun.com/xml/ns/javaee", "transaction-type"), new QName("http://java.sun.com/xml/ns/javaee", "message-selector"), new QName("http://java.sun.com/xml/ns/javaee", "acknowledge-mode"), new QName("http://java.sun.com/xml/ns/javaee", "message-driven-destination"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination-type"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination-link"), new QName("http://java.sun.com/xml/ns/javaee", "activation-config"), new QName("http://java.sun.com/xml/ns/javaee", "around-invoke"), new QName("http://java.sun.com/xml/ns/javaee", "around-timeout"), new QName("http://java.sun.com/xml/ns/javaee", "env-entry"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-ref"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-local-ref"), new QName("http://java.sun.com/xml/ns/javaee", "service-ref"), new QName("http://java.sun.com/xml/ns/javaee", "resource-ref"), new QName("http://java.sun.com/xml/ns/javaee", "resource-env-ref"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination-ref"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-context-ref"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-unit-ref"), new QName("http://java.sun.com/xml/ns/javaee", "post-construct"), new QName("http://java.sun.com/xml/ns/javaee", "pre-destroy"), new QName("http://java.sun.com/xml/ns/javaee", "data-source"), new QName("http://java.sun.com/xml/ns/javaee", "security-role-ref"), new QName("http://java.sun.com/xml/ns/javaee", "security-identity"));
            }
        }
        if (descriptions != null) {
            try {
                messageDrivenBean.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, MessageDrivenBean.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                messageDrivenBean.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, MessageDrivenBean.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            messageDrivenBean.icon = icon;
        }
        if (timer != null) {
            messageDrivenBean.timer = timer;
        }
        if (aroundInvoke != null) {
            messageDrivenBean.aroundInvoke = aroundInvoke;
        }
        if (aroundTimeout != null) {
            messageDrivenBean.aroundTimeout = aroundTimeout;
        }
        if (envEntry != null) {
            messageDrivenBean.envEntry = envEntry;
        }
        if (ejbRef != null) {
            messageDrivenBean.ejbRef = ejbRef;
        }
        if (ejbLocalRef != null) {
            messageDrivenBean.ejbLocalRef = ejbLocalRef;
        }
        if (serviceRef != null) {
            messageDrivenBean.serviceRef = serviceRef;
        }
        if (resourceRef != null) {
            messageDrivenBean.resourceRef = resourceRef;
        }
        if (resourceEnvRef != null) {
            messageDrivenBean.resourceEnvRef = resourceEnvRef;
        }
        if (messageDestinationRef != null) {
            messageDrivenBean.messageDestinationRef = messageDestinationRef;
        }
        if (persistenceContextRef != null) {
            messageDrivenBean.persistenceContextRef = persistenceContextRef;
        }
        if (persistenceUnitRef != null) {
            messageDrivenBean.persistenceUnitRef = persistenceUnitRef;
        }
        if (postConstruct != null) {
            messageDrivenBean.postConstruct = postConstruct;
        }
        if (preDestroy != null) {
            messageDrivenBean.preDestroy = preDestroy;
        }
        if (dataSource != null) {
            messageDrivenBean.dataSource = dataSource;
        }
        if (securityRoleRef != null) {
            messageDrivenBean.securityRoleRef = securityRoleRef;
        }

        context.afterUnmarshal(messageDrivenBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);

        return messageDrivenBean;
    }

    public final MessageDrivenBean read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final MessageDrivenBean messageDrivenBean, RuntimeContext context)
        throws Exception {
        if (messageDrivenBean == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (MessageDrivenBean.class != messageDrivenBean.getClass()) {
            context.unexpectedSubclass(writer, messageDrivenBean, MessageDrivenBean.class);
            return;
        }

        context.beforeMarshal(messageDrivenBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = messageDrivenBean.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(messageDrivenBean, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = messageDrivenBean.getDescriptions();
        } catch (final Exception e) {
            context.getterError(messageDrivenBean, "descriptions", MessageDrivenBean.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = messageDrivenBean.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(messageDrivenBean, "displayNames", MessageDrivenBean.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = messageDrivenBean.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "icon");
                }
            }
        }

        // ELEMENT: ejbName
        final String ejbNameRaw = messageDrivenBean.ejbName;
        String ejbName = null;
        try {
            ejbName = Adapters.collapsedStringAdapterAdapter.marshal(ejbNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(messageDrivenBean, "ejbName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbName != null) {
            writer.writeStartElement(prefix, "ejb-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(messageDrivenBean, "ejbName");
        }

        // ELEMENT: mappedName
        final String mappedNameRaw = messageDrivenBean.mappedName;
        String mappedName = null;
        try {
            mappedName = Adapters.collapsedStringAdapterAdapter.marshal(mappedNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(messageDrivenBean, "mappedName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (mappedName != null) {
            writer.writeStartElement(prefix, "mapped-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(mappedName);
            writer.writeEndElement();
        }

        // ELEMENT: ejbClass
        final String ejbClassRaw = messageDrivenBean.ejbClass;
        String ejbClass = null;
        try {
            ejbClass = Adapters.collapsedStringAdapterAdapter.marshal(ejbClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(messageDrivenBean, "ejbClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbClass != null) {
            writer.writeStartElement(prefix, "ejb-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbClass);
            writer.writeEndElement();
        }

        // ELEMENT: messagingType
        final String messagingTypeRaw = messageDrivenBean.messagingType;
        String messagingType = null;
        try {
            messagingType = Adapters.collapsedStringAdapterAdapter.marshal(messagingTypeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(messageDrivenBean, "messagingType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (messagingType != null) {
            writer.writeStartElement(prefix, "messaging-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(messagingType);
            writer.writeEndElement();
        }

        // ELEMENT: timeoutMethod
        final NamedMethod timeoutMethod = messageDrivenBean.timeoutMethod;
        if (timeoutMethod != null) {
            writer.writeStartElement(prefix, "timeout-method", "http://java.sun.com/xml/ns/javaee");
            writeNamedMethod(writer, timeoutMethod, context);
            writer.writeEndElement();
        }

        // ELEMENT: timer
        final List<Timer> timer = messageDrivenBean.timer;
        if (timer != null) {
            for (final Timer timerItem : timer) {
                writer.writeStartElement(prefix, "timer", "http://java.sun.com/xml/ns/javaee");
                if (timerItem != null) {
                    writeTimer(writer, timerItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: transactionType
        final TransactionType transactionType = messageDrivenBean.transactionType;
        if (transactionType != null) {
            writer.writeStartElement(prefix, "transaction-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringTransactionType(messageDrivenBean, null, context, transactionType));
            writer.writeEndElement();
        }

        // ELEMENT: messageSelector
        String messageSelectorRaw = null;
        try {
            messageSelectorRaw = messageDrivenBean.getMessageSelector();
        } catch (final Exception e) {
            context.getterError(messageDrivenBean, "messageSelector", MessageDrivenBean.class, "getMessageSelector", e);
        }
        String messageSelector = null;
        try {
            messageSelector = Adapters.collapsedStringAdapterAdapter.marshal(messageSelectorRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(messageDrivenBean, "messageSelector", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (messageSelector != null) {
            writer.writeStartElement(prefix, "message-selector", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(messageSelector);
            writer.writeEndElement();
        }

        // ELEMENT: acknowledgeMode
        String acknowledgeModeRaw = null;
        try {
            acknowledgeModeRaw = messageDrivenBean.getAcknowledgeMode();
        } catch (final Exception e) {
            context.getterError(messageDrivenBean, "acknowledgeMode", MessageDrivenBean.class, "getAcknowledgeMode", e);
        }
        String acknowledgeMode = null;
        try {
            acknowledgeMode = Adapters.collapsedStringAdapterAdapter.marshal(acknowledgeModeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(messageDrivenBean, "acknowledgeMode", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (acknowledgeMode != null) {
            writer.writeStartElement(prefix, "acknowledge-mode", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(acknowledgeMode);
            writer.writeEndElement();
        }

        // ELEMENT: messageDrivenDestination
        MessageDrivenDestination messageDrivenDestination = null;
        try {
            messageDrivenDestination = messageDrivenBean.getMessageDrivenDestination();
        } catch (final Exception e) {
            context.getterError(messageDrivenBean, "messageDrivenDestination", MessageDrivenBean.class, "getMessageDrivenDestination", e);
        }
        if (messageDrivenDestination != null) {
            writer.writeStartElement(prefix, "message-driven-destination", "http://java.sun.com/xml/ns/javaee");
            writeMessageDrivenDestination(writer, messageDrivenDestination, context);
            writer.writeEndElement();
        }

        // ELEMENT: messageDestinationType
        final String messageDestinationTypeRaw = messageDrivenBean.messageDestinationType;
        String messageDestinationType = null;
        try {
            messageDestinationType = Adapters.collapsedStringAdapterAdapter.marshal(messageDestinationTypeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(messageDrivenBean, "messageDestinationType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (messageDestinationType != null) {
            writer.writeStartElement(prefix, "message-destination-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(messageDestinationType);
            writer.writeEndElement();
        }

        // ELEMENT: messageDestinationLink
        final String messageDestinationLinkRaw = messageDrivenBean.messageDestinationLink;
        String messageDestinationLink = null;
        try {
            messageDestinationLink = Adapters.collapsedStringAdapterAdapter.marshal(messageDestinationLinkRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(messageDrivenBean, "messageDestinationLink", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (messageDestinationLink != null) {
            writer.writeStartElement(prefix, "message-destination-link", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(messageDestinationLink);
            writer.writeEndElement();
        }

        // ELEMENT: activationConfig
        final ActivationConfig activationConfig = messageDrivenBean.activationConfig;
        if (activationConfig != null) {
            writer.writeStartElement(prefix, "activation-config", "http://java.sun.com/xml/ns/javaee");
            writeActivationConfig(writer, activationConfig, context);
            writer.writeEndElement();
        }

        // ELEMENT: aroundInvoke
        final List<AroundInvoke> aroundInvoke = messageDrivenBean.aroundInvoke;
        if (aroundInvoke != null) {
            for (final AroundInvoke aroundInvokeItem : aroundInvoke) {
                if (aroundInvokeItem != null) {
                    writer.writeStartElement(prefix, "around-invoke", "http://java.sun.com/xml/ns/javaee");
                    writeAroundInvoke(writer, aroundInvokeItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "aroundInvoke");
                }
            }
        }

        // ELEMENT: aroundTimeout
        final List<AroundTimeout> aroundTimeout = messageDrivenBean.aroundTimeout;
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
        final KeyedCollection<String, EnvEntry> envEntry = messageDrivenBean.envEntry;
        if (envEntry != null) {
            for (final EnvEntry envEntryItem : envEntry) {
                if (envEntryItem != null) {
                    writer.writeStartElement(prefix, "env-entry", "http://java.sun.com/xml/ns/javaee");
                    writeEnvEntry(writer, envEntryItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "envEntry");
                }
            }
        }

        // ELEMENT: ejbRef
        final KeyedCollection<String, EjbRef> ejbRef = messageDrivenBean.ejbRef;
        if (ejbRef != null) {
            for (final EjbRef ejbRefItem : ejbRef) {
                if (ejbRefItem != null) {
                    writer.writeStartElement(prefix, "ejb-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbRef(writer, ejbRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "ejbRef");
                }
            }
        }

        // ELEMENT: ejbLocalRef
        final KeyedCollection<String, EjbLocalRef> ejbLocalRef = messageDrivenBean.ejbLocalRef;
        if (ejbLocalRef != null) {
            for (final EjbLocalRef ejbLocalRefItem : ejbLocalRef) {
                if (ejbLocalRefItem != null) {
                    writer.writeStartElement(prefix, "ejb-local-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbLocalRef(writer, ejbLocalRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "ejbLocalRef");
                }
            }
        }

        // ELEMENT: serviceRef
        final KeyedCollection<String, ServiceRef> serviceRef = messageDrivenBean.serviceRef;
        if (serviceRef != null) {
            for (final ServiceRef serviceRefItem : serviceRef) {
                if (serviceRefItem != null) {
                    writer.writeStartElement(prefix, "service-ref", "http://java.sun.com/xml/ns/javaee");
                    writeServiceRef(writer, serviceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "serviceRef");
                }
            }
        }

        // ELEMENT: resourceRef
        final KeyedCollection<String, ResourceRef> resourceRef = messageDrivenBean.resourceRef;
        if (resourceRef != null) {
            for (final ResourceRef resourceRefItem : resourceRef) {
                if (resourceRefItem != null) {
                    writer.writeStartElement(prefix, "resource-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceRef(writer, resourceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "resourceRef");
                }
            }
        }

        // ELEMENT: resourceEnvRef
        final KeyedCollection<String, ResourceEnvRef> resourceEnvRef = messageDrivenBean.resourceEnvRef;
        if (resourceEnvRef != null) {
            for (final ResourceEnvRef resourceEnvRefItem : resourceEnvRef) {
                if (resourceEnvRefItem != null) {
                    writer.writeStartElement(prefix, "resource-env-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceEnvRef(writer, resourceEnvRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "resourceEnvRef");
                }
            }
        }

        // ELEMENT: messageDestinationRef
        final KeyedCollection<String, MessageDestinationRef> messageDestinationRef = messageDrivenBean.messageDestinationRef;
        if (messageDestinationRef != null) {
            for (final MessageDestinationRef messageDestinationRefItem : messageDestinationRef) {
                if (messageDestinationRefItem != null) {
                    writer.writeStartElement(prefix, "message-destination-ref", "http://java.sun.com/xml/ns/javaee");
                    writeMessageDestinationRef(writer, messageDestinationRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "messageDestinationRef");
                }
            }
        }

        // ELEMENT: persistenceContextRef
        final KeyedCollection<String, PersistenceContextRef> persistenceContextRef = messageDrivenBean.persistenceContextRef;
        if (persistenceContextRef != null) {
            for (final PersistenceContextRef persistenceContextRefItem : persistenceContextRef) {
                if (persistenceContextRefItem != null) {
                    writer.writeStartElement(prefix, "persistence-context-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceContextRef(writer, persistenceContextRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "persistenceContextRef");
                }
            }
        }

        // ELEMENT: persistenceUnitRef
        final KeyedCollection<String, PersistenceUnitRef> persistenceUnitRef = messageDrivenBean.persistenceUnitRef;
        if (persistenceUnitRef != null) {
            for (final PersistenceUnitRef persistenceUnitRefItem : persistenceUnitRef) {
                if (persistenceUnitRefItem != null) {
                    writer.writeStartElement(prefix, "persistence-unit-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceUnitRef(writer, persistenceUnitRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "persistenceUnitRef");
                }
            }
        }

        // ELEMENT: postConstruct
        final List<org.apache.openejb.jee.LifecycleCallback> postConstruct = messageDrivenBean.postConstruct;
        if (postConstruct != null) {
            for (final org.apache.openejb.jee.LifecycleCallback postConstructItem : postConstruct) {
                if (postConstructItem != null) {
                    writer.writeStartElement(prefix, "post-construct", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, postConstructItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "postConstruct");
                }
            }
        }

        // ELEMENT: preDestroy
        final List<org.apache.openejb.jee.LifecycleCallback> preDestroy = messageDrivenBean.preDestroy;
        if (preDestroy != null) {
            for (final org.apache.openejb.jee.LifecycleCallback preDestroyItem : preDestroy) {
                if (preDestroyItem != null) {
                    writer.writeStartElement(prefix, "pre-destroy", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, preDestroyItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "preDestroy");
                }
            }
        }

        // ELEMENT: dataSource
        final KeyedCollection<String, DataSource> dataSource = messageDrivenBean.dataSource;
        if (dataSource != null) {
            for (final DataSource dataSourceItem : dataSource) {
                if (dataSourceItem != null) {
                    writer.writeStartElement(prefix, "data-source", "http://java.sun.com/xml/ns/javaee");
                    writeDataSource(writer, dataSourceItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "dataSource");
                }
            }
        }

        // ELEMENT: securityRoleRef
        final List<SecurityRoleRef> securityRoleRef = messageDrivenBean.securityRoleRef;
        if (securityRoleRef != null) {
            for (final SecurityRoleRef securityRoleRefItem : securityRoleRef) {
                if (securityRoleRefItem != null) {
                    writer.writeStartElement(prefix, "security-role-ref", "http://java.sun.com/xml/ns/javaee");
                    writeSecurityRoleRef(writer, securityRoleRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDrivenBean, "securityRoleRef");
                }
            }
        }

        // ELEMENT: securityIdentity
        final SecurityIdentity securityIdentity = messageDrivenBean.securityIdentity;
        if (securityIdentity != null) {
            writer.writeStartElement(prefix, "security-identity", "http://java.sun.com/xml/ns/javaee");
            writeSecurityIdentity(writer, securityIdentity, context);
            writer.writeEndElement();
        }

        context.afterMarshal(messageDrivenBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);
    }

}
