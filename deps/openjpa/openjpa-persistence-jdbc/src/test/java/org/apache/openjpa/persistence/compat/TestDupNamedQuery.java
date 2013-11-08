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
package org.apache.openjpa.persistence.compat;

import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;

import junit.textui.TestRunner;
import org.apache.openjpa.conf.OpenJPAVersion;
import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * <b>Compatible testcases</b> are used to test various backwards compatibility scenarios between JPA 2.0 and JPA 1.2
 * 
 * <p>The following scenarios are tested:
 * <ol>
 * <li>query.setParameter()
 * </ol>
 * <p> 
 * <b>Note(s):</b>
 * <ul>
 * <li>The proper openjpa.Compatibility value(s) must be provided in order for the testcase(s) to succeed
 * </ul>
 */
public class TestDupNamedQuery extends SingleEMFTestCase {

    public void setUp() {
        setUp(SimpleEntity.class, SimpleEntity2.class, CLEAR_TABLES);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(new SimpleEntity("Name One", "Value One"));
        em.persist(new SimpleEntity("Name Two", "Value Two"));
        em.persist(new SimpleEntity2("Name2 One", "Value2 One"));
        em.persist(new SimpleEntity2("Name2 Two", "Value2 Two"));
        em.getTransaction().commit();
        em.close();
    }

    public void testSimpleQuery() {
        simpleQuery(false, "FindOne", "FindAll", "Name One", "Value One",
            "Name Two", "Value Two");
        simpleQuery(true, "Find2One", "Find2All", "Name2 One", "Value2 One",
            "Name2 Two", "Value2 Two");

        simpleQuery(false, "FindXTwo", null, "Name Two", "Value Two", null,
            null);
    }

    private void simpleQuery(boolean simple2, String findOneQName,
        String findAllQName, String nameOne, String ValueOne, String nameTwo,
        String ValueTwo) {
        EntityManager em = emf.createEntityManager();

        try {
            List list = em.createNamedQuery(findOneQName).setParameter(1, nameOne).getResultList();
            assertNotNull(list);
            assertEquals(list.size(), 1);
            Object o = list.get(0);
            assertSame(o.getClass(), simple2 ? SimpleEntity2.class : SimpleEntity.class);
            assertEquals(simple2 ? ((SimpleEntity2) o).getValue()
                : ((SimpleEntity) o).getValue(), ValueOne);

            if (findAllQName != null) {
                list = em.createNamedQuery(findAllQName).getResultList();
                assertNotNull(list);
                assertEquals(list.size(), 2);
                for (Iterator resultIter = list.iterator(); resultIter.hasNext();) {
                    o = resultIter.next();
                    assertSame(o.getClass(), simple2 ? SimpleEntity2.class
                        : SimpleEntity.class);
                    String n = null;
                    String v = null;
                    if (simple2) {
                        n = ((SimpleEntity2) o).getName();
                        v = ((SimpleEntity2) o).getValue();
                    } else {
                        n = ((SimpleEntity) o).getName();
                        v = ((SimpleEntity) o).getValue();
                    }
                    if (n.equals(nameOne)) {
                        assertTrue(v.equals(ValueOne));
                    } else if (n.equals(nameTwo)) {
                        assertTrue(v.equals(ValueTwo));
                    } else {
                        assertTrue(false);
                    }
                }
            }
        } catch (ArgumentException ae) {
            if ((OpenJPAVersion.MAJOR_RELEASE == 1) &&
                 (OpenJPAVersion.MINOR_RELEASE >= 3)) {
                // new behavior - expected exception
            } else {
                // unexpected exception
               throw ae;
            }
        } catch (IllegalArgumentException iae) {
            if (OpenJPAVersion.MAJOR_RELEASE >= 2) {
                // expected exception for new behavior
            } else {
                // unexpected exception
                throw iae;
            }
        } finally {
            em.close();
        }
    }

    public static void main(String[] args) {
        TestRunner.run(TestDupNamedQuery.class);
    }
}

