/**
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
package org.apache.openejb.core.stateful;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import javax.transaction.Transaction;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;

import org.apache.openejb.util.Index;

public class BeanEntry implements Serializable {
    private static final long serialVersionUID = 5940667199866151048L;

    protected final Object bean;
    protected final Object primaryKey;
    protected boolean inQueue = false;
    private long timeStamp;
    protected long timeOutInterval;
    protected transient Transaction beanTransaction;
    // todo if we keyed by an entity manager factory id we would not have to make this transient and rebuild the index below
    // This would require that we crete an id and that we track it
    // alternatively, we could use ImmutableArtifact with some read/write replace magic
    private transient Map<EntityManagerFactory, EntityManager> entityManagers;
    private EntityManager[] entityManagerArray;

    protected BeanEntry(Object beanInstance, Object primKey, long timeOut) {
        bean = beanInstance;
        primaryKey = primKey;
        beanTransaction = null;
        timeStamp = System.currentTimeMillis();
        timeOutInterval = timeOut;
    }

    protected BeanEntry(BeanEntry prototype) {
        bean = prototype.bean;
        primaryKey = prototype.primaryKey;
        beanTransaction = null;
        timeStamp = prototype.timeStamp;
        timeOutInterval = prototype.timeOutInterval;
    }
    
    protected boolean isTimedOut() {
        if (timeOutInterval == 0) {
            return false;
        }
        long now = System.currentTimeMillis();
        return (now - timeStamp) > timeOutInterval;
    }

    protected void resetTimeOut() {
        if (timeOutInterval > 0) {
            timeStamp = System.currentTimeMillis();
        }
    }

    public Map<EntityManagerFactory, EntityManager> getEntityManagers(Index<EntityManagerFactory, Map> factories) {
        if (entityManagers == null && entityManagerArray != null) {
            entityManagers = new HashMap<EntityManagerFactory, EntityManager>();
            for (int i = 0; i < entityManagerArray.length; i++) {
                EntityManagerFactory entityManagerFactory = factories.getKey(i);
                EntityManager entityManager = entityManagerArray[i];
                entityManagers.put(entityManagerFactory, entityManager);
            }
        }
        return entityManagers;
    }

    public void setEntityManagers(Index<EntityManagerFactory, EntityManager> entityManagers) {
        this.entityManagers = entityManagers;
        if (entityManagers != null) {
            entityManagerArray = entityManagers.values().toArray(new EntityManager[entityManagers.size()]);
        } else {
            entityManagerArray = null;
        }
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public Object getBean() {
        return bean;
    }

}
