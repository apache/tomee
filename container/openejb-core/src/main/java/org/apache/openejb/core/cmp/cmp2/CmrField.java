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
package org.apache.openejb.core.cmp.cmp2;

import org.objectweb.asm.Type;

public class CmrField {
    private final String name;
    private final CmrStyle cmrStyle;
    private final Type type;
    private final Type proxyType;
    private final String relatedName;

    public CmrField(String name, CmrStyle cmrStyle, Type type, Type proxyType, String relatedName) {
        this.name = name;
        this.cmrStyle = cmrStyle;
        this.type = type;
        this.proxyType = proxyType;
        this.relatedName = relatedName;
    }

    public CmrField(String fieldName, String fieldType, String ejbClass, String local, String relatedName) {
        this.name = fieldName;
        if (fieldType == null) {
            cmrStyle = CmrStyle.SINGLE;
        } else if ("java.util.Collection".equals(fieldType)) {
            cmrStyle = CmrStyle.COLLECTION;
        } else if ("java.util.Set".equals(fieldType)) {
            cmrStyle = CmrStyle.SET;
        } else {
            throw new IllegalArgumentException("Unsupported fieldType " + fieldType);
        }
        type = Type.getType("L" + ejbClass.replace('.', '/') + "_JPA;");
        proxyType = Type.getType("L" + local.replace('.', '/') + ";");
        this.relatedName = relatedName;
    }

    public String getName() {
        return name;
    }

    public CmrStyle getCmrStyle() {
        return cmrStyle;
    }

    public Type getType() {
        return type;
    }

    public Type getProxyType() {
        return proxyType;
    }

    public Type getInitialValueType() {
        return cmrStyle.getIntiCollectionType();
    }

    public String getRelatedName() {
        return relatedName;
    }

    public String getDescriptor() {
        Type collectionType = cmrStyle.getCollectionType();
        if (collectionType == null) {
            return type.getDescriptor();
        }
        return collectionType.getDescriptor();
    }

    public String getGenericSignature() {
        Type collectionType = cmrStyle.getCollectionType();
        if (collectionType == null) {
            return null;
        }
        return createSignature(collectionType, type);
    }

    public String getProxyDescriptor() {
        Type collectionType = cmrStyle.getCollectionType();
        if (collectionType == null) {
            return proxyType.getDescriptor();
        }
        return collectionType.getDescriptor();
    }

    public String getAccessorInternalName() {
        return cmrStyle.getAccessorType().getInternalName();
    }

    public String getAccessorDescriptor() {
        return cmrStyle.getAccessorType().getDescriptor();
    }

    public String getAccessorGenericSignature() {
        Type collectionType = cmrStyle.getCollectionType();
        if (collectionType == null) {
            return null;
        }
        return createSignature(cmrStyle.getAccessorType(), type, proxyType);
    }

    private static String createSignature(Type type, Type... genericTypes) {
        StringBuilder builder = new StringBuilder();
        builder.append("L").append(type.getInternalName());
        if (genericTypes.length > 0) {
            builder.append("<");
            for (Type genericType : genericTypes) {
                builder.append(genericType.getDescriptor());
            }
            builder.append(">");
        }
        builder.append(";");
        return builder.toString();
    }
}
