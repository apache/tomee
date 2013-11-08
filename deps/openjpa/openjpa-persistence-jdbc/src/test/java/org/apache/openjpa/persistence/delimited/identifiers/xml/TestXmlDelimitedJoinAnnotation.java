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
package org.apache.openjpa.persistence.delimited.identifiers.xml;

import java.util.List;

import javax.persistence.Query;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestXmlDelimitedJoinAnnotation extends SQLListenerTestCase {
    OpenJPAEntityManager em;
    int id = 0;
    EntityH entityH;
    EntityI entityI;
    EntityI2 entityI2;
    EntityI3 entityI3;
    EntityI4 entityI4;
    
    @Override
    public void setUp() throws Exception {
        setSupportedDatabases(
            org.apache.openjpa.jdbc.sql.DerbyDictionary.class,
            org.apache.openjpa.jdbc.sql.DB2Dictionary.class);
        if (isTestsDisabled()) {
            return;
        }
        
        super.setUp(
            org.apache.openjpa.persistence.delimited.identifiers.xml.EntityH.class,
            org.apache.openjpa.persistence.delimited.identifiers.xml.EntityI.class,
            org.apache.openjpa.persistence.delimited.identifiers.xml.EntityI2.class,
            org.apache.openjpa.persistence.delimited.identifiers.xml.EntityI3.class,
            org.apache.openjpa.persistence.delimited.identifiers.xml.EntityI4.class,
            DROP_TABLES);
        assertNotNull(emf);
        
        em = emf.createEntityManager();
        assertNotNull(em);
    }
    
    @Override
    public void tearDown() throws Exception {
        if (em != null && em.isOpen()) {
            em.close();
            em = null;
        }
        super.tearDown();
    }

    @Override
    protected String getPersistenceUnitName() {
        return "delimited-identifiers-joins-xml";
    }    
    
    public void createHandI(int id) {
        entityH = new EntityH(id);
        entityH.setName("eh");
        entityH.setSecName("secName1");
        
        entityI = new EntityI(id);
        entityI.setName("ei");
        
        entityI2 = new EntityI2(id);
        entityI2.setName("ei2");
        
        entityI3 = new EntityI3(id);
        entityI3.setName("ei3");
        
        entityI4 = new EntityI4(id);
        entityI4.setName("ei4");
        
        entityH.addEntityI(entityI);
        entityI.addEntityH(entityH);
        
        entityH.setEntityI2(entityI2);
        
        entityH.addMapValues(entityI3, entityI4);
        entityH.addMap2Values(entityI4, entityI3);
        
        entityI2.setEntityI3(entityI3);
    }

    public void testCreate() {
        id++;
        createHandI(id);
        
        em.getTransaction().begin();
        em.persist(entityH);
        em.persist(entityI);
        em.persist(entityI2);
        em.persist(entityI3);
        em.persist(entityI4);
        em.getTransaction().commit();
        
        runQueries();
    }
    
    private void runQueries() {
        em.clear();
        queryJoinTable();
        em.clear();
        queryJoinColumn();
        em.clear();
        querySecondaryTableValue();
        em.clear();
        queryMapValue();
    }
    
    private void queryJoinTable() {
        String query =
            "SELECT h " +
            "FROM EntityH h JOIN h.eIs i " +
            "WHERE i.name = 'ei'";
        Query q = em.createQuery(query);
        List<EntityH> results = (List<EntityH>)q.getResultList();
        assertEquals(1,results.size());
    }

    private void queryJoinColumn() {
        String query = 
            "SELECT h " +
            "FROM EntityH h JOIN h.eI2 i2 " +
            "WHERE i2.name = 'ei2'";
        Query q = em.createQuery(query);
        List<EntityH> results = (List<EntityH>)q.getResultList();
        assertEquals(1,results.size());
    }

    private void querySecondaryTableValue() {
        String query = 
            "SELECT h " +
            "FROM EntityH h " +
            "WHERE h.secName = 'secName1'";
        Query q = em.createQuery(query);
        List<EntityH> results = (List<EntityH>)q.getResultList();
        assertEquals(1,results.size());
    }

    private void queryMapValue() {
        String query =
            "SELECT h " +
            "FROM EntityH h, IN(h.map2) m " +
            "WHERE m.name = 'ei3'";
        Query q = em.createQuery(query);
        List<EntityH> results = (List<EntityH>)q.getResultList();
        assertEquals(1,results.size());
    }
    
    
}
