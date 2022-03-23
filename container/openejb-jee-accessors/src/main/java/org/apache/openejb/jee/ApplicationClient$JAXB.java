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
import static org.apache.openejb.jee.MessageDestination$JAXB.readMessageDestination;
import static org.apache.openejb.jee.MessageDestination$JAXB.writeMessageDestination;
import static org.apache.openejb.jee.MessageDestinationRef$JAXB.readMessageDestinationRef;
import static org.apache.openejb.jee.MessageDestinationRef$JAXB.writeMessageDestinationRef;
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.readPersistenceContextRef;
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.writePersistenceContextRef;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.readPersistenceUnitRef;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.writePersistenceUnitRef;
import static org.apache.openejb.jee.ResourceEnvRef$JAXB.readResourceEnvRef;
import static org.apache.openejb.jee.ResourceEnvRef$JAXB.writeResourceEnvRef;
import static org.apache.openejb.jee.ResourceRef$JAXB.readResourceRef;
import static org.apache.openejb.jee.ResourceRef$JAXB.writeResourceRef;
import static org.apache.openejb.jee.ServiceRef$JAXB.readServiceRef;
import static org.apache.openejb.jee.ServiceRef$JAXB.writeServiceRef;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class ApplicationClient$JAXB
    extends JAXBObject<ApplicationClient> {


    public ApplicationClient$JAXB() {
        super(ApplicationClient.class, new QName("http://java.sun.com/xml/ns/javaee".intern(), "application-client".intern()), new QName("http://java.sun.com/xml/ns/javaee".intern(), "application-clientType".intern()), Text$JAXB.class, Icon$JAXB.class, EnvEntry$JAXB.class, EjbRef$JAXB.class, EjbLocalRef$JAXB.class, ServiceRef$JAXB.class, ResourceRef$JAXB.class, ResourceEnvRef$JAXB.class, MessageDestinationRef$JAXB.class, PersistenceContextRef$JAXB.class, PersistenceUnitRef$JAXB.class, LifecycleCallback$JAXB.class, MessageDestination$JAXB.class, DataSource$JAXB.class);
    }

    public static ApplicationClient readApplicationClient(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeApplicationClient(final XoXMLStreamWriter writer, final ApplicationClient applicationClient, final RuntimeContext context)
        throws Exception {
        _write(writer, applicationClient, context);
    }

    public void write(final XoXMLStreamWriter writer, final ApplicationClient applicationClient, final RuntimeContext context)
        throws Exception {
        _write(writer, applicationClient, context);
    }

    public final static ApplicationClient _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final ApplicationClient applicationClient = new ApplicationClient();
        context.beforeUnmarshal(applicationClient, org.metatype.sxc.jaxb.LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
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
        KeyedCollection<String, MessageDestination> messageDestination = null;
        KeyedCollection<String, DataSource> dataSource = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("application-clientType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ApplicationClient.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, applicationClient);
                applicationClient.id = id;
            } else if (("metadata-complete" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: metadataComplete
                final Boolean metadataComplete = ("1".equals(attribute.getValue()) || "true".equals(attribute.getValue()));
                applicationClient.metadataComplete = metadataComplete;
            } else if (("version" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: version
                applicationClient.version = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"), new QName("", "metadata-complete"), new QName("", "version"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("module-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: moduleName
                final String moduleNameRaw = elementReader.getElementAsString();

                final String moduleName;
                try {
                    moduleName = Adapters.collapsedStringAdapterAdapter.unmarshal(moduleNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                applicationClient.moduleName = moduleName;
            } else if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
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
                    icon = applicationClient.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("env-entry" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: envEntry
                final EnvEntry envEntryItem = readEnvEntry(elementReader, context);
                if (envEntry == null) {
                    envEntry = applicationClient.envEntry;
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
                    ejbRef = applicationClient.ejbRef;
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
                    ejbLocalRef = applicationClient.ejbLocalRef;
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
                    serviceRef = applicationClient.serviceRef;
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
                    resourceRef = applicationClient.resourceRef;
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
                    resourceEnvRef = applicationClient.resourceEnvRef;
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
                    messageDestinationRef = applicationClient.messageDestinationRef;
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
                    persistenceContextRef = applicationClient.persistenceContextRef;
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
                    persistenceUnitRef = applicationClient.persistenceUnitRef;
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
                    postConstruct = applicationClient.postConstruct;
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
                    preDestroy = applicationClient.preDestroy;
                    if (preDestroy != null) {
                        preDestroy.clear();
                    } else {
                        preDestroy = new ArrayList<org.apache.openejb.jee.LifecycleCallback>();
                    }
                }
                preDestroy.add(preDestroyItem);
            } else if (("callback-handler" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: callbackHandler
                final String callbackHandlerRaw = elementReader.getElementAsString();

                final String callbackHandler;
                try {
                    callbackHandler = Adapters.collapsedStringAdapterAdapter.unmarshal(callbackHandlerRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                applicationClient.callbackHandler = callbackHandler;
            } else if (("message-destination" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageDestination
                final MessageDestination messageDestinationItem = readMessageDestination(elementReader, context);
                if (messageDestination == null) {
                    messageDestination = applicationClient.messageDestination;
                    if (messageDestination != null) {
                        messageDestination.clear();
                    } else {
                        messageDestination = new KeyedCollection<String, MessageDestination>();
                    }
                }
                messageDestination.add(messageDestinationItem);
            } else if (("data-source" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: dataSource
                final DataSource dataSourceItem = readDataSource(elementReader, context);
                if (dataSource == null) {
                    dataSource = applicationClient.dataSource;
                    if (dataSource != null) {
                        dataSource.clear();
                    } else {
                        dataSource = new KeyedCollection<String, DataSource>();
                    }
                }
                dataSource.add(dataSourceItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "module-name"), new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "env-entry"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-ref"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-local-ref"), new QName("http://java.sun.com/xml/ns/javaee", "service-ref"), new QName("http://java.sun.com/xml/ns/javaee", "resource-ref"), new QName("http://java.sun.com/xml/ns/javaee", "resource-env-ref"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination-ref"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-context-ref"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-unit-ref"), new QName("http://java.sun.com/xml/ns/javaee", "post-construct"), new QName("http://java.sun.com/xml/ns/javaee", "pre-destroy"), new QName("http://java.sun.com/xml/ns/javaee", "callback-handler"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination"), new QName("http://java.sun.com/xml/ns/javaee", "data-source"));
            }
        }
        if (descriptions != null) {
            try {
                applicationClient.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, ApplicationClient.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                applicationClient.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, ApplicationClient.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            applicationClient.icon = icon;
        }
        if (envEntry != null) {
            applicationClient.envEntry = envEntry;
        }
        if (ejbRef != null) {
            applicationClient.ejbRef = ejbRef;
        }
        if (ejbLocalRef != null) {
            applicationClient.ejbLocalRef = ejbLocalRef;
        }
        if (serviceRef != null) {
            applicationClient.serviceRef = serviceRef;
        }
        if (resourceRef != null) {
            applicationClient.resourceRef = resourceRef;
        }
        if (resourceEnvRef != null) {
            applicationClient.resourceEnvRef = resourceEnvRef;
        }
        if (messageDestinationRef != null) {
            applicationClient.messageDestinationRef = messageDestinationRef;
        }
        if (persistenceContextRef != null) {
            applicationClient.persistenceContextRef = persistenceContextRef;
        }
        if (persistenceUnitRef != null) {
            applicationClient.persistenceUnitRef = persistenceUnitRef;
        }
        if (postConstruct != null) {
            applicationClient.postConstruct = postConstruct;
        }
        if (preDestroy != null) {
            applicationClient.preDestroy = preDestroy;
        }
        if (messageDestination != null) {
            applicationClient.messageDestination = messageDestination;
        }
        if (dataSource != null) {
            applicationClient.dataSource = dataSource;
        }

        context.afterUnmarshal(applicationClient, org.metatype.sxc.jaxb.LifecycleCallback.NONE);

        return applicationClient;
    }

    public final ApplicationClient read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final ApplicationClient applicationClient, RuntimeContext context)
        throws Exception {
        if (applicationClient == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ApplicationClient.class != applicationClient.getClass()) {
            context.unexpectedSubclass(writer, applicationClient, ApplicationClient.class);
            return;
        }

        context.beforeMarshal(applicationClient, org.metatype.sxc.jaxb.LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = applicationClient.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(applicationClient, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ATTRIBUTE: metadataComplete
        final Boolean metadataComplete = applicationClient.metadataComplete;
        if (metadataComplete != null) {
            writer.writeAttribute("", "", "metadata-complete", Boolean.toString(metadataComplete));
        }

        // ATTRIBUTE: version
        final String versionRaw = applicationClient.version;
        if (versionRaw != null) {
            String version = null;
            try {
                version = Adapters.collapsedStringAdapterAdapter.marshal(versionRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(applicationClient, "version", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "version", version);
        }

        // ELEMENT: moduleName
        final String moduleNameRaw = applicationClient.moduleName;
        String moduleName = null;
        try {
            moduleName = Adapters.collapsedStringAdapterAdapter.marshal(moduleNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(applicationClient, "moduleName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (moduleName != null) {
            writer.writeStartElement(prefix, "module-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(moduleName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(applicationClient, "moduleName");
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = applicationClient.getDescriptions();
        } catch (final Exception e) {
            context.getterError(applicationClient, "descriptions", ApplicationClient.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(applicationClient, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = applicationClient.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(applicationClient, "displayNames", ApplicationClient.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(applicationClient, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = applicationClient.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(applicationClient, "icon");
                }
            }
        }

        // ELEMENT: envEntry
        final KeyedCollection<String, EnvEntry> envEntry = applicationClient.envEntry;
        if (envEntry != null) {
            for (final EnvEntry envEntryItem : envEntry) {
                if (envEntryItem != null) {
                    writer.writeStartElement(prefix, "env-entry", "http://java.sun.com/xml/ns/javaee");
                    writeEnvEntry(writer, envEntryItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(applicationClient, "envEntry");
                }
            }
        }

        // ELEMENT: ejbRef
        final KeyedCollection<String, EjbRef> ejbRef = applicationClient.ejbRef;
        if (ejbRef != null) {
            for (final EjbRef ejbRefItem : ejbRef) {
                if (ejbRefItem != null) {
                    writer.writeStartElement(prefix, "ejb-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbRef(writer, ejbRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(applicationClient, "ejbRef");
                }
            }
        }

        // ELEMENT: ejbLocalRef
        final KeyedCollection<String, EjbLocalRef> ejbLocalRef = applicationClient.ejbLocalRef;
        if (ejbLocalRef != null) {
            for (final EjbLocalRef ejbLocalRefItem : ejbLocalRef) {
                if (ejbLocalRefItem != null) {
                    writer.writeStartElement(prefix, "ejb-local-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbLocalRef(writer, ejbLocalRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(applicationClient, "ejbLocalRef");
                }
            }
        }

        // ELEMENT: serviceRef
        final KeyedCollection<String, ServiceRef> serviceRef = applicationClient.serviceRef;
        if (serviceRef != null) {
            for (final ServiceRef serviceRefItem : serviceRef) {
                if (serviceRefItem != null) {
                    writer.writeStartElement(prefix, "service-ref", "http://java.sun.com/xml/ns/javaee");
                    writeServiceRef(writer, serviceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(applicationClient, "serviceRef");
                }
            }
        }

        // ELEMENT: resourceRef
        final KeyedCollection<String, ResourceRef> resourceRef = applicationClient.resourceRef;
        if (resourceRef != null) {
            for (final ResourceRef resourceRefItem : resourceRef) {
                if (resourceRefItem != null) {
                    writer.writeStartElement(prefix, "resource-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceRef(writer, resourceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(applicationClient, "resourceRef");
                }
            }
        }

        // ELEMENT: resourceEnvRef
        final KeyedCollection<String, ResourceEnvRef> resourceEnvRef = applicationClient.resourceEnvRef;
        if (resourceEnvRef != null) {
            for (final ResourceEnvRef resourceEnvRefItem : resourceEnvRef) {
                if (resourceEnvRefItem != null) {
                    writer.writeStartElement(prefix, "resource-env-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceEnvRef(writer, resourceEnvRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(applicationClient, "resourceEnvRef");
                }
            }
        }

        // ELEMENT: messageDestinationRef
        final KeyedCollection<String, MessageDestinationRef> messageDestinationRef = applicationClient.messageDestinationRef;
        if (messageDestinationRef != null) {
            for (final MessageDestinationRef messageDestinationRefItem : messageDestinationRef) {
                if (messageDestinationRefItem != null) {
                    writer.writeStartElement(prefix, "message-destination-ref", "http://java.sun.com/xml/ns/javaee");
                    writeMessageDestinationRef(writer, messageDestinationRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(applicationClient, "messageDestinationRef");
                }
            }
        }

        // ELEMENT: persistenceContextRef
        final KeyedCollection<String, PersistenceContextRef> persistenceContextRef = applicationClient.persistenceContextRef;
        if (persistenceContextRef != null) {
            for (final PersistenceContextRef persistenceContextRefItem : persistenceContextRef) {
                if (persistenceContextRefItem != null) {
                    writer.writeStartElement(prefix, "persistence-context-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceContextRef(writer, persistenceContextRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(applicationClient, "persistenceContextRef");
                }
            }
        }

        // ELEMENT: persistenceUnitRef
        final KeyedCollection<String, PersistenceUnitRef> persistenceUnitRef = applicationClient.persistenceUnitRef;
        if (persistenceUnitRef != null) {
            for (final PersistenceUnitRef persistenceUnitRefItem : persistenceUnitRef) {
                if (persistenceUnitRefItem != null) {
                    writer.writeStartElement(prefix, "persistence-unit-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceUnitRef(writer, persistenceUnitRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(applicationClient, "persistenceUnitRef");
                }
            }
        }

        // ELEMENT: postConstruct
        final List<org.apache.openejb.jee.LifecycleCallback> postConstruct = applicationClient.postConstruct;
        if (postConstruct != null) {
            for (final org.apache.openejb.jee.LifecycleCallback postConstructItem : postConstruct) {
                if (postConstructItem != null) {
                    writer.writeStartElement(prefix, "post-construct", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, postConstructItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(applicationClient, "postConstruct");
                }
            }
        }

        // ELEMENT: preDestroy
        final List<org.apache.openejb.jee.LifecycleCallback> preDestroy = applicationClient.preDestroy;
        if (preDestroy != null) {
            for (final org.apache.openejb.jee.LifecycleCallback preDestroyItem : preDestroy) {
                if (preDestroyItem != null) {
                    writer.writeStartElement(prefix, "pre-destroy", "http://java.sun.com/xml/ns/javaee");
                    writeLifecycleCallback(writer, preDestroyItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(applicationClient, "preDestroy");
                }
            }
        }

        // ELEMENT: callbackHandler
        final String callbackHandlerRaw = applicationClient.callbackHandler;
        String callbackHandler = null;
        try {
            callbackHandler = Adapters.collapsedStringAdapterAdapter.marshal(callbackHandlerRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(applicationClient, "callbackHandler", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (callbackHandler != null) {
            writer.writeStartElement(prefix, "callback-handler", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(callbackHandler);
            writer.writeEndElement();
        }

        // ELEMENT: messageDestination
        final KeyedCollection<String, MessageDestination> messageDestination = applicationClient.messageDestination;
        if (messageDestination != null) {
            for (final MessageDestination messageDestinationItem : messageDestination) {
                if (messageDestinationItem != null) {
                    writer.writeStartElement(prefix, "message-destination", "http://java.sun.com/xml/ns/javaee");
                    writeMessageDestination(writer, messageDestinationItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(applicationClient, "messageDestination");
                }
            }
        }

        // ELEMENT: dataSource
        final KeyedCollection<String, DataSource> dataSource = applicationClient.dataSource;
        if (dataSource != null) {
            for (final DataSource dataSourceItem : dataSource) {
                if (dataSourceItem != null) {
                    writer.writeStartElement(prefix, "data-source", "http://java.sun.com/xml/ns/javaee");
                    writeDataSource(writer, dataSourceItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(applicationClient, org.metatype.sxc.jaxb.LifecycleCallback.NONE);
    }

}
