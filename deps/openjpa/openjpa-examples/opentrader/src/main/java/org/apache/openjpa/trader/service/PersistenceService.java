/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.trader.service;

import java.io.Serializable;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContextType;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;

/**
 * An abstract utility for JPA based service. 
 * This thin wrapper over a {@link EntityManagerFactory Persistence Unit} maintains
 * <LI>per-thread persistence context
 * <LI>relinquishes direct transaction control under a managed environment 
 * 
 * @see #getEntityManager()
 * @see #newEntityManager()
 * 
 * @author Pinaki Poddar
 * 
 */
@SuppressWarnings("serial")
public abstract class PersistenceService extends Observable implements Serializable {
    private final OpenJPAEntityManagerFactory emf;
    private final String unitName;
    private final boolean isManaged;
    private final PersistenceContextType scope;
    
    private ThreadLocal<EntityManager> thread = new ThreadLocal<EntityManager>();
    private ReentrantLock lock = new ReentrantLock();

    protected PersistenceService(String unit) {
        this(unit, false, PersistenceContextType.EXTENDED, null);
    }
    
    protected PersistenceService(String unit, boolean managed, PersistenceContextType scope,
            Map<String,Object> config) {
        this.emf = OpenJPAPersistence.cast(Persistence.createEntityManagerFactory(unit, config));
        this.unitName  = unit;
        this.isManaged = managed;
        this.scope     = scope;
    }
    
    public final OpenJPAEntityManagerFactory getUnit() {
        return emf;
    }

    public final String getUnitName() {
        return unitName;
    }
    
    public final boolean isManaged() {
        return isManaged;
    }
    
    public final PersistenceContextType getContextType() {
        return scope;
    }

    /**
     * Gets an entity manager associated with the current thread. If the
     * current thread is not associated with any entity manager or the
     * associated entity manager has been closed, creates a new entity manager
     * and associates with the current thread.
     * 
     * @return an entity manager associated with the current thread.
     */
    protected EntityManager getEntityManager() {
        try {
            lock.lock();
            EntityManager em = thread.get();
            if (em == null || !em.isOpen()) {
                em = emf.createEntityManager();
                thread.set(em);
            }
            return em;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Creates a new entity manager. The entity manager is not associated with
     * the current thread.
     */
    protected EntityManager newEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Begins a transaction on the current thread. If the thread is associated
     * with a persistence context, then a transaction is started if necessary.
     * If the thread is not associated with a persistence context, then a new
     * context is created, associated with the thread, new transaction is
     * started.
     * 
     * @see #getEntityManager()
     */
    protected EntityManager begin() {
        try {
            lock.lock();
            EntityManager em = getEntityManager();
            if (isManaged) {
                em.joinTransaction();
            } else {
                if (!em.getTransaction().isActive()) {
                    em.getTransaction().begin();
                }
            }
            return em;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Commits a transaction on the current thread.
     */
    protected void commit() {
        try {
            lock.lock();
            EntityManager em = getEntityManager();
            if (isManaged) {
                em.flush();
            } else {
                assertActive();
                em.getTransaction().commit();
            }
            if (scope == PersistenceContextType.TRANSACTION) {
                em.clear();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Rolls back a transaction on the current thread.
     */
    protected void rollback() {
        try {
            lock.lock();
            EntityManager em = getEntityManager();
            if (isManaged) {
                
            } else {
                em.getTransaction().rollback();
            }
            if (scope == PersistenceContextType.TRANSACTION) {
                em.clear();
            }
        } finally {
            lock.unlock();
        }
    }
    
    public void close() {
        try {
            EntityManager em = thread.get();
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                em.close();
            }
            thread.set(null);
            emf.close();
        } finally {
            
        }
        
    }

    /**
     * Assert current thread is associated with an active transaction.
     */
    protected void assertActive() {
        EntityManager em = thread.get();
        String thread = Thread.currentThread().getName();
        assertTrue("No persistent context is associated with " + thread, em != null);
        assertTrue("Persistent context " + em + " associated with " + thread + " has been closed", em.isOpen());
        if (!isManaged) {
            assertTrue("Persistent context " + em + " associated with " + thread + " has no active transaction", 
                    em.getTransaction().isActive());
        }
    }

    protected void assertTrue(String s, boolean p) {
        if (!p) {
            System.err.println(s);
            throw new RuntimeException(s);
        }
    }
}
