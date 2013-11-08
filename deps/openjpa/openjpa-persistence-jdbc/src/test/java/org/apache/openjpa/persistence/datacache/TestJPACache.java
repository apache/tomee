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
package org.apache.openjpa.persistence.datacache;

import java.util.ArrayList;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.util.UserException;

public class TestJPACache extends SingleEMFTestCase {

    public void setUp() {
        super.setUp(CachedPerson.class, CachedManager.class, CachedEmployee.class, "openjpa.DataCache", "true",
            "openjpa.RemoteCommitProvider", "sjvm");
    }

    private void populate() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        CachedPerson p = new CachedPerson();
        p.setFirstName("John");
        p.setLastName("Doe");
        p.setId(10);
        em.persist(p);

        p = new CachedPerson();
        p.setFirstName("Jane");
        p.setLastName("Doe");
        p.setId(11);
        em.persist(p);

        CachedManager m = new CachedManager();
        m.setFirstName("Joan");
        m.setLastName("Baker");
        m.setId(12);
        m.setEmployees(new ArrayList<CachedEmployee>());
        em.persist(m);

        CachedEmployee e = new CachedEmployee();
        e.setFirstName("Jim");
        e.setFirstName("Smith");
        e.setManager(m);
        e.setId(13);
        m.getEmployees().add(e);
        em.persist(e);

        e = new CachedEmployee();
        e.setFirstName("Jeff");
        e.setFirstName("Parker");
        e.setId(14);
        e.setManager(m);
        m.getEmployees().add(e);
        em.persist(e);

        em.getTransaction().commit();

