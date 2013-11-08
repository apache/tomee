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
package embeddables;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class Main {

    public static void main(String[] args) throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("embeddables", System.getProperties());
        EntityManager em = emf.createEntityManager();
        init(em);
        runQueries(em);
    }

    public static void runQueries(EntityManager em) {
        // Find all users that have a secondary address that is in MI
        Query q = em.createQuery("SELECT u FROM User u , in (u.addresses) a " + "WHERE a.state='xx'");

        List<User> res = q.getResultList();
        for (User b : res) {
            System.out.println(b);
        }

        Query q1 = em.createQuery("SELECT u FROM User u , in (u.addresses) a " + "WHERE a.coordinates.longitude='38'");
        List<User> res1 = q1.getResultList();
        for (User b : res1) {
            System.out.println(b);
        }

        // Find users who's conatctInfo cell phone is 507-555-5555
        // Entity -> embedded -> embedded
        q =
            em.createQuery("SELECT DISTINCT u FROM User u " + "WHERE u.contactInfo.homePhone.number='507-555-5555'"
                + "AND u.contactInfo.homePhone.type='cell'");
        res = q.getResultList();
        for (User b : res) {
            System.out.println(b);
        }

        // Find users who's conatctInfo cell phone is 507-555-5555
        // Entity -> embedded -> embedded
        q =
            em.createQuery("SELECT u FROM User u " + "WHERE u.contactInfo.homePhone.number='507-555-5555'"
                + "AND u.contactInfo.homePhone.type='cell'");
        res = q.getResultList();
        for (User b : res) {
            System.out.println(b);
        }

    }

    public static void init(EntityManager em) {
        Coordinates c = new Coordinates("37.0", "23.516");
        Coordinates c1 = new Coordinates("38", "23.516");
        Coordinates c2 = new Coordinates("39", "23.516");

        Phone p = new Phone("507-555-5555", "cell");
        Address a = new Address("Cariou Ln", "Minneapolis", "MN", 90210, c);
        ContactInfo ci = new ContactInfo(a, p);

        User u = new User(ci, "user_name" + System.currentTimeMillis(), "user_asdf");
        u.addAddress(new Address("100 Rodeo Dr", "Arroyo Grande", "CA", 93420, c1));
        u.addAddress(new Address("1700 W 3rd Ave", "Flint", "MI", 48504, c2));
        u.addAddress(new Address("4301 Farm Ln.", "East Lansing", "MI", 48824, c2));

        em.getTransaction().begin();
        em.persist(c);
        em.persist(c1);
        em.persist(c2);
        em.persist(u);
        em.getTransaction().commit();

    }

}
