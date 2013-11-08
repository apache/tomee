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
package org.apache.openjpa.persistence.cache.jpa;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.openjpa.lib.jdbc.JDBCListener;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.cache.jpa.model.CacheEntity;
import org.apache.openjpa.persistence.cache.jpa.model.CacheableEntity;
import org.apache.openjpa.persistence.cache.jpa.model.NegatedCachableEntity;
import org.apache.openjpa.persistence.cache.jpa.model.NegatedUncacheableEntity;
import org.apache.openjpa.persistence.cache.jpa.model.UncacheableEntity;
import org.apache.openjpa.persistence.cache.jpa.model.UnspecifiedEntity;
import org.apache.openjpa.persistence.cache.jpa.model.XmlCacheableEntity;
import org.apache.openjpa.persistence.cache.jpa.model.XmlUncacheableEntity;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

public abstract class AbstractCacheTestCase extends AbstractPersistenceTestCase {
    protected final String RETRIEVE_MODE_PROP = "javax.persistence.cache.retrieveMode";
    protected final String STORE_MODE_PROP = "javax.persistence.cache.storeMode";
    
    protected static OpenJPAEntityManagerFactorySPI emf = null;

    abstract OpenJPAEntityManagerFactorySPI getEntityManagerFactory();
    abstract JDBCListener getListener();

    protected static Class<?>[] persistentTypes =
        { CacheableEntity.class, UncacheableEntity.class, UnspecifiedEntity.class, NegatedCachableEntity.class,
            NegatedUncacheableEntity.class, XmlCacheableEntity.class, XmlUncacheableEntity.class };

    public void populate() throws IllegalAccessException, InstantiationException {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        for (Class<?> clss : persistentTypes) {
            if (!Modifier.isAbstract(clss.getModifiers())) {
                CacheEntity ce = (CacheEntity) clss.newInstance();
                ce.setId(1);
                em.persist(ce);
            }
        }
        em.getTransaction().commit();
        em.close();
    }

    public OpenJPAEntityManagerFactorySPI createEntityManagerFactory(String puName,
        Map<String, Object> additionalProperties) {
        Map<String, Object> propertiesMap = getPropertiesMap( persistentTypes, 
            "openjpa.jdbc.JDBCListeners", new JDBCListener [] { getListener() });
        if (additionalProperties != null) {
            Set<String> keys = additionalProperties.keySet();
            for (String key : keys) {
                propertiesMap.put(key, additionalProperties.get(key));
            }
        }
        OpenJPAEntityManagerFactorySPI emf =
            (OpenJPAEntityManagerFactorySPI) OpenJPAPersistence.createEntityManagerFactory(puName,
                "META-INF/caching-persistence.xml", propertiesMap );
        return emf;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cleanDatabase();
        populate();
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (emf != null) {
            clear(emf);
            closeEMF(emf);
            emf = null;
        }
    }
    
    public void cleanDatabase() throws Exception {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        for (Class<?> clss : persistentTypes) {
            if (!Modifier.isAbstract(clss.getModifiers())) {
                em.createQuery("Delete from " + clss.getSimpleName()).executeUpdate();
            }
        }
        em.getTransaction().commit();
        em.close();
    }

}

