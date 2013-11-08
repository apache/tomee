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
package org.apache.openjpa.persistence.query;

import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestResultSetMapping 
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(SimpleEntity.class);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(new SimpleEntity("tName", "tValue"));
        em.getTransaction().commit();
        em.close();
    }

    public void testSimpleQuery() {
        EntityManager em = emf.createEntityManager();
        Query q = em.createNamedQuery("findSimpleEntitites");
        List res = q.getResultList();
        assertNotNull(res);
        for (Iterator resultIter = res.iterator(); resultIter.hasNext();) {
            assertSame(resultIter.next().getClass(), SimpleEntity.class);
        }
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestResultSetMapping.class);
    }
}
