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
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;

@SuppressWarnings({
        "StringEquality"
})
public class VariableMapping$JAXB
        extends JAXBObject<VariableMapping> {


    public VariableMapping$JAXB() {
        super(VariableMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "variable-mappingType".intern()));
    }

    public static VariableMapping readVariableMapping(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeVariableMapping(XoXMLStreamWriter writer, VariableMapping variableMapping, RuntimeContext context)
            throws Exception {
        _write(writer, variableMapping, context);
    }

    public void write(XoXMLStreamWriter writer, VariableMapping variableMapping, RuntimeContext context)
            throws Exception {
        _write(writer, variableMapping, context);
    }

    public final static VariableMapping _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        VariableMapping variableMapping = new VariableMapping();
        context.beforeUnmarshal(variableMapping, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("variable-mappingType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, VariableMapping.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, variableMapping);
                variableMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("java-variable-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: javaVariableName
                String javaVariableNameRaw = elementReader.getElementAsString();

                String javaVariableName;
                try {
                    javaVariableName = Adapters.collapsedStringAdapterAdapter.unmarshal(javaVariableNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                variableMapping.javaVariableName = javaVariableName;
            } else if (("data-member" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: dataMember
                Element dataMember = elementReader.getElementAsDomElement();
                variableMapping.dataMember = dataMember;
            } else if (("xml-attribute-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: xmlAttributeName
                String xmlAttributeNameRaw = elementReader.getElementAsString();

                String xmlAttributeName;
                try {
                    xmlAttributeName = Adapters.collapsedStringAdapterAdapter.unmarshal(xmlAttributeNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                variableMapping.xmlAttributeName = xmlAttributeName;
            } else if (("xml-element-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: xmlElementName
                String xmlElementNameRaw = elementReader.getElementAsString();

                String xmlElementName;
                try {
                    xmlElementName = Adapters.collapsedStringAdapterAdapter.unmarshal(xmlElementNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                variableMapping.xmlElementName = xmlElementName;
            } else if (("xml-wildcard" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: xmlWildcard
                Element xmlWildcard = elementReader.getElementAsDomElement();
                variableMapping.xmlWildcard = xmlWildcard;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "java-variable-name"), new QName("http://java.sun.com/xml/ns/javaee", "data-member"), new QName("http://java.sun.com/xml/ns/javaee", "xml-attribute-name"), new QName("http://java.sun.com/xml/ns/javaee", "xml-element-name"), new QName("http://java.sun.com/xml/ns/javaee", "xml-wildcard"));
            }
        }

        context.afterUnmarshal(variableMapping, LifecycleCallback.NONE);

        return variableMapping;
    }

    public final VariableMapping read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, VariableMapping variableMapping, RuntimeContext context)
            throws Exception {
        if (variableMapping == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (VariableMapping.class != variableMapping.getClass()) {
            context.unexpectedSubclass(writer, variableMapping, VariableMapping.class);
            return;
        }

        context.beforeMarshal(variableMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = variableMapping.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(variableMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: javaVariableName
        String javaVariableNameRaw = variableMapping.javaVariableName;
        String javaVariableName = null;
        try {
            javaVariableName = Adapters.collapsedStringAdapterAdapter.marshal(javaVariableNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(variableMapping, "javaVariableName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (javaVariableName != null) {
            writer.writeStartElement(prefix, "java-variable-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(javaVariableName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(variableMapping, "javaVariableName");
        }

        // ELEMENT: dataMember
        Object dataMember = variableMapping.dataMember;
        if (dataMember != null) {
            writer.writeStartElement(prefix, "data-member", "http://java.sun.com/xml/ns/javaee");
            writer.writeDomElement(((Element) dataMember), false);
            writer.writeEndElement();
        }

        // ELEMENT: xmlAttributeName
        String xmlAttributeNameRaw = variableMapping.xmlAttributeName;
        String xmlAttributeName = null;
        try {
            xmlAttributeName = Adapters.collapsedStringAdapterAdapter.marshal(xmlAttributeNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(variableMapping, "xmlAttributeName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (xmlAttributeName != null) {
            writer.writeStartElement(prefix, "xml-attribute-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(xmlAttributeName);
            writer.writeEndElement();
        }

        // ELEMENT: xmlElementName
        String xmlElementNameRaw = variableMapping.xmlElementName;
        String xmlElementName = null;
        try {
            xmlElementName = Adapters.collapsedStringAdapterAdapter.marshal(xmlElementNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(variableMapping, "xmlElementName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (xmlElementName != null) {
            writer.writeStartElement(prefix, "xml-element-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(xmlElementName);
            writer.writeEndElement();
        }

        // ELEMENT: xmlWildcard
        Object xmlWildcard = variableMapping.xmlWildcard;
        if (xmlWildcard != null) {
            writer.writeStartElement(prefix, "xml-wildcard", "http://java.sun.com/xml/ns/javaee");
            writer.writeDomElement(((Element) xmlWildcard), false);
            writer.writeEndElement();
        }

        context.afterMarshal(variableMapping, LifecycleCallback.NONE);
    }

}
