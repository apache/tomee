/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.axis.assembler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.JavaXmlTypeMapping;
import org.apache.openejb.jee.VariableMapping;
import static org.apache.openejb.server.axis.assembler.JaxRpcTypeInfo.SerializerType;

import javax.xml.namespace.QName;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HeavyweightTypeInfoBuilder {
    private static final String SOAP_ENCODING_NS = "http://schemas.xmlsoap.org/soap/encoding/";
    private static final String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";

    private static final Log log = LogFactory.getLog(HeavyweightTypeInfoBuilder.class);

    private final JavaWsdlMapping mapping;
    private final ClassLoader classLoader;
    private final XmlSchemaInfo schemaInfo;
    private final Set wrapperElementQNames;
    private final Collection<JaxRpcOperationInfo> operations;
    private final boolean hasEncoded;

    public HeavyweightTypeInfoBuilder(JavaWsdlMapping mapping, XmlSchemaInfo schemaInfo, ClassLoader classLoader, Set wrapperElementQNames, Collection<JaxRpcOperationInfo> operations, boolean hasEncoded) {
        this.mapping = mapping;
        this.classLoader = classLoader;
        this.schemaInfo = schemaInfo;
        this.wrapperElementQNames = wrapperElementQNames;
        this.operations = operations;
        this.hasEncoded = hasEncoded;
    }

    public List<JaxRpcTypeInfo>  buildTypeInfo() throws OpenEJBException {
        List<JaxRpcTypeInfo> typeInfos = new ArrayList<JaxRpcTypeInfo>();

        Set<QName> mappedTypeQNames = new HashSet<QName>();

        //
        // Map types with explicity Java to XML mappings
        //
        for (JavaXmlTypeMapping javaXmlTypeMapping : mapping.getJavaXmlTypeMapping()) {
            // get the QName for this mapping
            QName qname;
            if (javaXmlTypeMapping.getRootTypeQname() != null) {
                qname = javaXmlTypeMapping.getRootTypeQname();

                // Skip the wrapper elements.
                if (wrapperElementQNames.contains(qname)) {
                    continue;
                }
            } else if (javaXmlTypeMapping.getAnonymousTypeQname() != null) {
                String anonTypeQNameString = javaXmlTypeMapping.getAnonymousTypeQname();

                // this appears to be ignored...
                int pos = anonTypeQNameString.lastIndexOf(":");
                if (pos == -1) {
                    throw new OpenEJBException("anon QName is invalid, no final ':' " + anonTypeQNameString);
                }
                String namespace = anonTypeQNameString.substring(0, pos);
                String localPart = anonTypeQNameString.substring(pos + 1);
                qname = new QName(namespace, localPart);

                // Skip the wrapper elements.
                // todo why is this +2
                if (wrapperElementQNames.contains(new QName(namespace, anonTypeQNameString.substring(pos + 2)))) {
                    continue;
                }
            } else {
                throw new OpenEJBException("either root type qname or anonymous type qname must be set");
            }

            // get the xml type qname of this mapping
            QName xmlTypeQName;
            if ("element".equals(javaXmlTypeMapping.getQNameScope())) {
                XmlElementInfo elementInfo = schemaInfo.elements.get(qname);
                if (elementInfo == null) {
                    log.warn("Element [" + qname + "] not been found in schema, known elements: " + schemaInfo.elements.keySet());
                }
                xmlTypeQName = elementInfo.xmlType;
            } else {
                xmlTypeQName = qname;
            }

            // finally, get the xml type info for the mapping
            XmlTypeInfo xmlTypeInfo = schemaInfo.types.get(xmlTypeQName);
            if (xmlTypeInfo == null) {
                // if this is a built in type then assume this is a redundant mapping
                if (WebserviceNameSpaces.contains(xmlTypeInfo.qname.getNamespaceURI())) {
                    continue;
                }
                log.warn("Schema type QName [" + qname + "] not been found in schema: " + schemaInfo.types.keySet());
                continue;
            }

            // mark this type as mapped
            mappedTypeQNames.add(xmlTypeInfo.qname);

            // load the java class
            Class clazz;
            try {
                clazz = Class.forName(javaXmlTypeMapping.getJavaType(), false, classLoader);
            } catch (ClassNotFoundException e) {
                throw new OpenEJBException("Could not load java type " + javaXmlTypeMapping.getJavaType(), e);
            }

            // create the jax-rpc type mapping
            JaxRpcTypeInfo typeInfo = createTypeInfo(qname, xmlTypeInfo, clazz);
            mapFields(clazz, xmlTypeInfo, javaXmlTypeMapping, typeInfo);

            typeInfos.add(typeInfo);
        }

        //
        // Map types used in operations
        //
        for (JaxRpcOperationInfo operationInfo : operations) {
            List<JaxRpcParameterInfo> parameters = new ArrayList<JaxRpcParameterInfo>(operationInfo.parameters);

            // add the return type to the parameters so it is processed below
            if (operationInfo.returnXmlType != null) {
                JaxRpcParameterInfo returnParameter = new JaxRpcParameterInfo();
                returnParameter.xmlType = operationInfo.returnXmlType;
                returnParameter.javaType = operationInfo.returnJavaType;
                parameters.add(returnParameter);
            }

            // add type mappings for each parameter (including the return type)
            for (JaxRpcParameterInfo parameterInfo : parameters) {
                QName xmlType = parameterInfo.xmlType;

                // skip types that have already been mapped or are built in types
                if (xmlType == null ||
                        mappedTypeQNames.contains(xmlType) ||
                        xmlType.getNamespaceURI().equals(XML_SCHEMA_NS) ||
                        xmlType.getNamespaceURI().equals(SOAP_ENCODING_NS)) {
                    continue;
                }

                // get the xml type info
                XmlTypeInfo xmlTypeInfo = schemaInfo.types.get(xmlType);
                if (xmlTypeInfo == null) {
                    log.warn("Type QName [" + xmlType + "] defined by operation [" + operationInfo + "] has not been found in schema: " + schemaInfo.types.keySet());
                    continue;
                }
                mappedTypeQNames.add(xmlTypeInfo.qname);

                // load the java class
                Class<?> clazz;
                try {
                    clazz = classLoader.loadClass(parameterInfo.javaType);
                } catch (ClassNotFoundException e) {
                    throw new OpenEJBException("Could not load paramter");
                }

                // we only process simpleTypes and arrays (not normal complex types)
                if (xmlTypeInfo.simpleBaseType == null && !clazz.isArray()) {
                    if (!mappedTypeQNames.contains(xmlTypeInfo.qname)) {
                        // TODO: this lookup is not enough: the jaxrpc mapping file may define an anonymous mapping
                        log.warn("Operation " + operationInfo.name + "] uses XML type [" + xmlTypeInfo + "], whose mapping is not declared by the jaxrpc mapping file.\n Continuing deployment; " + "yet, the deployment is not-portable.");
                    }
                    continue;
                }

                // create the jax-rpc type mapping
                JaxRpcTypeInfo typeInfo = createTypeInfo(parameterInfo.qname, xmlTypeInfo, clazz);
                typeInfos.add(typeInfo);
            }
        }

        return typeInfos;
    }

    /**
     * Creates a JaxRpcTypeInfo based on the information contained in the XML Schema Type and Java Class.
     * @param xmlTypeInfo the xml schema for the type
     * @param clazz the java class for the type
     * @return the JaxRpcTypeInfo object
     * @throws OpenEJBException if the schema is invalid
     */
    private JaxRpcTypeInfo createTypeInfo(QName qname, XmlTypeInfo xmlTypeInfo, Class clazz) throws OpenEJBException {
        SerializerType serializerType;
        if (xmlTypeInfo.listType) {
            serializerType = SerializerType.LIST;
        } else if (clazz.isArray()) {
            serializerType = SerializerType.ARRAY;
        } else if (xmlTypeInfo.enumType) {
            serializerType = SerializerType.ENUM;
        } else {
            serializerType = SerializerType.OTHER;
        }

        JaxRpcTypeInfo typeInfo = new JaxRpcTypeInfo();
        typeInfo.qname = qname;
        typeInfo.javaType = clazz.getName();
        typeInfo.serializerType = serializerType;
        typeInfo.simpleBaseType = xmlTypeInfo.simpleBaseType;

        // If we understand the axis comments correctly, componentQName is never set for a webservice.
        if (serializerType == SerializerType.ARRAY) {
            typeInfo.componentType = xmlTypeInfo.arrayComponentType;
        }

        return typeInfo;
    }

    /**
     * Map the (nested) fields of a XML Schema Type to Java Beans properties or public fields of the specified Java Class.
     * @param javaClass the java class to map
     * @param xmlTypeInfo the xml schema for the type
     * @param javaXmlTypeMapping the java to xml type mapping metadata
     * @param typeInfo the JaxRpcTypeInfo for this type
     * @throws OpenEJBException if the XML Schema Type can not be mapped to the Java Class
     */
    private void mapFields(Class javaClass, XmlTypeInfo xmlTypeInfo, JavaXmlTypeMapping javaXmlTypeMapping, JaxRpcTypeInfo typeInfo) throws OpenEJBException {
        // Skip arrays since they can't define a variable-mapping element
        if (!javaClass.isArray()) {
            // if there is a variable-mapping, log a warning
            if (!javaXmlTypeMapping.getVariableMapping().isEmpty()) {
                log.warn("Ignoring variable-mapping defined for class " + javaClass + " which is an array.");
            }
            return;
        }

        // Index Java bean properties by name
        Map<String,Class> properties = new HashMap<String,Class>();
        try {
            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(javaClass).getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                properties.put(propertyDescriptor.getName(), propertyDescriptor.getPropertyType());
            }
        } catch (IntrospectionException e) {
            throw new OpenEJBException("Class " + javaClass + " is not a valid javabean", e);
        }

        for (VariableMapping variableMapping : javaXmlTypeMapping.getVariableMapping()) {
            String fieldName = variableMapping.getJavaVariableName();

            if (variableMapping.getXmlAttributeName() != null) {
                JaxRpcFieldInfo fieldInfo = new JaxRpcFieldInfo();
                fieldInfo.name = fieldName;

                // verify that the property exists on the java class
                Class javaType = properties.get(fieldName);
                if (javaType == null) {
                    throw new OpenEJBException("field name " + fieldName + " not found in " + properties);
                }

                String attributeLocalName = variableMapping.getXmlAttributeName();
                QName xmlName = new QName("", attributeLocalName);
                fieldInfo.xmlName = xmlName;

                fieldInfo.xmlType = xmlTypeInfo.attributes.get(attributeLocalName);
                if (fieldInfo.xmlType == null) {
                    throw new OpenEJBException("attribute " + xmlName + " not found in schema " + xmlTypeInfo.qname);
                }

                typeInfo.fields.add(fieldInfo);
            } else {
                JaxRpcFieldInfo fieldInfo = new JaxRpcFieldInfo();
                fieldInfo.isElement = true;
                fieldInfo.name = fieldName;

                // verify that the property exists on the java class or there is a public field
                Class javaType = properties.get(fieldName);
                if (javaType == null) {
                    //see if it is a public field
                    try {
                        Field field = javaClass.getField(fieldName);
                        javaType = field.getType();
                    } catch (NoSuchFieldException e) {
                        throw new OpenEJBException("field name " + fieldName + " not found in " + properties, e);
                    }
                }


                QName xmlName = new QName("", variableMapping.getXmlElementName());
                XmlElementInfo nestedElement = xmlTypeInfo.elements.get(xmlName);
                if (nestedElement == null) {
                    String ns = xmlTypeInfo.qname.getNamespaceURI();
                    xmlName = new QName(ns, variableMapping.getXmlElementName());
                    nestedElement = xmlTypeInfo.elements.get(xmlName);
                    if (nestedElement == null) {
                        throw new OpenEJBException("element " + xmlName + " not found in schema " + xmlTypeInfo.qname);
                    }
                }
                fieldInfo.isNillable = nestedElement.nillable || hasEncoded;
                fieldInfo.xmlName = xmlName;

                // xml type
                if (nestedElement.xmlType != null) {
                    fieldInfo.xmlType = nestedElement.xmlType;
                } else {
                    QName anonymousName;
                    if (xmlTypeInfo.anonymous) {
                        anonymousName = new QName(xmlTypeInfo.qname.getNamespaceURI(), xmlTypeInfo.qname.getLocalPart() +
                                ">" + nestedElement.qname.getLocalPart());
                    } else {
                        anonymousName = new QName(xmlTypeInfo.qname.getNamespaceURI(),
                                ">" + xmlTypeInfo.qname.getLocalPart() + ">" + nestedElement.qname.getLocalPart());
                    }
                    fieldInfo.xmlType = anonymousName;
                }

                if (javaType.isArray()) {
                    fieldInfo.minOccurs = nestedElement.minOccurs;
                    fieldInfo.maxOccurs = nestedElement.maxOccurs;
                }

                typeInfo.fields.add(fieldInfo);
            }
        }
    }

    /**
     * All of the known built in XML Schemas used by webservices.  This is used to supress unknown type exceptions
     */
    private static final Set<String> WebserviceNameSpaces = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList(
            "http://schemas.xmlsoap.org/soap/encoding/", // SOAP 1.1
            "http://www.w3.org/2003/05/soap-encoding",   // SOAP 1.2
            "http://xml.apache.org/xml-soap",            // Apache XMLSOAP
            "http://www.w3.org/1999/XMLSchema",          // XSD 1999
            "http://www.w3.org/2000/10/XMLSchema",       // XSD 2000
            "http://www.w3.org/2001/XMLSchema",          // XSD 2001
            "http://www.w3.org/XML/1998/namespace"       // XML (for xml-any)
    )));
}
