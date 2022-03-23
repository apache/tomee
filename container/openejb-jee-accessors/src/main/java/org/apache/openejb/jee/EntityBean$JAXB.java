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

import static org.apache.openejb.jee.CmpField$JAXB.readCmpField;
import static org.apache.openejb.jee.CmpField$JAXB.writeCmpField;
import static org.apache.openejb.jee.CmpVersion$JAXB.parseCmpVersion;
import static org.apache.openejb.jee.CmpVersion$JAXB.toStringCmpVersion;
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
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.readPersistenceContextRef;
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.writePersistenceContextRef;
import static org.apache.openejb.jee.PersistenceType$JAXB.parsePersistenceType;
import static org.apache.openejb.jee.PersistenceType$JAXB.toStringPersistenceType;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.readPersistenceUnitRef;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.writePersistenceUnitRef;
import static org.apache.openejb.jee.Query$JAXB.readQuery;
import static org.apache.openejb.jee.Query$JAXB.writeQuery;
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

@SuppressWarnings({
    "StringEquality"
})
public class EntityBean$JAXB
    extends JAXBObject<EntityBean> {


    public EntityBean$JAXB() {
        super(EntityBean.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "entity-beanType".intern()), Text$JAXB.class, Icon$JAXB.class, PersistenceType$JAXB.class, CmpVersion$JAXB.class, CmpField$JAXB.class, EnvEntry$JAXB.class, EjbRef$JAXB.class, EjbLocalRef$JAXB.class, ServiceRef$JAXB.class, ResourceRef$JAXB.class, ResourceEnvRef$JAXB.class, MessageDestinationRef$JAXB.class, PersistenceContextRef$JAXB.class, PersistenceUnitRef$JAXB.class, LifecycleCallback$JAXB.class, DataSource$JAXB.class, SecurityRoleRef$JAXB.class, SecurityIdentity$JAXB.class, Query$JAXB.class);
    }

    public static EntityBean readEntityBean(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeEntityBean(final XoXMLStreamWriter writer, final EntityBean entityBean, final RuntimeContext context)
        throws Exception {
        _write(writer, entityBean, context);
    }

    public void write(final XoXMLStreamWriter writer, final EntityBean entityBean, final RuntimeContext context)
        throws Exception {
        _write(writer, entityBean, context);
    }

    public final static EntityBean _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final EntityBean entityBean = new EntityBean();
        context.beforeUnmarshal(entityBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<CmpField> cmpField = null;
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
        List<Query> query = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("entity-beanType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, EntityBean.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, entityBean);
                entityBean.id = id;
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
                    icon = entityBean.icon;
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

                entityBean.ejbName = ejbName;
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

                entityBean.mappedName = mappedName;
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

                entityBean.home = home;
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

                entityBean.remote = remote;
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

                entityBean.localHome = localHome;
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

                entityBean.local = local;
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

                entityBean.ejbClass = ejbClass;
            } else if (("persistence-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: persistenceType
                final PersistenceType persistenceType = parsePersistenceType(elementReader, context, elementReader.getElementAsString());
                if (persistenceType != null) {
                    entityBean.persistenceType = persistenceType;
                }
            } else if (("prim-key-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: primKeyClass
                final String primKeyClassRaw = elementReader.getElementAsString();

                final String primKeyClass;
                try {
                    primKeyClass = Adapters.collapsedStringAdapterAdapter.unmarshal(primKeyClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                entityBean.primKeyClass = primKeyClass;
            } else if (("reentrant" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: reentrant
                final String reentrantRaw = elementReader.getElementAsString();

                final Boolean reentrant;
                try {
                    reentrant = Adapters.booleanAdapterAdapter.unmarshal(reentrantRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, BooleanAdapter.class, Boolean.class, Boolean.class, e);
                    continue;
                }

                entityBean.reentrant = reentrant;
            } else if (("cmp-version" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: cmpVersion
                final CmpVersion cmpVersion = parseCmpVersion(elementReader, context, elementReader.getElementAsString());
                if (cmpVersion != null) {
                    entityBean.cmpVersion = cmpVersion;
                }
            } else if (("abstract-schema-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: abstractSchemaName
                final String abstractSchemaNameRaw = elementReader.getElementAsString();

                final String abstractSchemaName;
                try {
                    abstractSchemaName = Adapters.collapsedStringAdapterAdapter.unmarshal(abstractSchemaNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                entityBean.abstractSchemaName = abstractSchemaName;
            } else if (("cmp-field" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: cmpField
                final CmpField cmpFieldItem = readCmpField(elementReader, context);
                if (cmpField == null) {
                    cmpField = entityBean.cmpField;
                    if (cmpField != null) {
                        cmpField.clear();
                    } else {
                        cmpField = new ArrayList<CmpField>();
                    }
                }
                cmpField.add(cmpFieldItem);
            } else if (("primkey-field" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: primkeyField
                final String primkeyFieldRaw = elementReader.getElementAsString();

                final String primkeyField;
                try {
                    primkeyField = Adapters.collapsedStringAdapterAdapter.unmarshal(primkeyFieldRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                entityBean.primkeyField = primkeyField;
            } else if (("env-entry" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: envEntry
                final EnvEntry envEntryItem = readEnvEntry(elementReader, context);
                if (envEntry == null) {
                    envEntry = entityBean.envEntry;
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
                    ejbRef = entityBean.ejbRef;
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
                    ejbLocalRef = entityBean.ejbLocalRef;
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
                    serviceRef = entityBean.serviceRef;
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
                    resourceRef = entityBean.resourceRef;
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
                    resourceEnvRef = entityBean.resourceEnvRef;
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
                    messageDestinationRef = entityBean.messageDestinationRef;
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
                    persistenceContextRef = entityBean.persistenceContextRef;
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
                    persistenceUnitRef = entityBean.persistenceUnitRef;
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
                    postConstruct = entityBean.postConstruct;
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
                    preDestroy = entityBean.preDestroy;
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
                    dataSource = entityBean.dataSource;
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
                    securityRoleRef = entityBean.securityRoleRef;
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
                entityBean.securityIdentity = securityIdentity;
            } else if (("query" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: query
                final Query queryItem = readQuery(elementReader, context);
                if (query == null) {
                    query = entityBean.query;
                    if (query != null) {
                        query.clear();
                    } else {
                        query = new ArrayList<Query>();
                    }
                }
                query.add(queryItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-name"), new QName("http://java.sun.com/xml/ns/javaee", "mapped-name"), new QName("http://java.sun.com/xml/ns/javaee", "home"), new QName("http://java.sun.com/xml/ns/javaee", "remote"), new QName("http://java.sun.com/xml/ns/javaee", "local-home"), new QName("http://java.sun.com/xml/ns/javaee", "local"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-class"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-type"), new QName("http://java.sun.com/xml/ns/javaee", "prim-key-class"), new QName("http://java.sun.com/xml/ns/javaee", "reentrant"), new QName("http://java.sun.com/xml/ns/javaee", "cmp-version"), new QName("http://java.sun.com/xml/ns/javaee", "abstract-schema-name"), new QName("http://java.sun.com/xml/ns/javaee", "cmp-field"), new QName("http://java.sun.com/xml/ns/javaee", "primkey-field"), new QName("http://java.sun.com/xml/ns/javaee", "env-entry"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-ref"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-local-ref"), new QName("http://java.sun.com/xml/ns/javaee", "service-ref"), new QName("http://java.sun.com/xml/ns/javaee", "resource-ref"), new QName("http://java.sun.com/xml/ns/javaee", "resource-env-ref"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination-ref"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-context-ref"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-unit-ref"), new QName("http://java.sun.com/xml/ns/javaee", "post-construct"), new QName("http://java.sun.com/xml/ns/javaee", "pre-destroy"), new QName("http://java.sun.com/xml/ns/javaee", "data-source"), new QName("http://java.sun.com/xml/ns/javaee", "security-role-ref"), new QName("http://java.sun.com/xml/ns/javaee", "security-identity"), new QName("http://java.sun.com/xml/ns/javaee", "query"));
            }
        }
        if (descriptions != null) {
            try {
                entityBean.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, EntityBean.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                entityBean.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, EntityBean.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            entityBean.icon = icon;
        }
        if (cmpField != null) {
            entityBean.cmpField = cmpField;
        }
        if (envEntry != null) {
            entityBean.envEntry = envEntry;
        }
        if (ejbRef != null) {
            entityBean.ejbRef = ejbRef;
        }
        if (ejbLocalRef != null) {
            entityBean.ejbLocalRef = ejbLocalRef;
        }
        if (serviceRef != null) {
            entityBean.serviceRef = serviceRef;
        }
        if (resourceRef != null) {
            entityBean.resourceRef = resourceRef;
        }
        if (resourceEnvRef != null) {
            entityBean.resourceEnvRef = resourceEnvRef;
        }
        if (messageDestinationRef != null) {
            entityBean.messageDestinationRef = messageDestinationRef;
        }
        if (persistenceContextRef != null) {
            entityBean.persistenceContextRef = persistenceContextRef;
        }
        if (persistenceUnitRef != null) {
            entityBean.persistenceUnitRef = persistenceUnitRef;
        }
        if (postConstruct != null) {
            entityBean.postConstruct = postConstruct;
        }
        if (preDestroy != null) {
            entityBean.preDestroy = preDestroy;
        }
        if (dataSource != null) {
            entityBean.dataSource = dataSource;
        }
        if (securityRoleRef != null) {
            entityBean.securityRoleRef = securityRoleRef;
        }
        if (query != null) {
            entityBean.query = query;
        }

        context.afterUnmarshal(entityBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);

        return entityBean;
    }

    public final EntityBean read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final EntityBean entityBean, RuntimeContext context)
        throws Exception {
        if (entityBean == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (EntityBean.class != entityBean.getClass()) {
            context.unexpectedSubclass(writer, entityBean, EntityBean.class);
            return;
        }

        context.beforeMarshal(entityBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = entityBean.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(entityBean, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = entityBean.getDescriptions();
        } catch (final Exception e) {
            context.getterError(entityBean, "descriptions", EntityBean.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = entityBean.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(entityBean, "displayNames", EntityBean.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = entityBean.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "icon");
                }
            }
        }

        // ELEMENT: ejbName
        final String ejbNameRaw = entityBean.ejbName;
        String ejbName = null;
        try {
            ejbName = Adapters.collapsedStringAdapterAdapter.marshal(ejbNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(entityBean, "ejbName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbName != null) {
            writer.writeStartElement(prefix, "ejb-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(entityBean, "ejbName");
        }

        // ELEMENT: mappedName
        final String mappedNameRaw = entityBean.mappedName;
        String mappedName = null;
        try {
            mappedName = Adapters.collapsedStringAdapterAdapter.marshal(mappedNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(entityBean, "mappedName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (mappedName != null) {
            writer.writeStartElement(prefix, "mapped-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(mappedName);
            writer.writeEndElement();
        }

        // ELEMENT: home
        final String homeRaw = entityBean.home;
        String home = null;
        try {
            home = Adapters.collapsedStringAdapterAdapter.marshal(homeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(entityBean, "home", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (home != null) {
            writer.writeStartElement(prefix, "home", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(home);
            writer.writeEndElement();
        }

        // ELEMENT: remote
        final String remoteRaw = entityBean.remote;
        String remote = null;
        try {
            remote = Adapters.collapsedStringAdapterAdapter.marshal(remoteRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(entityBean, "remote", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (remote != null) {
            writer.writeStartElement(prefix, "remote", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(remote);
            writer.writeEndElement();
        }

        // ELEMENT: localHome
        final String localHomeRaw = entityBean.localHome;
        String localHome = null;
        try {
            localHome = Adapters.collapsedStringAdapterAdapter.marshal(localHomeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(entityBean, "localHome", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (localHome != null) {
            writer.writeStartElement(prefix, "local-home", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(localHome);
            writer.writeEndElement();
        }

        // ELEMENT: local
        final String localRaw = entityBean.local;
        String local = null;
        try {
            local = Adapters.collapsedStringAdapterAdapter.marshal(localRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(entityBean, "local", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (local != null) {
            writer.writeStartElement(prefix, "local", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(local);
            writer.writeEndElement();
        }

        // ELEMENT: ejbClass
        final String ejbClassRaw = entityBean.ejbClass;
        String ejbClass = null;
        try {
            ejbClass = Adapters.collapsedStringAdapterAdapter.marshal(ejbClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(entityBean, "ejbClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbClass != null) {
            writer.writeStartElement(prefix, "ejb-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(entityBean, "ejbClass");
        }

        // ELEMENT: persistenceType
        final PersistenceType persistenceType = entityBean.persistenceType;
        if (persistenceType != null) {
            writer.writeStartElement(prefix, "persistence-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringPersistenceType(entityBean, null, context, persistenceType));
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(entityBean, "persistenceType");
        }

        // ELEMENT: primKeyClass
        final String primKeyClassRaw = entityBean.primKeyClass;
        String primKeyClass = null;
        try {
            primKeyClass = Adapters.collapsedStringAdapterAdapter.marshal(primKeyClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(entityBean, "primKeyClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (primKeyClass != null) {
            writer.writeStartElement(prefix, "prim-key-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(primKeyClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(entityBean, "primKeyClass");
        }

        // ELEMENT: reentrant
        final Boolean reentrantRaw = entityBean.reentrant;
        String reentrant = null;
        try {
            reentrant = Adapters.booleanAdapterAdapter.marshal(reentrantRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(entityBean, "reentrant", BooleanAdapter.class, Boolean.TYPE, Boolean.TYPE, e);
        }
        if (reentrant != null) {
            writer.writeStartElement(prefix, "reentrant", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(reentrant);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(entityBean, "reentrant");
        }

        // ELEMENT: cmpVersion
        final CmpVersion cmpVersion = entityBean.cmpVersion;
        if (cmpVersion != null) {
            writer.writeStartElement(prefix, "cmp-version", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringCmpVersion(entityBean, null, context, cmpVersion));
            writer.writeEndElement();
        }

        // ELEMENT: abstractSchemaName
        final String abstractSchemaNameRaw = entityBean.abstractSchemaName;
        String abstractSchemaName = null;
        try {
            abstractSchemaName = Adapters.collapsedStringAdapterAdapter.marshal(abstractSchemaNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(entityBean, "abstractSchemaName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (abstractSchemaName != null) {
            writer.writeStartElement(prefix, "abstract-schema-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(abstractSchemaName);
            writer.writeEndElement();
        }

        // ELEMENT: cmpField
        final List<CmpField> cmpField = entityBean.cmpField;
        if (cmpField != null) {
            for (final CmpField cmpFieldItem : cmpField) {
                if (cmpFieldItem != null) {
                    writer.writeStartElement(prefix, "cmp-field", "http://java.sun.com/xml/ns/javaee");
                    writeCmpField(writer, cmpFieldItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "cmpField");
                }
            }
        }

        // ELEMENT: primkeyField
        final String primkeyFieldRaw = entityBean.primkeyField;
        String primkeyField = null;
        try {
            primkeyField = Adapters.collapsedStringAdapterAdapter.marshal(primkeyFieldRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(entityBean, "primkeyField", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (primkeyField != null) {
            writer.writeStartElement(prefix, "primkey-field", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(primkeyField);
            writer.writeEndElement();
        }

        // ELEMENT: envEntry
        final KeyedCollection<String, EnvEntry> envEntry = entityBean.envEntry;
        if (envEntry != null) {
            for (final EnvEntry envEntryItem : envEntry) {
                if (envEntryItem != null) {
                    writer.writeStartElement(prefix, "env-entry", "http://java.sun.com/xml/ns/javaee");
                    writeEnvEntry(writer, envEntryItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "envEntry");
                }
            }
        }

        // ELEMENT: ejbRef
        final KeyedCollection<String, EjbRef> ejbRef = entityBean.ejbRef;
        if (ejbRef != null) {
            for (final EjbRef ejbRefItem : ejbRef) {
                if (ejbRefItem != null) {
                    writer.writeStartElement(prefix, "ejb-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbRef(writer, ejbRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "ejbRef");
                }
            }
        }

        // ELEMENT: ejbLocalRef
        final KeyedCollection<String, EjbLocalRef> ejbLocalRef = entityBean.ejbLocalRef;
        if (ejbLocalRef != null) {
            for (final EjbLocalRef ejbLocalRefItem : ejbLocalRef) {
                if (ejbLocalRefItem != null) {
                    writer.writeStartElement(prefix, "ejb-local-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbLocalRef(writer, ejbLocalRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "ejbLocalRef");
                }
            }
        }

        // ELEMENT: serviceRef
        final KeyedCollection<String, ServiceRef> serviceRef = entityBean.serviceRef;
        if (serviceRef != null) {
            for (final ServiceRef serviceRefItem : serviceRef) {
                if (serviceRefItem != null) {
                    writer.writeStartElement(prefix, "service-ref", "http://java.sun.com/xml/ns/javaee");
                    writeServiceRef(writer, serviceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "serviceRef");
                }
            }
        }

        // ELEMENT: resourceRef
        final KeyedCollection<String, ResourceRef> resourceRef = entityBean.resourceRef;
        if (resourceRef != null) {
            for (final ResourceRef resourceRefItem : resourceRef) {
                if (resourceRefItem != null) {
                    writer.writeStartElement(prefix, "resource-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceRef(writer, resourceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "resourceRef");
                }
            }
        }

        // ELEMENT: resourceEnvRef
        final KeyedCollection<String, ResourceEnvRef> resourceEnvRef = entityBean.resourceEnvRef;
        if (resourceEnvRef != null) {
            for (final ResourceEnvRef resourceEnvRefItem : resourceEnvRef) {
                if (resourceEnvRefItem != null) {
                    writer.writeStartElement(prefix, "resource-env-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceEnvRef(writer, resourceEnvRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "resourceEnvRef");
                }
            }
        }

        // ELEMENT: messageDestinationRef
        final KeyedCollection<String, MessageDestinationRef> messageDestinationRef = entityBean.messageDestinationRef;
        if (messageDestinationRef != null) {
            for (final MessageDestinationRef messageDestinationRefItem : messageDestinationRef) {
                if (messageDestinationRefItem != null) {
                    writer.writeStartElement(prefix, "message-destination-ref", "http://java.sun.com/xml/ns/javaee");
                    writeMessageDestinationRef(writer, messageDestinationRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "messageDestinationRef");
                }
            }
        }

        // ELEMENT: persistenceContextRef
        final KeyedCollection<String, PersistenceContextRef> persistenceContextRef = entityBean.persistenceContextRef;
        if (persistenceContextRef != null) {
            for (final PersistenceContextRef persistenceContextRefItem : persistenceContextRef) {
                if (persistenceContextRefItem != null) {
                    writer.writeStartElement(prefix, "persistence-context-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceContextRef(writer, persistenceContextRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "persistenceContextRef");
                }
            }
        }

        // ELEMENT: persistenceUnitRef
        final KeyedCollection<String, PersistenceUnitRef> persistenceUnitRef = entityBean.persistenceUnitRef;
        if (persistenceUnitRef != null) {
            for (final PersistenceUnitRef persistenceUnitRefItem : persistenceUnitRef) {
                if (persistenceUnitRefItem != null) {
                    writer.writeStartElement(prefix, "persistence-unit-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceUnitRef(writer, persistenceUnitRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "persistenceUnitRef");
                }
            }
        }

        // ELEMENT: postConstruct
        final List<org.apache.openejb.jee.LifecycleCallback> postConstruct = entityBean.postConstruct;
        if (postConstruct != null) {
            for (final org.apache.openejb.jee.LifecycleCallback postConstructItem : postConstruct) {
                if (postConstructItem != null) {
                    writer.writeStartElement(prefix, "post-construct", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, postConstructItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "postConstruct");
                }
            }
        }

        // ELEMENT: preDestroy
        final List<org.apache.openejb.jee.LifecycleCallback> preDestroy = entityBean.preDestroy;
        if (preDestroy != null) {
            for (final org.apache.openejb.jee.LifecycleCallback preDestroyItem : preDestroy) {
                if (preDestroyItem != null) {
                    writer.writeStartElement(prefix, "pre-destroy", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, preDestroyItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "preDestroy");
                }
            }
        }

        // ELEMENT: dataSource
        final KeyedCollection<String, DataSource> dataSource = entityBean.dataSource;
        if (dataSource != null) {
            for (final DataSource dataSourceItem : dataSource) {
                if (dataSourceItem != null) {
                    writer.writeStartElement(prefix, "data-source", "http://java.sun.com/xml/ns/javaee");
                    writeDataSource(writer, dataSourceItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: securityRoleRef
        final List<SecurityRoleRef> securityRoleRef = entityBean.securityRoleRef;
        if (securityRoleRef != null) {
            for (final SecurityRoleRef securityRoleRefItem : securityRoleRef) {
                if (securityRoleRefItem != null) {
                    writer.writeStartElement(prefix, "security-role-ref", "http://java.sun.com/xml/ns/javaee");
                    writeSecurityRoleRef(writer, securityRoleRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "securityRoleRef");
                }
            }
        }

        // ELEMENT: securityIdentity
        final SecurityIdentity securityIdentity = entityBean.securityIdentity;
        if (securityIdentity != null) {
            writer.writeStartElement(prefix, "security-identity", "http://java.sun.com/xml/ns/javaee");
            writeSecurityIdentity(writer, securityIdentity, context);
            writer.writeEndElement();
        }

        // ELEMENT: query
        final List<Query> query = entityBean.query;
        if (query != null) {
            for (final Query queryItem : query) {
                if (queryItem != null) {
                    writer.writeStartElement(prefix, "query", "http://java.sun.com/xml/ns/javaee");
                    writeQuery(writer, queryItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(entityBean, "query");
                }
            }
        }

        context.afterMarshal(entityBean, org.metatype.sxc.jaxb.LifecycleCallback.NONE);
    }

}
