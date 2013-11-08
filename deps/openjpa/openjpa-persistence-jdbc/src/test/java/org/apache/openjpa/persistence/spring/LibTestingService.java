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

public class LibTestingService {
    private EntityManager em;

    public LibTestingService() {
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    public void depopulateDB() {
        // delete everything
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.createQuery("delete from Book").executeUpdate();
        em.createQuery("delete from Borrower").executeUpdate();
        em.createQuery("delete from Subject").executeUpdate();
        tx.commit();
    }

    public void repopulateDB() {
        depopulateDB();

        if (!isDBClean())
            throw new IllegalStateException("Failed to clean the database");

        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            // create three borrowers Tom, Dick, and Harry
            Borrower tom = new Borrower("Tom");
            Borrower dick = new Borrower("Dick");
            Borrower harry = new Borrower("Harry");

            // make them persistent
            em.persist(tom);
            em.persist(dick);
            em.persist(harry);

            // make Dick a volunteer
            Volunteer v = new Volunteer(dick);
            v.setHoursPerWeek(10);

            // create six books
            Book fishing = new Book("Gone Fishing");
            Book hunting = new Book("Gone Hunting");
            Book sailing = new Book("Gone Sailing");
            Book fighting = new Book("Gone to War");
            Book visiting = new Book("Gone Visiting");
            Book working = new Book("Gone to Work");
            Book sleeping = new Book("Gone to Bed");

            em.persist(fishing);
            em.persist(hunting);
            em.persist(sailing);
            em.persist(fighting);
            em.persist(visiting);
            em.persist(working);
            em.persist(sleeping);

            // create categories for the books
            Subject outdoors = new Subject("Outdoors");
            Subject military = new Subject("Military");
            Subject sport = new Subject("Sportsman");
            Subject travel = new Subject("Travel");
            Subject industry = new Subject("Industry");
            Subject space = new Subject("Space");

            // link the books with the categories
            fishing.addSubject(outdoors);
            fishing.addSubject(sport);
            hunting.addSubject(outdoors);
            hunting.addSubject(sport);
            sailing.addSubject(outdoors);
            sailing.addSubject(travel);
            fighting.addSubject(military);
            fighting.addSubject(travel);
            visiting.addSubject(travel);
            working.addSubject(industry);

            // this Subject has no books
            em.persist(space);

            // borrow some books
            tom.borrowBook(fishing);
            dick.borrowBook(hunting);
            dick.borrowBook(sailing);
            harry.borrowBook(working);

            // commit the transaction
            tx.commit();
        } catch (RuntimeException e) {
            System.err.println("Unable to repopulate the database");
            System.err.println("Caught exception: " + e.getMessage());
            e.printStackTrace(System.err);
            throw e;
        } finally {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
        }
    }

    public boolean isDBClean() {
        // Do a series of counts
        // (Avoid any problem in generating a Cartesian join)
        long count = 0;
        count += (Long) em.createQuery("select count(b) from Book b")
                .getSingleResult();
        count += (Long) em.createQuery("select count(b) from Borrower b")
                .getSingleResult();
        count += (Long) em.createQuery("select count(v) from Volunteer v")
                .getSingleResult();
        count += (Long) em.createQuery("select count(s) from Subject s")
                .getSingleResult();
        return count <= 0;
    }

    /**
     * Close the Service. The method is idempotent. It may be called multiple
     * times without ill effect.
     */
    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
