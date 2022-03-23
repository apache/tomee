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

import static org.apache.openejb.jee.CmrField$JAXB.readCmrField;
import static org.apache.openejb.jee.CmrField$JAXB.writeCmrField;
import static org.apache.openejb.jee.Empty$JAXB.readEmpty;
import static org.apache.openejb.jee.Empty$JAXB.writeEmpty;
import static org.apache.openejb.jee.Multiplicity$JAXB.parseMultiplicity;
import static org.apache.openejb.jee.Multiplicity$JAXB.toStringMultiplicity;
import static org.apache.openejb.jee.RelationshipRoleSource$JAXB.readRelationshipRoleSource;
import static org.apache.openejb.jee.RelationshipRoleSource$JAXB.writeRelationshipRoleSource;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class EjbRelationshipRole$JAXB
    extends JAXBObject<EjbRelationshipRole> {


    public EjbRelationshipRole$JAXB() {
        super(EjbRelationshipRole.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "ejb-relationship-roleType".intern()), Text$JAXB.class, Multiplicity$JAXB.class, Empty$JAXB.class, RelationshipRoleSource$JAXB.class, CmrField$JAXB.class);
    }

    public static EjbRelationshipRole readEjbRelationshipRole(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeEjbRelationshipRole(final XoXMLStreamWriter writer, final EjbRelationshipRole ejbRelationshipRole, final RuntimeContext context)
        throws Exception {
        _write(writer, ejbRelationshipRole, context);
    }

    public void write(final XoXMLStreamWriter writer, final EjbRelationshipRole ejbRelationshipRole, final RuntimeContext context)
        throws Exception {
        _write(writer, ejbRelationshipRole, context);
    }

    public final static EjbRelationshipRole _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final EjbRelationshipRole ejbRelationshipRole = new EjbRelationshipRole();
        context.beforeUnmarshal(ejbRelationshipRole, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("ejb-relationship-roleType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, EjbRelationshipRole.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, ejbRelationshipRole);
                ejbRelationshipRole.id = id;
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
            } else if (("ejb-relationship-role-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbRelationshipRoleName
                final String ejbRelationshipRoleNameRaw = elementReader.getElementAsString();

                final String ejbRelationshipRoleName;
                try {
                    ejbRelationshipRoleName = Adapters.collapsedStringAdapterAdapter.unmarshal(ejbRelationshipRoleNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                ejbRelationshipRole.ejbRelationshipRoleName = ejbRelationshipRoleName;
            } else if (("multiplicity" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: multiplicity
                final Multiplicity multiplicity = parseMultiplicity(elementReader, context, elementReader.getElementAsString());
                if (multiplicity != null) {
                    ejbRelationshipRole.multiplicity = multiplicity;
                }
            } else if (("cascade-delete" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: cascadeDelete
                final Empty cascadeDelete = readEmpty(elementReader, context);
                ejbRelationshipRole.cascadeDelete = cascadeDelete;
            } else if (("relationship-role-source" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: relationshipRoleSource
                final RelationshipRoleSource relationshipRoleSource = readRelationshipRoleSource(elementReader, context);
                ejbRelationshipRole.relationshipRoleSource = relationshipRoleSource;
            } else if (("cmr-field" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: cmrField
                final CmrField cmrField = readCmrField(elementReader, context);
                ejbRelationshipRole.cmrField = cmrField;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-relationship-role-name"), new QName("http://java.sun.com/xml/ns/javaee", "multiplicity"), new QName("http://java.sun.com/xml/ns/javaee", "cascade-delete"), new QName("http://java.sun.com/xml/ns/javaee", "relationship-role-source"), new QName("http://java.sun.com/xml/ns/javaee", "cmr-field"));
            }
        }
        if (descriptions != null) {
            try {
                ejbRelationshipRole.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, EjbRelationshipRole.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(ejbRelationshipRole, LifecycleCallback.NONE);

        return ejbRelationshipRole;
    }

    public final EjbRelationshipRole read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final EjbRelationshipRole ejbRelationshipRole, RuntimeContext context)
        throws Exception {
        if (ejbRelationshipRole == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (EjbRelationshipRole.class != ejbRelationshipRole.getClass()) {
            context.unexpectedSubclass(writer, ejbRelationshipRole, EjbRelationshipRole.class);
            return;
        }

        context.beforeMarshal(ejbRelationshipRole, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = ejbRelationshipRole.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(ejbRelationshipRole, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = ejbRelationshipRole.getDescriptions();
        } catch (final Exception e) {
            context.getterError(ejbRelationshipRole, "descriptions", EjbRelationshipRole.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(ejbRelationshipRole, "descriptions");
                }
            }
        }

        // ELEMENT: ejbRelationshipRoleName
        final String ejbRelationshipRoleNameRaw = ejbRelationshipRole.ejbRelationshipRoleName;
        String ejbRelationshipRoleName = null;
        try {
            ejbRelationshipRoleName = Adapters.collapsedStringAdapterAdapter.marshal(ejbRelationshipRoleNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(ejbRelationshipRole, "ejbRelationshipRoleName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbRelationshipRoleName != null) {
            writer.writeStartElement(prefix, "ejb-relationship-role-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbRelationshipRoleName);
            writer.writeEndElement();
        }

        // ELEMENT: multiplicity
        final Multiplicity multiplicity = ejbRelationshipRole.multiplicity;
        if (multiplicity != null) {
            writer.writeStartElement(prefix, "multiplicity", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringMultiplicity(ejbRelationshipRole, null, context, multiplicity));
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(ejbRelationshipRole, "multiplicity");
        }

        // ELEMENT: cascadeDelete
        final Empty cascadeDelete = ejbRelationshipRole.cascadeDelete;
        if (cascadeDelete != null) {
            writer.writeStartElement(prefix, "cascade-delete", "http://java.sun.com/xml/ns/javaee");
            writeEmpty(writer, cascadeDelete, context);
            writer.writeEndElement();
        }

        // ELEMENT: relationshipRoleSource
        final RelationshipRoleSource relationshipRoleSource = ejbRelationshipRole.relationshipRoleSource;
        if (relationshipRoleSource != null) {
            writer.writeStartElement(prefix, "relationship-role-source", "http://java.sun.com/xml/ns/javaee");
            writeRelationshipRoleSource(writer, relationshipRoleSource, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(ejbRelationshipRole, "relationshipRoleSource");
        }

        // ELEMENT: cmrField
        final CmrField cmrField = ejbRelationshipRole.cmrField;
        if (cmrField != null) {
            writer.writeStartElement(prefix, "cmr-field", "http://java.sun.com/xml/ns/javaee");
            writeCmrField(writer, cmrField, context);
            writer.writeEndElement();
        }

        context.afterMarshal(ejbRelationshipRole, LifecycleCallback.NONE);
    }

}
