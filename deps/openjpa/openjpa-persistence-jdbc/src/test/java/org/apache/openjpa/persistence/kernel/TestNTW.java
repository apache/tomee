/*
 * TestNTW.java
 *
 * Created on October 12, 2006, 2:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
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
package org.apache.openjpa.persistence.kernel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;

public class TestNTW extends BaseKernelTest {

    private OpenJPAEntityManagerFactory factory;

    /**
     * Creates a new instance of TestNTW
     */
    public TestNTW() {
    }

    public TestNTW(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        deleteAll(RuntimeTest1.class);

        OpenJPAEntityManager em = getPM();
        startTx(em);
        em.persist(new RuntimeTest1("ntw0", 0));
        em.persist(new RuntimeTest1("ntw1", 1));
        em.persist(new RuntimeTest1("ntw2", 2));
        em.persist(new RuntimeTest1("ntw3", 3));
        em.persist(new RuntimeTest1("ntw4", 4));
        endTx(em);
        endEm(em);

        em = getPM();
        startTx(em);
    }

    public void testNonTransactionalWrite() throws Exception {
        OpenJPAEntityManagerFactory factory = broker();
        OpenJPAEntityManager em = factory.createEntityManager();
        OpenJPAQuery q = em.createQuery(
            "SELECT o FROM RuntimeTest1 o ORDER BY o.stringField ASC");
        q.setSubclasses(false);
        Collection c = (Collection) q.getResultList();

        Iterator iter = c.iterator();
        RuntimeTest1 o;
        while (iter.hasNext()) {
            o = (RuntimeTest1) iter.next();
            o.setStringField(o.getStringField() + " modified");
        }

        startTx(em);
        endTx(em);

        q = em.createQuery(
            "SELECT o FROM RuntimeTest1 o ORDER BY o.stringField ASC");
        q.setSubclasses(false);
        Collection results = (Collection) q.getResultList();
        assertEquals(5, results.size());
        endEm(em);
    }

    private OpenJPAEntityManagerFactory broker() {
        Map map = new HashMap();
        map.put("OpenJPA.Optimistic", "true");
        map.put("OpenJPA.NontransactionalRead", "true");
        map.put("OpenJPA.NontransactionalWrite", "true");

        if (factory == null) {
            factory = OpenJPAPersistence
                .createEntityManagerFactory("TestConv", null, map);
        }
        return factory;
    }
}
