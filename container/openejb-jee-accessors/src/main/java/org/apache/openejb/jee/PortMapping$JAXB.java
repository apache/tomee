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
public class PortMapping$JAXB
    extends JAXBObject<PortMapping>
{


    public PortMapping$JAXB() {
        super(PortMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "port-mappingType".intern()));
    }

    public static PortMapping readPortMapping(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writePortMapping(XoXMLStreamWriter writer, PortMapping portMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, portMapping, context);
    }

    public void write(XoXMLStreamWriter writer, PortMapping portMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, portMapping, context);
    }

    public static final PortMapping _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        PortMapping portMapping = new PortMapping();
        context.beforeUnmarshal(portMapping, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("port-mappingType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, PortMapping.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, portMapping);
                portMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("port-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: portName
                String portNameRaw = elementReader.getElementText();

                String portName;
                try {
                    portName = Adapters.collapsedStringAdapterAdapter.unmarshal(portNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                portMapping.portName = portName;
            } else if (("java-port-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: javaPortName
                String javaPortNameRaw = elementReader.getElementText();

                String javaPortName;
                try {
                    javaPortName = Adapters.collapsedStringAdapterAdapter.unmarshal(javaPortNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                portMapping.javaPortName = javaPortName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "port-name"), new QName("http://java.sun.com/xml/ns/javaee", "java-port-name"));
            }
        }

        context.afterUnmarshal(portMapping, LifecycleCallback.NONE);

        return portMapping;
    }

    public final PortMapping read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, PortMapping portMapping, RuntimeContext context)
        throws Exception
    {
        if (portMapping == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (PortMapping.class!= portMapping.getClass()) {
            context.unexpectedSubclass(writer, portMapping, PortMapping.class);
            return ;
        }

        context.beforeMarshal(portMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = portMapping.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(portMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: portName
        String portNameRaw = portMapping.portName;
        String portName = null;
        try {
            portName = Adapters.collapsedStringAdapterAdapter.marshal(portNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(portMapping, "portName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (portName!= null) {
            writer.writeStartElement(prefix, "port-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(portName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(portMapping, "portName");
        }

        // ELEMENT: javaPortName
        String javaPortNameRaw = portMapping.javaPortName;
        String javaPortName = null;
        try {
            javaPortName = Adapters.collapsedStringAdapterAdapter.marshal(javaPortNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(portMapping, "javaPortName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (javaPortName!= null) {
            writer.writeStartElement(prefix, "java-port-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(javaPortName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(portMapping, "javaPortName");
        }

        context.afterMarshal(portMapping, LifecycleCallback.NONE);
    }

}
