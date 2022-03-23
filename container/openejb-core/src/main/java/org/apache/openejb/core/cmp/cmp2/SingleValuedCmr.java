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

import org.apache.openejb.BeanContext;

import jakarta.ejb.EJBException;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EntityBean;

//
// WARNING: Do not refactor this class.  It is used by the Cmp2Generator.
//
public class SingleValuedCmr<Bean extends EntityBean, Proxy extends EJBLocalObject> {
    private final EntityBean source;
    private final String sourceProperty;
    private final String relatedProperty;
    private final BeanContext relatedInfo;

    public SingleValuedCmr(final EntityBean source, final String sourceProperty, final Class<Bean> relatedType, final String relatedProperty) {
        if (source == null) {
            throw new NullPointerException("source is null");
        }
        if (relatedType == null) {
            throw new NullPointerException("relatedType is null");
        }
        this.source = source;
        this.sourceProperty = sourceProperty;
        this.relatedProperty = relatedProperty;

        this.relatedInfo = Cmp2Util.getBeanContext(relatedType);
    }

    public Proxy get(final Bean entity) throws EJBException {
        if (sourceProperty == null) {
            throw new EJBException("Internal error: this container managed relationship is unidirectional and, " +
                "this entity does not have a cmr field for the relationship");
        }

        if (entity == null) {
            return null;
        }

        final Proxy ejbProxy = Cmp2Util.<Proxy>getEjbProxy(relatedInfo, entity);
        return ejbProxy;
    }

    public Bean set(final Bean oldBean, final Proxy newValue) throws EJBException {
        final Bean newBean = Cmp2Util.<Bean>getEntityBean(newValue);
        if (newValue != null && newBean == null) {
            // todo verify that this is the only way null can be returned
            throw new IllegalArgumentException("A deleted bean can not be assigned to a relationship");
        }

        if (relatedProperty != null) {
            // clear back reference in the old related bean
            if (oldBean != null) {
                toCmp2Entity(oldBean).OpenEJB_removeCmr(relatedProperty, source);
            }

            if (newValue != null) {
                // set the back reference in the new related bean
                final Object oldBackRef = toCmp2Entity(newBean).OpenEJB_addCmr(relatedProperty, source);

                // if the new related beas was related to another bean, we need
                // to clear the back reference in that old bean
                if (oldBackRef != null) {
                    toCmp2Entity(oldBackRef).OpenEJB_removeCmr(sourceProperty, newBean);
                }
            }
        }
        return newBean;
    }

    public void deleted(final Bean oldBean) throws EJBException {
        set(oldBean, null);
    }

    private Cmp2Entity toCmp2Entity(final Object object) {
        return (Cmp2Entity) object;
    }
}
