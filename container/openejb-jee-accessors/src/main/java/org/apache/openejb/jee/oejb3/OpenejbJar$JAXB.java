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

import java.util.ArrayList;
import java.util.List;
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


import static org.apache.openejb.jee.oejb3.EjbDeployment$JAXB.readEjbDeployment;
import static org.apache.openejb.jee.oejb3.EjbDeployment$JAXB.writeEjbDeployment;
import static org.apache.openejb.jee.oejb3.PojoDeployment$JAXB.readPojoDeployment;
import static org.apache.openejb.jee.oejb3.PojoDeployment$JAXB.writePojoDeployment;

@SuppressWarnings({
    "StringEquality"
})
public class OpenejbJar$JAXB
    extends JAXBObject<OpenejbJar>
{


    public OpenejbJar$JAXB() {
        super(OpenejbJar.class, new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "openejb-jar".intern()), new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "openejbJar".intern()), EjbDeployment$JAXB.class, PojoDeployment$JAXB.class);
    }

    public static OpenejbJar readOpenejbJar(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeOpenejbJar(XoXMLStreamWriter writer, OpenejbJar openejbJar, RuntimeContext context)
        throws Exception
    {
        _write(writer, openejbJar, context);
    }

    public void write(XoXMLStreamWriter writer, OpenejbJar openejbJar, RuntimeContext context)
        throws Exception
    {
        _write(writer, openejbJar, context);
    }

    public static final OpenejbJar _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        OpenejbJar openejbJar = new OpenejbJar();
        context.beforeUnmarshal(openejbJar, LifecycleCallback.NONE);

        List<EjbDeployment> ejbDeployment = null;
        List<PojoDeployment> pojoDeployment = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("openejbJar"!= xsiType.getLocalPart())||("http://www.openejb.org/openejb-jar/1.1"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, OpenejbJar.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute);
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("module-name" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: moduleName
                String moduleNameRaw = elementReader.getElementText();

                String moduleName;
                try {
                    moduleName = Adapters.collapsedStringAdapterAdapter.unmarshal(moduleNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                openejbJar.moduleName = moduleName;
            } else if (("properties" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: properties
                String propertiesRaw = elementReader.getElementText();

                Properties properties;
                try {
                    properties = Adapters.propertiesAdapterAdapter.unmarshal(propertiesRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, PropertiesAdapter.class, Properties.class, Properties.class, e);
                    continue;
                }

                openejbJar.properties = properties;
            } else if (("ejb-deployment" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbDeployment
                EjbDeployment ejbDeploymentItem = readEjbDeployment(elementReader, context);
                if (ejbDeployment == null) {
                    ejbDeployment = openejbJar.ejbDeployment;
                    if (ejbDeployment!= null) {
                        ejbDeployment.clear();
                    } else {
                        ejbDeployment = new ArrayList<>();
                    }
                }
                ejbDeployment.add(ejbDeploymentItem);
            } else if (("pojo-deployment" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: pojoDeployment
                PojoDeployment pojoDeploymentItem = readPojoDeployment(elementReader, context);
                if (pojoDeployment == null) {
                    pojoDeployment = openejbJar.pojoDeployment;
                    if (pojoDeployment!= null) {
                        pojoDeployment.clear();
                    } else {
                        pojoDeployment = new ArrayList<>();
                    }
                }
                pojoDeployment.add(pojoDeploymentItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://www.openejb.org/openejb-jar/1.1", "module-name"), new QName("http://www.openejb.org/openejb-jar/1.1", "properties"), new QName("http://www.openejb.org/openejb-jar/1.1", "ejb-deployment"), new QName("http://www.openejb.org/openejb-jar/1.1", "pojo-deployment"));
            }
        }
        if (ejbDeployment!= null) {
            openejbJar.ejbDeployment = ejbDeployment;
        }
        if (pojoDeployment!= null) {
            openejbJar.pojoDeployment = pojoDeployment;
        }

        context.afterUnmarshal(openejbJar, LifecycleCallback.NONE);

        return openejbJar;
    }

    public final OpenejbJar read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, OpenejbJar openejbJar, RuntimeContext context)
        throws Exception
    {
        if (openejbJar == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://www.openejb.org/openejb-jar/1.1");
        if (OpenejbJar.class!= openejbJar.getClass()) {
            context.unexpectedSubclass(writer, openejbJar, OpenejbJar.class);
            return ;
        }

        context.beforeMarshal(openejbJar, LifecycleCallback.NONE);


        // ELEMENT: moduleName
        String moduleNameRaw = openejbJar.moduleName;
        String moduleName = null;
        try {
            moduleName = Adapters.collapsedStringAdapterAdapter.marshal(moduleNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(openejbJar, "moduleName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (moduleName!= null) {
            writer.writeStartElement(prefix, "module-name", "http://www.openejb.org/openejb-jar/1.1");
            writer.writeCharacters(moduleName);
            writer.writeEndElement();
        }

        // ELEMENT: properties
        Properties propertiesRaw = openejbJar.properties;
        String properties = null;
        try {
            properties = Adapters.propertiesAdapterAdapter.marshal(propertiesRaw);
        } catch (Exception e) {
            context.xmlAdapterError(openejbJar, "properties", PropertiesAdapter.class, Properties.class, Properties.class, e);
        }
        if (properties!= null) {
            writer.writeStartElement(prefix, "properties", "http://www.openejb.org/openejb-jar/1.1");
            writer.writeCharacters(properties);
            writer.writeEndElement();
        }

        // ELEMENT: ejbDeployment
        List<EjbDeployment> ejbDeployment = openejbJar.ejbDeployment;
        if (ejbDeployment!= null) {
            for (EjbDeployment ejbDeploymentItem: ejbDeployment) {
                if (ejbDeploymentItem!= null) {
                    writer.writeStartElement(prefix, "ejb-deployment", "http://www.openejb.org/openejb-jar/1.1");
                    writeEjbDeployment(writer, ejbDeploymentItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(openejbJar, "ejbDeployment");
                }
            }
        }

        // ELEMENT: pojoDeployment
        List<PojoDeployment> pojoDeployment = openejbJar.pojoDeployment;
        if (pojoDeployment!= null) {
            for (PojoDeployment pojoDeploymentItem: pojoDeployment) {
                if (pojoDeploymentItem!= null) {
                    writer.writeStartElement(prefix, "pojo-deployment", "http://www.openejb.org/openejb-jar/1.1");
                    writePojoDeployment(writer, pojoDeploymentItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(openejbJar, LifecycleCallback.NONE);
    }

}
