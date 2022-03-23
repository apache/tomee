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

import static org.apache.openejb.jee.CmrFieldType$JAXB.parseCmrFieldType;
import static org.apache.openejb.jee.CmrFieldType$JAXB.toStringCmrFieldType;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class CmrField$JAXB
    extends JAXBObject<CmrField> {


    public CmrField$JAXB() {
        super(CmrField.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "cmr-fieldType".intern()), Text$JAXB.class, CmrFieldType$JAXB.class);
    }

    public static CmrField readCmrField(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeCmrField(final XoXMLStreamWriter writer, final CmrField cmrField, final RuntimeContext context)
        throws Exception {
        _write(writer, cmrField, context);
    }

    public void write(final XoXMLStreamWriter writer, final CmrField cmrField, final RuntimeContext context)
        throws Exception {
        _write(writer, cmrField, context);
    }

    public final static CmrField _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final CmrField cmrField = new CmrField();
        context.beforeUnmarshal(cmrField, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("cmr-fieldType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, CmrField.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, cmrField);
                cmrField.id = id;
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
            } else if (("cmr-field-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: cmrFieldName
                final String cmrFieldNameRaw = elementReader.getElementAsString();

                final String cmrFieldName;
                try {
                    cmrFieldName = Adapters.collapsedStringAdapterAdapter.unmarshal(cmrFieldNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                cmrField.cmrFieldName = cmrFieldName;
            } else if (("cmr-field-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: cmrFieldType
                final CmrFieldType cmrFieldType = parseCmrFieldType(elementReader, context, elementReader.getElementAsString());
                if (cmrFieldType != null) {
                    cmrField.cmrFieldType = cmrFieldType;
                }
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "cmr-field-name"), new QName("http://java.sun.com/xml/ns/javaee", "cmr-field-type"));
            }
        }
        if (descriptions != null) {
            try {
                cmrField.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, CmrField.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(cmrField, LifecycleCallback.NONE);

        return cmrField;
    }

    public final CmrField read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final CmrField cmrField, RuntimeContext context)
        throws Exception {
        if (cmrField == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (CmrField.class != cmrField.getClass()) {
            context.unexpectedSubclass(writer, cmrField, CmrField.class);
            return;
        }

        context.beforeMarshal(cmrField, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = cmrField.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(cmrField, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = cmrField.getDescriptions();
        } catch (final Exception e) {
            context.getterError(cmrField, "descriptions", CmrField.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(cmrField, "descriptions");
                }
            }
        }

        // ELEMENT: cmrFieldName
        final String cmrFieldNameRaw = cmrField.cmrFieldName;
        String cmrFieldName = null;
        try {
            cmrFieldName = Adapters.collapsedStringAdapterAdapter.marshal(cmrFieldNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(cmrField, "cmrFieldName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (cmrFieldName != null) {
            writer.writeStartElement(prefix, "cmr-field-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(cmrFieldName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(cmrField, "cmrFieldName");
        }

        // ELEMENT: cmrFieldType
        final CmrFieldType cmrFieldType = cmrField.cmrFieldType;
        if (cmrFieldType != null) {
            writer.writeStartElement(prefix, "cmr-field-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringCmrFieldType(cmrField, null, context, cmrFieldType));
            writer.writeEndElement();
        }

        context.afterMarshal(cmrField, LifecycleCallback.NONE);
    }

}
