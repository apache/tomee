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

public class TestXmlDelimIdResultSets 
        extends SQLListenerTestCase {
    OpenJPAEntityManager em;
    int id = 0;
    Car car;
    Pontiac pontiac;
    Chevrolet chevrolet;
    Car2 car2;
    Pontiac2 pontiac2;
    Chevrolet2 chevrolet2;
    
    @Override
    public void setUp() throws Exception {
        setSupportedDatabases(
            org.apache.openjpa.jdbc.sql.DerbyDictionary.class,
            org.apache.openjpa.jdbc.sql.DB2Dictionary.class);
        if (isTestsDisabled()) {
            return;
        }
        
        super.setUp(
            org.apache.openjpa.persistence.delimited.identifiers.xml.Car.class,
            org.apache.openjpa.persistence.delimited.identifiers.xml.Pontiac.class,
            org.apache.openjpa.persistence.delimited.identifiers.xml.Chevrolet.class,
            org.apache.openjpa.persistence.delimited.identifiers.xml.Car2.class,
            org.apache.openjpa.persistence.delimited.identifiers.xml.Pontiac2.class,
            org.apache.openjpa.persistence.delimited.identifiers.xml.Chevrolet2.class,
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
        return "delimited-identifiers-result-set-xml";
    }    
    
    private void createChevrolet(int id) {
        chevrolet = new Chevrolet(id);
        chevrolet.setModel("Malibu");
        chevrolet.setColor("black");
        chevrolet.setModelYear("2009");
    }

    private void createPontiac(int id) {
        pontiac = new Pontiac(id);
        pontiac.setModel("G6");
        pontiac.setColor("red");
        pontiac.setModelYear("2005");
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
        resultSetQuery();
    }
    
    private void resultSetQuery() {
        String query = 
            "SELECT c.id, c.\"car model\", c.\"car color\", " +
            "c.\"discr col\", c.\"model year\" " +
            "FROM \"XmlCar\" c ";
        Query q = em.createNativeQuery(query,"XmlCarResultSet");
        List<Object[]> results = (List<Object[]>)q.getResultList();
        assertEquals(2,results.size());
        
        for (Object[] result : results) {
            assertEquals(2, result.length);
            assertTrue(result[0] instanceof Car2);
            assertTrue(result[1] instanceof String);
            Car2 car2 = (Car2)result[0];
            String modelYear = (String)result[1];
            if (car2.getModel().equals("G6")) {
                assertEquals("2005", modelYear);
            }
            else if (car2.getModel().equals("Malibu")) {
                assertEquals("2009", modelYear);
            }
        }
    }
}
