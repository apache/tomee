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

@SuppressWarnings({
    "StringEquality"
})
public class WsdlReturnValueMapping$JAXB
    extends JAXBObject<WsdlReturnValueMapping>
{


    public WsdlReturnValueMapping$JAXB() {
        super(WsdlReturnValueMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "wsdl-return-value-mappingType".intern()));
    }

    public static WsdlReturnValueMapping readWsdlReturnValueMapping(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeWsdlReturnValueMapping(XoXMLStreamWriter writer, WsdlReturnValueMapping wsdlReturnValueMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, wsdlReturnValueMapping, context);
    }

    public void write(XoXMLStreamWriter writer, WsdlReturnValueMapping wsdlReturnValueMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, wsdlReturnValueMapping, context);
    }

    public static final WsdlReturnValueMapping _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        WsdlReturnValueMapping wsdlReturnValueMapping = new WsdlReturnValueMapping();
        context.beforeUnmarshal(wsdlReturnValueMapping, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("wsdl-return-value-mappingType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, WsdlReturnValueMapping.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, wsdlReturnValueMapping);
                wsdlReturnValueMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("method-return-value" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodReturnValue
                String methodReturnValueRaw = elementReader.getElementText();

                String methodReturnValue;
                try {
                    methodReturnValue = Adapters.collapsedStringAdapterAdapter.unmarshal(methodReturnValueRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                wsdlReturnValueMapping.methodReturnValue = methodReturnValue;
            } else if (("wsdl-message" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wsdlMessage
                QName wsdlMessage = elementReader.getElementAsQName();
                wsdlReturnValueMapping.wsdlMessage = wsdlMessage;
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

                wsdlReturnValueMapping.wsdlMessagePartName = wsdlMessagePartName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "method-return-value"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-message"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-message-part-name"));
            }
        }

        context.afterUnmarshal(wsdlReturnValueMapping, LifecycleCallback.NONE);

        return wsdlReturnValueMapping;
    }

    public final WsdlReturnValueMapping read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, WsdlReturnValueMapping wsdlReturnValueMapping, RuntimeContext context)
        throws Exception
    {
        if (wsdlReturnValueMapping == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (WsdlReturnValueMapping.class!= wsdlReturnValueMapping.getClass()) {
            context.unexpectedSubclass(writer, wsdlReturnValueMapping, WsdlReturnValueMapping.class);
            return ;
        }

        context.beforeMarshal(wsdlReturnValueMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = wsdlReturnValueMapping.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(wsdlReturnValueMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: methodReturnValue
        String methodReturnValueRaw = wsdlReturnValueMapping.methodReturnValue;
        String methodReturnValue = null;
        try {
            methodReturnValue = Adapters.collapsedStringAdapterAdapter.marshal(methodReturnValueRaw);
        } catch (Exception e) {
            context.xmlAdapterError(wsdlReturnValueMapping, "methodReturnValue", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (methodReturnValue!= null) {
            writer.writeStartElement(prefix, "method-return-value", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(methodReturnValue);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(wsdlReturnValueMapping, "methodReturnValue");
        }

        // ELEMENT: wsdlMessage
        QName wsdlMessage = wsdlReturnValueMapping.wsdlMessage;
        if (wsdlMessage!= null) {
            writer.writeStartElement(prefix, "wsdl-message", "http://java.sun.com/xml/ns/javaee");
            writer.writeQName(wsdlMessage);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(wsdlReturnValueMapping, "wsdlMessage");
        }

        // ELEMENT: wsdlMessagePartName
        String wsdlMessagePartNameRaw = wsdlReturnValueMapping.wsdlMessagePartName;
        String wsdlMessagePartName = null;
        try {
            wsdlMessagePartName = Adapters.collapsedStringAdapterAdapter.marshal(wsdlMessagePartNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(wsdlReturnValueMapping, "wsdlMessagePartName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (wsdlMessagePartName!= null) {
            writer.writeStartElement(prefix, "wsdl-message-part-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(wsdlMessagePartName);
            writer.writeEndElement();
        }

        context.afterMarshal(wsdlReturnValueMapping, LifecycleCallback.NONE);
    }

}
