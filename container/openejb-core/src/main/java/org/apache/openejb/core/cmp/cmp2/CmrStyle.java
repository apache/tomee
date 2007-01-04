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

import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Method;

import org.objectweb.asm.Type;

public enum CmrStyle {
    SINGLE(SingleValuedCmr.class, null, null),
    COLLECTION(SetValuedCmr.class, Set.class, HashSet.class),
    SET(SetValuedCmr.class, Set.class, HashSet.class);

    private final Type accessorType;
    private final Type collectionType;
    private final Type intiCollectionType;
    private final String getterDescriptor;
    private final String setterDescriptor;
    private final String deletedDescriptor;

    CmrStyle(Class accessorClass, Class collectionClass, Class initCollectionClass) {
        this.accessorType = Type.getType(accessorClass);
        if (collectionClass != null) {
            this.collectionType = Type.getType(collectionClass);
            this.intiCollectionType = Type.getType(initCollectionClass);
        } else {
            this.collectionType = null;
            this.intiCollectionType = null;
        }

        String getterDescriptor = null;
        String setterDescriptor = null;
        String deletedDescriptor = null;
        for (Method method : accessorClass.getMethods()) {
            if ("get".equals(method.getName())) {
                getterDescriptor = Type.getMethodDescriptor(method);
            }
            if ("set".equals(method.getName())) {
                setterDescriptor = Type.getMethodDescriptor(method);
            }
            if ("deleted".equals(method.getName())) {
                deletedDescriptor = Type.getMethodDescriptor(method);
            }
        }
        if (getterDescriptor == null) {
            throw new AssertionError("No get method found in cmr accessor class " + accessorClass.getName());
        }
        if (setterDescriptor == null) {
            throw new AssertionError("No set method found in cmr accessor class " + accessorClass.getName());
        }
        if (deletedDescriptor == null) {
            throw new AssertionError("No deleted method found in cmr accessor class " + accessorClass.getName());
        }
        this.getterDescriptor = getterDescriptor;
        this.setterDescriptor = setterDescriptor;
        this.deletedDescriptor = deletedDescriptor;
    }

    public String getCmrFieldDescriptor(Type relatedType) {
        String relatedDescriptor = relatedType.getDescriptor();
        if (collectionType != null) {
            return collectionType.getDescriptor() +
                    "<" + relatedDescriptor + ">";
        } else {
            return relatedDescriptor;
        }
    }

    public Type getAccessorType() {
        return accessorType;
    }

    public Type getCollectionType() {
        return collectionType;
    }

    public Type getIntiCollectionType() {
        return intiCollectionType;
    }

    public String getGetterDescriptor() {
        return getterDescriptor;
    }

    public String getSetterDescriptor() {
        return setterDescriptor;
    }

    public String getDeletedDescriptor() {
        return deletedDescriptor;
    }
}
