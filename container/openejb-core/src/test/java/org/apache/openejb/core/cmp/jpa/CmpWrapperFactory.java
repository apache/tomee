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
package org.apache.openejb.core.cmp.jpa;

import java.lang.reflect.Method;

public class CmpWrapperFactory {
    private final Method addCmrMethod;
    private final Method removeCmrMethod;

    public CmpWrapperFactory(Class relatedType) {
        try {
            addCmrMethod = relatedType.getMethod("OpenEJB_addCmr", String.class, Object.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("EntityBean class " + relatedType.getName() +
                    " does not contain the generated method OpenEJB_addCmr(String name, Object bean) method");
        }
        try {
            removeCmrMethod = relatedType.getMethod("OpenEJB_removeCmr", String.class, Object.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("EntityBean class " + relatedType.getName() +
                    " does not contain the generated method OpenEJB_removeCmr(String name, Object bean) method");
        }
    }

    public CmpWrapper createCmpEntityBean(Object bean) {
        return new CmpWrapper(bean, addCmrMethod, removeCmrMethod);
    }
}
