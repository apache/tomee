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
package org.apache.openjpa.persistence.query;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestQueryEscapeCharacters
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(Employee.class, CLEAR_TABLES);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Employee e = new Employee();
        e.setName("Mike Dick");
        e.setEmpId(1);
        em.persist(e);

        e = new Employee();
        e.setName("Mike Jones");
        e.setEmpId(2);
        em.persist(e);

        e = new Employee();
        e.setName("Mike Smith");
        e.setEmpId(3);
        em.persist(e);
        
        e = new Employee();
        e.setName("M%ke Smith");
        e.setEmpId(4);
        em.persist(e);
        em.getTransaction().commit();
        em.close();
    }

    public void tearDown() throws Exception {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("Delete from Employee").executeUpdate();
        em.getTransaction().commit();
        em.close();
        super.tearDown();
    }
    
    public void testNormalQuery() {
        performFind ("Employee.findByName", "%Dick", 1);
    }

    public void testMultiResultQuery() {
        performFind ("Employee.findByName", "Mike%", 3);
    }

    public void testEscapedQuery() {
        performFind ("Employee.findByNameEscaped", 
                "M\\%%", 1);
    }

    public void testDoubleEscapedQuery() {
        performFind ("Employee.findByName", "\\\\", 0);
    }

    public void testWrongEscape() {
        performFind ("Employee.findByName", "M|%%", 0);
    }

    public void testDoubleSlashQuery() {
        // get the Dictionary and check the requiresSearchStringEscapeForLike flag
        OpenJPAEntityManagerFactorySPI ojpaEmf = emf;
        JDBCConfiguration conf = (JDBCConfiguration)ojpaEmf.getConfiguration();
        
        if (conf.getDBDictionaryInstance().
                requiresSearchStringEscapeForLike == true) {
            return;
        }

        performFind ("Employee.findByName", "\\", 0);
    }

    @SuppressWarnings("unchecked")
    public void testDifferentEscapeCharacter () {
        OpenJPAEntityManagerFactorySPI ojpaEmf = emf;
        JDBCConfiguration conf = (JDBCConfiguration)ojpaEmf.getConfiguration();

        // Would be nice to just pass a map to the createEntityManager, but
        // seems like it would be too much trouble to get the proper DB type
        // and then build the string for the map.
        conf.getDBDictionaryInstance().requiresSearchStringEscapeForLike = true;
        conf.getDBDictionaryInstance().searchStringEscape = "|";
        EntityManager em = emf.createEntityManager();

        Query q = em.createNamedQuery("Employee.findByName");
        q.setParameter("name", "M|%%");
        List<Employee> emps = q.getResultList();
        assertEquals(1, emps.size());

        String unnamedQuery =
            "Select e from Employee e where e.name LIKE :name";

        q = em.createQuery(unnamedQuery);
        q.setParameter("name", "M|%%");
        emps = q.getResultList();
        assertEquals(1, emps.size());
        em.close();
    }

    @SuppressWarnings("unchecked")
    private void performFind (String namedQuery, String parameter,
            int expected) {
        EntityManager em = emf.createEntityManager();

        Query q = em.createNamedQuery(namedQuery);
        q.setParameter("name", parameter);
        List<Employee> emps = q.getResultList();
        assertEquals(expected, emps.size());

        String unnamedQuery =
            "Select e from Employee e where e.name LIKE :name";
        if (namedQuery.equals("Employee.findByNameEscaped")) {
            unnamedQuery =
                "Select e from Employee e where e.name LIKE :name ESCAPE '\\'";
        }
        q = em.createQuery(unnamedQuery);
        q.setParameter("name", parameter);
        emps = q.getResultList();
        assertEquals(expected, emps.size());
        em.close();
    }
}
