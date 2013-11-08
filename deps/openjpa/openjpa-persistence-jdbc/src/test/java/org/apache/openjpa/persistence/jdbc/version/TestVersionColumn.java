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
 package org.apache.openjpa.persistence.jdbc.version;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.openjpa.persistence.jdbc.version.model.IntVersion;
import org.apache.openjpa.persistence.jdbc.version.model.ShortVersion;
import org.apache.openjpa.persistence.jdbc.version.model.TimestampVersion;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestVersionColumn extends SQLListenerTestCase {
    public void setUp() {
        super.setUp(IntVersion.class, TimestampVersion.class, ShortVersion.class);
    }

    public void testNullIntegerVersion() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        em.persist(new IntVersion());
        em.persist(new IntVersion());
        tran.commit();

        tran.begin();
        em.createNativeQuery("UPDATE IntVersion set version = NULL").executeUpdate();
        tran.commit();
        em.clear();

        resetSQL();

        List<IntVersion> results = em.createQuery("SELECT i from IntVersion i", IntVersion.class).getResultList();
        assertNotNull("No results found", results);
        assertFalse("No results found", results.isEmpty());
        for (IntVersion iv : results) {
            assertEquals("Version should be initialized to 0, was: " + iv.getVersion(), 0, iv.getVersion());
            em.find(IntVersion.class, iv.getId());
        }

        assertEquals("Unexpected number of SQL statements: " + getSQLCount(), 1, getSQLCount());

        em.close();
    }

    public void testNullTimestampVersion() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        em.persist(new TimestampVersion());
        em.persist(new TimestampVersion());
        tran.commit();

        tran.begin();
        em.createNativeQuery("UPDATE TimestampVersion set version = NULL").executeUpdate();
        tran.commit();
        em.clear();

        resetSQL();
        List<TimestampVersion> results =
            em.createQuery("SELECT i from TimestampVersion i", TimestampVersion.class).getResultList();
        assertNotNull("No results found", results);
        assertFalse("No results found", results.isEmpty());
        for (TimestampVersion iv : results) {
            assertEquals("Version should be initialized to null, was: " + iv.getVersion(), null, iv.getVersion());
            em.find(TimestampVersion.class, iv.getId());
        }

        assertEquals("Unexpected number of SQL statements: " + getSQLCount(), 1, getSQLCount());

        em.close();
    }
    
    public void testNullShortVersion() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        em.persist(new ShortVersion());
        em.persist(new ShortVersion());
        tran.commit();

        tran.begin();
        em.createNativeQuery("UPDATE ShortVersion set version = NULL").executeUpdate();
        tran.commit();
        em.clear();

        resetSQL();
        List<ShortVersion> results =
            em.createQuery("SELECT i from ShortVersion i", ShortVersion.class).getResultList();
        assertNotNull("No results found", results);
        assertFalse("No results found", results.isEmpty());
        for (ShortVersion iv : results) {
            assertEquals("Version should be initialized to 0, was" + iv.getVersion(), 0, iv.getVersion());
            em.find(ShortVersion.class, iv.getId());
        }


        assertEquals("Unexpected number of SQL statements: " + getSQLCount(), 1, getSQLCount());

        em.close();
    }
}
