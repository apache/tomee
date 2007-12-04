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
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;

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
    private final Map<SchemaTypeKey, SchemaType> schemaTypeKeyToSchemaTypeMap;
    private final ClassLoader classLoader;

    public LightweightTypeInfoBuilder(JavaWsdlMapping mapping, Map<SchemaTypeKey, SchemaType> schemaTypeKeyToSchemaTypeMap, ClassLoader classLoader) {
        this.mapping = mapping;
        this.classLoader = classLoader;
        this.schemaTypeKeyToSchemaTypeMap = schemaTypeKeyToSchemaTypeMap;
    }

    public List<JaxRpcTypeInfo> buildTypeInfo() throws OpenEJBException {
        List<JaxRpcTypeInfo> typeInfoList = new ArrayList<JaxRpcTypeInfo>();

        for (Map.Entry<SchemaTypeKey, SchemaType> entry : schemaTypeKeyToSchemaTypeMap.entrySet()) {
            SchemaTypeKey key = entry.getKey();
            SchemaType schemaType = entry.getValue();

            if (!key.isElement() && !key.isAnonymous()) {
                QName typeQName = key.getQName();
                Class clazz = loadClass(typeQName, mapping);

                JaxRpcTypeInfo.SerializerType serializerType = JaxRpcTypeInfo.SerializerType.OTHER;
                if (clazz.isArray()) {
                    serializerType = JaxRpcTypeInfo.SerializerType.ARRAY;
                }

                JaxRpcTypeInfo typeInfo = new JaxRpcTypeInfo();
                typeInfo.qname = typeQName;
                typeInfo.javaType = clazz.getName();
                typeInfo.serializerType = serializerType;
                typeInfo.xmlType = typeQName;
                typeInfo.canSearchParents = schemaType.getDerivationType() == SchemaType.DT_RESTRICTION;

                mapFields(clazz, schemaType, typeInfo);

                typeInfoList.add(typeInfo);
            }
        }

        return typeInfoList;
    }

    private void mapFields(Class javaClass, SchemaType schemaType, JaxRpcTypeInfo typeInfo) throws OpenEJBException {
        // Map type QName to declaration (including nested types)
        Map<QName, SchemaParticle> paramNameToType = new HashMap<QName, SchemaParticle>();
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
            } else  {
                throw new OpenEJBException("Only all, choice and sequence particle types are supported." + " SchemaType name =" + schemaType.getName());
            }
        }

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
        for (Map.Entry<QName, SchemaParticle> entry : paramNameToType.entrySet()) {
            QName fieldQName = entry.getKey();
            SchemaParticle particle = entry.getValue();

            String fieldName = fieldQName.getLocalPart();
            Class javaType = propertyToClass.get(fieldName);
            if (javaType == null) {
                throw new OpenEJBException("Field " + fieldName + " is not defined by class " + javaClass.getName());
            }

            JaxRpcFieldInfo fieldInfo = new JaxRpcFieldInfo();
            fieldInfo.name = fieldName;
            fieldInfo.isNillable = particle.isNillable();
            fieldInfo.xmlName = fieldQName;
            fieldInfo.xmlType = particle.getType().getName();

            if (javaType.isArray()) {
                fieldInfo.minOccurs = particle.getIntMinOccurs();
                fieldInfo.maxOccurs = particle.getIntMaxOccurs();
                fieldInfo.maxOccursUnbounded = particle.getIntMaxOccurs() > 1;
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
