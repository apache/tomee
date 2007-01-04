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

import org.apache.openejb.core.CoreDeploymentInfo;

import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.ejb.EntityBean;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Collections;

public class CmrSet<Bean extends EntityBean, Proxy extends EJBLocalObject> extends AbstractSet<Proxy> {
    private final EntityBean source;
    private final String sourceProperty;
    private final CoreDeploymentInfo relatedInfo;
    private final String relatedProperty;
    private final CollectionRef<Bean> relatedBeanRef;
    private Class relatedLocal;

    public CmrSet(EntityBean source, String sourceProperty, CoreDeploymentInfo relatedInfo, String relatedProperty, CollectionRef<Bean> relatedBeanRef) {
        this.source = source;
        this.sourceProperty = sourceProperty;
        this.relatedInfo = relatedInfo;
        this.relatedProperty = relatedProperty;
        this.relatedBeanRef = relatedBeanRef;

        relatedLocal = relatedInfo.getLocalInterface();
    }

    public boolean isEmpty() {
        return getRelatedBeans(false).isEmpty();
    }

    public int size() {
        return getRelatedBeans(false).size();
    }

    public boolean contains(Object o) {
        if (relatedLocal.isInstance(o)) {
            Bean entity = getEntityBean((EJBLocalObject) o);
            return entity != null && getRelatedBeans(false).contains(entity);
        }
        return false;
    }

    public boolean addAll(Collection<? extends Proxy> c) {
        Set<Bean> entityBeans = getEntityBeans(c, relatedLocal);
        boolean changed = false;
        for (Iterator<Bean> iterator = entityBeans.iterator(); iterator.hasNext();) {
            Bean bean = iterator.next();
            changed = add(bean) || changed;
        }
        return changed;
    }

    public boolean add(Proxy proxy) {
        if (!relatedLocal.isInstance(proxy)) {
            throw new IllegalArgumentException("Object is not an instance of " + relatedLocal.getName() +
                                ": " + (proxy == null ? "null" : proxy.getClass().getName()));

        }
        Bean newEntity = getEntityBean(proxy);
        if (newEntity == null) {
            throw new IllegalArgumentException("Ejb has been deleted");
        }

        return add(newEntity);
    }

    private boolean add(Bean newEntity) {
        boolean changed = getRelatedBeans(true).add(newEntity);
        if (changed) {
            // set the back reference in the new related bean
            Object oldBackRef = toCmp2Entity(newEntity).OpenEJB_addCmr(relatedProperty, source);

            // if the new related beas was related to another bean, we need
            // to clear the back reference in that old bean
            if (oldBackRef != null) {
                toCmp2Entity(oldBackRef).OpenEJB_removeCmr(sourceProperty, newEntity);
            }
        }
        return changed;
    }

    public boolean remove(Object o) {
        if (!relatedLocal.isInstance(o)) {
            return false;
        }

        Bean entity = getEntityBean((EJBLocalObject) o);
        boolean changed = entity != null && getRelatedBeans(false).remove(entity);
        if (changed) {
            toCmp2Entity(entity).OpenEJB_removeCmr(relatedProperty, source);
        }
        return changed;
    }

    public boolean retainAll(Collection<?> c) {
        Set entityBeans = getEntityBeans(c, null);

        boolean changed = false;
        for (Iterator<Bean> iterator = getRelatedBeans(false).iterator(); iterator.hasNext();) {
            Bean entity = iterator.next();
            if (!entityBeans.contains(entity)) {
                iterator.remove();
                toCmp2Entity(entity).OpenEJB_removeCmr(relatedProperty, source);
                changed = true;
            }
        }
        return changed;
    }

    public Iterator<Proxy> iterator() {
        return new Iterator<Proxy>() {
            private Bean currentEntity;
            private Iterator<Bean> iterator = getRelatedBeans(true).iterator();

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public Proxy next() {
                currentEntity = iterator.next();
                return getEjbProxy(currentEntity);
            }

            public void remove() {
                iterator.remove();
                toCmp2Entity(currentEntity).OpenEJB_removeCmr(relatedProperty, source);
            }
        };
    }

    private Proxy getEjbProxy(Bean entity) throws EJBException {
        if (entity == null) return null;

        Proxy ejbProxy = Cmp2Util.<Proxy>getEjbProxy(relatedInfo, entity);
        return ejbProxy;
    }

    private Bean getEntityBean(EJBLocalObject proxy) {
        if (proxy == null) return null;

        Bean bean = Cmp2Util.<Bean>getEntityBean(proxy);
        return bean;
    }

    private static <Bean extends EntityBean> Set<Bean> getEntityBeans(Collection<?> proxies, Class type) {
        if (proxies == null) return null;

        Set<Bean> entities = new HashSet<Bean>();
        for (Object value : proxies) {
            if (type != null && !type.isInstance(value)) {
                throw new IllegalArgumentException("Object is not an instance of " + type.getName() +
                                    ": " + (value == null ? "null" : value.getClass().getName()));
            }
            Bean entity = Cmp2Util.<Bean>getEntityBean((EJBLocalObject) value);
            if (entity == null) {
                throw new IllegalArgumentException("Entity has been deleted");
            }
            entities.add(entity);
        }
        return entities;
    }

    private Cmp2Entity toCmp2Entity(Object object) {
        return (Cmp2Entity) object;
    }

    private Collection<Bean> getRelatedBeans(boolean mustExist) {
        Collection<Bean> relatedBeans = relatedBeanRef.get();
        if (relatedBeans == null) {
            if (mustExist) {
                throw new IllegalStateException("Entity has been deleted therefore this cmr collection can no longer be modified");
            }
            relatedBeans = Collections.emptySet();
        }
        return relatedBeans;
    }
}
