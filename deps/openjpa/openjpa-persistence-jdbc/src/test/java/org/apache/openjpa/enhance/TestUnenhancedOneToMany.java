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
package org.apache.openjpa.enhance;

import java.lang.reflect.Proxy;
import java.util.Collection;

import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestUnenhancedOneToMany extends SingleEMTestCase {

    public void setUp() {
        setUp(UnenhancedOne.class, UnenhancedMany.class, CLEAR_TABLES);
    }

    public void testOneToMany() throws Exception {
        assertFalse(PersistenceCapable.class.isAssignableFrom(
            UnenhancedOne.class));
        assertFalse(PersistenceCapable.class.isAssignableFrom(
            UnenhancedMany.class));

        em.getTransaction().begin();

        UnenhancedOne one = new UnenhancedOne(1000);

        UnenhancedMany manyA = new UnenhancedMany(1);
        one.getMany().add(manyA);
        manyA.setOne(one);

        UnenhancedMany manyB = new UnenhancedMany(2);
        one.getMany().add(manyB);
        manyB.setOne(one);

        UnenhancedMany manyC = new UnenhancedMany(3);
        one.getMany().add(manyC);
        manyC.setOne(one);

        // em should not know about our entities
        assertFalse(em.contains(one));
        assertFalse(em.contains(manyA));
        assertFalse(em.contains(manyB));
        assertFalse(em.contains(manyC));

        // persist the entity
        em.persist(one);
        em.persist(manyA);
        em.persist(manyB);
        em.persist(manyC);
        em.flush();

        // em should now be aware of our entity
        assertTrue(em.contains(one));
        assertTrue(em.contains(manyA));
        assertTrue(em.contains(manyB));
        assertTrue(em.contains(manyC));

        em.getTransaction().commit();

        // recreate entity manager to avoid caching
        one = null;
        manyA = null;
        manyB = null;
        manyC = null;
        em.close();
        em = emf.createEntityManager();
        em.getTransaction().begin();

        // reload one
        one = em.find(UnenhancedOne.class, 1000);
        assertNotNull("one is null", one);

        // verify one.getMany(); ensure that it's a dynamic proxy before
        // it is accessed
        assertTrue(Proxy.isProxyClass(one.many.getClass()));
        assertNotNull("one.getMany() is null", one.getMany());
        Collection<UnenhancedMany> many = one.getMany();
        assertEquals(3, many.size());

        // reload the many
        manyA = em.find(UnenhancedMany.class, 1);
        assertNotNull("manyA is null", manyA);
        manyB = em.find(UnenhancedMany.class, 2);
        assertNotNull("manyB is null", manyA);
        manyC = em.find(UnenhancedMany.class, 3);
        assertNotNull("manyc is null", manyA);

        // verify many.getOne()
        assertNotNull("manyA.getOne() is null", manyA.getOne());
        assertEquals(one, manyA.getOne());
        assertNotNull("manyB.getOne() is null", manyB.getOne());
        assertEquals(one, manyB.getOne());
        assertNotNull("manyC.getOne() is null", manyC.getOne());
        assertEquals(one, manyC.getOne());

        // verify collection contains each many
        assertTrue(many.contains(manyA));
        assertTrue(many.contains(manyB));
        assertTrue(many.contains(manyC));

        em.getTransaction().commit();
    }
}
