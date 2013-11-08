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
package org.apache.openjpa.conf;

import java.util.*;

import javax.persistence.*;

import junit.framework.*;

import org.apache.openjpa.persistence.*;

public class TestGetProperty extends TestCase {
    private EntityManagerFactory emf;

    public void setUp() throws Exception {
        emf = (OpenJPAEntityManagerFactory) Persistence
                .createEntityManagerFactory("test");
    }

    public void tearDown() throws Exception {
        emf.close();
        emf = null;
    }

    public void testGetProperty() {
        try {
            Collection<Thread> tests = new ArrayList<Thread>();
            for (int i = 0; i < 10; i++) {
                Test test = new Test();
                test.start();
                tests.add(test);
            }

            for (Thread test : tests)
                test.join();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    class Test extends Thread {
        EntityManager em;

        public Test() {
            this.em = emf.createEntityManager();
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                em.getProperties();
            }
            if (em != null && em.isOpen())
                em.close();
        }
    }
}
