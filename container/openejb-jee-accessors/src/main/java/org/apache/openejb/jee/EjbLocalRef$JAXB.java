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

import static org.apache.openejb.jee.EjbRefType$JAXB.parseEjbRefType;
import static org.apache.openejb.jee.EjbRefType$JAXB.toStringEjbRefType;
import static org.apache.openejb.jee.InjectionTarget$JAXB.readInjectionTarget;
import static org.apache.openejb.jee.InjectionTarget$JAXB.writeInjectionTarget;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class EjbLocalRef$JAXB
    extends JAXBObject<EjbLocalRef> {


    public EjbLocalRef$JAXB() {
        super(EjbLocalRef.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "ejb-local-refType".intern()), Text$JAXB.class, EjbRefType$JAXB.class, InjectionTarget$JAXB.class);
    }

    public static EjbLocalRef readEjbLocalRef(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeEjbLocalRef(final XoXMLStreamWriter writer, final EjbLocalRef ejbLocalRef, final RuntimeContext context)
        throws Exception {
        _write(writer, ejbLocalRef, context);
    }

    public void write(final XoXMLStreamWriter writer, final EjbLocalRef ejbLocalRef, final RuntimeContext context)
        throws Exception {
        _write(writer, ejbLocalRef, context);
    }

    public final static EjbLocalRef _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final EjbLocalRef ejbLocalRef = new EjbLocalRef();
        context.beforeUnmarshal(ejbLocalRef, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        Set<InjectionTarget> injectionTarget = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("ejb-local-refType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, EjbLocalRef.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, ejbLocalRef);
                ejbLocalRef.id = id;
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
            } else if (("ejb-ref-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbRefName
                final String ejbRefNameRaw = elementReader.getElementAsString();

                final String ejbRefName;
                try {
                    ejbRefName = Adapters.collapsedStringAdapterAdapter.unmarshal(ejbRefNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                ejbLocalRef.ejbRefName = ejbRefName;
            } else if (("ejb-ref-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbRefType
                final EjbRefType ejbRefType = parseEjbRefType(elementReader, context, elementReader.getElementAsString());
                if (ejbRefType != null) {
                    ejbLocalRef.ejbRefType = ejbRefType;
                }
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

                ejbLocalRef.localHome = localHome;
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

                ejbLocalRef.local = local;
            } else if (("ejb-link" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbLink
                final String ejbLinkRaw = elementReader.getElementAsString();

                final String ejbLink;
                try {
                    ejbLink = Adapters.collapsedStringAdapterAdapter.unmarshal(ejbLinkRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                ejbLocalRef.ejbLink = ejbLink;
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

                ejbLocalRef.mappedName = mappedName;
            } else if (("injection-target" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: injectionTarget
                final InjectionTarget injectionTargetItem = readInjectionTarget(elementReader, context);
                if (injectionTarget == null) {
                    injectionTarget = ejbLocalRef.injectionTarget;
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

                ejbLocalRef.lookupName = lookupName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-ref-name"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-ref-type"), new QName("http://java.sun.com/xml/ns/javaee", "local-home"), new QName("http://java.sun.com/xml/ns/javaee", "local"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-link"), new QName("http://java.sun.com/xml/ns/javaee", "mapped-name"), new QName("http://java.sun.com/xml/ns/javaee", "injection-target"), new QName("http://java.sun.com/xml/ns/javaee", "lookup-name"));
            }
        }
        if (descriptions != null) {
            try {
                ejbLocalRef.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, EjbLocalRef.class, "setDescriptions", Text[].class, e);
            }
        }
        if (injectionTarget != null) {
            ejbLocalRef.injectionTarget = injectionTarget;
        }

        context.afterUnmarshal(ejbLocalRef, LifecycleCallback.NONE);

        return ejbLocalRef;
    }

    public final EjbLocalRef read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final EjbLocalRef ejbLocalRef, RuntimeContext context)
        throws Exception {
        if (ejbLocalRef == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (EjbLocalRef.class != ejbLocalRef.getClass()) {
            context.unexpectedSubclass(writer, ejbLocalRef, EjbLocalRef.class);
            return;
        }

        context.beforeMarshal(ejbLocalRef, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = ejbLocalRef.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(ejbLocalRef, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = ejbLocalRef.getDescriptions();
        } catch (final Exception e) {
            context.getterError(ejbLocalRef, "descriptions", EjbLocalRef.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(ejbLocalRef, "descriptions");
                }
            }
        }

        // ELEMENT: ejbRefName
        final String ejbRefNameRaw = ejbLocalRef.ejbRefName;
        String ejbRefName = null;
        try {
            ejbRefName = Adapters.collapsedStringAdapterAdapter.marshal(ejbRefNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(ejbLocalRef, "ejbRefName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbRefName != null) {
            writer.writeStartElement(prefix, "ejb-ref-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbRefName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(ejbLocalRef, "ejbRefName");
        }

        // ELEMENT: ejbRefType
        final EjbRefType ejbRefType = ejbLocalRef.ejbRefType;
        if (ejbRefType != null) {
            writer.writeStartElement(prefix, "ejb-ref-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringEjbRefType(ejbLocalRef, null, context, ejbRefType));
            writer.writeEndElement();
        }

        // ELEMENT: localHome
        final String localHomeRaw = ejbLocalRef.localHome;
        String localHome = null;
        try {
            localHome = Adapters.collapsedStringAdapterAdapter.marshal(localHomeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(ejbLocalRef, "localHome", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (localHome != null) {
            writer.writeStartElement(prefix, "local-home", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(localHome);
            writer.writeEndElement();
        }

        // ELEMENT: local
        final String localRaw = ejbLocalRef.local;
        String local = null;
        try {
            local = Adapters.collapsedStringAdapterAdapter.marshal(localRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(ejbLocalRef, "local", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (local != null) {
            writer.writeStartElement(prefix, "local", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(local);
            writer.writeEndElement();
        }

        // ELEMENT: ejbLink
        final String ejbLinkRaw = ejbLocalRef.ejbLink;
        String ejbLink = null;
        try {
            ejbLink = Adapters.collapsedStringAdapterAdapter.marshal(ejbLinkRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(ejbLocalRef, "ejbLink", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbLink != null) {
            writer.writeStartElement(prefix, "ejb-link", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbLink);
            writer.writeEndElement();
        }

        // ELEMENT: mappedName
        final String mappedNameRaw = ejbLocalRef.mappedName;
        String mappedName = null;
        try {
            mappedName = Adapters.collapsedStringAdapterAdapter.marshal(mappedNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(ejbLocalRef, "mappedName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (mappedName != null) {
            writer.writeStartElement(prefix, "mapped-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(mappedName);
            writer.writeEndElement();
        }

        // ELEMENT: injectionTarget
        final Set<InjectionTarget> injectionTarget = ejbLocalRef.injectionTarget;
        if (injectionTarget != null) {
            for (final InjectionTarget injectionTargetItem : injectionTarget) {
                if (injectionTargetItem != null) {
                    writer.writeStartElement(prefix, "injection-target", "http://java.sun.com/xml/ns/javaee");
                    writeInjectionTarget(writer, injectionTargetItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(ejbLocalRef, "injectionTarget");
                }
            }
        }

        // ELEMENT: lookupName
        final String lookupNameRaw = ejbLocalRef.lookupName;
        String lookupName = null;
        try {
            lookupName = Adapters.collapsedStringAdapterAdapter.marshal(lookupNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(ejbLocalRef, "lookupName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (lookupName != null) {
            writer.writeStartElement(prefix, "lookup-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(lookupName);
            writer.writeEndElement();
        }

        context.afterMarshal(ejbLocalRef, LifecycleCallback.NONE);
    }

}
