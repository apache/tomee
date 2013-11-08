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
package org.apache.openjpa.persistence.spring;

import javax.persistence.*;

import org.apache.openjpa.persistence.models.library.*;

/**
 * This service uses a transactionally scoped entity manager. It grabs the EM at
 * the beginning of each business method and closes it at the end of each
 * business method. Transactional methods start a transaction and either commit
 * or roll back at the end of the method. This behavior mimics the behavior of
 * containers that inject transactionally scoped entity managers at the
 * beginning of each service method and close them at the end.
 */
public class LibServiceImpl implements LibService {
    private TransactionalEntityManagerFactory txEMF;

    private void closeEM(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    private EntityManager openEM() {
        EntityManager em = txEMF.getTransactionalEntityManager();
        return em;
    }

    private void commit(EntityTransaction tx) {
        if (tx != null && tx.isActive()) {
            if (tx.getRollbackOnly())
                tx.rollback();
            else
                tx.commit();
        }
    }

    public void setTransactionalEntityManagerFactory(
            TransactionalEntityManagerFactory txEMF) {
        this.txEMF = txEMF;
    }

    public Book findBookByTitle(String title) {
        EntityManager em = null;

        try {
            em = openEM();

            // check the name passed in
            if (title != null)
                title = title.trim();

            if (title == null || title.length() <= 0)
                throw new IllegalArgumentException(
                        "the title cannot be null or empty");

            // set up the query
            Query query = em.createQuery(
               "select b from Book b join fetch b.subjects where b.title = :x");
            query.setParameter("x", title);

            // execute the query and return the books
            Book retv = (Book) query.getSingleResult();
            return retv;
        } finally {
            closeEM(em);
        }
    }

    public Borrower findBorrowerByName(String name) {
        EntityManager em = null;

        try {
            em = openEM();

            // check the name passed in
            if (name != null)
                name = name.trim();

            if (name == null || name.length() <= 0)
                throw new IllegalArgumentException(
                        "the name cannot be null or empty");

            // set up the query
            Query query = em
                    .createQuery("select b from Borrower b where b.name = :x");
            query.setParameter("x", name);

            // execute the query and return the books
            Borrower retv = (Borrower) query.getSingleResult();
            return retv;
        } finally {
            closeEM(em);
        }
    }

    public void borrowBook(Borrower borrower, Book book) {
        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = openEM();
            tx = em.getTransaction();
            tx.begin();

            // do nothing if one of the parameters is null
            if (borrower == null || book == null)
                return;

            borrower.borrowBook(book);

            // merge the owner of the relationship
            em.merge(book);
        } catch (RuntimeException e) {
            tx.setRollbackOnly();
            throw e;
        } finally {
            commit(tx);
            closeEM(em);
        }
    }

    public void returnBook(Book book) {
        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = openEM();
            tx = em.getTransaction();
            tx.begin();

            // do nothing if the parameter is null
            if (book == null)
                return;

            Borrower borrower = book.getBorrower();
            if (borrower != null) {
                borrower.returnBook(book);

                // merge the owner of the relationship
                em.merge(book);
            }
        } catch (RuntimeException e) {
            tx.setRollbackOnly();
            throw e;
        } finally {
            commit(tx);
            closeEM(em);
        }
    }
}
