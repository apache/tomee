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

import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.cmp.CmpUtil;
import org.apache.openejb.test.entity.SingleValuedCmr;

import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.ejb.EntityBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SingleValuedCmrImpl<Bean extends EntityBean, Proxy extends EJBLocalObject> implements SingleValuedCmr<Bean, Proxy> {
    private final EntityBean source;
    private final Class<? extends EntityBean> sourceType;
    private final String sourceProperty;
    private final Class<Bean> relatedType;
    private final String relatedProperty;
    private final CoreDeploymentInfo relatedInfo;
    private final CmpWrapperFactory sourceWrapperFactory;
    private final CmpWrapperFactory relatedWrapperFactory;

    public SingleValuedCmrImpl(EntityBean source, String sourceProperty, Class<Bean> relatedType, String relatedProperty) {
        if (source == null) throw new NullPointerException("source is null");
        if (sourceProperty == null) throw new NullPointerException("sourceProperty is null");
        if (relatedType == null) throw new NullPointerException("relatedType is null");
        if (relatedProperty == null) throw new NullPointerException("relatedProperty is null");
        this.source = source;
        this.sourceProperty = sourceProperty;
        this.relatedType = relatedType;
        this.relatedProperty = relatedProperty;
        this.sourceType = source.getClass();

        try {
            Field deploymentInfoField = relatedType.getField("deploymentInfo");
            relatedInfo = (CoreDeploymentInfo) deploymentInfoField.get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException("EntityBean class " + relatedType.getName() +
                    " does not contain a deploymentInfo field.  Is this a generated CMP 2 entity implementation?");
        }

        sourceWrapperFactory = new CmpWrapperFactory(sourceType);
        relatedWrapperFactory = new CmpWrapperFactory(relatedType);
    }

    public Proxy get(Bean entity) throws EJBException {
        if (entity == null) return null;

        Proxy ejbProxy = (Proxy) CmpUtil.getEjbProxy(relatedInfo, entity);
        return ejbProxy;
    }

    public Bean set(Bean oldBean, Proxy newValue) throws EJBException {
        Bean newBean = (Bean) CmpUtil.getEntityBean(relatedInfo, newValue);

        // clear back reference in the old related bean
        if (oldBean != null) {
            getCmpWrapper(oldBean).setCmr(relatedProperty, null);
        }

        if (newValue != null) {
            // set the back reference in the new related bean
            Object oldSource = getCmpWrapper(newBean).setCmr(relatedProperty, source);

            // if the new related beas was related to another bean, we need
            // to clear the back reference in that old bean
            if (oldSource != null) {
                getCmpWrapper(oldSource).setCmr(sourceProperty, null);
            }
        }
        return newBean;
    }

    private CmpWrapper getCmpWrapper(Object object) {
        if (object == null) return null;
        if (sourceType.isInstance(object)) {
            return sourceWrapperFactory.createCmpEntityBean(object);
        } else if (relatedType.isInstance(object)) {
            return relatedWrapperFactory.createCmpEntityBean(object);
        }
        throw new IllegalArgumentException("Unknown cmp bean type " + object.getClass().getName());
    }

    public static class CmpWrapperFactory {
        private final Method getValueMethod;
        private final Method setValueMethod;

        public CmpWrapperFactory(Class relatedType) {
            try {
                setValueMethod = relatedType.getMethod("OpenEJB_setCmr", String.class, Object.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("EntityBean class " + relatedType.getName() +
                        " does not contain the generated method OpenEJB_setCmr(String name, Object bean) method");
            }
            try {
                getValueMethod = relatedType.getMethod("OpenEJB_getCmr", String.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("EntityBean class " + relatedType.getName() +
                        " does not contain the generated method OpenEJB_getCmr(String name) method");
            }
        }

        public CmpWrapper createCmpEntityBean(Object bean) {
            return new CmpWrapper(bean, getValueMethod, setValueMethod);
        }
    }


    public static class CmpWrapper {
        private final Object bean;
        private final Class type;
        private final Method getValueMethod;
        private final Method setValueMethod;

        public CmpWrapper(Object bean, Method getValueMethod, Method setValueMethod) {
            this.bean = bean;
            this.getValueMethod = getValueMethod;
            this.setValueMethod = setValueMethod;
            type = getValueMethod.getDeclaringClass();
        }

        public Object getCmr(String property) {
            if (property == null) throw new NullPointerException("property is null");
            try {
                Object value = getValueMethod.invoke(bean, property);
                return value;
            } catch (Exception e) {
                throw new EJBException("Error getting property " + property + " value from entity bean of type " + type.getName());
            }
        }

        public Object setCmr(String property, Object value) {
            if (property == null) throw new NullPointerException("property is null");
            try {
                Object oldValue = setValueMethod.invoke(bean, property, value);
                return oldValue;
            } catch (Exception e) {
                throw new EJBException("Error setting property " + property + " on entity bean of type " + type.getName());
            }
        }
    }
}
