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

import org.apache.openejb.core.cmp.CmpUtil;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.test.entity.SingleValuedCmr;

import javax.ejb.EJBLocalObject;
import javax.ejb.EntityBean;
import javax.ejb.EJBException;
import java.lang.reflect.Field;

public class SingleValuedCmrImpl<I extends EntityBean, P extends EJBLocalObject> implements SingleValuedCmr<I, P> {
    private final EntityBean source;
//    private final CoreDeploymentInfo sourceInfo;
    private final Field relatedField;
    private final CoreDeploymentInfo relatedInfo;

    public SingleValuedCmrImpl(EntityBean source, Class<I> relatedType, String property) {
        if (source == null) throw new NullPointerException("source is null");
        if (relatedType == null) throw new NullPointerException("relatedType is null");
        if (property == null) throw new NullPointerException("property is null");

        this.source = source;
        try {
            relatedField = relatedType.getField(property);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Related type " + relatedType.getName() + " does not contain a property " + property);
        }
        if (!relatedField.getType().isAssignableFrom(source.getClass())) {
            throw new IllegalArgumentException("Related type is " + relatedType.getName() + " but field " +
                    property + " type is " + relatedField.getType().getName());
        }

        try {
            Field deploymentInfoField = relatedType.getField("deploymentInfo");
            relatedInfo = (CoreDeploymentInfo) deploymentInfoField.get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException("EntityBean class " + relatedType.getName() +
                    " does not contain a deploymentInfo field.  Is this a generated CMP 2 entity implementation?");
        }
//
//        try {
//            Field deploymentInfoField = source.getClass().getField("deploymentInfo");
//            sourceInfo = (CoreDeploymentInfo) deploymentInfoField.get(null);
//        } catch (Exception e) {
//            throw new IllegalArgumentException("EntityBean class " + source.getClass().getName() +
//                    " does not contain a deploymentInfo field.  Is this a generated CMP 2 entity implementation?");
//        }
    }

    public P getEjbProxy(I entity) throws EJBException {
        if (entity == null) return null;

        P ejbProxy = (P) CmpUtil.getEjbProxy(relatedInfo, entity);
        return ejbProxy;
    }

    public I updateEntityBean(EJBLocalObject proxy) throws EJBException {
        if (proxy == null) return null;

        I bean = (I) CmpUtil.getEntityBean(relatedInfo, proxy);
        try {
            relatedField.set(bean, source);
        } catch (IllegalAccessException e) {
            throw new EJBException("Error setting " + relatedField.getName() + " on bean " + proxy.getPrimaryKey());
        }
        return bean;
    }
}
