/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.cmp.jpa;

import junit.framework.Assert;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.transaction.Status;
import javax.transaction.TransactionManager;
import java.util.Collection;

public class UnenhancedUnits extends Assert {
    private EntityManagerFactory entityManagerFactory;
    private TransactionManager transactionManager;

    private EntityManager entityManager;
    private EntityTransaction transaction;

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void setUp() throws Exception {
    }

    public void tearDown() throws Exception {
        if (entityManager != null && entityManager.isOpen()) {
            if (transaction != null) {
                try {
                    if (transaction.getRollbackOnly()) {
                        transaction.rollback();
                    } else {
                        transaction.commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    if (transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
                        transactionManager.rollback();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            entityManager.close();
        }

        entityManager = null;
        entityManagerFactory = null;
        transactionManager = null;
    }

    public void complexId() throws Exception {
        beginTx();

        // constructor
        ComplexStandalone complex = new ComplexStandalone("first", "second");

        // em should not know about our entity
        assertFalse(entityManager.contains(complex));

        // persist the entity
        entityManager.persist(complex);

        // em should now be aware of our entity
        assertTrue(entityManager.contains(complex));

        commitTx();
    }

    public void complexIdSubclass() throws Exception {
        beginTx();

        // create entity
        ComplexSuperclass complex = new ComplexSubclass();
        complex.firstId = "first";
        complex.secondId = "second";

        // em should not know about our entity
        assertFalse(entityManager.contains(complex));

        // persist the entity
        entityManager.persist(complex);

        // em should now be aware of our entity
        assertTrue(entityManager.contains(complex));

        commitTx();
    }

    public void generatedId() throws Exception {
        beginTx();

        // constructor
        GeneratedStandalone generated = new GeneratedStandalone();

        // entity should not have an id yet
        assertNull("generated.getId() is not null", generated.getId());

        // em should not know about our entity
        assertFalse(entityManager.contains(generated));

        // persist the entity
        entityManager.persist(generated);
        entityManager.flush();

        // entity should now have an id
        assertNotNull("generated.getId() is null", generated.getId());

        // em should now be aware of our entity
        assertTrue(entityManager.contains(generated));

        commitTx();
    }

    public void generatedIdSubclass() throws Exception {
        beginTx();

        // constructor
        GeneratedSuperclass generated = new GeneratedSubclass();

        // entity should not have an id yet
        assertNull("generated.getId() is not null", generated.getId());

        // em should not know about our entity
        assertFalse(entityManager.contains(generated));

        // persist the entity
        entityManager.persist(generated);
        entityManager.flush();

        // entity should now have an id
        assertNotNull("generated.getId() is null", generated.getId());

        // em should now be aware of our entity
        assertTrue(entityManager.contains(generated));

        commitTx();
    }

    public void collection() throws Exception {
        beginTx();

        OneStandalone one = new OneStandalone(1000);

        ManyStandalone manyA = new ManyStandalone(1);
        one.getMany().add(manyA);
        manyA.setOne(one);

        ManyStandalone manyB = new ManyStandalone(2);
        one.getMany().add(manyB);
        manyB.setOne(one);

        ManyStandalone manyC = new ManyStandalone(3);
        one.getMany().add(manyC);
        manyC.setOne(one);

        // em should not know about our entities
        assertFalse(entityManager.contains(one));
        assertFalse(entityManager.contains(manyA));
        assertFalse(entityManager.contains(manyB));
        assertFalse(entityManager.contains(manyC));

        // persist the entity
        entityManager.persist(one);
        entityManager.persist(manyA);
        entityManager.persist(manyB);
        entityManager.persist(manyC);
        entityManager.flush();

        // em should now be aware of our entity
        assertTrue(entityManager.contains(one));
        assertTrue(entityManager.contains(manyA));
        assertTrue(entityManager.contains(manyB));
        assertTrue(entityManager.contains(manyC));

        commitTx();

        one = null;
        manyA = null;
        manyB = null;
        manyC = null;

        beginTx();

        // reload one
        one = entityManager.find(OneStandalone.class, 1000);
        assertNotNull("one is null", one);

        // verify one.getMany()
        assertNotNull("one.getMany() is null", one.getMany());
        Collection<ManyStandalone> many = one.getMany();
        assertEquals(3, many.size());

        // reload the many
        manyA = entityManager.find(ManyStandalone.class, 1);
        assertNotNull("manyA is null", manyA);
        manyB = entityManager.find(ManyStandalone.class, 2);
        assertNotNull("manyB is null", manyA);
        manyC = entityManager.find(ManyStandalone.class, 3);
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

        commitTx();
    }

    private void beginTx() throws Exception {
        entityManager = entityManagerFactory.createEntityManager();

        try {
            transaction = entityManager.getTransaction();
        } catch (Exception e) {
            // must be JTA
        }

        log("BEGIN_TX");
        if (transaction == null) {
            transactionManager.begin();
            entityManager.joinTransaction();
        } else {
            transaction.begin();
        }
    }

    private void commitTx() throws Exception {
        log("  BEFORE_COMMIT_TX");
        try {
            if (transaction == null) {
                transactionManager.commit();
            } else {
                transaction.commit();
            }
        } finally {
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
            entityManager = null;
            log("AFTER_COMMIT_TX");
        }
    }

    public void log(String msg) {
//        System.out.println(msg);
    }
}
