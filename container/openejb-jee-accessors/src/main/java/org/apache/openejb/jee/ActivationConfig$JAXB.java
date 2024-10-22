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
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.ActivationConfigProperty$JAXB.readActivationConfigProperty;
import static org.apache.openejb.jee.ActivationConfigProperty$JAXB.writeActivationConfigProperty;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class ActivationConfig$JAXB
    extends JAXBObject<ActivationConfig>
{


    public ActivationConfig$JAXB() {
        super(ActivationConfig.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "activation-configType".intern()), Text$JAXB.class, ActivationConfigProperty$JAXB.class);
    }

    public static ActivationConfig readActivationConfig(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeActivationConfig(XoXMLStreamWriter writer, ActivationConfig activationConfig, RuntimeContext context)
        throws Exception
    {
        _write(writer, activationConfig, context);
    }

    public void write(XoXMLStreamWriter writer, ActivationConfig activationConfig, RuntimeContext context)
        throws Exception
    {
        _write(writer, activationConfig, context);
    }

    public static final ActivationConfig _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ActivationConfig activationConfig = new ActivationConfig();
        context.beforeUnmarshal(activationConfig, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        List<ActivationConfigProperty> activationConfigProperty = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("activation-configType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ActivationConfig.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, activationConfig);
                activationConfig.id = id;
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
            } else if (("activation-config-property" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: activationConfigProperty
                ActivationConfigProperty activationConfigPropertyItem = readActivationConfigProperty(elementReader, context);
                if (activationConfigProperty == null) {
                    activationConfigProperty = activationConfig.activationConfigProperty;
                    if (activationConfigProperty!= null) {
                        activationConfigProperty.clear();
                    } else {
                        activationConfigProperty = new ArrayList<>();
                    }
                }
                activationConfigProperty.add(activationConfigPropertyItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "activation-config-property"));
            }
        }
        if (descriptions!= null) {
            try {
                activationConfig.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, ActivationConfig.class, "setDescriptions", Text[].class, e);
            }
        }
        if (activationConfigProperty!= null) {
            activationConfig.activationConfigProperty = activationConfigProperty;
        }

        context.afterUnmarshal(activationConfig, LifecycleCallback.NONE);

        return activationConfig;
    }

    public final ActivationConfig read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, ActivationConfig activationConfig, RuntimeContext context)
        throws Exception
    {
        if (activationConfig == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ActivationConfig.class!= activationConfig.getClass()) {
            context.unexpectedSubclass(writer, activationConfig, ActivationConfig.class);
            return ;
        }

        context.beforeMarshal(activationConfig, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = activationConfig.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(activationConfig, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = activationConfig.getDescriptions();
        } catch (Exception e) {
            context.getterError(activationConfig, "descriptions", ActivationConfig.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(activationConfig, "descriptions");
                }
            }
        }

        // ELEMENT: activationConfigProperty
        List<ActivationConfigProperty> activationConfigProperty = activationConfig.activationConfigProperty;
        if (activationConfigProperty!= null) {
            for (ActivationConfigProperty activationConfigPropertyItem: activationConfigProperty) {
                if (activationConfigPropertyItem!= null) {
                    writer.writeStartElement(prefix, "activation-config-property", "http://java.sun.com/xml/ns/javaee");
                    writeActivationConfigProperty(writer, activationConfigPropertyItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(activationConfig, "activationConfigProperty");
                }
            }
        }

        context.afterMarshal(activationConfig, LifecycleCallback.NONE);
    }

}
