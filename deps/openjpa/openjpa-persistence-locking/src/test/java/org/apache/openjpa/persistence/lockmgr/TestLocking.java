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
package org.apache.openjpa.persistence.lockmgr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.LockModeType;
import javax.persistence.PessimisticLockScope;

import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestLocking extends SQLListenerTestCase {
    String _phone = "5075555555";

    @Override
    protected String getPersistenceUnitName() {
        return "locking-test";
    }

    public void setUp() {
        super.setUp(CLEAR_TABLES, Person.class, PhoneNumber.class
        // ,"openjpa.Log", "SQL=trace"
            );
        populate();
    }

    public void testExtendedLockScope() throws Exception {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("javax.persistence.lock.scope", PessimisticLockScope.EXTENDED);

        OpenJPAEntityManagerSPI em1 = emf.createEntityManager();
        OpenJPAEntityManagerSPI em2 = emf.createEntityManager();
        CommitterThread committer = new CommitterThread(em2);

        em1.getTransaction().begin();
        Person e1 = em1.find(Person.class, 1);
        assertEquals(1, e1.getPhoneNumbers().size());

        // This SHOULD lock Employee with id=1 AND the join table.
        // 
        // pg 86
        // Element collections and relationships owned by the entity that are contained in join tables will be
        // locked if the javax.persistence.lock.scope property is specified with a value of
        // PessimisticLockScope.EXTENDED. The state of entities referenced by such relationships will
        // not be locked (unless those entities are explicitly locked). This property may be passed as an argument
        // to the methods of the EntityManager, Query, and TypedQuery interfaces that allow lock modes
        // to be specified or used with the NamedQuery annotation.

        em1.refresh(e1, LockModeType.PESSIMISTIC_FORCE_INCREMENT, props);

        // Kick off the committer thread
        committer.start();

        // Make sure to sleep at least for 5 seconds AFTER the committer calls commit
        while (System.currentTimeMillis() - committer.sleepStartTime < 5000) {
            Thread.sleep(5000);
        }
        // The committer should still be waiting because the em1.refresh(...) call should have locked the join table and
        // the remove can't complete
        assertFalse(committer.commitComplete);
        em1.getTransaction().commit();
        em1.close();
        // wait for child thread to finish
        committer.join();
    }

    private class CommitterThread extends Thread {
        OpenJPAEntityManagerSPI _em2;
        boolean inCommit = false;
        boolean commitComplete = false;
        long sleepStartTime = Long.MAX_VALUE;

        public CommitterThread(OpenJPAEntityManagerSPI e) {
            _em2 = e;
        }

        @Override
        public void run() {
            _em2.getTransaction().begin();
            PhoneNumber phoneNumber = _em2.find(PhoneNumber.class, _phone);
            _em2.remove(phoneNumber);
            inCommit = true;
            sleepStartTime = System.currentTimeMillis();
            _em2.getTransaction().commit();
            commitComplete = true;
            _em2.close();
        }
    }

    private void populate() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        em.getTransaction().begin();

        PhoneNumber p = new PhoneNumber(_phone);
        List<PhoneNumber> numbers = Arrays.asList(new PhoneNumber[] { p });

        Person e1 = new Person();
        e1.setId(1);
        e1.setPhoneNumbers(numbers);
        Person e2 = new Person();
        e2.setId(2);
        e2.setPhoneNumbers(numbers);

        p.setOwners(Arrays.asList(new Person[] { e1, e2 }));
        em.persist(e1);
        em.persist(e2);
        em.persist(p);

        em.getTransaction().commit();
        em.close();
    }
}
