package org.apache.openjpa.persistence.delimited.identifiers;

import java.util.List;

import javax.persistence.Query;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
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
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestDelimInheritance extends SQLListenerTestCase {
    OpenJPAEntityManager em;
    int id = 0;
    Pontiac pontiac;
    Chevrolet chevrolet;
    
    @Override
    public void setUp() throws Exception {
        setSupportedDatabases(
            org.apache.openjpa.jdbc.sql.DerbyDictionary.class,
            org.apache.openjpa.jdbc.sql.DB2Dictionary.class);
        if (isTestsDisabled()) {
            return;
        }
        
        super.setUp(
            org.apache.openjpa.persistence.delimited.identifiers.Car.class,
            org.apache.openjpa.persistence.delimited.identifiers.Pontiac.class,
            org.apache.openjpa.persistence.delimited.identifiers.Chevrolet.class,
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
    protected OpenJPAEntityManagerFactorySPI createEMF(final Object... props) {
        return createNamedEMF("delimited-identifiers", props);
    }
    
    private void createPontiac(int id) {
        pontiac = new Pontiac(id);
        pontiac.setModel("G6");
        pontiac.setColor("red");
    }
    
    private void createChevrolet(int id) {
        chevrolet = new Chevrolet(id);
        chevrolet.setModel("Malibu");
        chevrolet.setColor("black");
    }
    
    public void testCreate() {
        id++;
        createPontiac(id);
        id++;
        createChevrolet(id);
        
        em.getTransaction().begin();
        em.persist(pontiac);
        em.persist(chevrolet);
        em.getTransaction().commit();
        
        runQueries();
    }
    
    private void runQueries() {
        em.clear();
        queryChevrolet();
        em.clear();
        queryPontiac();
    }
    
    private void queryChevrolet() {
        String query =
            "SELECT DISTINCT c " +
            "FROM Car c " +
            "WHERE c.model = 'Malibu'";
        Query q = em.createQuery(query);
        List<Car> results = (List<Car>)q.getResultList();
        assertEquals(1,results.size());
    }
    
    // Use native query
    private void queryPontiac() {
        String query = 
            "SELECT * " +
            "FROM \"Car\" c " +
            "WHERE c.\"discr col\" = 'Pontiac'";
        Query q = em.createNativeQuery(query);
        List<Car> results = (List<Car>)q.getResultList();
        assertEquals(1,results.size());
    }
}
