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

import javax.ejb.EJBLocalObject;
import javax.ejb.EntityBean;
import java.util.Set;

import org.apache.openejb.core.CoreDeploymentInfo;

//
// WARNING: Do not refactor this class.  It is used by the Cmp2Generator.
//
public class SetValuedCmr<Bean extends EntityBean, Proxy extends EJBLocalObject> {
    private final EntityBean source;
    private final String sourceProperty;
    private final String relatedProperty;
    private final CoreDeploymentInfo relatedInfo;
    private final CollectionRef<Bean> collectionRef = new CollectionRef<Bean>();

    public SetValuedCmr(EntityBean source, String sourceProperty, Class<Bean> relatedType, String relatedProperty) {
        if (source == null) {
            throw new NullPointerException("source is null");
        }
        if (sourceProperty == null) {
            throw new NullPointerException("sourceProperty is null");
        }
        if (relatedType == null) {
            throw new NullPointerException("relatedType is null");
        }
        if (relatedProperty == null) {
            throw new NullPointerException("relatedProperty is null");
        }
        this.source = source;
        this.sourceProperty = sourceProperty;
        this.relatedProperty = relatedProperty;

        this.relatedInfo = Cmp2Util.getDeploymentInfo(relatedType);
    }

    public Set<Proxy> get(Set<Bean> others) {
        if (others == null) {
            throw new NullPointerException("others is null");
        }
        collectionRef.set(others);
        Set<Proxy> cmrSet = new CmrSet<Bean, Proxy>(source, sourceProperty, relatedInfo, relatedProperty, collectionRef);
        return cmrSet;
    }

    public void set(Set<Bean> relatedBeans, Set<Proxy> newProxies) {
        // clear back reference in the old related beans
        for (Bean oldBean : relatedBeans) {
            if (oldBean != null) {
                toCmp2Entity(oldBean).OpenEJB_removeCmr(relatedProperty, source);
            }
        }
        relatedBeans.clear();

        for (Proxy newProxy : newProxies) {
            Bean newBean = Cmp2Util.<Bean>getEntityBean(newProxy);

            if (newProxy != null) {
                // set the back reference in the new related bean
                Object oldBackRef = toCmp2Entity(newBean).OpenEJB_addCmr(relatedProperty, source);

                // add the bean to our value map
                relatedBeans.add(newBean);

                // if the new related beas was related to another bean, we need
                // to clear the back reference in that old bean
                if (oldBackRef != null) {
                    toCmp2Entity(oldBackRef).OpenEJB_removeCmr(sourceProperty, newBean);
                }
            }
        }
    }

    public void deleted(Set<Bean> relatedBeans) {
        collectionRef.set(null);

        // clear back reference in the old related beans
        for (Bean oldBean : relatedBeans) {
            if (oldBean != null) {
                toCmp2Entity(oldBean).OpenEJB_removeCmr(relatedProperty, source);
            }
        }
    }

    private Cmp2Entity toCmp2Entity(Object object) {
        return (Cmp2Entity) object;
    }
}
