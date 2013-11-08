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
package org.apache.openjpa.openjpa2018;

import org.apache.openjpa.persistence.test.SingleEMTestCase;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This test verifies that select IN statements with
 * arrays and Collections are fine.
 */
public class OpenJPA2018Test extends SingleEMTestCase
{

    @Override
    public void setUp() {
        super.setUp(User2018.class, CLEAR_TABLES);
    }

    public void testInCriteriaWithArray() {
        em.getTransaction().begin();
        try {
            User2018 user = new User2018();
            em.persist(user);
            em.flush();

            CriteriaBuilder builder = em.getCriteriaBuilder();

            CriteriaQuery<User2018> criteria = builder.createQuery(User2018.class);
            Root<User2018> root = criteria.from(User2018.class);
            criteria.where(root.get("id").in(builder.parameter(Long[].class)));

            TypedQuery<User2018> query = em.createQuery(criteria);
            for (ParameterExpression parameter : criteria.getParameters()) {
                query.setParameter(parameter, new Long[] { user.id, 123456789L });
            }

            List<User2018> result = query.getResultList();
            assertTrue(!result.isEmpty());
        } finally {
            em.getTransaction().commit();
        }
    }

    public void testInJpqlWithArray() {
        em.getTransaction().begin();
        try {
            User2018 user = new User2018();
            em.persist(user);
            em.flush();

            TypedQuery<User2018> query = em.createQuery("select u from User2018 as u where u.id in (:userIds)",
                                                        User2018.class);
            query.setParameter("userIds", new Long[] { user.id, 123456789L });

            List<User2018> result = query.getResultList();
            assertTrue(!result.isEmpty());
        } finally {
            em.getTransaction().commit();
        }
    }

    public void testInCriteriaWithCollection() {
        em.getTransaction().begin();
        try {
            User2018 user = new User2018();
            em.persist(user);
            em.flush();

            CriteriaBuilder builder = em.getCriteriaBuilder();

            CriteriaQuery<User2018> criteria = builder.createQuery(User2018.class);
            Root<User2018> root = criteria.from(User2018.class);
            criteria.where(root.get("id").in(builder.parameter(Collection.class)));

            TypedQuery<User2018> query = em.createQuery(criteria);
            for (ParameterExpression parameter : criteria.getParameters()) {
                query.setParameter(parameter, Arrays.asList(user.id, 123456789L));
            }

            List<User2018> result = query.getResultList();
            assertTrue(!result.isEmpty());
        } finally {
            em.getTransaction().commit();
        }
    }

    public void testInJpqlWithCollection() {
        em.getTransaction().begin();
        try {
            User2018 user = new User2018();
            em.persist(user);
            em.flush();

            TypedQuery<User2018> query = em.createQuery("select u from User2018 as u where u.id in (:userIds)",
                    User2018.class);
            query.setParameter("userIds", Arrays.asList(user.id, 123456789L));

            List<User2018> result = query.getResultList();
            assertTrue(!result.isEmpty());
        } finally {
            em.getTransaction().commit();
        }
    }


    public void testId() {
        em.getTransaction().begin();
        try {
            User2018 user = new User2018();
            em.persist(user);
            em.flush();

            CriteriaBuilder builder = em.getCriteriaBuilder();

            CriteriaQuery<User2018> criteria = builder.createQuery(User2018.class);
            Root<User2018> root = criteria.from(User2018.class);
            criteria.where(builder.equal(root.get("id"), user.id));

            TypedQuery<User2018> query = em.createQuery(criteria);
            for (ParameterExpression parameter : criteria.getParameters()) {
                query.setParameter(parameter, user.id);
            }

            List<User2018> result = query.getResultList();
            assertTrue(!result.isEmpty());
        } finally {
            em.getTransaction().commit();
        }
    }

}
