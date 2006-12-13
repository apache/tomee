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

import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.ejb.EntityBean;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 *
 *
 * @version $Revision: 945 $ $Date: 2003-11-18 20:04:26 -0600 (Tue, 18 Nov 2003) $
 */
public class CmrSet<Bean extends EntityBean, Proxy extends EJBLocalObject, PK> extends AbstractSet<Proxy> {
    private final CoreDeploymentInfo sourceInfo;
    private final EntityBean source;
    private final Class<? extends EntityBean> sourceType;
    private final String sourceProperty;
    private final CmpWrapperFactory sourceWrapperFactory;
    private final CoreDeploymentInfo relatedInfo;
    private final Class<Bean> relatedType;
    private final String relatedProperty;
    private final CmpWrapperFactory relatedWrapperFactory;
    private final Map<PK, Bean> relatedBeans;

    public CmrSet(CoreDeploymentInfo sourceInfo, EntityBean source, String sourceProperty, CmpWrapperFactory sourceWrapperFactory, CoreDeploymentInfo relatedInfo, Class<Bean> relatedType, String relatedProperty, CmpWrapperFactory relatedWrapperFactory, Map<PK, Bean> relatedBeans) {
        this.sourceInfo = sourceInfo;
        this.source = source;
        this.sourceProperty = sourceProperty;
        this.sourceWrapperFactory = sourceWrapperFactory;
        this.relatedInfo = relatedInfo;
        this.relatedType = relatedType;
        this.relatedProperty = relatedProperty;
        this.relatedWrapperFactory = relatedWrapperFactory;
        this.relatedBeans = relatedBeans;
        this.sourceType = source.getClass();

    }

    // todo should we support a close method?
//    void invalidate() {
//        context = null;
//        slot = -1;
//        relatedContainer = null;
//        keys = null;
//        relatedLocalInterface = null;
//    }

    public boolean isEmpty() {
        return relatedBeans.isEmpty();
    }

    public int size() {
        return relatedBeans.size();
    }

    public boolean contains(Object o) {
        if (relatedType.isInstance(o)) {
            PK primaryKey = getPrimaryKey(o);
            return relatedBeans.containsKey(primaryKey);
        }
        return false;
    }

    public boolean add(Proxy proxy) {
        Object sourcePk = getSourcePk();
        PK newPk = getPrimaryKey(proxy);
        Bean newBean = getEntityBean(proxy);
        boolean changed = relatedBeans.put(newPk, newBean) == null;
        if (changed) {
            // set the back reference in the new related bean
            Object oldBackRef = getCmpWrapper(newBean).addCmr(relatedProperty, sourcePk, source);

            // add the bean to our value map
            relatedBeans.put(newPk, newBean);

            // if the new related beas was related to another bean, we need
            // to clear the back reference in that old bean
            if (oldBackRef != null) {
                getCmpWrapper(oldBackRef).removeCmr(sourceProperty, newPk, newBean);
            }
        }
        return changed;
    }

    public boolean remove(Object o) {
        PK primaryKey = getPrimaryKey(o);
        Bean oldBean = relatedBeans.remove(primaryKey);
        if (oldBean != null) {
            Object sourcePk = getSourcePk();
            getCmpWrapper(oldBean).removeCmr(relatedProperty, sourcePk, source);
        }
        return oldBean != null;
    }

    public boolean retainAll(Collection<?> c) {
        Collection<PK> inputKeys = getPrimaryKeys(c);

        boolean changed = false;
        Object sourcePk = getSourcePk();
        for (Iterator<Map.Entry<PK, Bean>> iterator = relatedBeans.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<PK, Bean> entry = iterator.next();
            PK pk = entry.getKey();
            Bean bean = entry.getValue();
            if (!inputKeys.contains(pk)) {
                changed = true;
                iterator.remove();
                getCmpWrapper(bean).removeCmr(relatedProperty, sourcePk, source);
            }
        }
        return changed;
    }

    public Iterator<Proxy> iterator() {
        return new Iterator<Proxy>() {
            private Bean currentEntity;
            private Object sourcePk = getSourcePk();

            // todo we should drop the reference to the iterator when the set is invalidated
            private Iterator<Bean> iterator = relatedBeans.values().iterator();

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public Proxy next() {
                currentEntity = iterator.next();
                return getEjbProxy(currentEntity);
            }

            public void remove() {
                iterator.remove();
                getCmpWrapper(currentEntity).removeCmr(relatedProperty, sourcePk, source);
            }
        };
    }

    private Collection<PK> getPrimaryKeys(Collection c) {
        Collection<PK> inputKeys = new HashSet<PK>(c.size());
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            Proxy ejb = (Proxy) iterator.next();
            inputKeys.add(getPrimaryKey(ejb));
        }
        return inputKeys;
    }

    private Proxy getEjbProxy(Bean entity) throws EJBException {
        if (entity == null) return null;

        Proxy ejbProxy = (Proxy) CmpUtil.getEjbProxy(relatedInfo, entity);
        return ejbProxy;
    }

    private Bean getEntityBean(EJBLocalObject proxy) {
        if (proxy == null) return null;

        Bean bean = (Bean) CmpUtil.getEntityBean(proxy);
        return bean;
    }

    private PK getPrimaryKey(Object o) {
        Proxy proxy = (Proxy) o;
        return (PK) proxy.getPrimaryKey();
    }

    private Object getSourcePk() {
        Object sourcePk = CmpUtil.getPrimaryKey(sourceInfo, source);
        if (sourcePk == null) {
            throw new IllegalStateException("CMR " + sourceProperty + " can not be modified on entity of type " +
                    sourceInfo.getBeanClass().getName() + " because primary key has not been established yet.");
        }
        return sourcePk;
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
}
