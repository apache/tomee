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
import java.lang.reflect.Field;
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
    private final EntityBean source;
//    private final CoreDeploymentInfo sourceInfo;
    private final Field relatedField;
    private final CoreDeploymentInfo relatedInfo;
    private Map<PK, Bean> beans;
    private final Class<Bean> relatedType;

    public CmrSet(EntityBean source, Field relatedField, CoreDeploymentInfo relatedInfo, Map<PK, Bean> beans) {
        this.source = source;
        this.relatedField = relatedField;
        this.relatedInfo = relatedInfo;
        this.beans = beans;
        relatedType = (Class<Bean>) relatedField.getDeclaringClass();
    }
    // todo override equals and hash code
//    void invalidate() {
//        context = null;
//        slot = -1;
//        relatedContainer = null;
//        keys = null;
//        relatedLocalInterface = null;
//    }

    public int size() {
        return beans.size();
    }

    public boolean contains(Object o) {
        if (relatedType.isInstance(o)) {
            PK primaryKey = getPrimaryKey(o);
            return beans.containsKey(primaryKey);
        }
        return false;
    }

    public void clear() {
        beans.clear();
    }

    public boolean add(Proxy proxy) {
        PK primaryKey = getPrimaryKey(proxy);
        Bean entity = getEntityBean(proxy);
        boolean changed = beans.put(primaryKey, entity) != null;
        if (changed) {
            // todo update related
        }
        return changed;
    }

    public boolean remove(Object o) {
        Object primaryKey = getPrimaryKey(o);
        boolean changed = beans.remove(primaryKey) != null;
        if (changed) {
            // todo update related
        }
        return changed;
    }

    public boolean retainAll(Collection<?> c) {
        Collection<PK> inputKeys = getPrimaryKeys(c);

        boolean changed = false;
        for (Iterator<PK> iterator = beans.keySet().iterator(); iterator.hasNext();) {
            PK primaryKey = iterator.next();
            if (!inputKeys.contains(primaryKey)) {
                iterator.remove();
                // todo update related
            }
        }
        return changed;
    }

    public Iterator<Proxy> iterator() {
        return new Iterator<Proxy>() {
            // todo we should drop the reference to the iterator when the set is invalidated
            private Iterator<Bean> iterator = beans.values().iterator();

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public Proxy next() {
                Bean entity = iterator.next();
                return getEjbProxy(entity);
            }

            public void remove() {
                iterator.remove();
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

//    private I updateEntityBean(EJBLocalObject proxy) throws EJBException {
//        if (proxy == null) return null;
//
//        I bean = getEntityBean(proxy);
//        try {
//            relatedField.set(bean, source);
//        } catch (IllegalAccessException e) {
//            throw new EJBException("Error setting " + relatedField.getName() + " on bean " + proxy.getPrimaryKey());
//        }
//        return bean;
//    }

    private Bean getEntityBean(EJBLocalObject proxy) {
        if (proxy == null) return null;

        Bean bean = (Bean) CmpUtil.getEntityBean(relatedInfo, proxy);
        return bean;
    }

    private PK getPrimaryKey(Object o) {
        Proxy proxy = (Proxy) o;
        return (PK) proxy.getPrimaryKey();
    }
}
