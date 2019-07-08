/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.core.cmp.cmp2;

import org.apache.xbean.asm7.Type;

public class CmrField {
    private final String name;
    private final CmrStyle cmrStyle;
    private final Type type;
    private final Type proxyType;
    private final String relatedName;
    private final boolean synthetic;

    public CmrField(final String fieldName, final String fieldType, final String cmpImplClass, final String local, final String relatedName, final boolean synthetic) {
        this.synthetic = synthetic;
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
        type = Type.getType("L" + cmpImplClass.replace('.', '/') + ";");
        if (local != null) {
            proxyType = Type.getType("L" + local.replace('.', '/') + ";");
        } else {
            proxyType = null;
        }
        this.relatedName = relatedName;
    }

    public String getName() {
        return name;
    }

    public boolean isSynthetic() {
        return synthetic;
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
        final Type collectionType = cmrStyle.getCollectionType();
        if (collectionType == null) {
            return type.getDescriptor();
        }
        return collectionType.getDescriptor();
    }

    public String getGenericSignature() {
        final Type collectionType = cmrStyle.getCollectionType();
        if (collectionType == null) {
            return null;
        }
        return createSignature(collectionType, type);
    }

    public String getProxyDescriptor() {
        final Type collectionType = cmrStyle.getCollectionType();
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
        final Type collectionType = cmrStyle.getCollectionType();
        if (collectionType == null) {
            return null;
        }
        return createSignature(cmrStyle.getAccessorType(), type, proxyType);
    }

    private static String createSignature(final Type type, final Type... genericTypes) {
        final StringBuilder builder = new StringBuilder();
        builder.append("L").append(type.getInternalName());
        if (genericTypes.length > 0) {
            builder.append("<");
            for (final Type genericType : genericTypes) {
                builder.append(genericType.getDescriptor());
            }
            builder.append(">");
        }
        builder.append(";");
        return builder.toString();
    }
}
