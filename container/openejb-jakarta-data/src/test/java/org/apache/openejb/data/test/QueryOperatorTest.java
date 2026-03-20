/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.data.test;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import org.apache.openejb.data.test.entity.Task;
import org.apache.openejb.data.test.repo.TaskRepository;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class QueryOperatorTest {

    @Inject
    private TaskRepository repo;

    @PersistenceContext
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    @Module
    @Classes(cdi = true, value = {TaskRepository.class})
    public EjbJar beans() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        final PersistenceUnit unit = new PersistenceUnit("task-unit");
        unit.setJtaDataSource("taskDatabase");
        unit.setNonJtaDataSource("taskDatabaseUnmanaged");
        unit.getClazz().add(Task.class.getName());
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        return unit;
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("taskDatabase", "new://Resource?type=DataSource");
        p.put("taskDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("taskDatabase.JdbcUrl", "jdbc:hsqldb:mem:taskdb-operators");
        return p;
    }

    private void seedTasks() {
        em.persist(new Task("Fix login bug", "Login page error", 3, false));
        em.persist(new Task("Fix logout bug", "Logout page error", 2, true));
        em.persist(new Task("Add feature X", "New feature", 5, false));
        em.persist(new Task("Add feature Y", "New feature", 1, false));
        em.persist(new Task("Refactor service", "Cleanup code", 4, true));
        em.flush();
    }

    @Test
    public void testStartsWith() throws Exception {
        utx.begin();
        try {
            seedTasks();

            final List<Task> result = repo.findByTitleStartsWith("Fix");
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(t -> t.getTitle().startsWith("Fix")));
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testEndsWith() throws Exception {
        utx.begin();
        try {
            seedTasks();

            final List<Task> result = repo.findByTitleEndsWith("bug");
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(t -> t.getTitle().endsWith("bug")));
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testBetween() throws Exception {
        utx.begin();
        try {
            seedTasks();

            final List<Task> result = repo.findByPriorityBetween(2, 4);
            assertEquals(3, result.size());
            assertTrue(result.stream().allMatch(t -> t.getPriority() >= 2 && t.getPriority() <= 4));
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testIn() throws Exception {
        utx.begin();
        try {
            seedTasks();

            final List<Task> result = repo.findByPriorityIn(List.of(1, 3, 5));
            assertEquals(3, result.size());
            assertTrue(result.stream().allMatch(t ->
                    t.getPriority() == 1 || t.getPriority() == 3 || t.getPriority() == 5));
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testOrConnector() throws Exception {
        utx.begin();
        try {
            seedTasks();

            // completed=true OR priority > 4
            final List<Task> result = repo.findByCompletedTrueOrPriorityGreaterThan(4);
            // completed=true: "Fix logout bug"(p2), "Refactor service"(p4) = 2
            // priority > 4: "Add feature X"(p5) = 1
            // union = 3
            assertEquals(3, result.size());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testTrue() throws Exception {
        utx.begin();
        try {
            seedTasks();

            final List<Task> result = repo.findByCompletedTrue();
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(Task::isCompleted));
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testFalse() throws Exception {
        utx.begin();
        try {
            seedTasks();

            final List<Task> result = repo.findByCompletedFalse();
            assertEquals(3, result.size());
            assertTrue(result.stream().noneMatch(Task::isCompleted));
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testNot() throws Exception {
        utx.begin();
        try {
            seedTasks();

            final List<Task> result = repo.findByTitleNot("Fix login bug");
            assertEquals(4, result.size());
            assertTrue(result.stream().noneMatch(t -> "Fix login bug".equals(t.getTitle())));
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testStartsWithAndFalse() throws Exception {
        utx.begin();
        try {
            seedTasks();

            // title starts with "Fix" AND completed=false
            final List<Task> result = repo.findByTitleStartsWithAndCompletedFalse("Fix");
            assertEquals(1, result.size());
            assertEquals("Fix login bug", result.get(0).getTitle());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testMultipleOrderClauses() throws Exception {
        utx.begin();
        try {
            seedTasks();

            // completed=false, order by priority DESC then title ASC
            final List<Task> result = repo.findByCompletedFalseOrderByPriorityDescTitleAsc();
            assertEquals(3, result.size());
            // priority 5 -> "Add feature X", priority 3 -> "Fix login bug", priority 1 -> "Add feature Y"
            assertEquals("Add feature X", result.get(0).getTitle());
            assertEquals("Fix login bug", result.get(1).getTitle());
            assertEquals("Add feature Y", result.get(2).getTitle());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testStartsWithNoMatch() throws Exception {
        utx.begin();
        try {
            seedTasks();

            final List<Task> result = repo.findByTitleStartsWith("ZZZ");
            assertTrue(result.isEmpty());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testBetweenSingleMatch() throws Exception {
        utx.begin();
        try {
            seedTasks();

            final List<Task> result = repo.findByPriorityBetween(5, 5);
            assertEquals(1, result.size());
            assertEquals(5, result.get(0).getPriority());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testInEmptyList() throws Exception {
        utx.begin();
        try {
            seedTasks();

            final List<Task> result = repo.findByPriorityIn(List.of());
            assertTrue(result.isEmpty());
        } finally {
            utx.rollback();
        }
    }
}
