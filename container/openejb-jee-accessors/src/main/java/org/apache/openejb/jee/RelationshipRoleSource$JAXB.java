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

import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class RelationshipRoleSource$JAXB
    extends JAXBObject<RelationshipRoleSource> {


    public RelationshipRoleSource$JAXB() {
        super(RelationshipRoleSource.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "relationship-role-sourceType".intern()), Text$JAXB.class);
    }

    public static RelationshipRoleSource readRelationshipRoleSource(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeRelationshipRoleSource(final XoXMLStreamWriter writer, final RelationshipRoleSource relationshipRoleSource, final RuntimeContext context)
        throws Exception {
        _write(writer, relationshipRoleSource, context);
    }

    public void write(final XoXMLStreamWriter writer, final RelationshipRoleSource relationshipRoleSource, final RuntimeContext context)
        throws Exception {
        _write(writer, relationshipRoleSource, context);
    }

    public final static RelationshipRoleSource _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final RelationshipRoleSource relationshipRoleSource = new RelationshipRoleSource();
        context.beforeUnmarshal(relationshipRoleSource, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("relationship-role-sourceType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, RelationshipRoleSource.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, relationshipRoleSource);
                relationshipRoleSource.id = id;
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

                relationshipRoleSource.ejbName = ejbName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-name"));
            }
        }
        if (descriptions != null) {
            try {
                relationshipRoleSource.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, RelationshipRoleSource.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(relationshipRoleSource, LifecycleCallback.NONE);

        return relationshipRoleSource;
    }

    public final RelationshipRoleSource read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final RelationshipRoleSource relationshipRoleSource, RuntimeContext context)
        throws Exception {
        if (relationshipRoleSource == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (RelationshipRoleSource.class != relationshipRoleSource.getClass()) {
            context.unexpectedSubclass(writer, relationshipRoleSource, RelationshipRoleSource.class);
            return;
        }

        context.beforeMarshal(relationshipRoleSource, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = relationshipRoleSource.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(relationshipRoleSource, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = relationshipRoleSource.getDescriptions();
        } catch (final Exception e) {
            context.getterError(relationshipRoleSource, "descriptions", RelationshipRoleSource.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(relationshipRoleSource, "descriptions");
                }
            }
        }

        // ELEMENT: ejbName
        final String ejbNameRaw = relationshipRoleSource.ejbName;
        String ejbName = null;
        try {
            ejbName = Adapters.collapsedStringAdapterAdapter.marshal(ejbNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(relationshipRoleSource, "ejbName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbName != null) {
            writer.writeStartElement(prefix, "ejb-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(relationshipRoleSource, "ejbName");
        }

        context.afterMarshal(relationshipRoleSource, LifecycleCallback.NONE);
    }

}
