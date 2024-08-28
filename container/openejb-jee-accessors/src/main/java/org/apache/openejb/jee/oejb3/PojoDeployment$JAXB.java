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

package org.apache.openejb.jee.oejb3;

import java.util.Properties;
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
public class PojoDeployment$JAXB
    extends JAXBObject<PojoDeployment>
{


    public PojoDeployment$JAXB() {
        super(PojoDeployment.class, new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "pojo-deployment".intern()), new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "pojoDeployment".intern()));
    }

    public static PojoDeployment readPojoDeployment(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writePojoDeployment(XoXMLStreamWriter writer, PojoDeployment pojoDeployment, RuntimeContext context)
        throws Exception
    {
        _write(writer, pojoDeployment, context);
    }

    public void write(XoXMLStreamWriter writer, PojoDeployment pojoDeployment, RuntimeContext context)
        throws Exception
    {
        _write(writer, pojoDeployment, context);
    }

    public static final PojoDeployment _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        PojoDeployment pojoDeployment = new PojoDeployment();
        context.beforeUnmarshal(pojoDeployment, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("pojoDeployment"!= xsiType.getLocalPart())||("http://www.openejb.org/openejb-jar/1.1"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, PojoDeployment.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("class-name" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: className
                String className = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, className, pojoDeployment);
                pojoDeployment.className = className;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "class-name"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("properties" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: properties
                String propertiesRaw = elementReader.getElementText();

                Properties properties;
                try {
                    properties = Adapters.propertiesAdapterAdapter.unmarshal(propertiesRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, PropertiesAdapter.class, Properties.class, Properties.class, e);
                    continue;
                }

                pojoDeployment.properties = properties;
            } else {
                context.unexpectedElement(elementReader, new QName("http://www.openejb.org/openejb-jar/1.1", "properties"));
            }
        }

        context.afterUnmarshal(pojoDeployment, LifecycleCallback.NONE);

        return pojoDeployment;
    }

    public final PojoDeployment read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, PojoDeployment pojoDeployment, RuntimeContext context)
        throws Exception
    {
        if (pojoDeployment == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (PojoDeployment.class!= pojoDeployment.getClass()) {
            context.unexpectedSubclass(writer, pojoDeployment, PojoDeployment.class);
            return ;
        }

        context.beforeMarshal(pojoDeployment, LifecycleCallback.NONE);


        // ATTRIBUTE: className
        String classNameRaw = pojoDeployment.className;
        if (classNameRaw!= null) {
            String className = null;
            try {
                className = Adapters.collapsedStringAdapterAdapter.marshal(classNameRaw);
            } catch (Exception e) {
                context.xmlAdapterError(pojoDeployment, "className", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "class-name", className);
        }

        // ELEMENT: properties
        Properties propertiesRaw = pojoDeployment.properties;
        String properties = null;
        try {
            properties = Adapters.propertiesAdapterAdapter.marshal(propertiesRaw);
        } catch (Exception e) {
            context.xmlAdapterError(pojoDeployment, "properties", PropertiesAdapter.class, Properties.class, Properties.class, e);
        }
        if (properties!= null) {
            writer.writeStartElementWithAutoPrefix("http://www.openejb.org/openejb-jar/1.1", "properties");
            writer.writeCharacters(properties);
            writer.writeEndElement();
        }

        context.afterMarshal(pojoDeployment, LifecycleCallback.NONE);
    }

}
