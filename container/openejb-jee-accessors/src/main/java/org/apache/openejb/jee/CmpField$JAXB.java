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

import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class CmpField$JAXB
        extends JAXBObject<CmpField> {


    public CmpField$JAXB() {
        super(CmpField.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "cmp-fieldType".intern()), Text$JAXB.class);
    }

    public static CmpField readCmpField(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeCmpField(XoXMLStreamWriter writer, CmpField cmpField, RuntimeContext context)
            throws Exception {
        _write(writer, cmpField, context);
    }

    public void write(XoXMLStreamWriter writer, CmpField cmpField, RuntimeContext context)
            throws Exception {
        _write(writer, cmpField, context);
    }

    public final static CmpField _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        CmpField cmpField = new CmpField();
        context.beforeUnmarshal(cmpField, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("cmp-fieldType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, CmpField.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, cmpField);
                cmpField.id = id;
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
            } else if (("field-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: fieldName
                String fieldNameRaw = elementReader.getElementAsString();

                String fieldName;
                try {
                    fieldName = Adapters.collapsedStringAdapterAdapter.unmarshal(fieldNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                cmpField.fieldName = fieldName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "field-name"));
            }
        }
        if (descriptions != null) {
            try {
                cmpField.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, CmpField.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(cmpField, LifecycleCallback.NONE);

        return cmpField;
    }

    public final CmpField read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, CmpField cmpField, RuntimeContext context)
            throws Exception {
        if (cmpField == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (CmpField.class != cmpField.getClass()) {
            context.unexpectedSubclass(writer, cmpField, CmpField.class);
            return;
        }

        context.beforeMarshal(cmpField, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = cmpField.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(cmpField, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = cmpField.getDescriptions();
        } catch (Exception e) {
            context.getterError(cmpField, "descriptions", CmpField.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(cmpField, "descriptions");
                }
            }
        }

        // ELEMENT: fieldName
        String fieldNameRaw = cmpField.fieldName;
        String fieldName = null;
        try {
            fieldName = Adapters.collapsedStringAdapterAdapter.marshal(fieldNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(cmpField, "fieldName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (fieldName != null) {
            writer.writeStartElement(prefix, "field-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(fieldName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(cmpField, "fieldName");
        }

        context.afterMarshal(cmpField, LifecycleCallback.NONE);
    }

}
