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
package org.apache.openjpa.persistence.detachment;

import javax.persistence.EntityManager;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test that manually constructing instances with existing primary key values
 * and attaching them works.
 *
 * @author Abe White
 */
public class TestAttachConstructedCopy
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(Record.class);
    }

    public void testAttachConstructedCopyWithGeneratedPKAndNoVersion() {
        Record record = new Record();
        record.setContent("orig");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(record);
        em.getTransaction().commit();
        em.close();
        int id = record.getId();

        Record copy = new Record();
        copy.setId(id);
        copy.setContent("new");

        em = emf.createEntityManager();
        em.getTransaction().begin(); 
        record = em.merge(copy);
        assertTrue(record != copy);
        assertEquals("new", record.getContent());
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        record = em.find(Record.class, id);
        assertEquals("new", record.getContent());
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestAttachConstructedCopy.class);
    }
}

