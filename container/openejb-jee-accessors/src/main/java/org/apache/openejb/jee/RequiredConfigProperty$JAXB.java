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
public class RequiredConfigProperty$JAXB
    extends JAXBObject<RequiredConfigProperty>
{


    public RequiredConfigProperty$JAXB() {
        super(RequiredConfigProperty.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "required-config-propertyType".intern()), Text$JAXB.class);
    }

    public static RequiredConfigProperty readRequiredConfigProperty(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeRequiredConfigProperty(XoXMLStreamWriter writer, RequiredConfigProperty requiredConfigProperty, RuntimeContext context)
        throws Exception
    {
        _write(writer, requiredConfigProperty, context);
    }

    public void write(XoXMLStreamWriter writer, RequiredConfigProperty requiredConfigProperty, RuntimeContext context)
        throws Exception
    {
        _write(writer, requiredConfigProperty, context);
    }

    public static final RequiredConfigProperty _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        RequiredConfigProperty requiredConfigProperty = new RequiredConfigProperty();
        context.beforeUnmarshal(requiredConfigProperty, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("required-config-propertyType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, RequiredConfigProperty.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, requiredConfigProperty);
                requiredConfigProperty.id = id;
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
            } else if (("config-property-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: configPropertyName
                String configPropertyNameRaw = elementReader.getElementText();

                String configPropertyName;
                try {
                    configPropertyName = Adapters.collapsedStringAdapterAdapter.unmarshal(configPropertyNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                requiredConfigProperty.configPropertyName = configPropertyName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "config-property-name"));
            }
        }
        if (descriptions!= null) {
            try {
                requiredConfigProperty.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, RequiredConfigProperty.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(requiredConfigProperty, LifecycleCallback.NONE);

        return requiredConfigProperty;
    }

    public final RequiredConfigProperty read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, RequiredConfigProperty requiredConfigProperty, RuntimeContext context)
        throws Exception
    {
        if (requiredConfigProperty == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (RequiredConfigProperty.class!= requiredConfigProperty.getClass()) {
            context.unexpectedSubclass(writer, requiredConfigProperty, RequiredConfigProperty.class);
            return ;
        }

        context.beforeMarshal(requiredConfigProperty, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = requiredConfigProperty.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(requiredConfigProperty, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = requiredConfigProperty.getDescriptions();
        } catch (Exception e) {
            context.getterError(requiredConfigProperty, "descriptions", RequiredConfigProperty.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(requiredConfigProperty, "descriptions");
                }
            }
        }

        // ELEMENT: configPropertyName
        String configPropertyNameRaw = requiredConfigProperty.configPropertyName;
        String configPropertyName = null;
        try {
            configPropertyName = Adapters.collapsedStringAdapterAdapter.marshal(configPropertyNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(requiredConfigProperty, "configPropertyName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (configPropertyName!= null) {
            writer.writeStartElement(prefix, "config-property-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(configPropertyName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(requiredConfigProperty, "configPropertyName");
        }

        context.afterMarshal(requiredConfigProperty, LifecycleCallback.NONE);
    }

}
