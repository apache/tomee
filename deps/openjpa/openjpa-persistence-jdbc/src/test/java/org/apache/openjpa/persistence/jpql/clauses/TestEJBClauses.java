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
package org.apache.openjpa.persistence.jpql.clauses;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.persistence.common.apps.Course;
import org.apache.openjpa.persistence.common.apps.ArtCourse;
import org.apache.openjpa.persistence.common.apps.Department;
import org.apache.openjpa.persistence.common.apps.Student;

public class TestEJBClauses extends AbstractTestCase {

    public TestEJBClauses(String name) {
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

    public void testFromClause1() {
        EntityManager em = currentEntityManager();
        String query = "SELECT o.name FROM Student o";

        List result = em.createQuery(query)
            .getResultList();

        assertNotNull(result);
        assertEquals(4, result.size());

        endEm(em);
    }

    public void testFromClause2() {
        EntityManager em = currentEntityManager();

        String query = "SELECT NEW apps.ArtCourse(e.name)" +
            "FROM Student e";

        List result = em.createQuery(query).getResultList();

        assertNotNull(result);
        assertEquals(4, result.size());

        endEm(em);
    }

    public void testFromClause3() {
        EntityManager em = currentEntityManager();
        String query = "SELECT o.name " +
            "FROM Student o, Course c " +
            "WHERE o.course IS NULL AND o.department IS NULL";

        List ls = (List) em.createQuery(query)
            .getResultList();
        String uno = (String) ls.get(0);

        assertNotNull(ls);
        assertEquals(1, ls.size());
        assertEquals("Bill", uno);

        endEm(em);
    }

    public void testWhereClause1() {
        EntityManager em = currentEntityManager();
        String query = "SELECT distinct s.name " +
            "FROM Student s, Course d " +
            "WHERE d.courseId >= 4 AND s.department IS NOT NULL";

        List ls = em.createQuery(query).getResultList();

        assertNotNull(ls);
        assertEquals(2, ls.size());

        endEm(em);
    }

    public void testWhereClause2() {
        EntityManager em = currentEntityManager();
        String query = "SELECT distinct s.name " +
            "FROM Student s " +
            "WHERE" +
            " Exists(SELECT c FROM s.course c WHERE c.name LIKE 'Math%')";

        List ls = em.createQuery(query).getResultList();

        assertNotNull(ls);
        assertEquals(2, ls.size());
        assertTrue(ls.contains("Jonathan"));
        assertTrue(ls.contains("John"));

        endEm(em);
    }

    public void testClauseRangeVar() {
        EntityManager em = currentEntityManager();
        String query = "SELECT DISTINCT s FROM Student s, Student s2 " +
            "WHERE s.name = 'John' AND s2.name = 'Jonathan'";

        List ls = em.createQuery(query).getResultList();

        assertNotNull(ls);
        assertEquals(1, ls.size());

        Student ret = (Student) ls.get(0);
        assertEquals("John", ret.getName());

        endEm(em);
    }

    public void testClausePathExpr() {
        EntityManager em = currentEntityManager();
        String failure = "SELECT DISTINCT s " +
            "FROM Student s WHERE" +
            " s.department.name = 'CompSci1'";
        // Changes related to OPENJPA-485 allows this query to pass.
        // The query is not kosher as it does navigate through a 
        // collection-valued-path-expression (s.department.name) where
        // department is a Collection. 
        // But we allow this because of the convenience of the query expression 
        List ls = em.createQuery(failure).getResultList();
        assertFalse(ls.isEmpty());
        endEm(em);
    }

    public void testClausePathExpr2() {
        EntityManager em = currentEntityManager();
        String success =
            "SELECT DISTINCT d.name FROM Student AS s, IN(s.department) d ";

        List ls2 = em.createQuery(success).getResultList();

        assertNotNull(ls2);
        assertEquals(5, ls2.size());

        endEm(em);
    }

    public void testCollMemberDecl() {
        EntityManager em = currentEntityManager();

        String colldec = "SELECT DISTINCT s.name " +
            "FROM Student s," +
            " IN(s.department) d" +
            " WHERE d.name = 'CompSci2'";

        List ls = em.createQuery(colldec).getResultList();

        assertNotNull(ls);
        assertEquals(2, ls.size());
        assertTrue(ls.contains("Jonathan"));
        assertTrue(ls.contains("Stam"));

        endEm(em);
    }

    /**
     * GroupBy , OrderBy clause is tested by testejbqlfunction under
     * functional directory.
     */
}
