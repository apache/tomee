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

import static org.apache.openejb.jee.EjbRelation$JAXB.readEjbRelation;
import static org.apache.openejb.jee.EjbRelation$JAXB.writeEjbRelation;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class Relationships$JAXB
    extends JAXBObject<Relationships> {


    public Relationships$JAXB() {
        super(Relationships.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "relationshipsType".intern()), Text$JAXB.class, EjbRelation$JAXB.class);
    }

    public static Relationships readRelationships(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeRelationships(final XoXMLStreamWriter writer, final Relationships relationships, final RuntimeContext context)
        throws Exception {
        _write(writer, relationships, context);
    }

    public void write(final XoXMLStreamWriter writer, final Relationships relationships, final RuntimeContext context)
        throws Exception {
        _write(writer, relationships, context);
    }

    public final static Relationships _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final Relationships relationships = new Relationships();
        context.beforeUnmarshal(relationships, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        List<EjbRelation> ejbRelation = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("relationshipsType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Relationships.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, relationships);
                relationships.id = id;
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
            } else if (("ejb-relation" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbRelation
                final EjbRelation ejbRelationItem = readEjbRelation(elementReader, context);
                if (ejbRelation == null) {
                    ejbRelation = relationships.ejbRelation;
                    if (ejbRelation != null) {
                        ejbRelation.clear();
                    } else {
                        ejbRelation = new ArrayList<EjbRelation>();
                    }
                }
                ejbRelation.add(ejbRelationItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-relation"));
            }
        }
        if (descriptions != null) {
            try {
                relationships.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, Relationships.class, "setDescriptions", Text[].class, e);
            }
        }
        if (ejbRelation != null) {
            relationships.ejbRelation = ejbRelation;
        }

        context.afterUnmarshal(relationships, LifecycleCallback.NONE);

        return relationships;
    }

    public final Relationships read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final Relationships relationships, RuntimeContext context)
        throws Exception {
        if (relationships == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Relationships.class != relationships.getClass()) {
            context.unexpectedSubclass(writer, relationships, Relationships.class);
            return;
        }

        context.beforeMarshal(relationships, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = relationships.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(relationships, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = relationships.getDescriptions();
        } catch (final Exception e) {
            context.getterError(relationships, "descriptions", Relationships.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(relationships, "descriptions");
                }
            }
        }

        // ELEMENT: ejbRelation
        final List<EjbRelation> ejbRelation = relationships.ejbRelation;
        if (ejbRelation != null) {
            for (final EjbRelation ejbRelationItem : ejbRelation) {
                if (ejbRelationItem != null) {
                    writer.writeStartElement(prefix, "ejb-relation", "http://java.sun.com/xml/ns/javaee");
                    writeEjbRelation(writer, ejbRelationItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(relationships, "ejbRelation");
                }
            }
        }

        context.afterMarshal(relationships, LifecycleCallback.NONE);
    }

}