        em.close();
    }

    /**
     * Ensure the cached returned by emf.getCache supports the JPA and OpenJPA interfaces. Expected interfaces are
     * <ul>
     * <li>javax.persistence.Cache</li>
     * <li>org.apache.openjpa.persistence.StoreCache</li>
     * </ul>
     */
    public void testInterfacesReturned() {
        Object cache = emf.getCache();
        assertNotNull("Cache is not enabled", cache);
        assertTrue(cache instanceof javax.persistence.Cache);
        assertTrue(cache instanceof org.apache.openjpa.persistence.StoreCache);
    }

    /**
     * Ensure that an Entity is not inserted in the cache until the transaction commits.
     */
    public void testContains() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        CachedPerson p = new CachedPerson();
        p.setFirstName("John");
        p.setLastName("Doe");
        p.setId(1);

        assertNotInCache(CachedPerson.class, 1);
        em.persist(p);
        assertNotInCache(CachedPerson.class, 1);

        em.flush();
        assertNotInCache(CachedPerson.class, 1);

        em.getTransaction().commit();
        assertInCache(CachedPerson.class, 1);

        em.close();
    }

    /**
     * Evict a single entity instance of type CachedPerson from the cache. Testcase will fail if
     * <ul>
     * <li>CachedPerson id:10 and 11 are not in the cache when the test starts.</li>
     * <li>CachedPerson id:1 <b>is</b> found in the cache when the test starts.</li>
     * <li>CachedPerson id:10 is not found in the cache after evicting CachedPerson id:11</li>
     * <li>CachedPerson id:11 is found in the cache after evicting CachedPerson id:11</li>
     * <li>CachedPerson id:1 is found in the cache after evicting CachedPerson id:11</li>
     * </ul>
     */
    public void testEvictInstance() {
        populate();
        assertInCache(CachedPerson.class, 10);
        assertInCache(CachedPerson.class, 11);
        assertNotInCache(CachedPerson.class, 1);

        emf.getCache().evict(CachedPerson.class, 11);

        assertInCache(CachedPerson.class, 10);
        assertNotInCache(CachedPerson.class, 11);
        assertNotInCache(CachedPerson.class, 1);
    }

    /**
     * Ensure that evict(Class cls) evicts the specified class and any subclasses. This test will fail if
     * <ul>
     * <li>Any of the entites created in populate() are not found in the cache before calling evict()</li>
     * <li>Any instance of CachedPerson from populate() is found in the cache after
     *   calling evict(CachedPerson.class)</li>
     * <li>Any instance of CachedManager or CachedEmployee is found in the cache after calling evict()</li>
     * </ul>
     */
    public void testEvictClass() {
        populate();
        assertInCache(CachedPerson.class, 10);
        assertInCache(CachedPerson.class, 11);
        assertInCache(CachedEmployee.class, 13);
        assertInCache(CachedEmployee.class, 14);
        assertInCache(CachedManager.class, 12);

        emf.getCache().evict(CachedPerson.class);

        assertNotInCache(CachedPerson.class, 10);
        assertNotInCache(CachedPerson.class, 11);
        assertNotInCache(CachedEmployee.class, 13);
        assertNotInCache(CachedEmployee.class, 14);
        assertNotInCache(CachedManager.class, 12);

    }

    /**
     * Ensure the cache is cleared after calling evictAll. This test will fail if :
     * <ul>
     * <li>Any of the entities created in populate() are not found in the cache</li>
     * <li>Any of the entities which were in the cache before calling evictAll() are still in the cache after calling
     * evictAll()</li>
     * </ul>
     * 
     */
    public void testEvictAll() {
        populate();

        assertInCache(CachedPerson.class, 10);
        assertInCache(CachedPerson.class, 11);
        assertInCache(CachedEmployee.class, 13);
        assertInCache(CachedEmployee.class, 14);
        assertInCache(CachedManager.class, 12);

        emf.getCache().evictAll();

        assertNotInCache(CachedPerson.class, 10);
        assertNotInCache(CachedPerson.class, 11);
        assertNotInCache(CachedEmployee.class, 13);
        assertNotInCache(CachedEmployee.class, 14);
        assertNotInCache(CachedManager.class, 12);
    }

    // test methods for bad input.
    public void testContainsNullEntity() {
        try {
            emf.getCache().contains(null, 1);
            fail("Expected ArgumentException when calling  " + "contains(<null>, <nonNull>)");
        } catch (ArgumentException ae) {
            // normal
        }
    }

    public void testContainsNonEntityClass() {
        try {
            emf.getCache().contains(Object.class, 1);
            fail("Expected ArgumentException when calling " + "contains(<nonEntityClass>, <nonNull>");
        } catch (ArgumentException ae) {
            // expected exception
        }
    }

    public void testContainsNullPrimaryKey() {
        assertFalse(emf.getCache().contains(CachedPerson.class, null));
    }

    public void testContainsNegativePrimaryKey() {
        assertFalse(emf.getCache().contains(CachedPerson.class, -1));
    }

    public void testContainsInvalidPrimaryKeyType() {
        try{
            emf.getCache().contains(CachedPerson.class, "abcd");
            fail();
        }catch(UserException ue){
            //expected
        }
    }

    public void testEvictNullInstance() {
        try {
            emf.getCache().evict(null, 1);
            fail("Expected ArgumentException when calling " + "evict(<null>, <id");
        } catch (ArgumentException ae) {
            // expected exception
        }
    }

    public void testEvictNonEntityInstance() {
        try {
            emf.getCache().evict(Object.class, 1);
            fail("Expected ArgumentException when calling " + "evict(<null>, <id");
        } catch (ArgumentException ae) {
            // expected exception
        }
    }

    public void testEvictNullPrimaryKey() {
        emf.getCache().evict(CachedPerson.class, null);
    }

    public void testEvictNegativePrimaryKey() {
        emf.getCache().evict(CachedPerson.class, -1);
    }

    public void testEvictInvalidPrimaryKeyType() {
        try{
            emf.getCache().evict(CachedPerson.class, "abcd");
            fail();
        }catch(UserException ue){
            //expected
        }
    }

    public void testEvictNullClass() {
        try {
            emf.getCache().evict(null);
            fail("Expected ArgumentException when calling " + "evict(<null>");
        } catch (ArgumentException ae) {
            // expected exception
        }
    }

    public void testEvictNonEntity() {
        try {
            emf.getCache().evict(Object.class);
            fail("Expected ArgumentException when calling " + "evict(<nonEntity>");
        } catch (ArgumentException ae) {
            // expected exception
        }
    }
    
    public void testIllegalStateExceptionAfterClose() { 
        emf.close();
        try {
            emf.getCache();
            fail("Expected IllegalStateException");
        }
        catch(IllegalStateException ise) {
            // expected
        }
    }
    public void testIllegalStateExceptionGetAfterClose() {
        emf.getCache();  // populate the EntityManagerFactoryImpl's wrapper for the StoreCache.
        emf.close();
        try {
            emf.getCache();
            fail("Expected IllegalStateException");
        }
        catch(IllegalStateException ise) {
            // expected
        }
    }

    /**
     * Convenience method. Asserts that the class & primary key do exist in the cache
     * 
     * @param cls
     *            Entity class.
     * @param primaryKey
     *            PrimaryKey of the entity.
     */
    private void assertInCache(Class<?> cls, Object primaryKey) {
        assertTrue(String.format("%s:%s should exist in cache", cls.toString(), primaryKey.toString()), emf.getCache()
            .contains(cls, primaryKey));
    }

    /**
     * Convenience method. Assert that the class and primary key do not exist in the cache
     * 
     * @param cls
     *            Entity class.
     * @param primaryKey
     *            PrimaryKey of the entity.
     */
    private void assertNotInCache(Class<?> cls, Object primaryKey) {
        assertFalse(String.format("%s:%s should not exist in cache", cls.toString(), primaryKey.toString()), emf
            .getCache().contains(cls, primaryKey));
    }

}
