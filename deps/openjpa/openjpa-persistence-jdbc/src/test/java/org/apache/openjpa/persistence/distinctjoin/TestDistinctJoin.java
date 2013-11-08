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
package org.apache.openjpa.persistence.distinctjoin;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.lib.jdbc.JDBCListener;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;
import org.apache.openjpa.persistence.test.FilteringJDBCListener;


/**
 * This test case demonstrates that we currently do way too much sub selects
 * if &#064;Embedded fields with a &#064;Lob are involved.
 *
 * For running the test you can use the following commandline in
 * openjpa-persistence-jdbc:
 *
 */
public class TestDistinctJoin extends AbstractPersistenceTestCase {

    protected List<String> sql = new ArrayList<String>();

    private Log log;

    @Override
    public void setUp() throws SQLException {
        OpenJPAEntityManagerFactorySPI emf = createEMF();

        log = emf.getConfiguration().getLog("Tests");

        emf.close();
    }

    /**
     * Gets the number of SQL issued since last reset.
     */
    public int getSQLCount() {
        return sql.size();
    }

    /**
     * Resets SQL count.
     * @return number of SQL counted since last reset.
     */
    public int resetSQL() {
        int tmp = sql.size();
        sql.clear();
        return tmp;
    }

    public void testJoinOnly() throws SQLException {

        log.error("THIS TEST IS CURRENTLY DISABLED!");
        if (true) return;


        OpenJPAEntityManagerFactorySPI emf =
            createEMF(Course.class, Lecturer.class, LocalizedText.class,
                "openjpa.jdbc.SchemaFactory", "native",
                "openjpa.jdbc.SynchronizeMappings",  "buildSchema(ForeignKeys=true)",
                "openjpa.jdbc.QuerySQLCache", "false",
                "openjpa.DataCache", "false",
                "openjpa.jdbc.JDBCListeners", new JDBCListener[] { new FilteringJDBCListener(sql) }
        );

        Long id;

        {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tran = em.getTransaction();
            tran.begin();
            em.createQuery("DELETE from Lecturer as l").executeUpdate();
            em.createQuery("DELETE from Course as c").executeUpdate();
            tran.commit();
            em.close();

        }

        {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tran = em.getTransaction();
            tran.begin();

            Course course = new Course();
            LocalizedText objective = new LocalizedText();
            objective.setTextDe("de-objective");
            objective.setTextEn("en-objective");
            course.setObjective(objective);

            LocalizedText title = new LocalizedText();
            title.setTextDe("title-de");
            title.setTextEn("title-en");
            course.setObjective(title);

            course.setLobColumn("oh this could be a very looooong text...");
            course.setCourseNumber("4711");

            em.persist(course);

            Lecturer l1 = new Lecturer();
            l1.setCourse(course);
            course.addLecturer(l1);

            id = course.getId();
            tran.commit();
            em.close();
        }

        {
            EntityManager em = emf.createEntityManager();
            Course course = em.find(Course.class, id);
            assertNotNull(course);

            em.close();
        }

        String msg;

        {
            msg = "Distinct and Join";
            log.info("\n\n" + msg); // this one does sub-selects for LocalizedString and changeLog
            EntityManager em = emf.createEntityManager();
            EntityTransaction tran = em.getTransaction();
            tran.begin();
            resetSQL();

            Query q = em.createQuery("select distinct c from Course c join  c.lecturers l ");
            List<Course> courses = q.getResultList();
            assertFalse(courses.isEmpty());
            assertNotNull(courses.get(0));
            assertMaxQueries(msg, 2);

            tran.commit();
            em.close();
        }
        
        {
            msg = "Distinct"; // creates NO sub-query!
            log.info("\n\n" + msg);
            EntityManager em = emf.createEntityManager();

            Query q = em.createQuery("select distinct c from Course c");
            List<Course> courses = q.getResultList();
            assertFalse(courses.isEmpty());
            assertNotNull(courses.get(0));
            assertMaxQueries(msg, 2);

            em.close();
        }
        
        {
            msg = "Join"; // creates NO sub-query!
            log.info("\n\n" + msg);
            EntityManager em = emf.createEntityManager();

            Query q = em.createQuery("select c from Course c join c.lecturers l ");
            List<Course> courses = q.getResultList();
            assertFalse(courses.isEmpty());
            assertNotNull(courses.get(0));
            assertMaxQueries(msg, 2);

            em.close();
        }
        
        {
            msg = "Distinct inverse join"; // this one does sub-selects for LocalizedString and changeLog
            log.info("\n\n" + msg);
            EntityManager em = emf.createEntityManager();

            Query q = em.createQuery("select distinct c from Lecturer l join l.course c");
            List<Course> courses = q.getResultList();
            assertFalse(courses.isEmpty());
            assertNotNull(courses.get(0));
            assertMaxQueries(msg, 2);

            em.close();
        }
        
        {
            msg = "Inverse join"; // this one does sub-selects for LocalizedString and changeLog
            log.info("\n\n" + msg);
            EntityManager em = emf.createEntityManager();

            Query q = em.createQuery("select c from Lecturer l join l.course c");
            List<Course> courses = q.getResultList();
            assertFalse(courses.isEmpty());
            assertNotNull(courses.get(0));
            assertMaxQueries(msg, 2);

            em.close();
        }


        emf.close();
    }

    private void assertMaxQueries(String msg, int queriesAllowed) {
        int queryCount = getSQLCount();
        if (queryCount > queriesAllowed) {
            StringBuilder sb = new StringBuilder("The following queries got executed\n");
            for (String query : sql) {
                sb.append(query).append('\n');
            }
            log.error(sb.toString());

            fail("got too many queries executed(" + queryCount + ") but only " + queriesAllowed+ " expected in " + msg);
        }
        resetSQL();
    }
}
