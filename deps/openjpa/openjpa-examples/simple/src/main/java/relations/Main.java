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
package relations;

import java.util.*;
import javax.persistence.*;

// import the enums for MALE and FEMALE
import static relations.Deity.Gender.*;


/** 
 * A very simple, stand-alone program that stores a new entity in the
 * database and then performs a query to retrieve it.
 */
public class Main {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        // Create a new EntityManagerFactory using the System properties.
        // The "relations" name will be used to configure based on the
        // corresponding name in the META-INF/persistence.xml file
        EntityManagerFactory factory = Persistence.
            createEntityManagerFactory("relations", System.getProperties());

        // Create a new EntityManager from the EntityManagerFactory. The
        // EntityManager is the main object in the persistence API, and is
        // used to create, delete, and query objects, as well as access
        // the current transaction
        EntityManager em = factory.createEntityManager();

        initFamilyTree(em);

        runQueries(em);

        // It is always good to clean up after ourselves
        em.close();
        factory.close();
    }

    /** 
     * Creates a partial family tree of the Greek dieties.
     *  
     * @param  em  the EntityManager to use in the persistence process
     */
    public static void initFamilyTree(EntityManager em) {

        // First delete all the members from the database the clean up
        em.getTransaction().begin();
        em.createQuery("delete from Deity").executeUpdate();
        em.getTransaction().commit();

        // Generation 1
        Deity uranus = new Deity("Uranus", MALE);
        Deity gaea = new Deity("Gaea", FEMALE);

        // Generation 2
        Deity cronus = gaea.giveBirth("Cronus", uranus, MALE);
        Deity rhea = gaea.giveBirth("Rhea", uranus, FEMALE);
        Deity coeus = gaea.giveBirth("Coeus", uranus, MALE);
        Deity phoebe  = gaea.giveBirth("Phoebe", uranus, FEMALE);
        Deity oceanus = gaea.giveBirth("Oceanus", uranus, MALE);
        Deity tethys = gaea.giveBirth("Tethys", uranus, FEMALE);

        // Generation 3
        Deity leto = phoebe.giveBirth("Leto", coeus, FEMALE);

        Deity hestia = rhea.giveBirth("Hestia", cronus, FEMALE);
        Deity pluto = rhea.giveBirth("Pluto", cronus, MALE);
        Deity poseidon = rhea.giveBirth("Poseidon", cronus, MALE);
        Deity zeus = rhea.giveBirth("Zeus", cronus, MALE);
        Deity hera = rhea.giveBirth("Hera", cronus, FEMALE);
        Deity demeter = rhea.giveBirth("Demeter", cronus, FEMALE);

        // Generation 4
        Deity iapetus = tethys.giveBirth("Iapetus", coeus, MALE);
        Deity clymene = new Deity("Clymene", FEMALE);

        Deity apollo = leto.giveBirth("Apollo", zeus, MALE);
        Deity artemis = leto.giveBirth("Artemis", zeus, MALE);

        Deity persephone = demeter.giveBirth("Persephone", zeus, MALE);

        Deity ares = hera.giveBirth("Ares", zeus, MALE);
        Deity hebe = hera.giveBirth("Hebe", zeus, FEMALE);
        Deity hephaestus = hera.giveBirth("Hephaestus", zeus, MALE);

        Deity prometheus = clymene.giveBirth("Prometheus", iapetus, MALE);
        Deity atlas = clymene.giveBirth("Atlas", iapetus, MALE);
        Deity epimetheus = clymene.giveBirth("Epimetheus", iapetus, FEMALE);

        Deity dione = new Deity("Dione", FEMALE);
        dione.giveBirth("Aphrodite", zeus, FEMALE);

        // Begin a new local transaction so that we can persist a new entity
        em.getTransaction().begin();

        // note that we only need to explicitly persist a single root of the
        // object graph (the family tree, in this case), since we have the
        // "cascade" annotation on all the relations
        em.persist(zeus);

        // Commit the transaction, which will cause the entity to
        // be stored in the database
        em.getTransaction().commit();
    }

    /** 
     * Run some sample queries against the family tree model.
     *  
     * @param  em  the EntityManager to use
     */
    public static void runQueries(EntityManager em) {

        System.out.println("Running query to find all instances..");

        // Perform a simple query for all the Deity entities
        Query q = em.createQuery("select x from Deity x");

        // Go through each of the entities and print out each of their
        // messages, as well as the date on which it was created 
        for (Deity m : (List<Deity>) q.getResultList()) {
            System.out.println(m.getName());
        }

        q = em.createQuery("select x from Deity x "
            + "where x.father.name = 'Zeus'");

        for (Deity m : (List<Deity>) q.getResultList()) {
            System.out.println("Child of Zeus: " + m.getName());
        }

        q = em.createNamedQuery("siblings").
            setParameter(1, em.getReference(Deity.class, "Rhea"));

        for (Deity m : (List<Deity>) em.createNamedQuery("siblings").
            setParameter(1, em.getReference(Deity.class, "Rhea")).
            getResultList()) {
            System.out.println("Siblings of Rhea: " + m.getName());
        }

        for (Deity m : (List<Deity>) em.createNamedQuery("half-siblings").
            setParameter(1, em.getReference(Deity.class, "Apollo")).
            getResultList()) {
            System.out.println("Half-siblings of Apollo: " + m.getName());
        }

        for (Deity m : (List<Deity>) em.createNamedQuery("cousins").
            setParameter(1, em.getReference(Deity.class, "Leto")).
            getResultList()) {
            System.out.println("Cousins of Leto: " + m.getName());
        }
    }
}
