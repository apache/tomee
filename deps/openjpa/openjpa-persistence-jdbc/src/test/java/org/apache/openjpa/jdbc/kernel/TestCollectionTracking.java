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

import java.util.*;
import javax.persistence.*;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestCollectionTracking extends SingleEMFTestCase {

    private JDBCConfiguration _conf;

    public void setUp() {
        super.setUp(A.class,"openjpa.Compatibility","autoOff=false");
    }

    public void testCollectTracking() {
        int initialValue = 10, changedAge = 40;
        String changedName = "changed Name";

        int incrementValue = 3;
        modifyA(initialValue, incrementValue, changedAge, changedName);
        assertA(initialValue, incrementValue, changedAge, changedName);

        incrementValue = 8;
        modifyA(initialValue, incrementValue, changedAge, changedName);
        assertA(initialValue, incrementValue, changedAge, changedName);
    }

    private void assertA(int initialValue, int incrementValue, int changedAge,
        String changedName) {
        EntityManager em3 = emf.createEntityManager();
        Query query3 =
            em3.createQuery("select emp from A as emp where emp.id = :id")
                .setParameter("id", 1);
        A changedA = (A) query3.getSingleResult();
        assertEquals(changedAge,changedA.getAge());
        assertEquals(changedName,changedA.getName());
        assertEquals(initialValue + incrementValue,changedA.getMap().size());
        em3.close();
    }

    private void modifyA(int initialvalue, int num, int changedAge,
        String changedName) {

        clearTables();

        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();

        /** EM1 create A */
        em1.getTransaction().begin();
        A newA = new A();
        newA.setId(1);
        newA.setAge(30);
        newA.setName("Initial");
        for (int i = 0; i < initialvalue; i++) {
            newA.getMap().put("key:" + i, "value:" + i);
        }
        em1.persist(newA);
        em1.getTransaction().commit();

        /* Start em1 transaction setting age to 40 */
        em1.getTransaction().begin();
        em1.merge(newA);
        newA.setAge(changedAge);
        Iterator it = newA.getMap().entrySet().iterator();
        for (int i = 0; i < num; i++) {
            it.next();
            it.remove();
        }

        for (int i = initialvalue; i < initialvalue + num; i++) {
            newA.getMap().put("key:" + i, "value:" + i);
        }

        /* Start em2 transaction setting name to Changed */
        em2.getTransaction().begin();
        Query query2 =
            em2.createQuery("select emp from A as emp where emp.id = :id")
                .setParameter("id", 1);
        A result2 = (A) query2.getSingleResult();
        result2.setName(changedName);
        for (int i = initialvalue + num; i < initialvalue + num + num; i++) {
            result2.getMap().put("key:" + i, "value:" + i);
        }
        em2.getTransaction().commit();
        em1.getTransaction().commit();

        em2.close();
        em1.close();
    }

    private void clearTables() {

        EntityManager em1 = emf.createEntityManager();
        em1.getTransaction().begin();
        Query query = em1.createNativeQuery("delete from JPA_A");
        query.executeUpdate();
        query = em1.createNativeQuery("delete from JPA_A_MAPS_C");
        query.executeUpdate();
        em1.getTransaction().commit();
        em1.close();
    }
}
