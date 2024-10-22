/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;
import org.w3c.dom.Element;

@SuppressWarnings({
    "StringEquality"
})
public class WsdlMessageMapping$JAXB
    extends JAXBObject<WsdlMessageMapping>
{


    public WsdlMessageMapping$JAXB() {
        super(WsdlMessageMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "wsdl-message-mappingType".intern()));
    }

    public static WsdlMessageMapping readWsdlMessageMapping(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeWsdlMessageMapping(XoXMLStreamWriter writer, WsdlMessageMapping wsdlMessageMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, wsdlMessageMapping, context);
    }

    public void write(XoXMLStreamWriter writer, WsdlMessageMapping wsdlMessageMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, wsdlMessageMapping, context);
    }

    public static final WsdlMessageMapping _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        WsdlMessageMapping wsdlMessageMapping = new WsdlMessageMapping();
        context.beforeUnmarshal(wsdlMessageMapping, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("wsdl-message-mappingType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, WsdlMessageMapping.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, wsdlMessageMapping);
                wsdlMessageMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("wsdl-message" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wsdlMessage
                QName wsdlMessage = elementReader.getElementAsQName();
                wsdlMessageMapping.wsdlMessage = wsdlMessage;
            } else if (("wsdl-message-part-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wsdlMessagePartName
                String wsdlMessagePartNameRaw = elementReader.getElementText();

                String wsdlMessagePartName;
                try {
                    wsdlMessagePartName = Adapters.collapsedStringAdapterAdapter.unmarshal(wsdlMessagePartNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                wsdlMessageMapping.wsdlMessagePartName = wsdlMessagePartName;
            } else if (("parameter-mode" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: parameterMode
                String parameterModeRaw = elementReader.getElementText();

                String parameterMode;
                try {
                    parameterMode = Adapters.collapsedStringAdapterAdapter.unmarshal(parameterModeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                wsdlMessageMapping.parameterMode = parameterMode;
            } else if (("soap-header" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: soapHeader
                Element soapHeader = elementReader.getElementAsDomElement();
                wsdlMessageMapping.soapHeader = soapHeader;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "wsdl-message"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-message-part-name"), new QName("http://java.sun.com/xml/ns/javaee", "parameter-mode"), new QName("http://java.sun.com/xml/ns/javaee", "soap-header"));
            }
        }

        context.afterUnmarshal(wsdlMessageMapping, LifecycleCallback.NONE);

        return wsdlMessageMapping;
    }

    public final WsdlMessageMapping read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, WsdlMessageMapping wsdlMessageMapping, RuntimeContext context)
        throws Exception
    {
        if (wsdlMessageMapping == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (WsdlMessageMapping.class!= wsdlMessageMapping.getClass()) {
            context.unexpectedSubclass(writer, wsdlMessageMapping, WsdlMessageMapping.class);
            return ;
        }

        context.beforeMarshal(wsdlMessageMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = wsdlMessageMapping.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(wsdlMessageMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: wsdlMessage
        QName wsdlMessage = wsdlMessageMapping.wsdlMessage;
        if (wsdlMessage!= null) {
            writer.writeStartElement(prefix, "wsdl-message", "http://java.sun.com/xml/ns/javaee");
            writer.writeQName(wsdlMessage);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(wsdlMessageMapping, "wsdlMessage");
        }

        // ELEMENT: wsdlMessagePartName
        String wsdlMessagePartNameRaw = wsdlMessageMapping.wsdlMessagePartName;
        String wsdlMessagePartName = null;
        try {
            wsdlMessagePartName = Adapters.collapsedStringAdapterAdapter.marshal(wsdlMessagePartNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(wsdlMessageMapping, "wsdlMessagePartName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (wsdlMessagePartName!= null) {
            writer.writeStartElement(prefix, "wsdl-message-part-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(wsdlMessagePartName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(wsdlMessageMapping, "wsdlMessagePartName");
        }

        // ELEMENT: parameterMode
        String parameterModeRaw = wsdlMessageMapping.parameterMode;
        String parameterMode = null;
        try {
            parameterMode = Adapters.collapsedStringAdapterAdapter.marshal(parameterModeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(wsdlMessageMapping, "parameterMode", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (parameterMode!= null) {
            writer.writeStartElement(prefix, "parameter-mode", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(parameterMode);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(wsdlMessageMapping, "parameterMode");
        }

        // ELEMENT: soapHeader
        Object soapHeader = wsdlMessageMapping.soapHeader;
        if (soapHeader!= null) {
            writer.writeStartElement(prefix, "soap-header", "http://java.sun.com/xml/ns/javaee");
            writer.writeDomElement(((Element) soapHeader), false);
            writer.writeEndElement();
        }

        context.afterMarshal(wsdlMessageMapping, LifecycleCallback.NONE);
    }

}
