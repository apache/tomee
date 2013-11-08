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
package org.apache.openjpa.persistence.meta;


import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.meta.common.apps.ExternalValues;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

/**
 * <p>Tests the {@link ExternalValuesFieldMapping}.</p>
 *
 * @author Abe White
 * @author Pinaki Poddar (added binding query parameter tests) 
 */
public class TestExternalValues
    extends AbstractTestCase {
    
    public TestExternalValues(String test) {
        super(test, "metacactusapp");
    }

    public void setUp()
        throws Exception {
        deleteAll(ExternalValues.class);
    }

    public void testInsert() {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);

        ExternalValues pc = new ExternalValues();
        pc.setBooleanToShort(true);
        pc.setByteToDouble((byte) 4);
        pc.setIntToFloat(4);
        pc.setLongToChar(4);
        pc.setShortToString((short) 4);
        pc.setFloatToBoolean(4.5f);
        pc.setDoubleToByte(4.5);
        pc.setCharToInt('f');
        pc.setStringToLong("foo");
        pm.persist(pc);
        Object oid = pm.getObjectId(pc);
        endTx(pm);
        endEm(pm);

        pm = (OpenJPAEntityManager) currentEntityManager();
        pc = (ExternalValues) pm.find(ExternalValues.class, oid);

        assertTrue(pc.getBooleanToShort());
        assertEquals((byte) 4, pc.getByteToDouble());
        assertEquals(4, pc.getIntToFloat());
        assertEquals(4, pc.getLongToChar());
        assertEquals((short) 4, pc.getShortToString());
        assertTrue(4.5f == pc.getFloatToBoolean());
        assertTrue(4.5 == pc.getDoubleToByte());
        assertEquals('f', pc.getCharToInt());
        assertEquals("foo", pc.getStringToLong());

        endEm(pm);
    }

    public void testComplexStrings() {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);

        ExternalValues pc = new ExternalValues();
        pc.setShortToString((short) 3);
        pc.setStringToLong("long string");

        pm.persist(pc);
        Object oid = pm.getObjectId(pc);
        endTx(pm);
        endEm(pm);

        pm = (OpenJPAEntityManager) currentEntityManager();
        pc = (ExternalValues) pm.find(ExternalValues.class, oid);
        assertEquals(3, pc.getShortToString());
        endEm(pm);
    }

    public void testAllNull() {
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);

        ExternalValues pc = new ExternalValues();
        pm.persist(pc);
        Object oid = pm.getObjectId(pc);
        endTx(pm);
        endEm(pm);

        pm = (OpenJPAEntityManager) currentEntityManager();
        pc = (ExternalValues) pm.find(ExternalValues.class, oid);
        endEm(pm);
    }
    
    public void testPositionalBindingQueryParameterEqualsDeclaredType() {
        UUID uuid = new UUID(1,4);
        createInstance(uuid);
        
        EntityManager em = currentEntityManager();
        String jpql = "SELECT p FROM ExternalValues p WHERE p.uuid=?1";
        List<ExternalValues> result = em.createQuery(jpql)
                                        .setParameter(1, uuid)
                                        .getResultList();
        assertFalse(result.isEmpty());
        for (ExternalValues x:result) {
            assertEquals(uuid, x.getUuid());
        }
    }
    
    public void testNamedBindingQueryParameterEqualsDeclaredType() {
        UUID uuid = new UUID(2,4);
        createInstance(uuid);
        
        EntityManager em = currentEntityManager();
        String jpql = "SELECT p FROM ExternalValues p WHERE p.uuid=:uuid";
        List<ExternalValues> result = em.createQuery(jpql)
                                        .setParameter("uuid", uuid)
                                        .getResultList();
        assertFalse(result.isEmpty());
        for (ExternalValues pc:result) {
            assertEquals(uuid, pc.getUuid());
        }
    }
    
    public void testPositionalBindingQueryParameterNotEqualsExternalizedType() {
        UUID uuid = new UUID(1,4);
        createInstance(uuid);
        
        EntityManager em = currentEntityManager();
        String jpql = "SELECT p FROM ExternalValues p WHERE p.uuid=?1";
        Query query = em.createQuery(jpql)
                        .setParameter(1, uuid.toString());
                                        
        try {
            query.getResultList();
            fail("Expected ArgumentException");
       } catch (ArgumentException ex) {
           // expected
       }
    }

    public void testNamedBindingQueryParameterNotEqualsExternalizedType() {
        UUID uuid = new UUID(2,4);
        createInstance(uuid);
        
        EntityManager em = currentEntityManager();
        String jpql = "SELECT p FROM ExternalValues p WHERE p.uuid=:uuid";
        Query query = em.createQuery(jpql)
                        .setParameter("uuid", uuid.toString());
        try {
             query.getResultList();
             fail("Expected ArgumentException");
        } catch (ArgumentException ex) {
            // expected
        }
    }
    
    private void createInstance(UUID uuid) {
        EntityManager em = currentEntityManager();
        em.getTransaction().begin();
        ExternalValues pc = new ExternalValues();
        pc.setUuid(uuid);
        em.persist(pc);
        em.getTransaction().commit();
        em.clear();
    }
}
