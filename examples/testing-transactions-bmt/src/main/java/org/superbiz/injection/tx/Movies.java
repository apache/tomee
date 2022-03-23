/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.injection.tx;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateful;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;

@Stateful(name = "Movies")
@TransactionManagement(TransactionManagementType.BEAN)
public class Movies {

    @PersistenceContext(unitName = "movie-unit", type = PersistenceContextType.TRANSACTION)
    private EntityManager entityManager;

    @Resource
    private UserTransaction userTransaction;

    public void addMovie(Movie movie) throws Exception {
        try {
            userTransaction.begin();
            entityManager.persist(movie);

            //For some dummy reason, this db can have only 5 titles. :O)
            if (countMovies() > 5) {
                userTransaction.rollback();
            } else {
                userTransaction.commit();
            }

        } catch (Exception e) {
            e.printStackTrace();
            userTransaction.rollback();
        }
    }

    public Long countMovies() throws Exception {
        Query query = entityManager.createQuery("SELECT COUNT(m) FROM Movie m");
        return Long.class.cast(query.getSingleResult());
    }
}

