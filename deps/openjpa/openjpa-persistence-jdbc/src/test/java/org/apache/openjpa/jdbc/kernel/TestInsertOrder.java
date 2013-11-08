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
package org.apache.openjpa.jdbc.kernel;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Test that insert order is preserved when using the ConstraintUpdateManager
 * for entities which are not annotated with ForeignKey constraints.
 */
public class TestInsertOrder extends SQLListenerTestCase {
    private String empTableName;
    private String taskTableName;
    private String storyTableName;

    public void setUp() {
        setUp(Employee.class, Task.class, Story.class);
        empTableName = getMapping(Employee.class).getTable().getFullName();
        taskTableName = getMapping(Task.class).getTable().getFullName();
        storyTableName = getMapping(Story.class).getTable().getFullName();
    }

    /**
     * <P>Persist an Employee entity and allow the cascade to insert the
     * children. The inserts should be executed in this order, Employee, Task,
     * Story.
     * </P>
     * 
     * <P> 
     * Originally this test would pass in some scenarios. I believe the order 
     * relied on the hashcode of the underlying entities. 
     * </P>
     */
    public void testCascadePersist() {
        Employee e = newTree(10);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        em.close();

        assertAllSQLInOrder("INSERT INTO " + empTableName + ".*", "INSERT INTO "
            + taskTableName + ".*", "INSERT INTO " + storyTableName + ".*");
    }
    
    /**
     * Merge an Employee entity and allow the cascade to insert the children.
     * The inserts should be executed in this order, Employee, Task, Story.
     */
    public void testCascadeMerge() {
        Employee e = newTree(11);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(e);
        em.getTransaction().commit();
        em.close();

        assertAllSQLInOrder("INSERT INTO " + empTableName + ".*", "INSERT INTO "
            + taskTableName + ".*", "INSERT INTO " + storyTableName + ".*");
    }


    /**
     * Helper to create a tree of entities
     * 
     * @param id
     *            ID for the entities.
     * @return an unmanaged Employee instance with the appropriate relationships
     *         set.
     */
    private Employee newTree(int id) {
        Employee e = new Employee();
        e.setId(id);

        Task t = new Task();
        t.setId(id);

        Story s = new Story();
        s.setId(id);

        Collection<Task> tasks = new ArrayList<Task>();
        tasks.add(t);

        Collection<Story> stories = new ArrayList<Story>();
        stories.add(s);

        e.setTasks(tasks);
        t.setEmployee(e);

        t.setStories(stories);
        s.setTask(t);

        return e;
    }
}
