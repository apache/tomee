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
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.ser.BaseDeserializerFactory;
import org.apache.axis.encoding.ser.BaseSerializerFactory;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.DeserializerFactory;
import javax.xml.rpc.encoding.SerializerFactory;
import javax.xml.rpc.encoding.TypeMapping;
import java.util.Iterator;
import java.util.List;

public class TypeInfo {
    private final Class clazz;
    private final QName qName;
    private final Class serFactoryClass;
    private final Class deserFactoryClass;
    private final boolean canSearchParents;
    private final FieldDesc[] fields;

    public static void register(List typeInfo, TypeMapping typeMapping) {
        for (Iterator iter = typeInfo.iterator(); iter.hasNext();) {
            TypeInfo info = (TypeInfo) iter.next();
            info.register(typeMapping);
        }
    }

    public TypeInfo(Class clazz, QName qName, Class serializerClass, Class deserializerClass, boolean canSearchParents, FieldDesc[] fields) {
        this.clazz = clazz;
        this.qName = qName;
        this.serFactoryClass = serializerClass;
        this.deserFactoryClass = deserializerClass;
        this.canSearchParents = canSearchParents;
        this.fields = fields;
    }

    public Class getClazz() {
        return clazz;
    }

    public QName getqName() {
        return qName;
    }

    public Class getSerFactoryClass() {
        return serFactoryClass;
    }

    public Class getDeserFactoryClass() {
        return deserFactoryClass;
    }

    public boolean isCanSearchParents() {
        return canSearchParents;
    }

    public FieldDesc[] getFields() {
        return fields;
    }

    public TypeDesc buildTypeDesc() {
        TypeDesc typeDesc = new TypeDesc(clazz, canSearchParents);
        typeDesc.setXmlType(qName);
        typeDesc.setFields(fields);
        return typeDesc;
    }

    public void register(TypeMapping typeMapping) {
        SerializerFactory ser = BaseSerializerFactory.createFactory(serFactoryClass, clazz, qName);
        DeserializerFactory deser = BaseDeserializerFactory.createFactory(deserFactoryClass, clazz, qName);

        typeMapping.register(clazz, qName, ser, deser);
    }

    public static class UpdatableTypeInfo {
        protected Class clazz;
        protected QName qName;
        protected Class serializerClass;
        protected Class deserializerClass;
        protected boolean canSearchParents;
        protected FieldDesc[] fields;

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
            return new TypeInfo(clazz, qName, serializerClass, deserializerClass, canSearchParents, fields);
        }

        public void setClazz(Class clazz) {
            this.clazz = clazz;
        }

        public void setDeserializerClass(Class deserializerClass) {
            this.deserializerClass = deserializerClass;
        }

        public void setFields(FieldDesc[] fields) {
            this.fields = fields;
        }

        public void setQName(QName name) {
            qName = name;
        }

        public void setSerializerClass(Class serializerClass) {
            this.serializerClass = serializerClass;
        }

        public void setCanSearchParents(boolean canSearchParents) {
            this.canSearchParents = canSearchParents;
        }
    }
}
