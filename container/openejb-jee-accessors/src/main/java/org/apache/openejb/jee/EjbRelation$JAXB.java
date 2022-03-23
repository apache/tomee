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

import static org.apache.openejb.jee.EjbRelationshipRole$JAXB.readEjbRelationshipRole;
import static org.apache.openejb.jee.EjbRelationshipRole$JAXB.writeEjbRelationshipRole;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class EjbRelation$JAXB
    extends JAXBObject<EjbRelation> {


    public EjbRelation$JAXB() {
        super(EjbRelation.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "ejb-relationType".intern()), Text$JAXB.class, EjbRelationshipRole$JAXB.class);
    }

    public static EjbRelation readEjbRelation(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeEjbRelation(final XoXMLStreamWriter writer, final EjbRelation ejbRelation, final RuntimeContext context)
        throws Exception {
        _write(writer, ejbRelation, context);
    }

    public void write(final XoXMLStreamWriter writer, final EjbRelation ejbRelation, final RuntimeContext context)
        throws Exception {
        _write(writer, ejbRelation, context);
    }

    public final static EjbRelation _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final EjbRelation ejbRelation = new EjbRelation();
        context.beforeUnmarshal(ejbRelation, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        List<EjbRelationshipRole> ejbRelationshipRole = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("ejb-relationType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, EjbRelation.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, ejbRelation);
                ejbRelation.id = id;
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
            } else if (("ejb-relation-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbRelationName
                final String ejbRelationNameRaw = elementReader.getElementAsString();

                final String ejbRelationName;
                try {
                    ejbRelationName = Adapters.collapsedStringAdapterAdapter.unmarshal(ejbRelationNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                ejbRelation.ejbRelationName = ejbRelationName;
            } else if (("ejb-relationship-role" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbRelationshipRole
                final EjbRelationshipRole ejbRelationshipRoleItem = readEjbRelationshipRole(elementReader, context);
                if (ejbRelationshipRole == null) {
                    ejbRelationshipRole = ejbRelation.ejbRelationshipRole;
                    if (ejbRelationshipRole != null) {
                        ejbRelationshipRole.clear();
                    } else {
                        ejbRelationshipRole = new ArrayList<EjbRelationshipRole>();
                    }
                }
                ejbRelationshipRole.add(ejbRelationshipRoleItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-relation-name"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-relationship-role"));
            }
        }
        if (descriptions != null) {
            try {
                ejbRelation.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, EjbRelation.class, "setDescriptions", Text[].class, e);
            }
        }
        if (ejbRelationshipRole != null) {
            ejbRelation.ejbRelationshipRole = ejbRelationshipRole;
        }

        context.afterUnmarshal(ejbRelation, LifecycleCallback.NONE);

        return ejbRelation;
    }

    public final EjbRelation read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final EjbRelation ejbRelation, RuntimeContext context)
        throws Exception {
        if (ejbRelation == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (EjbRelation.class != ejbRelation.getClass()) {
            context.unexpectedSubclass(writer, ejbRelation, EjbRelation.class);
            return;
        }

        context.beforeMarshal(ejbRelation, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = ejbRelation.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(ejbRelation, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = ejbRelation.getDescriptions();
        } catch (final Exception e) {
            context.getterError(ejbRelation, "descriptions", EjbRelation.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(ejbRelation, "descriptions");
                }
            }
        }

        // ELEMENT: ejbRelationName
        final String ejbRelationNameRaw = ejbRelation.ejbRelationName;
        String ejbRelationName = null;
        try {
            ejbRelationName = Adapters.collapsedStringAdapterAdapter.marshal(ejbRelationNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(ejbRelation, "ejbRelationName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbRelationName != null) {
            writer.writeStartElement(prefix, "ejb-relation-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbRelationName);
            writer.writeEndElement();
        }

        // ELEMENT: ejbRelationshipRole
        final List<EjbRelationshipRole> ejbRelationshipRole = ejbRelation.ejbRelationshipRole;
        if (ejbRelationshipRole != null) {
            for (final EjbRelationshipRole ejbRelationshipRoleItem : ejbRelationshipRole) {
                if (ejbRelationshipRoleItem != null) {
                    writer.writeStartElement(prefix, "ejb-relationship-role", "http://java.sun.com/xml/ns/javaee");
                    writeEjbRelationshipRole(writer, ejbRelationshipRoleItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(ejbRelation, "ejbRelationshipRole");
                }
            }
        }

        context.afterMarshal(ejbRelation, LifecycleCallback.NONE);
    }

}
