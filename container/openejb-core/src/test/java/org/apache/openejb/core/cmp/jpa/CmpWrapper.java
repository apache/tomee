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

import javax.ejb.EJBException;
import java.lang.reflect.Method;

public class CmpWrapper {
    private final Object bean;
    private final Class type;
    private final Method addCmrMethod;
    private final Method removeCmrMethod;

    public CmpWrapper(Object bean, Method addCmrMethod, Method removeCmrMethod) {
        this.bean = bean;
        this.addCmrMethod = addCmrMethod;
        this.removeCmrMethod = removeCmrMethod;
        type = addCmrMethod.getDeclaringClass();
    }

    public Object addCmr(String property, Object pk, Object bean) {
        if (property == null) throw new NullPointerException("property is null");
        try {
            Object oldValue = addCmrMethod.invoke(this.bean, property, pk, bean);
            return oldValue;
        } catch (Exception e) {
            throw new EJBException("Error setting property " + property + " on entity bean of type " + type.getName());
        }
    }

    public Object removeCmr(String property, Object pk, Object bean) {
        if (property == null) throw new NullPointerException("property is null");
        try {
            Object oldValue = removeCmrMethod.invoke(this.bean, property, pk, bean);
            return oldValue;
        } catch (Exception e) {
            throw new EJBException("Error setting property " + property + " on entity bean of type " + type.getName());
        }
    }
}
