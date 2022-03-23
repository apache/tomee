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
import org.metatype.sxc.jaxb.LifecycleCallback;
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
import static org.apache.openejb.jee.MessageDestination$JAXB.readMessageDestination;
import static org.apache.openejb.jee.MessageDestination$JAXB.writeMessageDestination;
import static org.apache.openejb.jee.MessageDestinationRef$JAXB.readMessageDestinationRef;
import static org.apache.openejb.jee.MessageDestinationRef$JAXB.writeMessageDestinationRef;
import static org.apache.openejb.jee.Module$JAXB.readModule;
import static org.apache.openejb.jee.Module$JAXB.writeModule;
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.readPersistenceContextRef;
import static org.apache.openejb.jee.PersistenceContextRef$JAXB.writePersistenceContextRef;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.readPersistenceUnitRef;
import static org.apache.openejb.jee.PersistenceUnitRef$JAXB.writePersistenceUnitRef;
import static org.apache.openejb.jee.ResourceEnvRef$JAXB.readResourceEnvRef;
import static org.apache.openejb.jee.ResourceEnvRef$JAXB.writeResourceEnvRef;
import static org.apache.openejb.jee.ResourceRef$JAXB.readResourceRef;
import static org.apache.openejb.jee.ResourceRef$JAXB.writeResourceRef;
import static org.apache.openejb.jee.SecurityRole$JAXB.readSecurityRole;
import static org.apache.openejb.jee.SecurityRole$JAXB.writeSecurityRole;
import static org.apache.openejb.jee.ServiceRef$JAXB.readServiceRef;
import static org.apache.openejb.jee.ServiceRef$JAXB.writeServiceRef;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class Application$JAXB
    extends JAXBObject<Application> {


    public Application$JAXB() {
        super(Application.class, new QName("http://java.sun.com/xml/ns/javaee".intern(), "application".intern()), new QName("http://java.sun.com/xml/ns/javaee".intern(), "applicationType".intern()), Text$JAXB.class, Icon$JAXB.class, Module$JAXB.class, SecurityRole$JAXB.class, EnvEntry$JAXB.class, EjbRef$JAXB.class, EjbLocalRef$JAXB.class, ServiceRef$JAXB.class, ResourceRef$JAXB.class, ResourceEnvRef$JAXB.class, MessageDestinationRef$JAXB.class, PersistenceContextRef$JAXB.class, PersistenceUnitRef$JAXB.class, MessageDestination$JAXB.class, DataSource$JAXB.class);
    }

    public static Application readApplication(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeApplication(final XoXMLStreamWriter writer, final Application application, final RuntimeContext context)
        throws Exception {
        _write(writer, application, context);
    }

    public void write(final XoXMLStreamWriter writer, final Application application, final RuntimeContext context)
        throws Exception {
        _write(writer, application, context);
    }

    public final static Application _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final Application application = new Application();
        context.beforeUnmarshal(application, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<Module> module = null;
        List<SecurityRole> securityRole = null;
        KeyedCollection<String, EnvEntry> envEntry = null;
        KeyedCollection<String, EjbRef> ejbRef = null;
        KeyedCollection<String, EjbLocalRef> ejbLocalRef = null;
        KeyedCollection<String, ServiceRef> serviceRef = null;
        KeyedCollection<String, ResourceRef> resourceRef = null;
        KeyedCollection<String, ResourceEnvRef> resourceEnvRef = null;
        KeyedCollection<String, MessageDestinationRef> messageDestinationRef = null;
        KeyedCollection<String, PersistenceContextRef> persistenceContextRef = null;
        KeyedCollection<String, PersistenceUnitRef> persistenceUnitRef = null;
        KeyedCollection<String, MessageDestination> messageDestination = null;
        KeyedCollection<String, DataSource> dataSource = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("applicationType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Application.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("version" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: version
                application.version = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, application);
                application.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "version"), new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("application-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: applicationName
                final String applicationNameRaw = elementReader.getElementAsString();

                final String applicationName;
                try {
                    applicationName = Adapters.collapsedStringAdapterAdapter.unmarshal(applicationNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                application.applicationName = applicationName;
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
                    icon = application.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("initialize-in-order" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: initializeInOrder
                final Boolean initializeInOrder = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                application.initializeInOrder = initializeInOrder;
            } else if (("module" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: module
                final Module moduleItem = readModule(elementReader, context);
                if (module == null) {
                    module = application.module;
                    if (module != null) {
                        module.clear();
                    } else {
                        module = new ArrayList<Module>();
                    }
                }
                module.add(moduleItem);
            } else if (("security-role" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: securityRole
                final SecurityRole securityRoleItem = readSecurityRole(elementReader, context);
                if (securityRole == null) {
                    securityRole = application.securityRole;
                    if (securityRole != null) {
                        securityRole.clear();
                    } else {
                        securityRole = new ArrayList<SecurityRole>();
                    }
                }
                securityRole.add(securityRoleItem);
            } else if (("library-directory" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: libraryDirectory
                final String libraryDirectoryRaw = elementReader.getElementAsString();

                final String libraryDirectory;
                try {
                    libraryDirectory = Adapters.collapsedStringAdapterAdapter.unmarshal(libraryDirectoryRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                application.libraryDirectory = libraryDirectory;
            } else if (("env-entry" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: envEntry
                final EnvEntry envEntryItem = readEnvEntry(elementReader, context);
                if (envEntry == null) {
                    envEntry = application.envEntry;
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
                    ejbRef = application.ejbRef;
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
                    ejbLocalRef = application.ejbLocalRef;
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
                    serviceRef = application.serviceRef;
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
                    resourceRef = application.resourceRef;
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
                    resourceEnvRef = application.resourceEnvRef;
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
                    messageDestinationRef = application.messageDestinationRef;
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
                    persistenceContextRef = application.persistenceContextRef;
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
                    persistenceUnitRef = application.persistenceUnitRef;
                    if (persistenceUnitRef != null) {
                        persistenceUnitRef.clear();
                    } else {
                        persistenceUnitRef = new KeyedCollection<String, PersistenceUnitRef>();
                    }
                }
                persistenceUnitRef.add(persistenceUnitRefItem);
            } else if (("message-destination" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageDestination
                final MessageDestination messageDestinationItem = readMessageDestination(elementReader, context);
                if (messageDestination == null) {
                    messageDestination = application.messageDestination;
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
                    dataSource = application.dataSource;
                    if (dataSource != null) {
                        dataSource.clear();
                    } else {
                        dataSource = new KeyedCollection<String, DataSource>();
                    }
                }
                dataSource.add(dataSourceItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "application-name"), new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "initialize-in-order"), new QName("http://java.sun.com/xml/ns/javaee", "module"), new QName("http://java.sun.com/xml/ns/javaee", "security-role"), new QName("http://java.sun.com/xml/ns/javaee", "library-directory"), new QName("http://java.sun.com/xml/ns/javaee", "env-entry"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-ref"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-local-ref"), new QName("http://java.sun.com/xml/ns/javaee", "service-ref"), new QName("http://java.sun.com/xml/ns/javaee", "resource-ref"), new QName("http://java.sun.com/xml/ns/javaee", "resource-env-ref"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination-ref"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-context-ref"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-unit-ref"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination"), new QName("http://java.sun.com/xml/ns/javaee", "data-source"));
            }
        }
        if (descriptions != null) {
            try {
                application.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, Application.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                application.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, Application.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            application.icon = icon;
        }
        if (module != null) {
            application.module = module;
        }
        if (securityRole != null) {
            application.securityRole = securityRole;
        }
        if (envEntry != null) {
            application.envEntry = envEntry;
        }
        if (ejbRef != null) {
            application.ejbRef = ejbRef;
        }
        if (ejbLocalRef != null) {
            application.ejbLocalRef = ejbLocalRef;
        }
        if (serviceRef != null) {
            application.serviceRef = serviceRef;
        }
        if (resourceRef != null) {
            application.resourceRef = resourceRef;
        }
        if (resourceEnvRef != null) {
            application.resourceEnvRef = resourceEnvRef;
        }
        if (messageDestinationRef != null) {
            application.messageDestinationRef = messageDestinationRef;
        }
        if (persistenceContextRef != null) {
            application.persistenceContextRef = persistenceContextRef;
        }
        if (persistenceUnitRef != null) {
            application.persistenceUnitRef = persistenceUnitRef;
        }
        if (messageDestination != null) {
            application.messageDestination = messageDestination;
        }
        if (dataSource != null) {
            application.dataSource = dataSource;
        }

        context.afterUnmarshal(application, LifecycleCallback.NONE);

        return application;
    }

    public final Application read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final Application application, RuntimeContext context)
        throws Exception {
        if (application == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Application.class != application.getClass()) {
            context.unexpectedSubclass(writer, application, Application.class);
            return;
        }

        context.beforeMarshal(application, LifecycleCallback.NONE);


        // ATTRIBUTE: version
        final String versionRaw = application.version;
        if (versionRaw != null) {
            String version = null;
            try {
                version = Adapters.collapsedStringAdapterAdapter.marshal(versionRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(application, "version", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "version", version);
        }

        // ATTRIBUTE: id
        final String idRaw = application.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(application, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: applicationName
        final String applicationNameRaw = application.applicationName;
        String applicationName = null;
        try {
            applicationName = Adapters.collapsedStringAdapterAdapter.marshal(applicationNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(application, "applicationName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (applicationName != null) {
            writer.writeStartElement(prefix, "application-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(applicationName);
            writer.writeEndElement();
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = application.getDescriptions();
        } catch (final Exception e) {
            context.getterError(application, "descriptions", Application.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(application, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = application.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(application, "displayNames", Application.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(application, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = application.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(application, "icon");
                }
            }
        }

        // ELEMENT: initializeInOrder
        final Boolean initializeInOrder = application.initializeInOrder;
        if (initializeInOrder != null) {
            writer.writeStartElement(prefix, "initialize-in-order", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(initializeInOrder));
            writer.writeEndElement();
        }

        // ELEMENT: module
        final List<Module> module = application.module;
        if (module != null) {
            for (final Module moduleItem : module) {
                if (moduleItem != null) {
                    writer.writeStartElement(prefix, "module", "http://java.sun.com/xml/ns/javaee");
                    writeModule(writer, moduleItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(application, "module");
                }
            }
        }

        // ELEMENT: securityRole
        final List<SecurityRole> securityRole = application.securityRole;
        if (securityRole != null) {
            for (final SecurityRole securityRoleItem : securityRole) {
                if (securityRoleItem != null) {
                    writer.writeStartElement(prefix, "security-role", "http://java.sun.com/xml/ns/javaee");
                    writeSecurityRole(writer, securityRoleItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: libraryDirectory
        final String libraryDirectoryRaw = application.libraryDirectory;
        String libraryDirectory = null;
        try {
            libraryDirectory = Adapters.collapsedStringAdapterAdapter.marshal(libraryDirectoryRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(application, "libraryDirectory", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (libraryDirectory != null) {
            writer.writeStartElement(prefix, "library-directory", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(libraryDirectory);
            writer.writeEndElement();
        }

        // ELEMENT: envEntry
        final KeyedCollection<String, EnvEntry> envEntry = application.envEntry;
        if (envEntry != null) {
            for (final EnvEntry envEntryItem : envEntry) {
                if (envEntryItem != null) {
                    writer.writeStartElement(prefix, "env-entry", "http://java.sun.com/xml/ns/javaee");
                    writeEnvEntry(writer, envEntryItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(application, "envEntry");
                }
            }
        }

        // ELEMENT: ejbRef
        final KeyedCollection<String, EjbRef> ejbRef = application.ejbRef;
        if (ejbRef != null) {
            for (final EjbRef ejbRefItem : ejbRef) {
                if (ejbRefItem != null) {
                    writer.writeStartElement(prefix, "ejb-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbRef(writer, ejbRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(application, "ejbRef");
                }
            }
        }

        // ELEMENT: ejbLocalRef
        final KeyedCollection<String, EjbLocalRef> ejbLocalRef = application.ejbLocalRef;
        if (ejbLocalRef != null) {
            for (final EjbLocalRef ejbLocalRefItem : ejbLocalRef) {
                if (ejbLocalRefItem != null) {
                    writer.writeStartElement(prefix, "ejb-local-ref", "http://java.sun.com/xml/ns/javaee");
                    writeEjbLocalRef(writer, ejbLocalRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(application, "ejbLocalRef");
                }
            }
        }

        // ELEMENT: serviceRef
        final KeyedCollection<String, ServiceRef> serviceRef = application.serviceRef;
        if (serviceRef != null) {
            for (final ServiceRef serviceRefItem : serviceRef) {
                if (serviceRefItem != null) {
                    writer.writeStartElement(prefix, "service-ref", "http://java.sun.com/xml/ns/javaee");
                    writeServiceRef(writer, serviceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(application, "serviceRef");
                }
            }
        }

        // ELEMENT: resourceRef
        final KeyedCollection<String, ResourceRef> resourceRef = application.resourceRef;
        if (resourceRef != null) {
            for (final ResourceRef resourceRefItem : resourceRef) {
                if (resourceRefItem != null) {
                    writer.writeStartElement(prefix, "resource-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceRef(writer, resourceRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(application, "resourceRef");
                }
            }
        }

        // ELEMENT: resourceEnvRef
        final KeyedCollection<String, ResourceEnvRef> resourceEnvRef = application.resourceEnvRef;
        if (resourceEnvRef != null) {
            for (final ResourceEnvRef resourceEnvRefItem : resourceEnvRef) {
                if (resourceEnvRefItem != null) {
                    writer.writeStartElement(prefix, "resource-env-ref", "http://java.sun.com/xml/ns/javaee");
                    writeResourceEnvRef(writer, resourceEnvRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(application, "resourceEnvRef");
                }
            }
        }

        // ELEMENT: messageDestinationRef
        final KeyedCollection<String, MessageDestinationRef> messageDestinationRef = application.messageDestinationRef;
        if (messageDestinationRef != null) {
            for (final MessageDestinationRef messageDestinationRefItem : messageDestinationRef) {
                if (messageDestinationRefItem != null) {
                    writer.writeStartElement(prefix, "message-destination-ref", "http://java.sun.com/xml/ns/javaee");
                    writeMessageDestinationRef(writer, messageDestinationRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(application, "messageDestinationRef");
                }
            }
        }

        // ELEMENT: persistenceContextRef
        final KeyedCollection<String, PersistenceContextRef> persistenceContextRef = application.persistenceContextRef;
        if (persistenceContextRef != null) {
            for (final PersistenceContextRef persistenceContextRefItem : persistenceContextRef) {
                if (persistenceContextRefItem != null) {
                    writer.writeStartElement(prefix, "persistence-context-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceContextRef(writer, persistenceContextRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(application, "persistenceContextRef");
                }
            }
        }

        // ELEMENT: persistenceUnitRef
        final KeyedCollection<String, PersistenceUnitRef> persistenceUnitRef = application.persistenceUnitRef;
        if (persistenceUnitRef != null) {
            for (final PersistenceUnitRef persistenceUnitRefItem : persistenceUnitRef) {
                if (persistenceUnitRefItem != null) {
                    writer.writeStartElement(prefix, "persistence-unit-ref", "http://java.sun.com/xml/ns/javaee");
                    writePersistenceUnitRef(writer, persistenceUnitRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(application, "persistenceUnitRef");
                }
            }
        }

        // ELEMENT: messageDestination
        final KeyedCollection<String, MessageDestination> messageDestination = application.messageDestination;
        if (messageDestination != null) {
            for (final MessageDestination messageDestinationItem : messageDestination) {
                if (messageDestinationItem != null) {
                    writer.writeStartElement(prefix, "message-destination", "http://java.sun.com/xml/ns/javaee");
                    writeMessageDestination(writer, messageDestinationItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(application, "messageDestination");
                }
            }
        }

        // ELEMENT: dataSource
        final KeyedCollection<String, DataSource> dataSource = application.dataSource;
        if (dataSource != null) {
            for (final DataSource dataSourceItem : dataSource) {
                if (dataSourceItem != null) {
                    writer.writeStartElement(prefix, "data-source", "http://java.sun.com/xml/ns/javaee");
                    writeDataSource(writer, dataSourceItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(application, LifecycleCallback.NONE);
    }

}
