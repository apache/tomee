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

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.apache.openjpa.persistence.datacache.SerializingConcurrentQueryCache;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestRemoteQueryCacheCriteriaQuery extends SingleEMFTestCase {
    protected EntityType<Department> department_ = null;
    protected OpenJPACriteriaBuilder cb;

    @Override
    public void setUp() throws Exception {
        super.setUp("openjpa.DataCache", "true", "openjpa.QueryCache",
            SerializingConcurrentQueryCache.SERIALIZING_CONCURRENT_QUERY_CACHE, Department.class, Employee.class,
            Contact.class, Manager.class, FrequentFlierPlan.class);
        EntityManager em = emf.createEntityManager();
        try {
            Metamodel mm = em.getMetamodel();
            department_ = mm.entity(Department.class);
            cb = emf.getCriteriaBuilder();
        } finally {
            em.close();
        }
    }

    public void test() {
        CriteriaQuery<Department> q = cb.createQuery(Department.class);
        Root<Department> dept = q.from(Department.class);
        q.select(dept).where(cb.equal(dept.get(department_.getSingularAttribute("name", String.class)), "test"));

        EntityManager em = emf.createEntityManager();
        try {
            em.createQuery(q).getResultList();
        } finally {
            em.close();
        }
    }
}
