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
package org.apache.openjpa.persistence.inheritance.serializable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestSerialization extends SingleEMFTestCase {

    public void setUp() {
        setUp(Person.class, Employee.class, DROP_TABLES, "openjpa.DetachState",
                "fgs(DetachedStateField=true)");
    }

    /**
     * Simulates detaching and sending an entity to a remote client. The remote
     * client updates the detached entity and sends it back to the server. The
     * server merges the entity back into a persistence context and commits the
     * changes. 
     */
    public void testDetachedUpdate() {
        Employee emp = new Employee();
        emp.setFirstName("John");
        emp.setLastName("Doe");
        emp.setSalary(15000);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(emp);
        em.refresh(emp);
        int id = emp.getId();

        em.getTransaction().commit();

        // detach
        em.clear();
        emp.setFirstName("Jane");

        Employee deserialized = (Employee) serializeObject(emp);

        assertNotNull(deserialized);
        assertEquals(emp, deserialized);

        em.getTransaction().begin();
        em.merge(deserialized);
        em.getTransaction().commit();

        em.clear();
        emp = em.find(Employee.class, id);

        assertEquals(deserialized, emp);
        em.close();
    }

    /**
     * Helper to serialize an object to a byte[]
     */
    private Object serializeObject(Object orig) {
        Object deserialized = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(orig);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos
                    .toByteArray());
            ois = new ObjectInputStream(bais);

            deserialized = ois.readObject();            
        } catch (Exception e) {
            fail(e.toString());
        } finally {
            try {
                oos.close();
                ois.close();
            } catch (IOException e) {
                // ignore
            }
        }
        return deserialized;
    }
}
