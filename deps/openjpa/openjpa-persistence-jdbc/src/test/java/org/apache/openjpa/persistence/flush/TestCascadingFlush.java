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
package org.apache.openjpa.persistence.flush;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestCascadingFlush extends SingleEMFTestCase {

    boolean supportsNativeSequence = false;

    public void setUp() {
        setUp(Assignment.class, ClassPeriod.class, Course.class, SubTopic.class, Topic.class, CLEAR_TABLES);

        try {
            supportsNativeSequence = ((JDBCConfiguration) emf
                .getConfiguration()).getDBDictionaryInstance()
                .nextSequenceQuery != null;
        } catch (Throwable t) {
            supportsNativeSequence = false;
        }
    }

    /**
     * Verifies flushing a complex bidirectional domain model results in retrieval and population of all sequence-gen
     * ID values.
     */
    public void testCascadingFlushBasic() {
        if (!supportsNativeSequence) {
            return;
        }
        EntityManager em = emf.createEntityManager();
        Long courseId = populate(em);
        em.clear();
        Course course = em.find(Course.class, courseId);
        verifyCascadingFlush(em, course);
        em.close();
    }

    /**
     * Verifies flushing a complex bidirectional domain model results in retrieval and population of all sequence-gen
     * ID values using an detached, then merged entity graph.
     */
    public void testCascadingFlushDetach() {
        if (!supportsNativeSequence) {
            return;
        }
        EntityManager em = emf.createEntityManager();
        Long courseId = populate(em);
        em.clear();
        Course tmpCourse = em.find(Course.class, courseId);

        Course course = OpenJPAPersistence.cast(em).detachCopy(tmpCourse);
        assertNotEquals(course, tmpCourse);
        verifyCascadingFlush(em, course);
        em.close();
    }

    /**
     * Verifies flushing a complex bidirectional domain model results in retrieval of all sequence-gen ID values using
     * an serialized (resulting in detach), then merged entity graph.
     */
    public void testCascadingFlushSerialize() {
        if (!supportsNativeSequence) {
            return;
        }
        EntityManager em = emf.createEntityManager();
        try {
            Long courseId = populate(em);
            em.clear();
            Course tmpCourse = em.find(Course.class, courseId);
            Course course = null;
            try {
                course = (Course)roundtrip(tmpCourse, false);
            } catch (Throwable t) {
                fail("Failed to serialize and deserialize persistent object.");
            }
            assertNotEquals(course, tmpCourse);
            verifyCascadingFlush(em, course);
        }
        finally {
            if (em != null) {
                em.close();
            }
        }
    }


    private void verifyCascadingFlush(EntityManager em, Course course) {
        try {
            beginTx(em);
            // Add a class period to the graph
            addClassPeriod(course);

            // Merge in the new entities
            Course course2 = em.merge(course);
            // Flush to the database.  ID's should be assigned to all elements in
            // the graph.
            em.flush();
            // Verify all id's are assigned
            assertTrue(course2.getCourseId() > 0);
            assertNotNull(course2.getClassPeriods());
            Set<ClassPeriod> cps = course2.getClassPeriods();
            assertTrue(cps.size() == 2);
            for (ClassPeriod cp : cps) {
                assertNotNull(cp);
                assertTrue(cp.getClassPeriodId() > 0);
                assertEquals(cp.getCourse(), course2);
                Set<Topic> topics = cp.getTopics();
                assertNotNull(topics);
                assertTrue(topics.size() > 0);
                for (Topic t : topics) {
                    assertNotNull(t);
                    assertTrue(t.getTopicId() > 0);
                    Set<Assignment> assignments = t.getAssignments();
                    assertNotNull(assignments);
                    assertTrue(assignments.size() == 1);
                    for (Assignment a : assignments) {
                        assertNotNull(a);
                        assertTrue(a.getAssignmentId() > 0);
                    }
                    Set<SubTopic> subTopics = t.getSubTopics();
                    assertNotNull(subTopics);
                    assertTrue(subTopics.size() == 1);
                    for (SubTopic s : subTopics) {
                        assertNotNull(s);
                        assertTrue(s.getSubtopicId() > 0);
                    }
                }
            }

            commitTx(em);
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
            fail();
        }
    }

    public static Long populate(EntityManager em) {

        beginTx(em);
        Course course = createNewCourse();
        em.persist(course);
        em.flush();

        commitTx(em);
        return course.getCourseId();
    }

    public static Course createNewCourse() {

        Course course = new Course();
        course.setCourseText("Nuclear Physics");

        Assignment assignment1 = new Assignment();
        assignment1.setAssignmentText("Lab: Nuclear Fusion");

        Set<Assignment> assignments = new HashSet<Assignment>();
        assignments.add(assignment1);

        SubTopic subtopic1 = new SubTopic();
        subtopic1.setSubtopicText("Nuclear Fusion");

        Set<SubTopic> subtopics = new HashSet<SubTopic>();
        subtopics.add(subtopic1);

        Topic topic1 = new Topic();
        topic1.setTopicText("Fundamentals of Nuclear Energy");
        topic1.setAssignments(assignments);
        topic1.setSubTopics(subtopics);

        assignment1.setTopic(topic1);
        subtopic1.setTopic(topic1);

        Set<Topic> topics = new HashSet<Topic>();
        topics.add(topic1);

        ClassPeriod cp1 = new ClassPeriod();
        cp1.setClassPeriodText("8844: M,W,Th 8:00AM");
        cp1.setTopics(topics);
        cp1.setCourse(course);

        topic1.setClassPeriod(cp1);

        Set<ClassPeriod> cps = new HashSet<ClassPeriod>();
        cps.add(cp1);

        course.setClassPeriods(cps);

        return course;
    }

    public static void addClassPeriod(Course course) {

        
        Assignment assignment = new Assignment();
        assignment.setAssignmentText("Read pages 442-645");

        Set<Assignment> assignments = new HashSet<Assignment>();
        assignments.add(assignment);

        SubTopic subTopic = new SubTopic();
        subTopic.setSubtopicText("Newton");

        Set<SubTopic> subTopics = new HashSet<SubTopic>();
        subTopics.add(subTopic);

        Topic topic = new Topic();
        topic.setTopicText("Gravity");
        topic.setSubTopics(subTopics);
        topic.setAssignments(assignments);

        assignment.setTopic(topic);
        subTopic.setTopic(topic);

        Set<Topic> topics = new HashSet<Topic>();
        topics.add(topic);

        // Add another topic
        Assignment assignment2 = new Assignment();
        assignment2.setAssignmentText("Read pages 645-785");

        Set<Assignment> assignments2 = new HashSet<Assignment>();
        assignments2.add(assignment2);

        SubTopic subTopic2 = new SubTopic();
        subTopic2.setSubtopicText("Forces");

        Set<SubTopic> subTopics2 = new HashSet<SubTopic>();
        subTopics2.add(subTopic2);

        Topic topic2 = new Topic();
        topic2.setTopicText("Magnetism");
        topic2.setSubTopics(subTopics2);
        topic2.setAssignments(assignments2);

        subTopic2.setTopic(topic);
        subTopic2.setTopic(topic);

        topics.add(topic2);

        ClassPeriod cp2 = new ClassPeriod();
        cp2.setClassPeriodText("8846: M,W,Th 11:00AM");
        cp2.setTopics(topics);
        cp2.setCourse(course);

        topic.setClassPeriod(cp2);
        topic2.setClassPeriod(cp2);

        course.getClassPeriods().add(cp2);
    }

    private static void beginTx(EntityManager em) {
        em.getTransaction().begin();
    }

    private static void commitTx(EntityManager em) {
        em.getTransaction().commit();
    }

    public static Object roundtrip(Object orig, boolean validateEquality)
        throws IOException, ClassNotFoundException {
        assertNotNull(orig);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(orig);
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bin);
        Object result = in.readObject();

        if (validateEquality) {
            assertEquals(orig.hashCode(), result.hashCode());
            assertEquals(orig, result);
        }

        return result;
    }
}
