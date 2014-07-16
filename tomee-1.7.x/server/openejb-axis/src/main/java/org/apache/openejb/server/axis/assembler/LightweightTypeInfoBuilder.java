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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.PackageMapping;

import javax.xml.namespace.QName;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LightweightTypeInfoBuilder {
    private final JavaWsdlMapping mapping;
    private final XmlSchemaInfo schemaInfo;
    private final ClassLoader classLoader;

    public LightweightTypeInfoBuilder(JavaWsdlMapping mapping, XmlSchemaInfo schemaInfo, ClassLoader classLoader) {
        this.mapping = mapping;
        this.classLoader = classLoader;
        this.schemaInfo = schemaInfo;
    }

    public List<JaxRpcTypeInfo> buildTypeInfo() throws OpenEJBException {
        List<JaxRpcTypeInfo> typeInfoList = new ArrayList<JaxRpcTypeInfo>();

        for (XmlTypeInfo xmlTypeInfo : schemaInfo.types.values()) {
            // skip anonymous elements
            if (!xmlTypeInfo.anonymous) {
                QName typeQName = xmlTypeInfo.qname;
                Class clazz = loadClass(typeQName, mapping);

                JaxRpcTypeInfo.SerializerType serializerType = JaxRpcTypeInfo.SerializerType.OTHER;
                if (clazz.isArray()) {
                    serializerType = JaxRpcTypeInfo.SerializerType.ARRAY;
                }

                JaxRpcTypeInfo typeInfo = new JaxRpcTypeInfo();
                typeInfo.qname = typeQName;
                typeInfo.javaType = clazz.getName();
                typeInfo.serializerType = serializerType;
                typeInfo.simpleBaseType = xmlTypeInfo.simpleBaseType;

                mapFields(clazz, xmlTypeInfo, typeInfo);

                typeInfoList.add(typeInfo);
            }
        }

        return typeInfoList;
    }

    private void mapFields(Class javaClass, XmlTypeInfo xmlTypeInfo, JaxRpcTypeInfo typeInfo) throws OpenEJBException {
        // Map JavaBean property name to propertyType
        Map<String, Class> propertyToClass = new HashMap<String, Class>();
        try {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(javaClass).getPropertyDescriptors()) {
                propertyToClass.put(descriptor.getName(), descriptor.getPropertyType());
            }
        } catch (IntrospectionException e) {
            throw new OpenEJBException("Class " + javaClass + " is not a valid javabean", e);
        }

        // Map the elements nexted in the XML Schema Type
        for (XmlElementInfo nestedElement : xmlTypeInfo.elements.values()) {
            String fieldName = nestedElement.qname.getLocalPart();
            Class javaType = propertyToClass.get(fieldName);
            if (javaType == null) {
                throw new OpenEJBException("Field " + fieldName + " is not defined by class " + javaClass.getName());
            }

            JaxRpcFieldInfo fieldInfo = new JaxRpcFieldInfo();
            fieldInfo.name = fieldName;
            fieldInfo.isNillable = nestedElement.nillable;
            fieldInfo.xmlName = nestedElement.qname;
            fieldInfo.xmlType = nestedElement.xmlType;

            if (javaType.isArray()) {
                fieldInfo.minOccurs = nestedElement.minOccurs;
                fieldInfo.maxOccurs = nestedElement.maxOccurs;
            }

            typeInfo.fields.add(fieldInfo);
        }
    }

    private Class loadClass(QName typeQName, JavaWsdlMapping mapping) throws OpenEJBException {
        String namespace = typeQName.getNamespaceURI();

        // package name comes from the package mapping
        PackageMapping packageMapping = mapping.getPackageMappingMap().get(namespace);
        if (packageMapping == null) {
            throw new OpenEJBException("Namespace " + namespace + " was not mapped in jaxrpc mapping file");
        }
        String packageName = packageMapping.getPackageType();

        // class name is package + type local part
        String className = packageName + "." + typeQName.getLocalPart();

        try {
            Class clazz = Class.forName(className, false, classLoader);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new OpenEJBException("Could not load java type " + className, e);
        }
    }
}
