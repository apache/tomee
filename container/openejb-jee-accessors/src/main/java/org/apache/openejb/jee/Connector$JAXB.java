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

import com.envoisolutions.sxc.jaxb.JAXBObject;
import com.envoisolutions.sxc.jaxb.LifecycleCallback;
import com.envoisolutions.sxc.jaxb.RuntimeContext;
import com.envoisolutions.sxc.util.Attribute;
import com.envoisolutions.sxc.util.XoXMLStreamReader;
import com.envoisolutions.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.License$JAXB.readLicense;
import static org.apache.openejb.jee.License$JAXB.writeLicense;
import static org.apache.openejb.jee.ResourceAdapter$JAXB.readResourceAdapter;
import static org.apache.openejb.jee.ResourceAdapter$JAXB.writeResourceAdapter;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class Connector$JAXB
        extends JAXBObject<Connector> {


    public Connector$JAXB() {
        super(Connector.class, new QName("http://java.sun.com/xml/ns/javaee".intern(), "connector".intern()), new QName("http://java.sun.com/xml/ns/javaee".intern(), "connectorType".intern()), Text$JAXB.class, Icon$JAXB.class, License$JAXB.class, ResourceAdapter$JAXB.class);
    }

    public static Connector readConnector(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeConnector(XoXMLStreamWriter writer, Connector connector, RuntimeContext context)
            throws Exception {
        _write(writer, connector, context);
    }

    public void write(XoXMLStreamWriter writer, Connector connector, RuntimeContext context)
            throws Exception {
        _write(writer, connector, context);
    }

    public final static Connector _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Connector connector = new Connector();
        context.beforeUnmarshal(connector, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<String> requiredWorkContext = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("connectorType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Connector.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, connector);
                connector.id = id;
            } else if (("version" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: version
                connector.version = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (("metadata-complete" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: metadataComplete
                Boolean metadataComplete = ("1".equals(attribute.getValue()) || "true".equals(attribute.getValue()));
                connector.metadataComplete = metadataComplete;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"), new QName("", "version"), new QName("", "metadata-complete"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("module-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: moduleName
                String moduleNameRaw = elementReader.getElementAsString();

                String moduleName;
                try {
                    moduleName = Adapters.collapsedStringAdapterAdapter.unmarshal(moduleNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                connector.moduleName = moduleName;
            } else if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<Text>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = connector.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("vendor-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: vendorName
                String vendorNameRaw = elementReader.getElementAsString();

                String vendorName;
                try {
                    vendorName = Adapters.collapsedStringAdapterAdapter.unmarshal(vendorNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                connector.vendorName = vendorName;
            } else if (("eis-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: eisType
                String eisTypeRaw = elementReader.getElementAsString();

                String eisType;
                try {
                    eisType = Adapters.collapsedStringAdapterAdapter.unmarshal(eisTypeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                connector.eisType = eisType;
            } else if (("resourceadapter-version" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceAdapterVersion
                String resourceAdapterVersionRaw = elementReader.getElementAsString();

                String resourceAdapterVersion;
                try {
                    resourceAdapterVersion = Adapters.collapsedStringAdapterAdapter.unmarshal(resourceAdapterVersionRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                connector.resourceAdapterVersion = resourceAdapterVersion;
            } else if (("license" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: license
                License license = readLicense(elementReader, context);
                connector.license = license;
            } else if (("resourceadapter" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceAdapter
                ResourceAdapter resourceAdapter = readResourceAdapter(elementReader, context);
                connector.resourceAdapter = resourceAdapter;
            } else if (("required-work-context" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: requiredWorkContext
                String requiredWorkContextItemRaw = elementReader.getElementAsString();

                String requiredWorkContextItem;
                try {
                    requiredWorkContextItem = Adapters.collapsedStringAdapterAdapter.unmarshal(requiredWorkContextItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (requiredWorkContext == null) {
                    requiredWorkContext = connector.requiredWorkContext;
                    if (requiredWorkContext != null) {
                        requiredWorkContext.clear();
                    } else {
                        requiredWorkContext = new ArrayList<String>();
                    }
                }
                requiredWorkContext.add(requiredWorkContextItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "module-name"), new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "vendor-name"), new QName("http://java.sun.com/xml/ns/javaee", "eis-type"), new QName("http://java.sun.com/xml/ns/javaee", "resourceadapter-version"), new QName("http://java.sun.com/xml/ns/javaee", "license"), new QName("http://java.sun.com/xml/ns/javaee", "resourceadapter"), new QName("http://java.sun.com/xml/ns/javaee", "required-work-context"));
            }
        }
        if (descriptions != null) {
            try {
                connector.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, Connector.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                connector.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (Exception e) {
                context.setterError(reader, Connector.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            connector.icon = icon;
        }
        if (requiredWorkContext != null) {
            connector.requiredWorkContext = requiredWorkContext;
        }

        context.afterUnmarshal(connector, LifecycleCallback.NONE);

        return connector;
    }

    public final Connector read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, Connector connector, RuntimeContext context)
            throws Exception {
        if (connector == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Connector.class != connector.getClass()) {
            context.unexpectedSubclass(writer, connector, Connector.class);
            return;
        }

        context.beforeMarshal(connector, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = connector.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(connector, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ATTRIBUTE: version
        String versionRaw = connector.version;
        if (versionRaw != null) {
            String version = null;
            try {
                version = Adapters.collapsedStringAdapterAdapter.marshal(versionRaw);
            } catch (Exception e) {
                context.xmlAdapterError(connector, "version", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "version", version);
        }

        // ATTRIBUTE: metadataComplete
        Boolean metadataComplete = connector.metadataComplete;
        if (metadataComplete != null) {
            writer.writeAttribute("", "", "metadata-complete", Boolean.toString(metadataComplete));
        }

        // ELEMENT: moduleName
        String moduleNameRaw = connector.moduleName;
        String moduleName = null;
        try {
            moduleName = Adapters.collapsedStringAdapterAdapter.marshal(moduleNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(connector, "moduleName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (moduleName != null) {
            writer.writeStartElement(prefix, "module-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(moduleName);
            writer.writeEndElement();
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = connector.getDescriptions();
        } catch (Exception e) {
            context.getterError(connector, "descriptions", Connector.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(connector, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = connector.getDisplayNames();
        } catch (Exception e) {
            context.getterError(connector, "displayNames", Connector.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(connector, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = connector.icon;
        if (icon != null) {
            for (Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(connector, "icon");
                }
            }
        }

        // ELEMENT: vendorName
        String vendorNameRaw = connector.vendorName;
        String vendorName = null;
        try {
            vendorName = Adapters.collapsedStringAdapterAdapter.marshal(vendorNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(connector, "vendorName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (vendorName != null) {
            writer.writeStartElement(prefix, "vendor-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(vendorName);
            writer.writeEndElement();
        }

        // ELEMENT: eisType
        String eisTypeRaw = connector.eisType;
        String eisType = null;
        try {
            eisType = Adapters.collapsedStringAdapterAdapter.marshal(eisTypeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(connector, "eisType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (eisType != null) {
            writer.writeStartElement(prefix, "eis-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(eisType);
            writer.writeEndElement();
        }

        // ELEMENT: resourceAdapterVersion
        String resourceAdapterVersionRaw = connector.resourceAdapterVersion;
        String resourceAdapterVersion = null;
        try {
            resourceAdapterVersion = Adapters.collapsedStringAdapterAdapter.marshal(resourceAdapterVersionRaw);
        } catch (Exception e) {
            context.xmlAdapterError(connector, "resourceAdapterVersion", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (resourceAdapterVersion != null) {
            writer.writeStartElement(prefix, "resourceadapter-version", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(resourceAdapterVersion);
            writer.writeEndElement();
        }

        // ELEMENT: license
        License license = connector.license;
        if (license != null) {
            writer.writeStartElement(prefix, "license", "http://java.sun.com/xml/ns/javaee");
            writeLicense(writer, license, context);
            writer.writeEndElement();
        }

        // ELEMENT: resourceAdapter
        ResourceAdapter resourceAdapter = connector.resourceAdapter;
        if (resourceAdapter != null) {
            writer.writeStartElement(prefix, "resourceadapter", "http://java.sun.com/xml/ns/javaee");
            writeResourceAdapter(writer, resourceAdapter, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(connector, "resourceAdapter");
        }

        // ELEMENT: requiredWorkContext
        List<String> requiredWorkContextRaw = connector.requiredWorkContext;
        if (requiredWorkContextRaw != null) {
            for (String requiredWorkContextItem : requiredWorkContextRaw) {
                String requiredWorkContext = null;
                try {
                    requiredWorkContext = Adapters.collapsedStringAdapterAdapter.marshal(requiredWorkContextItem);
                } catch (Exception e) {
                    context.xmlAdapterError(connector, "requiredWorkContext", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (requiredWorkContext != null) {
                    writer.writeStartElement(prefix, "required-work-context", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(requiredWorkContext);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(connector, LifecycleCallback.NONE);
    }

}
