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
package org.apache.openjpa.persistence.delimited.identifiers.noschema;

import java.util.List;

import javax.persistence.Query;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.MariaDBDictionary;
import org.apache.openjpa.jdbc.sql.MySQLDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestNoSchemaManualDelimitedJoinAnnotations extends SQLListenerTestCase {
    OpenJPAEntityManager em;
    int id = 0;
    EntityC entityC;
    EntityD entityD;
    EntityD2 entityD2;
    EntityD3 entityD3;
    EntityD4 entityD4;
    JDBCConfiguration conf;
    DBDictionary dict;
    
    @Override
    public void setUp() throws Exception {
        
        setUnsupportedDatabases(MariaDBDictionary.class);
        setUnsupportedDatabases(MySQLDictionary.class);
        if (isTestsDisabled())
            return;
        
        super.setUp(
            org.apache.openjpa.persistence.delimited.identifiers.noschema.EntityC.class,
            org.apache.openjpa.persistence.delimited.identifiers.noschema.EntityD.class,
            org.apache.openjpa.persistence.delimited.identifiers.noschema.EntityD2.class,
            org.apache.openjpa.persistence.delimited.identifiers.noschema.EntityD3.class,
            org.apache.openjpa.persistence.delimited.identifiers.noschema.EntityD4.class,
            DROP_TABLES);
        assertNotNull(emf);
        
        em = emf.createEntityManager();
        assertNotNull(em);
        
        conf = (JDBCConfiguration) emf.getConfiguration();
        dict = conf.getDBDictionaryInstance();
    }
    
    @Override
    public void tearDown() throws Exception {
        if (em != null && em.isOpen()) {
            em.close();
            em = null;
        }
        dict = null;
        conf = null;
        super.tearDown();
    }

    private void createCandD(int id) {
        entityC = new EntityC(id);
        entityC.setName("ec");
        entityC.setSecName("secName1");
        
        entityD = new EntityD(id);
        entityD.setName("ed");
        
        entityD2 = new EntityD2(id);
        entityD2.setName("ed2");
        
        entityD3 = new EntityD3(id);
        entityD3.setName("ed3");
        
        entityD4 = new EntityD4(id);
        entityD4.setName("ed4");
        
        entityC.addEntityD(entityD);
        entityD.addEntityC(entityC);
        
        entityC.setEntityD2(entityD2);
        
        entityC.addMapValues(entityD3, entityD4);
        entityC.addMap2Values(entityD4, entityD3);
        
        entityD2.setEntityD3(entityD3);
    }
    
    public void testCreate() {
        id++;
        createCandD(id);
        // TODO: Maybe create another one.
        
        em.getTransaction().begin();
        em.persist(entityC);
        em.persist(entityD);
        em.persist(entityD2);
        em.persist(entityD3);
        em.persist(entityD4);
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
            "SELECT c " +
            "FROM EntityC c JOIN c.entityDs d " +
            "WHERE d.name = 'ed'";
        Query q = em.createQuery(query);
        List<EntityC> results = (List<EntityC>)q.getResultList();
        assertEquals(1,results.size());
    }
    
    private void queryJoinColumn() {
        String query = 
            "SELECT c " +
            "FROM EntityC c JOIN c.entityD2 d2 " +
            "WHERE d2.name = 'ed2'";
        Query q = em.createQuery(query);
        List<EntityC> results = (List<EntityC>)q.getResultList();
        assertEquals(1,results.size());
    }
    
    private void querySecondaryTableValue() {
        String query = 
            "SELECT c " +
            "FROM EntityC c " +
            "WHERE c.secName = 'secName1'";
        Query q = em.createQuery(query);
        List<EntityC> results = (List<EntityC>)q.getResultList();
        assertEquals(1,results.size());
    }
    
    private void queryMapValue() {
        String query =
            "SELECT c " +
            "FROM EntityC c, IN(c.map2) m " +
            "WHERE m.name = 'ed3'";
        Query q = em.createQuery(query);
        List<EntityC> results = (List<EntityC>)q.getResultList();
        assertEquals(1,results.size());
    }
        
      
}
