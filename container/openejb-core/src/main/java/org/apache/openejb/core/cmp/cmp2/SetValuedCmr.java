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
import javax.ejb.EJBException;
import javax.transaction.TransactionSynchronizationRegistry;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.loader.SystemInstance;

//
// WARNING: Do not refactor this class.  It is used by the Cmp2Generator.
//
public class SetValuedCmr<Bean extends EntityBean, Proxy extends EJBLocalObject> {
    private final EntityBean source;
    private final String sourceProperty;
    private final String relatedProperty;
    private final CoreDeploymentInfo relatedInfo;
    private final TransactionSynchronizationRegistry transactionRegistry;

    public SetValuedCmr(EntityBean source, String sourceProperty, Class<Bean> relatedType, String relatedProperty) {
        if (source == null) throw new NullPointerException("source is null");
        if (relatedType == null) throw new NullPointerException("relatedType is null");

        this.source = source;
        this.sourceProperty = sourceProperty;
        this.relatedProperty = relatedProperty;

        this.relatedInfo = Cmp2Util.getDeploymentInfo(relatedType);

        transactionRegistry = SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class);
    }

    public Set<Proxy> get(Set<Bean> others) {
        if (sourceProperty == null) {
            throw new EJBException("Internal error: this container managed relationship is unidirectional and, " +
                    "this entity does not have a cmr field for the relationship");
        }

        if (others == null) {
            throw new NullPointerException("others is null");
        }

        // This may not work if the JPA implementation creates multiple instances in the same tx
        // in that case we need to key of of the deploymentId, primary key and sourceProperty name
        CmrSet<Bean, Proxy> cmrSet = null;
        try {
            cmrSet = (CmrSet<Bean, Proxy>) transactionRegistry.getResource(this);
        } catch (IllegalStateException ignored) {
            // no tx, which is fine
        }
        
        if (cmrSet == null) {
            cmrSet = new CmrSet<Bean, Proxy>(source, sourceProperty, relatedInfo, relatedProperty, others);
            try {
                transactionRegistry.putResource(this, cmrSet);
            } catch (IllegalStateException ignored) {
                // we tried but there is no tx
            }
        }
        return cmrSet;
    }

    public void set(Set<Bean> relatedBeans, Collection newProxies) {
        if (sourceProperty == null) {
            throw new EJBException("Internal error: this container managed relationship is unidirectional and, " +
                    "this entity does not have a cmr field for the relationship");
        }

        // null can not be set into a cmr field
        // EJB 3.0 Section 8.3.8 "Collections Managed by the Container" bullet 4 
        if (newProxies == null) {
            throw new IllegalArgumentException("null can not be set into a collection-valued cmr-field");
        }

        // clear back reference in the old related beans
        if (relatedProperty != null) {
            for (Bean oldBean : relatedBeans) {
                if (oldBean != null) {
                    toCmp2Entity(oldBean).OpenEJB_removeCmr(relatedProperty, source);
                }
            }
        }
        relatedBeans.clear();

        for (Iterator iterator = new ArrayList(newProxies).iterator(); iterator.hasNext();) {
            Proxy newProxy = (Proxy) iterator.next();
            Bean newBean = Cmp2Util.<Bean>getEntityBean(newProxy);

            if (newProxy != null) {
                // set the back reference in the new related bean
                Object oldBackRef = null;
                if (relatedProperty != null) {
                    oldBackRef = toCmp2Entity(newBean).OpenEJB_addCmr(relatedProperty, source);
                }

                // add the bean to our value map
                relatedBeans.add(newBean);

                // if the new related beas was related to another bean, we need
                // to clear the back reference in that old bean
                if (relatedProperty != null && oldBackRef != null) {
                    toCmp2Entity(oldBackRef).OpenEJB_removeCmr(sourceProperty, newBean);
                }
            }
        }
    }

    public void deleted(Set<Bean> relatedBeans) {
        CmrSet<Bean, Proxy> cmrSet = (CmrSet<Bean, Proxy>) transactionRegistry.getResource(this);
        if (cmrSet != null) {
            transactionRegistry.putResource(this, null);
            cmrSet.entityDeleted();
        }

        // clear back reference in the old related beans
        if (relatedProperty != null) {
            for (Bean oldBean : relatedBeans) {
                if (oldBean != null) {
                    toCmp2Entity(oldBean).OpenEJB_removeCmr(relatedProperty, source);
                }
            }
        }
    }

    private Cmp2Entity toCmp2Entity(Object object) {
        return (Cmp2Entity) object;
    }
}
