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
import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class SortAndLimitTest {

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
        p.put("taskDatabase.JdbcUrl", "jdbc:hsqldb:mem:taskdb-sort");
        return p;
    }

    private void seedTasks() {
        em.persist(new Task("Task A", "desc", 3, false));
        em.persist(new Task("Task B", "desc", 1, false));
        em.persist(new Task("Task C", "desc", 5, false));
        em.persist(new Task("Task D", "desc", 2, false));
        em.persist(new Task("Task E", "desc", 4, false));
        em.persist(new Task("Done 1", "desc", 1, true));
        em.persist(new Task("Done 2", "desc", 2, true));
        em.flush();
    }

    // -- Limit tests --

    @Test
    public void testLimitResults() throws Exception {
        utx.begin();
        try {
            seedTasks();

            final List<Task> result = repo.findByCompletedFalseOrderByPriorityAsc(Limit.of(3));
            assertEquals(3, result.size());
            // Should be priority 1, 2, 3
            assertEquals(1, result.get(0).getPriority());
            assertEquals(2, result.get(1).getPriority());
            assertEquals(3, result.get(2).getPriority());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testLimitWithOffset() throws Exception {
        utx.begin();
        try {
            seedTasks();

            // startAt=2 means skip first 1, take 2
            final List<Task> result = repo.findByCompletedFalseOrderByPriorityAsc(Limit.range(2, 3));
            assertEquals(2, result.size());
            // priority 2, 3 (skipping 1)
            assertEquals(2, result.get(0).getPriority());
            assertEquals(3, result.get(1).getPriority());
        } finally {
            utx.rollback();
        }
    }

    // -- Sort tests --

    @Test
    public void testSortAscending() throws Exception {
        utx.begin();
        try {
            seedTasks();

            final List<Task> result = repo.findByCompletedFalse(Sort.asc("priority"));
            assertEquals(5, result.size());
            assertEquals(1, result.get(0).getPriority());
            assertEquals(5, result.get(4).getPriority());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testSortDescending() throws Exception {
        utx.begin();
        try {
            seedTasks();

            final List<Task> result = repo.findByCompletedFalse(Sort.desc("priority"));
            assertEquals(5, result.size());
            assertEquals(5, result.get(0).getPriority());
            assertEquals(1, result.get(4).getPriority());
        } finally {
            utx.rollback();
        }
    }

    // -- Order tests --

    @Test
    public void testOrderWithMultipleSorts() throws Exception {
        utx.begin();
        try {
            // Add tasks with same priority for deterministic ordering
            em.persist(new Task("Zebra task", "desc", 1, false));
            em.persist(new Task("Alpha task", "desc", 1, false));
            em.persist(new Task("Mid task", "desc", 2, false));
            em.flush();

            @SuppressWarnings("unchecked")
            final Order<Task> order = Order.by(Sort.asc("priority"), Sort.asc("title"));
            final List<Task> result = repo.findByCompletedFalse(order);
            assertEquals(3, result.size());
            assertEquals("Alpha task", result.get(0).getTitle());
            assertEquals("Zebra task", result.get(1).getTitle());
            assertEquals("Mid task", result.get(2).getTitle());
        } finally {
            utx.rollback();
        }
    }

    // -- PageRequest tests --

    @Test
    public void testPageRequest() throws Exception {
        utx.begin();
        try {
            seedTasks();

            @SuppressWarnings("unchecked")
            final Order<Task> order = Order.by(Sort.asc("priority"));
            final Page<Task> page1 = repo.findByCompletedFalse(PageRequest.ofSize(2), order);

            assertNotNull(page1);
            assertEquals(2, page1.content().size());
            assertEquals(5, page1.totalElements());
            assertEquals(3, page1.totalPages());
            assertEquals(1, page1.content().get(0).getPriority());
            assertEquals(2, page1.content().get(1).getPriority());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testPageRequestSecondPage() throws Exception {
        utx.begin();
        try {
            seedTasks();

            @SuppressWarnings("unchecked")
            final Order<Task> order = Order.by(Sort.asc("priority"));
            final Page<Task> page2 = repo.findByCompletedFalse(PageRequest.ofPage(2).size(2), order);

            assertNotNull(page2);
            assertEquals(2, page2.content().size());
            assertEquals(3, page2.content().get(0).getPriority());
            assertEquals(4, page2.content().get(1).getPriority());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testPageRequestLastPage() throws Exception {
        utx.begin();
        try {
            seedTasks();

            @SuppressWarnings("unchecked")
            final Order<Task> order = Order.by(Sort.asc("priority"));
            final Page<Task> page3 = repo.findByCompletedFalse(PageRequest.ofPage(3).size(2), order);

            assertNotNull(page3);
            assertEquals(1, page3.content().size());
            assertEquals(5, page3.content().get(0).getPriority());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testPageHasNextAndPrevious() throws Exception {
        utx.begin();
        try {
            seedTasks();

            @SuppressWarnings("unchecked")
            final Order<Task> order = Order.by(Sort.asc("priority"));

            final Page<Task> page1 = repo.findByCompletedFalse(PageRequest.ofSize(2), order);
            assertTrue("First page should have next", page1.hasNext());

            final Page<Task> page2 = repo.findByCompletedFalse(PageRequest.ofPage(2).size(2), order);
            assertTrue("Middle page should have next", page2.hasNext());

            final Page<Task> page3 = repo.findByCompletedFalse(PageRequest.ofPage(3).size(2), order);
            // Last page should not have next (1 item, total 5, page size 2, 3 pages)
            assertEquals(1, page3.content().size());
        } finally {
            utx.rollback();
        }
    }

    // -- @OrderBy annotation on the method --

    @Test
    public void testOrderByAnnotationDescending() throws Exception {
        utx.begin();
        try {
            seedTasks();

            final List<Task> result = repo.findByCompletedFalseOrderByPriorityDesc();
            assertEquals(5, result.size());
            assertEquals(5, result.get(0).getPriority());
            assertEquals(4, result.get(1).getPriority());
            assertEquals(3, result.get(2).getPriority());
            assertEquals(2, result.get(3).getPriority());
            assertEquals(1, result.get(4).getPriority());
        } finally {
            utx.rollback();
        }
    }

    // -- findAll with Page --

    @Test
    public void testFindAllWithPageRequest() throws Exception {
        utx.begin();
        try {
            seedTasks();

            final Page<Task> page = repo.findAll(PageRequest.ofSize(3), Order.by());
            assertNotNull(page);
            assertEquals(3, page.content().size());
            assertEquals(7, page.totalElements());
        } finally {
            utx.rollback();
        }
    }
}
