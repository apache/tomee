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

import java.util.ArrayList;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class ParamValue$JAXB
    extends JAXBObject<ParamValue>
{


    public ParamValue$JAXB() {
        super(ParamValue.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "param-valueType".intern()), Text$JAXB.class);
    }

    public static ParamValue readParamValue(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeParamValue(XoXMLStreamWriter writer, ParamValue paramValue, RuntimeContext context)
        throws Exception
    {
        _write(writer, paramValue, context);
    }

    public void write(XoXMLStreamWriter writer, ParamValue paramValue, RuntimeContext context)
        throws Exception
    {
        _write(writer, paramValue, context);
    }

    public static final ParamValue _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ParamValue paramValue = new ParamValue();
        context.beforeUnmarshal(paramValue, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("param-valueType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ParamValue.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, paramValue);
                paramValue.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("description" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<>();
                }
                descriptions.add(descriptionsItem);
            } else if (("param-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: paramName
                String paramNameRaw = elementReader.getElementText();

                String paramName;
                try {
                    paramName = Adapters.collapsedStringAdapterAdapter.unmarshal(paramNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                paramValue.paramName = paramName;
            } else if (("param-value" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: paramValue
                String paramValue1Raw = elementReader.getElementText();

                String paramValue1;
                try {
                    paramValue1 = Adapters.collapsedStringAdapterAdapter.unmarshal(paramValue1Raw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                paramValue.paramValue = paramValue1;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "param-name"), new QName("http://java.sun.com/xml/ns/javaee", "param-value"));
            }
        }
        if (descriptions!= null) {
            try {
                paramValue.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, ParamValue.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(paramValue, LifecycleCallback.NONE);

        return paramValue;
    }

    public final ParamValue read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, ParamValue paramValue, RuntimeContext context)
        throws Exception
    {
        if (paramValue == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ParamValue.class!= paramValue.getClass()) {
            context.unexpectedSubclass(writer, paramValue, ParamValue.class);
            return ;
        }

        context.beforeMarshal(paramValue, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = paramValue.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(paramValue, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = paramValue.getDescriptions();
        } catch (Exception e) {
            context.getterError(paramValue, "descriptions", ParamValue.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(paramValue, "descriptions");
                }
            }
        }

        // ELEMENT: paramName
        String paramNameRaw = paramValue.paramName;
        String paramName = null;
        try {
            paramName = Adapters.collapsedStringAdapterAdapter.marshal(paramNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(paramValue, "paramName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (paramName!= null) {
            writer.writeStartElement(prefix, "param-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(paramName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(paramValue, "paramName");
        }

        // ELEMENT: paramValue
        String paramValueRaw = paramValue.paramValue;
        String paramValue1 = null;
        try {
            paramValue1 = Adapters.collapsedStringAdapterAdapter.marshal(paramValueRaw);
        } catch (Exception e) {
            context.xmlAdapterError(paramValue, "paramValue", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (paramValue1 != null) {
            writer.writeStartElement(prefix, "param-value", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(paramValue1);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(paramValue, "paramValue");
        }

        context.afterMarshal(paramValue, LifecycleCallback.NONE);
    }

}
