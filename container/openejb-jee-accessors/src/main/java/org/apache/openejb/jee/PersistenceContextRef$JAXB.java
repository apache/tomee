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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.apache.openejb.jee.InjectionTarget$JAXB.readInjectionTarget;
import static org.apache.openejb.jee.InjectionTarget$JAXB.writeInjectionTarget;
import static org.apache.openejb.jee.PersistenceContextType$JAXB.parsePersistenceContextType;
import static org.apache.openejb.jee.PersistenceContextType$JAXB.toStringPersistenceContextType;
import static org.apache.openejb.jee.Property$JAXB.readProperty;
import static org.apache.openejb.jee.Property$JAXB.writeProperty;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class PersistenceContextRef$JAXB
        extends JAXBObject<PersistenceContextRef> {


    public PersistenceContextRef$JAXB() {
        super(PersistenceContextRef.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "persistence-context-refType".intern()), Text$JAXB.class, PersistenceContextType$JAXB.class, Property$JAXB.class, InjectionTarget$JAXB.class);
    }

    public static PersistenceContextRef readPersistenceContextRef(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writePersistenceContextRef(XoXMLStreamWriter writer, PersistenceContextRef persistenceContextRef, RuntimeContext context)
            throws Exception {
        _write(writer, persistenceContextRef, context);
    }

    public void write(XoXMLStreamWriter writer, PersistenceContextRef persistenceContextRef, RuntimeContext context)
            throws Exception {
        _write(writer, persistenceContextRef, context);
    }

    public final static PersistenceContextRef _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        PersistenceContextRef persistenceContextRef = new PersistenceContextRef();
        context.beforeUnmarshal(persistenceContextRef, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        List<Property> persistenceProperty = null;
        Set<InjectionTarget> injectionTarget = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("persistence-context-refType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, PersistenceContextRef.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, persistenceContextRef);
                persistenceContextRef.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("persistence-context-ref-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: persistenceContextRefName
                String persistenceContextRefNameRaw = elementReader.getElementAsString();

                String persistenceContextRefName;
                try {
                    persistenceContextRefName = Adapters.collapsedStringAdapterAdapter.unmarshal(persistenceContextRefNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                persistenceContextRef.persistenceContextRefName = persistenceContextRefName;
            } else if (("persistence-unit-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: persistenceUnitName
                String persistenceUnitNameRaw = elementReader.getElementAsString();

                String persistenceUnitName;
                try {
                    persistenceUnitName = Adapters.collapsedStringAdapterAdapter.unmarshal(persistenceUnitNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                persistenceContextRef.persistenceUnitName = persistenceUnitName;
            } else if (("persistence-context-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: persistenceContextType
                PersistenceContextType persistenceContextType = parsePersistenceContextType(elementReader, context, elementReader.getElementAsString());
                if (persistenceContextType != null) {
                    persistenceContextRef.persistenceContextType = persistenceContextType;
                }
            } else if (("persistence-property" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: persistenceProperty
                Property persistencePropertyItem = readProperty(elementReader, context);
                if (persistenceProperty == null) {
                    persistenceProperty = persistenceContextRef.persistenceProperty;
                    if (persistenceProperty != null) {
                        persistenceProperty.clear();
                    } else {
                        persistenceProperty = new ArrayList<Property>();
                    }
                }
                persistenceProperty.add(persistencePropertyItem);
            } else if (("mapped-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: mappedName
                String mappedNameRaw = elementReader.getElementAsString();

                String mappedName;
                try {
                    mappedName = Adapters.collapsedStringAdapterAdapter.unmarshal(mappedNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                persistenceContextRef.mappedName = mappedName;
            } else if (("injection-target" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: injectionTarget
                InjectionTarget injectionTargetItem = readInjectionTarget(elementReader, context);
                if (injectionTarget == null) {
                    injectionTarget = persistenceContextRef.injectionTarget;
                    if (injectionTarget != null) {
                        injectionTarget.clear();
                    } else {
                        injectionTarget = new LinkedHashSet<InjectionTarget>();
                    }
                }
                injectionTarget.add(injectionTargetItem);
            } else if (("lookup-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: lookupName
                String lookupNameRaw = elementReader.getElementAsString();

                String lookupName;
                try {
                    lookupName = Adapters.collapsedStringAdapterAdapter.unmarshal(lookupNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                persistenceContextRef.lookupName = lookupName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-context-ref-name"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-unit-name"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-context-type"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-property"), new QName("http://java.sun.com/xml/ns/javaee", "mapped-name"), new QName("http://java.sun.com/xml/ns/javaee", "injection-target"), new QName("http://java.sun.com/xml/ns/javaee", "lookup-name"));
            }
        }
        if (descriptions != null) {
            try {
                persistenceContextRef.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, PersistenceContextRef.class, "setDescriptions", Text[].class, e);
            }
        }
        if (persistenceProperty != null) {
            persistenceContextRef.persistenceProperty = persistenceProperty;
        }
        if (injectionTarget != null) {
            persistenceContextRef.injectionTarget = injectionTarget;
        }

        context.afterUnmarshal(persistenceContextRef, LifecycleCallback.NONE);

        return persistenceContextRef;
    }

    public final PersistenceContextRef read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, PersistenceContextRef persistenceContextRef, RuntimeContext context)
            throws Exception {
        if (persistenceContextRef == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (PersistenceContextRef.class != persistenceContextRef.getClass()) {
            context.unexpectedSubclass(writer, persistenceContextRef, PersistenceContextRef.class);
            return;
        }

        context.beforeMarshal(persistenceContextRef, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = persistenceContextRef.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(persistenceContextRef, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = persistenceContextRef.getDescriptions();
        } catch (Exception e) {
            context.getterError(persistenceContextRef, "descriptions", PersistenceContextRef.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(persistenceContextRef, "descriptions");
                }
            }
        }

        // ELEMENT: persistenceContextRefName
        String persistenceContextRefNameRaw = persistenceContextRef.persistenceContextRefName;
        String persistenceContextRefName = null;
        try {
            persistenceContextRefName = Adapters.collapsedStringAdapterAdapter.marshal(persistenceContextRefNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(persistenceContextRef, "persistenceContextRefName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (persistenceContextRefName != null) {
            writer.writeStartElement(prefix, "persistence-context-ref-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(persistenceContextRefName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(persistenceContextRef, "persistenceContextRefName");
        }

        // ELEMENT: persistenceUnitName
        String persistenceUnitNameRaw = persistenceContextRef.persistenceUnitName;
        String persistenceUnitName = null;
        try {
            persistenceUnitName = Adapters.collapsedStringAdapterAdapter.marshal(persistenceUnitNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(persistenceContextRef, "persistenceUnitName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (persistenceUnitName != null) {
            writer.writeStartElement(prefix, "persistence-unit-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(persistenceUnitName);
            writer.writeEndElement();
        }

        // ELEMENT: persistenceContextType
        PersistenceContextType persistenceContextType = persistenceContextRef.persistenceContextType;
        if (persistenceContextType != null) {
            writer.writeStartElement(prefix, "persistence-context-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringPersistenceContextType(persistenceContextRef, null, context, persistenceContextType));
            writer.writeEndElement();
        }

        // ELEMENT: persistenceProperty
        List<Property> persistenceProperty = persistenceContextRef.persistenceProperty;
        if (persistenceProperty != null) {
            for (Property persistencePropertyItem : persistenceProperty) {
                if (persistencePropertyItem != null) {
                    writer.writeStartElement(prefix, "persistence-property", "http://java.sun.com/xml/ns/javaee");
                    writeProperty(writer, persistencePropertyItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(persistenceContextRef, "persistenceProperty");
                }
            }
        }

        // ELEMENT: mappedName
        String mappedNameRaw = persistenceContextRef.mappedName;
        String mappedName = null;
        try {
            mappedName = Adapters.collapsedStringAdapterAdapter.marshal(mappedNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(persistenceContextRef, "mappedName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (mappedName != null) {
            writer.writeStartElement(prefix, "mapped-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(mappedName);
            writer.writeEndElement();
        }

        // ELEMENT: injectionTarget
        Set<InjectionTarget> injectionTarget = persistenceContextRef.injectionTarget;
        if (injectionTarget != null) {
            for (InjectionTarget injectionTargetItem : injectionTarget) {
                if (injectionTargetItem != null) {
                    writer.writeStartElement(prefix, "injection-target", "http://java.sun.com/xml/ns/javaee");
                    writeInjectionTarget(writer, injectionTargetItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(persistenceContextRef, "injectionTarget");
                }
            }
        }

        // ELEMENT: lookupName
        String lookupNameRaw = persistenceContextRef.lookupName;
        String lookupName = null;
        try {
            lookupName = Adapters.collapsedStringAdapterAdapter.marshal(lookupNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(persistenceContextRef, "lookupName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (lookupName != null) {
            writer.writeStartElement(prefix, "lookup-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(lookupName);
            writer.writeEndElement();
        }

        context.afterMarshal(persistenceContextRef, LifecycleCallback.NONE);
    }

}
