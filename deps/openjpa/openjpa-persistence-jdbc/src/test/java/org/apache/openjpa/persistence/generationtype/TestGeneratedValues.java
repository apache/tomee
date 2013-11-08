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
package org.apache.openjpa.persistence.generationtype;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.PostgresDictionary;
import org.apache.openjpa.persistence.InvalidStateException;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestGeneratedValues extends SingleEMFTestCase {
    DBDictionary _dict;
    
    public void setUp() { 
        setUp(GeneratedValues.class, CLEAR_TABLES);
        _dict = ((JDBCConfiguration)emf.getConfiguration()).getDBDictionaryInstance();
    }

    public void testDefaultValues() {
        if (_dict instanceof PostgresDictionary) 
            return;
        
        EntityManager em = emf.createEntityManager();

        GeneratedValues gv = new GeneratedValues();
        GeneratedValues gv2 = new GeneratedValues();

        em.getTransaction().begin();
        em.persist(gv);
        em.persist(gv2);
        em.getTransaction().commit();

        em.refresh(gv);
        em.refresh(gv2);

        // Note: UUID 'string' values are not compared (intermittent failures
        // on DB2.) In an environment where data is converted to
        // a considerably different character encoding of the database (ex.
        // Unicode -> EBCDIC) upon persistence, the uuid string returned by the 
        // database may not be equal to the original value.  This is a common 
        // issue with string data, but even more likely for a uuids given that 
        // uuid strings are produced from pseudo-random byte arrays, which yield
        // all sorts of variant characters.
        assertFalse(gv.getId() == gv2.getId());
        assertFalse(gv.getField() == gv2.getField());
        // assertFalse(gv.getUuidstring().equals(gv2.getUuidstring()));
        assertFalse(gv.getUuidhex().equals(gv2.getUuidhex()));
        assertFalse(gv.getUuidT4hex().equals(gv2.getUuidT4hex()));
        assertFalse(gv.getUuidT4string().equals(gv2.getUuidT4string()));
        closeEM(em);
    }
    
    public void testInitialValues() { 
        EntityManager em = emf.createEntityManager();

        GeneratedValues gv = new GeneratedValues(7, 9, "a", "b", "c", "d");

        try {
            em.getTransaction().begin();
            em.persist(gv);
            em.getTransaction().commit();
        } catch (InvalidStateException ise) {
            // expected result
            return;
        }  catch (EntityExistsException eee) {
            // also ok
            return;
        } finally {
            closeEM(em);
        }
        
        // should not get here...
        fail();
    }
    
    public void testIdSetter() { 
        EntityManager em = emf.createEntityManager();

        GeneratedValues gv = new GeneratedValues();
        gv.setId(3);

        try {
            em.getTransaction().begin();
            em.persist(gv);
            em.getTransaction().commit();
        } catch (InvalidStateException ise) {
            // expected result
            return;
        }  catch (EntityExistsException eee) {
            // also ok
            return;
        } finally {
            closeEM(em);
        }
        
        // should not get here...
        fail();
    }
    
    public void testFieldSetter() { 
        EntityManager em = emf.createEntityManager();

        GeneratedValues gv = new GeneratedValues();
        gv.setField(5);

        try {
            em.getTransaction().begin();
            em.persist(gv);
            em.getTransaction().commit();
        } catch (InvalidStateException ise) {
            // expected result
            return;
        } finally {
            closeEM(em);
        }
        
        // should not get here...
        fail();
    }

//    public void testCustomSequenceGenerator() {
//        EntityManager em = emf.createEntityManager();
//
//        GeneratedValues gv = new GeneratedValues();
//
//        em.getTransaction().begin();
//        em.persist(gv);
//        em.getTransaction().commit();
//
//        assertNotEquals(0, gv.getCustomSeqField());
//    }

    public void testCustomSequenceGeneratorWithIndirection() {
        if (_dict instanceof PostgresDictionary) 
            return;
        EntityManager em = emf.createEntityManager();

        GeneratedValues gv = new GeneratedValues();

        em.getTransaction().begin();
        em.persist(gv);
        em.getTransaction().commit();

        assertNotEquals(0, gv.getCustomSeqWithIndirectionField());
        closeEM(em);
    }
    
    public void testUUIDGenerators() {
        if (_dict instanceof PostgresDictionary) 
            return;
        
        EntityManager em = emf.createEntityManager();

        GeneratedValues gv = new GeneratedValues();
        em.getTransaction().begin();
        em.persist(gv);
        em.getTransaction().commit();
        
        int id = gv.getId();

        assertTrue(isStringUUID(gv.getUuidT4string(), 4));
        assertTrue(isStringUUID(gv.getUuidstring(), 1));
        assertTrue(isHexUUID(gv.getUuidhex(), 1));
        assertTrue(isHexUUID(gv.getUuidT4hex(), 4));     
        
        em.clear();
        
        GeneratedValues gv2 = em.find(GeneratedValues.class, id);  
        assertNotNull(gv2);
        // The string value could contain null values and such so length
        // calculations may be non-deterministic.  For string generators, 
        // simply ensure the fields are populated (not null). 
        assertNotNull(gv2.getUuidstring());
        assertTrue(isHexUUID(gv2.getUuidhex(), 1));
        assertNotNull(gv2.getUuidT4string());
        assertTrue(isHexUUID(gv2.getUuidT4hex(), 4));     
        
        // Compare original hex values with new values.  They should be equal.
        // Note: UUID 'string' values are not compared.  In most cases they will
        // be the same, but in an environment where data is converted to
        // a considerably different character encoding of the database (ex.
        // Unicode -> EBCDIC) upon persistence, the uuid string returned by the 
        // database may not be equal to the original value.  This is a common 
        // issue with string data, but even more likely for a uuids given that 
        // uuid strings are produced from pseudo-random byte arrays, which yield
        // all sorts of variant characters.
        assertTrue(gv.getId() == gv2.getId());
        assertTrue(gv.getField() == gv2.getField());
        assertTrue(gv.getUuidhex().equals(gv2.getUuidhex()));
        assertTrue(gv.getUuidT4hex().equals(gv2.getUuidT4hex()));
        closeEM(em);
    }
                
    /*
     * Verify a uuid string is 16 characters long and is the expected type.
     */
    private boolean isStringUUID(String value, int type) {
        if (value.length() != 16)
            return false;
        byte version = (byte)(value.charAt(6) >>> 4);
        if (type != version) return false;
        return true;
    }
    
    /*
     * Verify a uuid hex string value is 32 characters long, consists entirely
     * of hex digits and is the correct version.
     */
    private boolean isHexUUID(String value, int type) {
        if (value.length() != 32) 
            return false;
        char[] chArr = value.toCharArray();
        for (int i = 0; i < 32; i++)
        {                
            char ch = chArr[i];
            if (!(Character.isDigit(ch) ||
                (ch >= 'a' && ch <= 'f') ||
                (ch >= 'A' && ch <= 'F')))
                return false;
            if (i == 12) {
                if (type == 1 && ch != '1')
                    return false;
                if (type == 4 && ch != '4')
                    return false;
            }
        }
        return true;
    }
}
