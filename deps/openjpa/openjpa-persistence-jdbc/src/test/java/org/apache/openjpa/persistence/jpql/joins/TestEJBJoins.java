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
package org.apache.openjpa.persistence.jpql.joins;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.common.apps.ArtCourse;
import org.apache.openjpa.persistence.common.apps.Course;
import org.apache.openjpa.persistence.common.apps.Department;
import org.apache.openjpa.persistence.common.apps.Student;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEJBJoins extends AbstractTestCase {

    public TestEJBJoins(String name) {
        super(name, "jpqlclausescactusapp");
    }

    public void setUp() {
        deleteAll(Course.class);
        deleteAll(Student.class);
        deleteAll(Department.class);

        EntityManager em = currentEntityManager();
        startTx(em);

        String name = "";
        List<Course> clist = new ArrayList<Course>();
        List<Department> dlist = new ArrayList<Department>();

        for (int i = 0; i < 5; i++) {
            Course curr = new Course("Math " + i, i * 2, i);
            Course acurr = new ArtCourse(i + 20, "English" + (2 * i));
            Department durr = new Department("CompSci" + i, null, i + 2);
            clist.add(curr);
            clist.add(acurr);
            dlist.add(durr);
        }

        Student stud = new Student("Jonathan", clist, dlist);
        Student stud2 = new Student("Stam", null, dlist);
        Student stud3 = new Student("John", clist, null);
        Student stud4 = new Student("Bill", null, null);

        em.persist(stud);
        em.persist(stud2);
        em.persist(stud3);
        em.persist(stud4);

        endTx(em);
        endEm(em);
    }

    public void testInnerJoin() {
        EntityManager em = currentEntityManager();
        String query = "SELECT distinct o.name from Student o JOIN " +
            "o.course d WHERE d.name" +
            "='Math 4'";

        List ls = (List) em.createQuery(query)
            .getResultList();

        assertNotNull(ls);

        if (ls != null) {
            assertEquals(2, ls.size());
        }
        endEm(em);
    }

    public void testOuterJoin() {
        EntityManager em = currentEntityManager();
        String query = "SELECT distinct s.name FROM Student " +
            "s LEFT JOIN s.department d";

        List ls = (List) em.createQuery(query)
            .getResultList();

        assertNotNull(ls);
        assertEquals(4, ls.size());

        endEm(em);
    }

    public void testFetchJoin1() {
        EntityManager em = currentEntityManager();
        String query = "SELECT s FROM Student s JOIN FETCH s.name";

        List ls = em.createQuery(query).getResultList();

        assertNotNull(ls);
        assertEquals(4, ls.size());

        endEm(em);
    }

    public void testFetchJoin2() {
        EntityManager em = currentEntityManager();
        String query = "SELECT s " +
            "FROM Student s " +
            "JOIN FETCH s.name d";

        try {
            List ls = em.createQuery(query).getResultList();
            fail("Not permitted to specify an id variable for entities ref." +
                    " by the right side of fetch join");
        }
        catch (Exception e) {
            //suppose to throw an exception..should not pass
        }

        endEm(em);
    }

    public void testLeftOuterJoin() {
        EntityManager em = currentEntityManager();

        String ljoin = "SELECT DISTINCT s.name FROM Student s " +
                "LEFT OUTER JOIN s.department d WHERE d.name = 'CompSci2'";

        List ls = em.createQuery(ljoin).getResultList();

        assertNotNull(ls);
        assertEquals(2, ls.size());

        assertTrue(ls.contains("Jonathan"));
        assertTrue(ls.contains("Stam"));

        endEm(em);
    }

    public void testInnerJoinFetch() {
        EntityManager em = currentEntityManager();

        String query = "SELECT s FROM Student " +
            "s JOIN FETCH s.department";

        List ls = (List) em.createQuery(query)
            .getResultList();

        assertNotNull(ls);
        assertEquals(2, ls.size());

        em.close();
    }

    public void testLeftJoinFetch() {
        EntityManager em = currentEntityManager();

        String query = "SELECT s FROM Student " +
            "s LEFT JOIN FETCH s.department";

        List ls = (List) em.createQuery(query)
            .getResultList();

        assertNotNull(ls);
        assertEquals(4, ls.size());

        em.close();
    }
}
