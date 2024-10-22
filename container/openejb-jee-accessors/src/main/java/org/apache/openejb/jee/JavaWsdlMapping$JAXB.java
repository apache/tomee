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


import static org.apache.openejb.jee.ExceptionMapping$JAXB.readExceptionMapping;
import static org.apache.openejb.jee.ExceptionMapping$JAXB.writeExceptionMapping;
import static org.apache.openejb.jee.JavaXmlTypeMapping$JAXB.readJavaXmlTypeMapping;
import static org.apache.openejb.jee.JavaXmlTypeMapping$JAXB.writeJavaXmlTypeMapping;
import static org.apache.openejb.jee.PackageMapping$JAXB.readPackageMapping;
import static org.apache.openejb.jee.PackageMapping$JAXB.writePackageMapping;
import static org.apache.openejb.jee.ServiceEndpointInterfaceMapping$JAXB.readServiceEndpointInterfaceMapping;
import static org.apache.openejb.jee.ServiceEndpointInterfaceMapping$JAXB.writeServiceEndpointInterfaceMapping;
import static org.apache.openejb.jee.ServiceInterfaceMapping$JAXB.readServiceInterfaceMapping;
import static org.apache.openejb.jee.ServiceInterfaceMapping$JAXB.writeServiceInterfaceMapping;

