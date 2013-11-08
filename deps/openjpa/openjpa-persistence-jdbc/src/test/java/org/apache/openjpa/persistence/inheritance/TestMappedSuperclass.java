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
package org.apache.openjpa.persistence.inheritance;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.inheritance.entity.BaseClass;
import org.apache.openjpa.persistence.inheritance.entity.MappedSuper;
import org.apache.openjpa.persistence.inheritance.entity.SubclassC;
import org.apache.openjpa.persistence.inheritance.entity.SubclassD;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * This test verifies basic Mapped Superclass functionality as dictated by
 * the JPA Specification contract. 
 * 
 * OpenJPA JIRA: {@link http://issues.apache.org/jira/browse/OPENJPA-1061}
 *
 */
public class TestMappedSuperclass extends SingleEMFTestCase {
    public void setUp() {
        setUp(BaseClass.class, SubclassC.class, MappedSuper.class,
                SubclassD.class);
    }

    private Class[] classArray(Class... classes) {
        return classes;
    }

    /**
     * Verify that two entity classes, extending a Mapped Superclass that
     * defines an ID field, are not members of a common inheritance
     * hierarchy.  This test variant inserts new entities into the persistence
     * context by calling EntityManager.persist() on the new entity object.
     */
    public void testMappedSuperclassContract001() {
        EntityManager em = emf.createEntityManager();

        // Create two entities, SubclassC and SubclassD, with the same
        // primary key value     
        SubclassC sc = new SubclassC();
        sc.setId(42);
        sc.setName("SubclassCMappedSuperName");
        sc.setClassCName("SubclassCName");

        em.getTransaction().begin();
        em.persist(sc);
        em.getTransaction().commit();

        SubclassD sd = new SubclassD();
        sd.setId(42);
        sd.setName("SubclassDMappedSuperName");
        sd.setClassDName("SubclassDName");

        // No EntityExistsException should be thrown by the persist
        em.getTransaction().begin();
        em.persist(sd);
        em.getTransaction().commit();
        em.close();
    }

    /**
     * Verify that two entity classes, extending a Mapped Superclass that
     * defines an ID field, are not members of a common inheritance
     * hierarchy.  This test variant inserts new entities into the persistence
     * context by calling EntityManager.merge() on the new entity object.
     */
    public void testMappedSuperclassContract002() {
        EntityManager em = emf.createEntityManager();

        // Create two entities, SubclassC and SubclassD, with the same
        // primary key value

        SubclassC sc = new SubclassC();
        sc.setId(43);
        sc.setName("SubclassCMappedSuperName");
        sc.setClassCName("SubclassCName");

        em.getTransaction().begin();
        em.merge(sc);
        em.getTransaction().commit();

        SubclassD sd = new SubclassD();
        sd.setId(43);
        sd.setName("SubclassDMappedSuperName");
        sd.setClassDName("SubclassDName");

        // No EntityExistsException should be thrown by the merge
        em.getTransaction().begin();
        em.merge(sd);
        em.getTransaction().commit();
        em.close();
    }
}
