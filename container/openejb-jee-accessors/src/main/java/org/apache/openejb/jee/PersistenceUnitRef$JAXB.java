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
import java.util.LinkedHashSet;
import java.util.Set;

import static org.apache.openejb.jee.InjectionTarget$JAXB.readInjectionTarget;
import static org.apache.openejb.jee.InjectionTarget$JAXB.writeInjectionTarget;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class PersistenceUnitRef$JAXB
    extends JAXBObject<PersistenceUnitRef> {


    public PersistenceUnitRef$JAXB() {
        super(PersistenceUnitRef.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "persistence-unit-refType".intern()), Text$JAXB.class, InjectionTarget$JAXB.class);
    }

    public static PersistenceUnitRef readPersistenceUnitRef(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writePersistenceUnitRef(final XoXMLStreamWriter writer, final PersistenceUnitRef persistenceUnitRef, final RuntimeContext context)
        throws Exception {
        _write(writer, persistenceUnitRef, context);
    }

    public void write(final XoXMLStreamWriter writer, final PersistenceUnitRef persistenceUnitRef, final RuntimeContext context)
        throws Exception {
        _write(writer, persistenceUnitRef, context);
    }

    public final static PersistenceUnitRef _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final PersistenceUnitRef persistenceUnitRef = new PersistenceUnitRef();
        context.beforeUnmarshal(persistenceUnitRef, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        Set<InjectionTarget> injectionTarget = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("persistence-unit-refType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, PersistenceUnitRef.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, persistenceUnitRef);
                persistenceUnitRef.id = id;
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
            } else if (("persistence-unit-ref-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: persistenceUnitRefName
                final String persistenceUnitRefNameRaw = elementReader.getElementAsString();

                final String persistenceUnitRefName;
                try {
                    persistenceUnitRefName = Adapters.collapsedStringAdapterAdapter.unmarshal(persistenceUnitRefNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                persistenceUnitRef.persistenceUnitRefName = persistenceUnitRefName;
            } else if (("persistence-unit-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: persistenceUnitName
                final String persistenceUnitNameRaw = elementReader.getElementAsString();

                final String persistenceUnitName;
                try {
                    persistenceUnitName = Adapters.collapsedStringAdapterAdapter.unmarshal(persistenceUnitNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                persistenceUnitRef.persistenceUnitName = persistenceUnitName;
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

                persistenceUnitRef.mappedName = mappedName;
            } else if (("injection-target" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: injectionTarget
                final InjectionTarget injectionTargetItem = readInjectionTarget(elementReader, context);
                if (injectionTarget == null) {
                    injectionTarget = persistenceUnitRef.injectionTarget;
                    if (injectionTarget != null) {
                        injectionTarget.clear();
                    } else {
                        injectionTarget = new LinkedHashSet<InjectionTarget>();
                    }
                }
                injectionTarget.add(injectionTargetItem);
            } else if (("lookup-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: lookupName
                final String lookupNameRaw = elementReader.getElementAsString();

                final String lookupName;
                try {
                    lookupName = Adapters.collapsedStringAdapterAdapter.unmarshal(lookupNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                persistenceUnitRef.lookupName = lookupName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-unit-ref-name"), new QName("http://java.sun.com/xml/ns/javaee", "persistence-unit-name"), new QName("http://java.sun.com/xml/ns/javaee", "mapped-name"), new QName("http://java.sun.com/xml/ns/javaee", "injection-target"), new QName("http://java.sun.com/xml/ns/javaee", "lookup-name"));
            }
        }
        if (descriptions != null) {
            try {
                persistenceUnitRef.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, PersistenceUnitRef.class, "setDescriptions", Text[].class, e);
            }
        }
        if (injectionTarget != null) {
            persistenceUnitRef.injectionTarget = injectionTarget;
        }

        context.afterUnmarshal(persistenceUnitRef, LifecycleCallback.NONE);

        return persistenceUnitRef;
    }

    public final PersistenceUnitRef read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final PersistenceUnitRef persistenceUnitRef, RuntimeContext context)
        throws Exception {
        if (persistenceUnitRef == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (PersistenceUnitRef.class != persistenceUnitRef.getClass()) {
            context.unexpectedSubclass(writer, persistenceUnitRef, PersistenceUnitRef.class);
            return;
        }

        context.beforeMarshal(persistenceUnitRef, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = persistenceUnitRef.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(persistenceUnitRef, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = persistenceUnitRef.getDescriptions();
        } catch (final Exception e) {
            context.getterError(persistenceUnitRef, "descriptions", PersistenceUnitRef.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(persistenceUnitRef, "descriptions");
                }
            }
        }

        // ELEMENT: persistenceUnitRefName
        final String persistenceUnitRefNameRaw = persistenceUnitRef.persistenceUnitRefName;
        String persistenceUnitRefName = null;
        try {
            persistenceUnitRefName = Adapters.collapsedStringAdapterAdapter.marshal(persistenceUnitRefNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(persistenceUnitRef, "persistenceUnitRefName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (persistenceUnitRefName != null) {
            writer.writeStartElement(prefix, "persistence-unit-ref-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(persistenceUnitRefName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(persistenceUnitRef, "persistenceUnitRefName");
        }

        // ELEMENT: persistenceUnitName
        final String persistenceUnitNameRaw = persistenceUnitRef.persistenceUnitName;
        String persistenceUnitName = null;
        try {
            persistenceUnitName = Adapters.collapsedStringAdapterAdapter.marshal(persistenceUnitNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(persistenceUnitRef, "persistenceUnitName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (persistenceUnitName != null) {
            writer.writeStartElement(prefix, "persistence-unit-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(persistenceUnitName);
            writer.writeEndElement();
        }

        // ELEMENT: mappedName
        final String mappedNameRaw = persistenceUnitRef.mappedName;
        String mappedName = null;
        try {
            mappedName = Adapters.collapsedStringAdapterAdapter.marshal(mappedNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(persistenceUnitRef, "mappedName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (mappedName != null) {
            writer.writeStartElement(prefix, "mapped-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(mappedName);
            writer.writeEndElement();
        }

        // ELEMENT: injectionTarget
        final Set<InjectionTarget> injectionTarget = persistenceUnitRef.injectionTarget;
        if (injectionTarget != null) {
            for (final InjectionTarget injectionTargetItem : injectionTarget) {
                if (injectionTargetItem != null) {
                    writer.writeStartElement(prefix, "injection-target", "http://java.sun.com/xml/ns/javaee");
                    writeInjectionTarget(writer, injectionTargetItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(persistenceUnitRef, "injectionTarget");
                }
            }
        }

        // ELEMENT: lookupName
        final String lookupNameRaw = persistenceUnitRef.lookupName;
        String lookupName = null;
        try {
            lookupName = Adapters.collapsedStringAdapterAdapter.marshal(lookupNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(persistenceUnitRef, "lookupName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (lookupName != null) {
            writer.writeStartElement(prefix, "lookup-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(lookupName);
            writer.writeEndElement();
        }

        context.afterMarshal(persistenceUnitRef, LifecycleCallback.NONE);
    }

}
