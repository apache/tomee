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

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestDetachment extends SingleEMFTestCase {

    private int id;

    public void setUp() {
        super.setUp(Record.class);
        id = prepare();
    }

    public void testAttachWithNewString() {
        // set up record with string "default" as content
        Record record = detach(id);

        // set different text
        record.setContent("a text different than the one in the record");

        attach(record);
    }

    public void testSetSameStringInstance() {
        Record record = detach(id);

        // same text, same String instance
        record.setContent(record.getContent());

        attach(record);
    }

    public void testSetSameString() {
        Record record = detach(id);
        // same text, different String instance
        record.setContent(record.getContent() + "");

        attach(record);
    }

    /**
     * Creates a new record, sets the content to "default" and returns the id.
     */
    private int prepare() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Record record = new Record();
        record.setContent("default");
        em.persist(record);
        em.getTransaction().commit();
        em.close();
        return record.getId();
    }

    /**
     * Fetches the record with the given id and returns a detached instance.
     */
    private Record detach(int id) {
        EntityManager em = emf.createEntityManager();
        Record record = em.find(Record.class, id);
        em.close(); // detach
        return record;
    }

    /**
     * Merges the record into a new persistence context.
     */
    private void attach(Record record) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        record = em.merge(record);
        em.getTransaction().commit();
        em.close();
    }
}
