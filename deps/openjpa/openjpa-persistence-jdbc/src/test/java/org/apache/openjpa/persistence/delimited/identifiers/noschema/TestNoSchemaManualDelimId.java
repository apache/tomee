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

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestNoSchemaManualDelimId extends SQLListenerTestCase {
    OpenJPAEntityManager em;
    EntityF entityF;
    
    @Override
    public void setUp() throws Exception {

        super.setUp(
            org.apache.openjpa.persistence.delimited.identifiers.noschema.EntityF.class,
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

    public void createEntityF() {
        entityF = new EntityF("fName");
        entityF.setNonDelimName("fNonDelimName");
        entityF.setSecName("sec name");
        entityF.addCollectionSet("xxx");
        entityF.addCollectionSet("yyy");
        entityF.addCollectionDelimSet("aaa");
        entityF.addCollectionDelimSet("bbb");
        entityF.addCollectionMap("aaa", "xxx");
        entityF.addCollectionMap("bbb", "yyy");
        entityF.addDelimCollectionMap("www", "xxx");
        entityF.addDelimCollectionMap("yyy", "zzz");
    }
    
    public void testCreateF() {
        createEntityF();
        
        em.getTransaction().begin();
        em.persist(entityF);
        em.getTransaction().commit();
        
        runQueries();
        
    }
    
    // Run a second time to re-create a situation that initially caused a problem when running this
    // test consecutive times.
    public void testCreateF2() {
        createEntityF();
        
        em.getTransaction().begin();
        em.persist(entityF);
        em.getTransaction().commit();
    }
    
    private void runQueries() {
        em.clear();
        queryOnEntityOnly();
        em.clear();
        queryOnColumnValue();
        em.clear();
        queryCollection();
    }
    
    private void queryOnEntityOnly() {
        String query =
            "SELECT DISTINCT f " +
            "FROM EntityF f";
        Query q = em.createQuery(query);
        List<EntityF> results = (List<EntityF>)q.getResultList();
        assertEquals(1,results.size());
    }
    
    private void queryOnColumnValue() {
        String query =
            "SELECT DISTINCT f " +
            "FROM EntityF f " +
            "WHERE f.name = 'fName'";
        Query q = em.createQuery(query);
        List<EntityF> results = (List<EntityF>)q.getResultList();
        assertEquals(1,results.size());
    }
    
    private void queryCollection() {
        String query =
            "SELECT DISTINCT f " +
            "FROM EntityF f, IN(f.nscds) s " +
            "WHERE s = 'aaa'";
        Query q = em.createQuery(query);
        List<EntityF> results = (List<EntityF>)q.getResultList();
        assertEquals(1,results.size());
    }
}
