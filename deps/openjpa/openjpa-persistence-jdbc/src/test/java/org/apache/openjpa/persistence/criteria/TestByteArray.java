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

package org.apache.openjpa.persistence.criteria;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestByteArray extends SingleEMFTestCase {
    protected CriteriaBuilder cb;
    String pic = "IamNotAPicture";
    Character[] chars = new Character[] { new Character('a'), new Character('b') };
    public void setUp() {
        super.setUp(BlogUser.class
            // Using the following property will require corresponding
            // changes in BlogUser_.java
//            ,"openjpa.Compatibility", "UseListAttributeForArrays=true"
            );
        cb = emf.getCriteriaBuilder();
        populate();
    }

    public void populate() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();

        BlogUser bu = new BlogUser();
        bu.setUsername("jode1");
        bu.setPic(pic.getBytes());
        bu.setCharacters(chars);
        bu.setChars(pic.toCharArray());

        em.persist(bu);
        tran.commit();

        em.close();
    }

    public void assertBlogUser(BlogUser bu) {
        assertNotNull(bu);
        assertNotNull(bu.getPic());
        assertNotNull(bu.getCharacters());
        assertNotNull(bu.getChars());
    }

    public void testSimpleQuery() {
        EntityManager em = emf.createEntityManager();
        CriteriaQuery<BlogUser> cq = cb.createQuery(BlogUser.class);
        cq.select(cq.from(BlogUser.class));

        List<BlogUser> users = em.createQuery(cq).getResultList();

        assertNotNull(users);
        assertFalse(users.isEmpty());

        assertBlogUser(users.get(0));

        em.close();
    }

    public void testSimpleQueryBytesNotNull() {
        EntityManager em = emf.createEntityManager();
        CriteriaQuery<BlogUser> cq = cb.createQuery(BlogUser.class);
        Root<BlogUser> bloguser = cq.from(BlogUser.class);
        cq.select(bloguser);
        cq.where(bloguser.get(BlogUser_.pic).isNotNull());

        List<BlogUser> users = em.createQuery(cq).getResultList();

        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertBlogUser(users.get(0));

        em.close();
    }     
    
    public void testSimpleQueryBytesNull() {
        EntityManager em = emf.createEntityManager();
        CriteriaQuery<BlogUser> cq = cb.createQuery(BlogUser.class);
        Root<BlogUser> bloguser = cq.from(BlogUser.class);
        cq.select(bloguser);
        cq.where(bloguser.get(BlogUser_.pic).isNull());

        List<BlogUser> users = em.createQuery(cq).getResultList();

        assertNotNull(users);
        assertTrue(users.isEmpty());

        em.close();
    }

    public void testSimpleQueryCharactersNotNull() {
        EntityManager em = emf.createEntityManager();
        CriteriaQuery<BlogUser> cq = cb.createQuery(BlogUser.class);
        Root<BlogUser> bloguser = cq.from(BlogUser.class);
        cq.select(bloguser);
        cq.where(bloguser.get(BlogUser_.characters).isNotNull());

        List<BlogUser> users = em.createQuery(cq).getResultList();

        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertBlogUser(users.get(0));

        em.close();
    }
    public void testSimpleQueryCharactersNull() {
        EntityManager em = emf.createEntityManager();
        CriteriaQuery<BlogUser> cq = cb.createQuery(BlogUser.class);
        Root<BlogUser> bloguser = cq.from(BlogUser.class);
        cq.select(bloguser);
        cq.where(bloguser.get(BlogUser_.characters).isNull());

        List<BlogUser> users = em.createQuery(cq).getResultList();

        assertNotNull(users);
        assertTrue(users.isEmpty());

        em.close();
    }

    public void testSimpleQueryCharsNotNull() {
        EntityManager em = emf.createEntityManager();
        CriteriaQuery<BlogUser> cq = cb.createQuery(BlogUser.class);
        Root<BlogUser> bloguser = cq.from(BlogUser.class);
        cq.select(bloguser);
        cq.where(bloguser.get(BlogUser_.chars).isNotNull());

        List<BlogUser> users = em.createQuery(cq).getResultList();

        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertBlogUser(users.get(0));

        em.close();
    }
    
    public void testSimpleQueryCharsNull() {
        EntityManager em = emf.createEntityManager();
        CriteriaQuery<BlogUser> cq = cb.createQuery(BlogUser.class);
        Root<BlogUser> bloguser = cq.from(BlogUser.class);
        cq.select(bloguser);
        cq.where(bloguser.get(BlogUser_.chars).isNull());

        List<BlogUser> users = em.createQuery(cq).getResultList();

        assertNotNull(users);
        assertTrue(users.isEmpty());

        em.close();
    }

}
