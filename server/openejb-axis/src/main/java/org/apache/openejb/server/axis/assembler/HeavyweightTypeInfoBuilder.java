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
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.soap.SOAPArrayType;
import org.apache.xmlbeans.soap.SchemaWSDLArrayType;

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
    private final Map<SchemaTypeKey, SchemaType> schemaTypeKeyToSchemaTypeMap;
    private final Set wrapperElementQNames;
    private final Collection<JaxRpcOperationInfo> operations;
    private final boolean hasEncoded;

    public HeavyweightTypeInfoBuilder(JavaWsdlMapping mapping, Map<SchemaTypeKey, SchemaType> schemaTypeKeyToSchemaTypeMap, ClassLoader classLoader, Set wrapperElementQNames, Collection<JaxRpcOperationInfo> operations, boolean hasEncoded) {
        this.mapping = mapping;
        this.classLoader = classLoader;
        this.schemaTypeKeyToSchemaTypeMap = schemaTypeKeyToSchemaTypeMap;
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
            SchemaTypeKey key;
            if (javaXmlTypeMapping.getRootTypeQname() != null) {
                QName typeQName = javaXmlTypeMapping.getRootTypeQname();

                // Skip the wrapper elements.
                if (wrapperElementQNames.contains(typeQName)) {
                    continue;
                }

                key = new SchemaTypeKey(typeQName, javaXmlTypeMapping.isElement(), javaXmlTypeMapping.isSimpleType(), false, null);
            } else if (javaXmlTypeMapping != null) {
                String anonTypeQNameString = javaXmlTypeMapping.getAnonymousTypeQname();

                // this appears to be ignored...
                int pos = anonTypeQNameString.lastIndexOf(":");
                if (pos == -1) {
                    throw new OpenEJBException("anon QName is invalid, no final ':' " + anonTypeQNameString);
                }
                QName typeQName = new QName(anonTypeQNameString.substring(0, pos), anonTypeQNameString.substring(pos + 1));

                // Skip the wrapper elements.
                // todo why is this +2
                if (wrapperElementQNames.contains(new QName(anonTypeQNameString.substring(0, pos), anonTypeQNameString.substring(pos + 2)))) {
                    continue;
                }

                key = new SchemaTypeKey(typeQName, javaXmlTypeMapping.isElement(), javaXmlTypeMapping.isSimpleType(), true, null);
            } else {
                throw new OpenEJBException("either root type qname or anonymous type qname must be set");
            }

            SchemaType schemaType = schemaTypeKeyToSchemaTypeMap.get(key);
            if (schemaType == null) {
                // if this is a built in type then assume this is a redundant mapping
                if (WebserviceNameSpaces.contains(key.getQName().getNamespaceURI())) {
                    continue;
                }
                log.warn("Schema type key " + key + " not found in analyzed schema: " + schemaTypeKeyToSchemaTypeMap);
                continue;
            }
            mappedTypeQNames.add(key.getQName());

            Class clazz;
            try {
                clazz = Class.forName(javaXmlTypeMapping.getJavaType(), false, classLoader);
            } catch (ClassNotFoundException e) {
                throw new OpenEJBException("Could not load java type " + javaXmlTypeMapping.getJavaType(), e);
            }

            JaxRpcTypeInfo typeInfo = createTypeInfo(schemaType, clazz);

            typeInfo.qname = key.getElementQName();
            if (typeInfo.qname == null) {
                typeInfo.qname = key.getQName();
            }

            mapFields(clazz, key, schemaType, javaXmlTypeMapping, typeInfo);

            typeInfos.add(typeInfo);
        }

        Map<QName, SchemaTypeKey> qnameToKey = new HashMap<QName, SchemaTypeKey>();
        for (SchemaTypeKey key : schemaTypeKeyToSchemaTypeMap.keySet()) {
            qnameToKey.put(key.getQName(), key);
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

                SchemaTypeKey key = qnameToKey.get(xmlType);
                if (key == null) {
                    log.warn("Type QName [" + xmlType + "] defined by operation [" + operationInfo + "] has not been found in schema: " + schemaTypeKeyToSchemaTypeMap);
                    continue;
                }
                SchemaType schemaType = schemaTypeKeyToSchemaTypeMap.get(key);
                mappedTypeQNames.add(key.getQName());

                Class<?> javaType;
                try {
                    javaType = classLoader.loadClass(parameterInfo.javaType);
                } catch (ClassNotFoundException e) {
                    throw new OpenEJBException("Could not load paramter");
                }

                if (!schemaType.isSimpleType() && !javaType.isArray()) {
                    if (!mappedTypeQNames.contains(schemaType.getName())) {
                        // TODO: this lookup is not enough: the jaxrpc mapping file may define an anonymous mapping
                        log.warn("Operation " + operationInfo.name + "] uses XML type [" + schemaType + "], whose mapping is not declared by the jaxrpc mapping file.\n Continuing deployment; " + "yet, the deployment is not-portable.");
                    }
                    continue;
                }

                JaxRpcTypeInfo typeInfo = createTypeInfo(schemaType, javaType);
                typeInfo.qname = key.getElementQName();
                if (typeInfo.qname == null) {
                    typeInfo.qname = key.getQName();
                }

                typeInfos.add(typeInfo);
            }
        }

        return typeInfos;
    }

    /**
     * Creates a JaxRpcTypeInfo based on the information contained in the XML Schema Type and Java Class.
     * @param schemaType the xml schema for the type
     * @param clazz the java class for the type
     * @return the JaxRpcTypeInfo object
     * @throws OpenEJBException if the schema is invalid
     */
    private JaxRpcTypeInfo createTypeInfo(SchemaType schemaType, Class clazz) throws OpenEJBException {
        SerializerType serializerType;
        QName xmlType = null;
        if (schemaType.isSimpleType()) {
            if (schemaType.getSimpleVariety() == SchemaType.ATOMIC) {
                if (clazz.isArray()) {
                    serializerType = SerializerType.ARRAY;
                } else if (schemaType.getEnumerationValues() != null) {
                    serializerType = SerializerType.ENUM;
                } else {
                    serializerType = SerializerType.OTHER;
                    xmlType = schemaType.getPrimitiveType().getName();
                }
            } else if (schemaType.getSimpleVariety() == SchemaType.LIST) {
                serializerType = SerializerType.LIST;
            } else {
                throw new OpenEJBException("Schema type [" + schemaType + "] is invalid.");
            }
        } else {
            if (clazz.isArray()) {
                serializerType = SerializerType.ARRAY;
            } else {
                serializerType = SerializerType.OTHER;
                if (SchemaType.SIMPLE_CONTENT == schemaType.getContentType()) {
                    xmlType = schemaType.getBaseType().getName();
                } else if (SchemaType.EMPTY_CONTENT == schemaType.getContentType() ||
                        SchemaType.ELEMENT_CONTENT == schemaType.getContentType() ||
                        SchemaType.MIXED_CONTENT == schemaType.getContentType()) {
                    xmlType = schemaType.getName();
                } else {
                    throw new OpenEJBException("Schema type [" + schemaType + "] is invalid.");
                }
            }
        }

        JaxRpcTypeInfo typeInfo = new JaxRpcTypeInfo();
        typeInfo.javaType = clazz.getName();
        typeInfo.serializerType = serializerType;
        typeInfo.xmlType = xmlType;
        typeInfo.canSearchParents = schemaType.getDerivationType() == SchemaType.DT_RESTRICTION;

        // If we understand the axis comments correctly, componentQName is never set for j2ee ws.
        if (serializerType == SerializerType.ARRAY) {
            typeInfo.componentType = getArrayComponentType(schemaType);
        }

        return typeInfo;
    }

    /**
     * Extract the nested component type of an Array from the XML Schema Type.
     * @param schemaType the XML Schema Type to inspect
     * @return the QName of the nested component type or null if the schema type can not be determined
     * @throws OpenEJBException if the XML Schema Type can not represent an Array
     */
    private QName getArrayComponentType(SchemaType schemaType) throws OpenEJBException {
        //First, handle case that looks like this:
        // <complexType name="ArrayOfstring">
        //     <complexContent>
        //         <restriction base="soapenc:Array">
        //             <attribute ref="soapenc:arrayType" wsdl:arrayType="xsd:string[]"/>
        //         </restriction>
        //     </complexContent>
        // </complexType>
        SchemaLocalAttribute arrayTypeAttribute =  schemaType.getAttributeModel().getAttribute(new QName(SOAP_ENCODING_NS, "arrayType"));
        if (arrayTypeAttribute != null) {
            SchemaWSDLArrayType wsdlArrayType = (SchemaWSDLArrayType) arrayTypeAttribute;
            SOAPArrayType soapArrayType = wsdlArrayType.getWSDLArrayType();
            if (soapArrayType != null) {
                QName componentType = soapArrayType.getQName();
                log.debug("Extracted componentType " + componentType + " from schemaType " + schemaType);
                return componentType;
            } else {
                log.info("No SOAPArrayType for component from schemaType " + schemaType);
            }
        } else {
            log.warn("No soap array info for schematype: " + schemaType);
        }

        // If that didn't work, try to handle case like this:
        // <complexType name="ArrayOfstring1">
        //     <complexContent>
        //         <restriction base="soapenc:Array">
        //             <sequence>
        //                 <element name="string1" type="xsd:string" minOccurs="0" maxOccurs="unbounded"/>
        //             </sequence>
        //         </restriction>
        //     </complexContent>
        // </complexType>
        if (schemaType.getBaseType().getName().equals(new QName(SOAP_ENCODING_NS, "Array"))) {
            SchemaProperty[] properties = schemaType.getDerivedProperties();
            if (properties.length != 1) {
                throw new OpenEJBException("more than one element inside array definition: " + schemaType);
            }
            QName componentType = properties[0].getType().getName();
            log.debug("determined component type from element type");
            return componentType;
        }

        return null;
    }

    /**
     * Map the (nested) fields of a XML Schema Type to Java Beans properties or public fields of the specified Java Class.
     * @param javaClass the java class to map
     * @param key
     * @param schemaType
     * @param javaXmlTypeMapping the java to xml type mapping metadata
     * @param typeInfo the JaxRpcTypeInfo for this type
     * @throws OpenEJBException if the XML Schema Type can not be mapped to the Java Class
     */
    private void mapFields(Class javaClass, SchemaTypeKey key, SchemaType schemaType, JavaXmlTypeMapping javaXmlTypeMapping, JaxRpcTypeInfo typeInfo) throws OpenEJBException {
        // Skip arrays since they can't define a variable-mapping element
        if (!javaClass.isArray()) {
            // if there is a variable-mapping, log a warning
            if (!javaXmlTypeMapping.getVariableMapping().isEmpty()) {
                log.warn("Ignoring variable-mapping defined for class " + javaClass + " which is an array.");
            }
            return;
        }

        // Index particles by name
        Map<QName,SchemaParticle> paramNameToType = new HashMap<QName,SchemaParticle>();
        if (schemaType.getContentModel() != null) {
            int particleType = schemaType.getContentModel().getParticleType();
            if (SchemaParticle.ELEMENT == particleType) {
                SchemaParticle parameter = schemaType.getContentModel();
                paramNameToType.put(parameter.getName(), parameter);
            } else if (particleType == SchemaParticle.SEQUENCE || particleType == SchemaParticle.ALL) {
                SchemaParticle[] properties = schemaType.getContentModel().getParticleChildren();
                for (SchemaParticle parameter : properties) {
                    paramNameToType.put(parameter.getName(), parameter);
                }
            } else {
                throw new OpenEJBException("Only element, sequence, and all particle types are supported. SchemaType name =" + schemaType.getName());
            }
        }

        // Index attributes by name
        Map<String,SchemaLocalAttribute> attNameToType = new HashMap<String,SchemaLocalAttribute>();
        if (schemaType.getAttributeModel() != null) {
            SchemaLocalAttribute[] attributes = schemaType.getAttributeModel().getAttributes();
            for (SchemaLocalAttribute attribute : attributes) {
                Object old = attNameToType.put(attribute.getName().getLocalPart(), attribute);
                if (old != null) {
                    throw new OpenEJBException("Complain to your expert group member, spec does not support attributes with the same local name and differing namespaces: original: " + old + ", duplicate local name: " + attribute);
                }
            }
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

                SchemaLocalAttribute attribute = attNameToType.get(attributeLocalName);
                if (null == attribute) {
                    throw new OpenEJBException("attribute " + xmlName + " not found in schema " + schemaType.getName());
                }
                fieldInfo.xmlType = attribute.getType().getName();

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
                SchemaParticle particle = paramNameToType.get(xmlName);
                if (particle == null) {
                    String ns = key.getQName().getNamespaceURI();
                    xmlName = new QName(ns, variableMapping.getXmlElementName());
                    particle = paramNameToType.get(xmlName);
                    if (particle == null) {
                        throw new OpenEJBException("element " + xmlName + " not found in schema " + schemaType.getName());
                    }
                } else if (SchemaParticle.ELEMENT != particle.getParticleType()) {
                    throw new OpenEJBException(xmlName + " is not an element in schema " + schemaType.getName());
                }
                fieldInfo.isNillable = particle.isNillable() || hasEncoded;
                fieldInfo.xmlName = xmlName;

                // xml type
                if (particle.getType().getName() != null) {
                    fieldInfo.xmlType = particle.getType().getName();
                } else {
                    QName anonymousName;
                    if (key.isAnonymous()) {
                        anonymousName = new QName(key.getQName().getNamespaceURI(), key.getQName().getLocalPart() +
                                ">" + particle.getName().getLocalPart());
                    } else {
                        anonymousName = new QName(key.getQName().getNamespaceURI(),
                                ">" + key.getQName().getLocalPart() + ">" + particle.getName().getLocalPart());
                    }
                    fieldInfo.xmlType = anonymousName;
                }

                if (javaType.isArray()) {
                    fieldInfo.minOccurs = particle.getIntMinOccurs();
                    fieldInfo.maxOccurs = particle.getIntMaxOccurs();
                    //TODO axis seems to have the wrong name for this property based on how it is used
                    fieldInfo.maxOccursUnbounded = particle.getIntMaxOccurs() > 1;
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
