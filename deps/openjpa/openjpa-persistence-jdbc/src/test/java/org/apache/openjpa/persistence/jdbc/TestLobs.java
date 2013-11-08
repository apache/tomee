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
package org.apache.openjpa.persistence.jdbc;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.RollbackException;

import junit.framework.AssertionFailedError;

import org.apache.openjpa.jdbc.sql.SybaseDictionary;
import org.apache.openjpa.persistence.kernel.common.apps.Blobs;
import org.apache.openjpa.persistence.kernel.common.apps.Lobs;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestLobs extends SingleEMFTestCase {
    /**
     * Support method to get a random Byte for testing.
     */
    public static Byte randomByte() {
        return new Byte((byte) (Math.random() * Byte.MAX_VALUE));
    }

    public void setUp() throws Exception {
        super.setUp(DROP_TABLES, Lobs.class, Blobs.class);
    }
    
    // blob tests 
    public void testBlobSetToNull() { 

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Blobs lobs = new Blobs();
        lobs.setLobNotNullable(null);
        lobs.setLobNullable(null);
        em.persist(lobs);
        try {
            em.getTransaction().commit();
            fail("Expected a RollbackException");
        } catch (Exception e) {
            assertError(e, RollbackException.class);
        }
        em.close();
    }
    
    public void testBlobPersistQuery() {
        // test with null
        EntityManager em = emf.createEntityManager();
        Blobs lobs = new Blobs();
        byte[] bytes = new byte[10];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = randomByte().byteValue();

        em.getTransaction().begin();
        lobs.setLobNotNullable(bytes);
        lobs.setLobNullable(null);
        em.persist(lobs);
        em.getTransaction().commit();
        em.close();
        
        em = emf.createEntityManager();
        em.getTransaction().begin();
        Query query = em.createQuery("select e from Blobs e");
        lobs = (Blobs)query.getSingleResult();
        assertTrue(lobs.getLobNullable() == null || lobs.getLobNullable().length == 0);   // still an empty string
        em.remove(lobs);
        em.getTransaction().commit();
        em.close();
    }

    public void testBlobZeroLengthByteArray() throws Exception { 
        // test with 0 length bytes
        byte[] bytes = new byte[0];
        EntityManager em = emf.createEntityManager();
        Blobs lobs = new Blobs();
        
        em.getTransaction().begin();
        lobs.setLobNotNullable(bytes);
        lobs.setLobNullable(bytes);
        em.persist(lobs);
        try {
            em.getTransaction().commit();
        } catch (Exception e) {
            if (getDBDictionary() instanceof SybaseDictionary) {
                assertTrue(e instanceof RollbackException);
                return;
            } else {
                throw e;
            }
        }
        em.close();

        em = emf.createEntityManager();
        em.getTransaction().begin();
        Query query = em.createQuery("select e from Blobs e");
        lobs = (Blobs)query.getSingleResult();
        assertTrue(lobs.getLobNullable() == null || lobs.getLobNullable().length == 0);
        assertTrue(lobs.getLobNotNullable() == null || lobs.getLobNotNullable().length == 0);
        for (int i = 0; i < bytes.length; i++) {
            assertEquals(bytes[i], lobs.getLobNullable()[i]);
        }
        em.remove(lobs);
        em.getTransaction().commit();
        em.close();
    }

    public void testBlobLargeData() { 
        // test with large data
        byte[] bytes = new byte[5000];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = randomByte().byteValue();

        EntityManager em = emf.createEntityManager();
        Blobs lobs = new Blobs();
        em.getTransaction().begin();
        lobs.setLobNotNullable(bytes);
        lobs.setLobNullable(bytes);
        em.persist(lobs);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        em.getTransaction().begin();
        Query query = em.createQuery("select e from Blobs e");
        lobs = (Blobs)query.getSingleResult();
        for (int i = 0; i < 5000; i++) {
            assertEquals(lobs.getLobNullable()[i], lobs.getLobNotNullable()[i]);
            assertEquals(bytes[i], lobs.getLobNullable()[i]);
        }
        em.remove(lobs);
        em.getTransaction().commit();
        em.close();
    }
    
    // lob tests

    public void testLobPersistQuery() {
        // test with null
        EntityManager em = emf.createEntityManager();
        Lobs lobs = new Lobs();
        em.getTransaction().begin();
        lobs.setLobNotNullable("test");
        lobs.setLobNullable(null);
        em.persist(lobs);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        em.getTransaction().begin();
        Query query = em.createQuery("select e from Lobs e");
        lobs = (Lobs)query.getSingleResult();
        // Oracle treats "" as null
        assertTrue(lobs.getLobNullable() == null || lobs.getLobNullable().length() == 0);   // still an empty string
        em.remove(lobs);
        em.getTransaction().commit();
        em.close();
    }
    
    public void testLobSetToNull() { 
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Lobs lobs = new Lobs();
        lobs.setLobNotNullable(null);
        lobs.setLobNullable(null);
        em.persist(lobs);
        try {
            em.getTransaction().commit();
            fail("Expected a RollbackException");
        } catch (Exception e) {
            assertError(e, RollbackException.class);
        }
        em.close();
    }
   
    public void testLobEmptyString() {
        // test with ""
        EntityManager em = emf.createEntityManager();
        Lobs lobs = new Lobs();
        em.getTransaction().begin();
        lobs.setLobNullable("");
        em.persist(lobs);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        em.getTransaction().begin();
        Query query = em.createQuery("select e from Lobs e");
        lobs = (Lobs)query.getSingleResult();
        
        if (lobs.getLobNullable() != null) {
            if (getDBDictionary() instanceof SybaseDictionary) {
                // Sybase stores empty strings as " "
                assertEquals(" ", lobs.getLobNullable());
            } else {
                assertEquals(0, lobs.getLobNullable().length());
            }
        }
        if (lobs.getLobNotNullable() != null) {
            if (getDBDictionary() instanceof SybaseDictionary) {
                // Sybase stores empty strings as " "
                assertEquals(" ", lobs.getLobNotNullable());
            } else {
                assertEquals(0, lobs.getLobNotNullable().length());
            }
        }
        assertEquals(lobs.getLobNullable(), lobs.getLobNotNullable());
        em.remove(lobs);
        em.getTransaction().commit();
        em.close();
    }
    
    public void testLobLargeData() { 
        // test with large data
        String temp = "";
        for (int i = 0; i < 500; i++) // at 400 it changes from strings to Objects
            temp = temp + "1234567890";

        EntityManager em = emf.createEntityManager();
        Lobs lobs = new Lobs();
        em.getTransaction().begin();
        lobs.setLobNotNullable(temp);
        lobs.setLobNullable(temp);
        em.persist(lobs);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        em.getTransaction().begin();
        Query query = em.createQuery("select e from Lobs e");
        lobs = (Lobs)query.getSingleResult();
        assertEquals(lobs.getLobNullable(), lobs.getLobNotNullable());
        assertEquals(temp, lobs.getLobNullable());
        em.remove(lobs);
        em.getTransaction().commit();
        em.close();
    }

    void assertError(Throwable actual, Class<? extends Throwable> expected) {
        if (!expected.isAssignableFrom(actual.getClass())) {
            actual.printStackTrace();
            throw new AssertionFailedError(actual.getClass().getName() + " was raised but expected "
                    + expected.getName());
        }
    }
}
