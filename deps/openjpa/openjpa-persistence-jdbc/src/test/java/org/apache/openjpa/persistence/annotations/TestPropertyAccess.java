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
package org.apache.openjpa.persistence.annotations;


import org.apache.openjpa.persistence.OpenJPAEntityManager;

import
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.*;

import org.apache.openjpa.persistence.test.AllowFailure;

@AllowFailure(message="excluded")
public class TestPropertyAccess extends AnnotationTestCase
{
	public TestPropertyAccess(String name)
	{
		super(name, "annotationcactusapp");
	}

    public void setUp() {
        deleteAll(PropertyAccess1.class);
    }

    public void testPropertyAccessBasicCreation() {
        OpenJPAEntityManager em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        PropertyAccess1 pa1_1 = new PropertyAccess1(10);
        pa1_1.setName("foo");
        em.persist(pa1_1);
        endTx(em);

        // getting a new EM should not be necessary once the extended PC stuff
        // is complete.
        em = (OpenJPAEntityManager) currentEntityManager();

        PropertyAccess1 pa1_2 = em.find(PropertyAccess1.class, 10);
        assertNotSame(pa1_1, pa1_2);
        assertNotNull(pa1_2);
        assertEquals(10, pa1_2.getId());
        assertEquals("foo", pa1_2.getName());
    }

    public void testPropertyAccessBasicMutation() {
        OpenJPAEntityManager em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        PropertyAccess1 pa1_1 = new PropertyAccess1(10);
        pa1_1.setName("foo");
        em.persist(pa1_1);
        endTx(em);

        // getting a new EM should not be necessary once the extended PC stuff
        // is complete.
        em = (OpenJPAEntityManager) currentEntityManager();

        startTx(em);
        PropertyAccess1 pa1_2 = em.find(PropertyAccess1.class, 10);
        pa1_2.setName(pa1_2.getName() + "bar");
        endTx(em);

        em = (OpenJPAEntityManager) currentEntityManager();
        PropertyAccess1 pa1_3 = em.find(PropertyAccess1.class, 10);
        assertNotSame(pa1_2, pa1_3);
        assertEquals("foobar", pa1_3.getName());
    }

    public void testJPQL() {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        em.createQuery("select o from PropertyAccess1 o where " +
            "o.name = 'foo'").getResultList();
        em.createQuery("select o from PropertyAccess1 o order by " +
            "o.name asc").getResultList();
        endEm(em);
    }

    public void testJPQLWithFieldNameMismatch() {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        em.createQuery("select o from PropertyAccess1 o where " +
            "o.intValue = 0").getResultList();
        em.createQuery("select o from PropertyAccess1 o order by " +
            "o.intValue asc").getResultList();
        endEm(em);
    }

    /*public void testJDOQL() {
        PersistenceManager pm = getPM();
        pm.newQuery("select from persistence.annotations.common.apps.annotApp."
            + "annotype.PropertyAccess1 where name == 'foo'").execute();
        pm.newQuery("select from persistence.annotations.common.apps.annotApp."
            + "annotype.PropertyAccess1 order by name ascending").execute();
        pm.close();
    }

    public void testJDOQLWithFieldNameMismatch() {
        PersistenceManager pm = getPM();
        pm.newQuery("select from persistence.annotations.common.apps.annotApp."
            + "annotype.PropertyAccess1 where intValue == 0").execute();
        pm.newQuery("select from persistence.annotations.common.apps.annotApp."
            + "annotype.PropertyAccess1 order by intValue asc").execute();
        pm.close();
    }*/
}
