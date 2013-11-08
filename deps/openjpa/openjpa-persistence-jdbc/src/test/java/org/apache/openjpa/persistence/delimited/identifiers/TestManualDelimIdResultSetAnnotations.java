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
package org.apache.openjpa.persistence.delimited.identifiers;

import java.util.List;

import javax.persistence.Query;

import org.apache.openjpa.jdbc.sql.DB2Dictionary;
import org.apache.openjpa.jdbc.sql.DerbyDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestManualDelimIdResultSetAnnotations 
        extends SQLListenerTestCase {
    OpenJPAEntityManager em;
    int id = 0;
    Animal animal;
    Dog dog;
    Cat cat;
    Animal2 animal2;
    Dog2 dog2;
    Cat2 cat2;
    
    @Override
    public void setUp() throws Exception {
        // NOTE: This test is only configured to run on DB2 and Derby since 
        // those DBs handle non-default schemas without additional authority or 
        // configuration  
        setSupportedDatabases(DB2Dictionary.class, DerbyDictionary.class);
        if (isTestsDisabled())
            return;
        
        super.setUp(
            org.apache.openjpa.persistence.delimited.identifiers.Animal.class,
            org.apache.openjpa.persistence.delimited.identifiers.Dog.class,
            org.apache.openjpa.persistence.delimited.identifiers.Cat.class,
            org.apache.openjpa.persistence.delimited.identifiers.Animal2.class,
            org.apache.openjpa.persistence.delimited.identifiers.Dog2.class,
            org.apache.openjpa.persistence.delimited.identifiers.Cat2.class,
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

    private void createCat(int id) {
        cat = new Cat(id);
        cat.setName("Puff");
        cat.setType("cat");
        cat.setAge(3);
    }

    private void createDog(int id) {
        dog = new Dog(id);
        dog.setName("Spot");
        dog.setType("dog");
        dog.setAge(9);
    }

    public void testCreate() {
        id++;
        createDog(id);
        id++;
        createCat(id);
        
        em.getTransaction().begin();
        em.persist(dog);
        em.persist(cat);
        em.getTransaction().commit();
        
        runQueries();
    }
    
    private void runQueries() {
        em.clear();
        resultSetQuery();
    }
    
    private void resultSetQuery() {
        String query = 
            "SELECT a.id, a.\"animal type\", a.\"animal name\", " +
            "a.\"discr col\", a.\"animal age\" " +
            "FROM \"Animal\" a ";
        Query q = em.createNativeQuery(query,"AnimalResultSet");
        List<Object[]> results = (List<Object[]>)q.getResultList();
        assertEquals(2,results.size());
        
        for (Object[] result : results) {
            assertEquals(2, result.length);
            assertTrue(result[0] instanceof Animal2);
            assertTrue(result[1] instanceof Integer);
            Animal2 animal2 = (Animal2)result[0];
            Integer age = (Integer)result[1];
            if (animal2.getName().equals("Spot")) {
                assertEquals(9, age.intValue());
            }
            else if (animal2.getName().equals("Puff")) {
                assertEquals(3, age.intValue());
            }
        }
    }
}
