/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.axis.client;

import org.apache.axis.description.FieldDesc;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMapping;

public class ArrayTypeInfo extends TypeInfo {

    private final QName componentType;
    private final QName componentQName;

    public ArrayTypeInfo(Class clazz,
            QName qName,
            Class serializerClass,
            Class deserializerClass,
            boolean canSearchParents,
            FieldDesc[] fields,
            QName componentType,
            QName componentQName) {
        super(clazz, qName, serializerClass, deserializerClass, canSearchParents, fields);
        this.componentType = componentType;
        this.componentQName = componentQName;
    }

    public void register(TypeMapping typeMapping) {
//        SerializerFactory ser = BaseSerializerFactory.createFactory(getSerFactoryClass(), getClazz(), getqName());
//        ((ArraySerializerFactory)ser).setComponentType(componentType);
//        ((ArraySerializerFactory)ser).setComponentQName(componentQName);
//        DeserializerFactory deser = BaseDeserializerFactory.createFactory(getDeserFactoryClass(), getClazz(), getqName());
//        ((ArrayDeserializerFactory)deser).setComponentType(componentType);

        ArraySerializerFactory ser = new ArraySerializerFactory(componentType, componentQName);
        ArrayDeserializerFactory deser = new ArrayDeserializerFactory();

        typeMapping.register(getClazz(), getqName(), ser, deser);
    }

    public static class UpdatableArrayTypeInfo extends TypeInfo.UpdatableTypeInfo {
        private QName componentType;
        private QName componentQName;

        public TypeInfo buildTypeInfo() {
            if (null == clazz) {
                throw new IllegalStateException("clazz is null");
            } else if (null == qName) {
                throw new IllegalStateException("qName is null");
            } else if (null == serializerClass) {
                throw new IllegalStateException("serializerClass is null");
            } else if (null == deserializerClass) {
                throw new IllegalStateException("deserializerClass is null");
            } else if (null == fields) {
                throw new IllegalStateException("fields is null");
            }
            return new ArrayTypeInfo(clazz, qName, serializerClass, deserializerClass, canSearchParents, fields, componentType, componentQName);
        }


        public QName getComponentType() {
            return componentType;
        }

        public QName getComponentQName() {
            return componentQName;
        }

        public void setComponentType(QName componentType) {
            this.componentType = componentType;
        }

        public void setComponentQName(QName componentQName) {
            this.componentQName = componentQName;
        }
    }
}