@SuppressWarnings({
    "StringEquality"
})
public class JavaWsdlMapping$JAXB
    extends JAXBObject<JavaWsdlMapping>
{


    public JavaWsdlMapping$JAXB() {
        super(JavaWsdlMapping.class, new QName("http://java.sun.com/xml/ns/javaee".intern(), "java-wsdl-mapping".intern()), new QName("http://java.sun.com/xml/ns/javaee".intern(), "java-wsdl-mappingType".intern()), PackageMapping$JAXB.class, JavaXmlTypeMapping$JAXB.class, ExceptionMapping$JAXB.class, ServiceInterfaceMapping$JAXB.class, ServiceEndpointInterfaceMapping$JAXB.class);
    }

    public static JavaWsdlMapping readJavaWsdlMapping(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeJavaWsdlMapping(XoXMLStreamWriter writer, JavaWsdlMapping javaWsdlMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, javaWsdlMapping, context);
    }

    public void write(XoXMLStreamWriter writer, JavaWsdlMapping javaWsdlMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, javaWsdlMapping, context);
    }

    public static final JavaWsdlMapping _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        JavaWsdlMapping javaWsdlMapping = new JavaWsdlMapping();
        context.beforeUnmarshal(javaWsdlMapping, LifecycleCallback.NONE);

        KeyedCollection<String, PackageMapping> packageMapping = null;
        List<JavaXmlTypeMapping> javaXmlTypeMapping = null;
        KeyedCollection<QName, ExceptionMapping> exceptionMapping = null;
        List<ServiceInterfaceMapping> serviceInterfaceMapping = null;
        KeyedCollection<String, ServiceEndpointInterfaceMapping> serviceEndpointInterfaceMapping = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("java-wsdl-mappingType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, JavaWsdlMapping.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, javaWsdlMapping);
                javaWsdlMapping.id = id;
            } else if (("version" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: version
                javaWsdlMapping.version = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"), new QName("", "version"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("package-mapping" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: packageMapping
                PackageMapping packageMappingItem = readPackageMapping(elementReader, context);
                if (packageMapping == null) {
                    packageMapping = javaWsdlMapping.packageMapping;
                    if (packageMapping!= null) {
                        packageMapping.clear();
                    } else {
                        packageMapping = new KeyedCollection<>();
                    }
                }
                packageMapping.add(packageMappingItem);
            } else if (("java-xml-type-mapping" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: javaXmlTypeMapping
                JavaXmlTypeMapping javaXmlTypeMappingItem = readJavaXmlTypeMapping(elementReader, context);
                if (javaXmlTypeMapping == null) {
                    javaXmlTypeMapping = javaWsdlMapping.javaXmlTypeMapping;
                    if (javaXmlTypeMapping!= null) {
                        javaXmlTypeMapping.clear();
                    } else {
                        javaXmlTypeMapping = new ArrayList<>();
                    }
                }
                javaXmlTypeMapping.add(javaXmlTypeMappingItem);
            } else if (("exception-mapping" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: exceptionMapping
                ExceptionMapping exceptionMappingItem = readExceptionMapping(elementReader, context);
                if (exceptionMapping == null) {
                    exceptionMapping = javaWsdlMapping.exceptionMapping;
                    if (exceptionMapping!= null) {
                        exceptionMapping.clear();
                    } else {
                        exceptionMapping = new KeyedCollection<>();
                    }
                }
                exceptionMapping.add(exceptionMappingItem);
            } else if (("service-interface-mapping" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceInterfaceMapping
                ServiceInterfaceMapping serviceInterfaceMappingItem = readServiceInterfaceMapping(elementReader, context);
                if (serviceInterfaceMapping == null) {
                    serviceInterfaceMapping = javaWsdlMapping.serviceInterfaceMapping;
                    if (serviceInterfaceMapping!= null) {
                        serviceInterfaceMapping.clear();
                    } else {
                        serviceInterfaceMapping = new ArrayList<>();
                    }
                }
                serviceInterfaceMapping.add(serviceInterfaceMappingItem);
            } else if (("service-endpoint-interface-mapping" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceEndpointInterfaceMapping
                ServiceEndpointInterfaceMapping serviceEndpointInterfaceMappingItem = readServiceEndpointInterfaceMapping(elementReader, context);
                if (serviceEndpointInterfaceMapping == null) {
                    serviceEndpointInterfaceMapping = javaWsdlMapping.serviceEndpointInterfaceMapping;
                    if (serviceEndpointInterfaceMapping!= null) {
                        serviceEndpointInterfaceMapping.clear();
                    } else {
                        serviceEndpointInterfaceMapping = new KeyedCollection<>();
                    }
                }
                serviceEndpointInterfaceMapping.add(serviceEndpointInterfaceMappingItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "package-mapping"), new QName("http://java.sun.com/xml/ns/javaee", "java-xml-type-mapping"), new QName("http://java.sun.com/xml/ns/javaee", "exception-mapping"), new QName("http://java.sun.com/xml/ns/javaee", "service-interface-mapping"), new QName("http://java.sun.com/xml/ns/javaee", "service-endpoint-interface-mapping"));
            }
        }
        if (packageMapping!= null) {
            javaWsdlMapping.packageMapping = packageMapping;
        }
        if (javaXmlTypeMapping!= null) {
            javaWsdlMapping.javaXmlTypeMapping = javaXmlTypeMapping;
        }
        if (exceptionMapping!= null) {
            javaWsdlMapping.exceptionMapping = exceptionMapping;
        }
        if (serviceInterfaceMapping!= null) {
            javaWsdlMapping.serviceInterfaceMapping = serviceInterfaceMapping;
        }
        if (serviceEndpointInterfaceMapping!= null) {
            javaWsdlMapping.serviceEndpointInterfaceMapping = serviceEndpointInterfaceMapping;
        }

        context.afterUnmarshal(javaWsdlMapping, LifecycleCallback.NONE);

        return javaWsdlMapping;
    }

    public final JavaWsdlMapping read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, JavaWsdlMapping javaWsdlMapping, RuntimeContext context)
        throws Exception
    {
        if (javaWsdlMapping == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (JavaWsdlMapping.class!= javaWsdlMapping.getClass()) {
            context.unexpectedSubclass(writer, javaWsdlMapping, JavaWsdlMapping.class);
            return ;
        }

        context.beforeMarshal(javaWsdlMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = javaWsdlMapping.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(javaWsdlMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ATTRIBUTE: version
        String versionRaw = javaWsdlMapping.version;
        if (versionRaw!= null) {
            String version = null;
            try {
                version = Adapters.collapsedStringAdapterAdapter.marshal(versionRaw);
            } catch (Exception e) {
                context.xmlAdapterError(javaWsdlMapping, "version", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "version", version);
        }

        // ELEMENT: packageMapping
        KeyedCollection<String, PackageMapping> packageMapping = javaWsdlMapping.packageMapping;
        if (packageMapping!= null) {
            for (PackageMapping packageMappingItem: packageMapping) {
                if (packageMappingItem!= null) {
                    writer.writeStartElement(prefix, "package-mapping", "http://java.sun.com/xml/ns/javaee");
                    writePackageMapping(writer, packageMappingItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(javaWsdlMapping, "packageMapping");
                }
            }
        }

        // ELEMENT: javaXmlTypeMapping
        List<JavaXmlTypeMapping> javaXmlTypeMapping = javaWsdlMapping.javaXmlTypeMapping;
        if (javaXmlTypeMapping!= null) {
            for (JavaXmlTypeMapping javaXmlTypeMappingItem: javaXmlTypeMapping) {
                if (javaXmlTypeMappingItem!= null) {
                    writer.writeStartElement(prefix, "java-xml-type-mapping", "http://java.sun.com/xml/ns/javaee");
                    writeJavaXmlTypeMapping(writer, javaXmlTypeMappingItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: exceptionMapping
        KeyedCollection<QName, ExceptionMapping> exceptionMapping = javaWsdlMapping.exceptionMapping;
        if (exceptionMapping!= null) {
            for (ExceptionMapping exceptionMappingItem: exceptionMapping) {
                if (exceptionMappingItem!= null) {
                    writer.writeStartElement(prefix, "exception-mapping", "http://java.sun.com/xml/ns/javaee");
                    writeExceptionMapping(writer, exceptionMappingItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: serviceInterfaceMapping
        List<ServiceInterfaceMapping> serviceInterfaceMapping = javaWsdlMapping.serviceInterfaceMapping;
        if (serviceInterfaceMapping!= null) {
            for (ServiceInterfaceMapping serviceInterfaceMappingItem: serviceInterfaceMapping) {
                if (serviceInterfaceMappingItem!= null) {
                    writer.writeStartElement(prefix, "service-interface-mapping", "http://java.sun.com/xml/ns/javaee");
                    writeServiceInterfaceMapping(writer, serviceInterfaceMappingItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: serviceEndpointInterfaceMapping
        KeyedCollection<String, ServiceEndpointInterfaceMapping> serviceEndpointInterfaceMapping = javaWsdlMapping.serviceEndpointInterfaceMapping;
        if (serviceEndpointInterfaceMapping!= null) {
            for (ServiceEndpointInterfaceMapping serviceEndpointInterfaceMappingItem: serviceEndpointInterfaceMapping) {
                if (serviceEndpointInterfaceMappingItem!= null) {
                    writer.writeStartElement(prefix, "service-endpoint-interface-mapping", "http://java.sun.com/xml/ns/javaee");
                    writeServiceEndpointInterfaceMapping(writer, serviceEndpointInterfaceMappingItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(javaWsdlMapping, LifecycleCallback.NONE);
    }

}
