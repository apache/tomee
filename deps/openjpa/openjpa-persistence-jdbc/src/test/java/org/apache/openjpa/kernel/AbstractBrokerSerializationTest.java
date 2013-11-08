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
package org.apache.openjpa.kernel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;

import org.apache.openjpa.event.AbstractLifecycleListener;
import org.apache.openjpa.event.AbstractTransactionListener;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.TransactionEvent;
import org.apache.openjpa.persistence.InvalidStateException;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.jdbc.JDBCFetchPlan;
import org.apache.openjpa.persistence.jdbc.JoinSyntax;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/*
 * To test:
 *  - managed transactions
 *  - converting non-enhanced classes to enhanced subclasses
 *    (maybe an ugly ThreadLocal, maybe through PCData?)
 */
public abstract class AbstractBrokerSerializationTest<T>
    extends SingleEMFTestCase {

    private static LifeListener deserializedLifeListener;
    private static int testGlobalRefreshCount = 0;

    private static TxListener deserializedTxListener;
    private static int testGlobalBeginCount = 0;


    private Object id;

    public void setUp() {
        testGlobalRefreshCount = 0;
        deserializedLifeListener = null;
        testGlobalBeginCount = 0;
        deserializedTxListener = null;

        setUp(getManagedType(), getSecondaryType(), CLEAR_TABLES,
            "openjpa.EntityManagerFactoryPool", "true");
        
        T e = newManagedInstance();
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        id = em.getObjectId(e);
        em.close();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        testGlobalRefreshCount = 0;
        deserializedLifeListener = null;
        testGlobalBeginCount = 0;
        deserializedTxListener = null;
    }

    public void testEmptyBrokerSerialization() {
        OpenJPAEntityManager em = emf.createEntityManager();
        OpenJPAEntityManager em2 = deserializeEM(serialize(em));

        assertTrue(em != em2);
        assertTrue(
            JPAFacadeHelper.toBroker(em) != JPAFacadeHelper.toBroker(em2));
        assertSame(em.getEntityManagerFactory(), em2.getEntityManagerFactory());

        assertSame(em2, JPAFacadeHelper.toBroker(em2)
            .getUserObject(JPAFacadeHelper.EM_KEY));

        em.close();
        assertTrue(em2.isOpen());
        em2.close();
    }

    public void testNontransactionalBrokerSerialization() {
        OpenJPAEntityManager em = emf.createEntityManager();
        T e = em.find(getManagedType(), id);
        OpenJPAEntityManager em2 = deserializeEM(serialize(em));

        assertFalse(em2.getTransaction().isActive());

        assertFalse(em2.contains(e));
        assertEquals(1*graphSize(), em2.getManagedObjects().size());
        T e2 = em2.find(getManagedType(), id);
        assertEquals(em.getObjectId(e), em2.getObjectId(e2));

        em.close();
        em2.close();
    }

    public void testUnflushedOptimisticTxBrokerSerialization() {
        OpenJPAEntityManager em = emf.createEntityManager();
        T e = em.find(getManagedType(), id);
        OpenJPAEntityManager em2 = null;
        OpenJPAEntityManager em3 = null;
        try {
            em.getTransaction().begin();
            modifyInstance(e);
            T newe = newManagedInstance();
            em.persist(newe);
            em2 = deserializeEM(serialize(em));

            assertTrue(em2.getTransaction().isActive());

            assertFalse(em2.contains(e));
            T e2 = em2.find(getManagedType(), id);
            assertEquals(em.getObjectId(e), em2.getObjectId(e2));

            assertEquals("modified", getModifiedValue(e2));

            em.getTransaction().rollback();
            assertTrue(em2.getTransaction().isActive());
            em2.getTransaction().commit();

            em3 = emf.createEntityManager();
            T e3 = em3.find(getManagedType(), id);
            assertEquals(getModifiedValue(e2), getModifiedValue(e3));
            assertTrue(1 < ((Number) em3.createQuery("select count(o) from "
                + getManagedType().getName() + " o").getSingleResult())
                .intValue());
        } finally {
            close(em);
            close(em2);
            close(em3);
        }
    }

    public void testFlushedOptimisticTxBrokerSerialization() {
        OpenJPAEntityManager em = emf.createEntityManager();
        T e = em.find(getManagedType(), id);
        em.getTransaction().begin();
        modifyInstance(e);
        em.flush();
        try {
            serialize(em);
        } catch (InvalidStateException ise) {
            // expected
            assertTrue(ise.getMessage().contains("flushed"));
        } finally {
            em.getTransaction().rollback();
            em.close();
        }
    }

    public void testConnectedOptimisticTxBrokerSerialization() {
        Map m = new HashMap();
        m.put("openjpa.ConnectionRetainMode", "always");
        OpenJPAEntityManager em = emf.createEntityManager(m);
        try {
            serialize(em);
        } catch (InvalidStateException ise) {
            // expected
            assertTrue(ise.getMessage().contains("connected"));
        } finally {
            em.close();
        }
    }

    public void testEmptyPessimisticTxBrokerSerialization() {
        Map m = new HashMap();
        m.put("openjpa.Optimistic", "false");
        OpenJPAEntityManager em = emf.createEntityManager(m);
        em.getTransaction().begin();
        try {
            serialize(em);
            fail("should not be able to serialize");
        } catch (InvalidStateException ise) {
            // expected
            assertTrue(ise.getMessage().contains("datastore (pessimistic)"));
        } finally {
            em.getTransaction().rollback();
            em.close();
        }
    }

    public void testNonEmptyPessimisticTxBrokerSerialization() {
        Map m = new HashMap();
        m.put("openjpa.Optimistic", "false");
        OpenJPAEntityManager em = emf.createEntityManager(m);
        T e = em.find(getManagedType(), id);
        em.getTransaction().begin();
        try {
            serialize(em);
            fail("should not be able to serialize");
        } catch (InvalidStateException ise) {
            // expected
            assertTrue(ise.getMessage().contains("datastore (pessimistic)"));
        } finally {
            em.getTransaction().rollback();
            em.close();
        }
    }

    public void testFetchConfigurationMutations() {
        OpenJPAEntityManager em = emf.createEntityManager();
        JDBCFetchPlan plan = (JDBCFetchPlan) em.getFetchPlan();

        assertNotEquals(17, plan.getLockTimeout());
        assertNotEquals(JoinSyntax.TRADITIONAL, plan.getJoinSyntax());

        plan.setLockTimeout(17);
        plan.setJoinSyntax(JoinSyntax.TRADITIONAL);

        OpenJPAEntityManager em2 = deserializeEM(serialize(em));
        JDBCFetchPlan plan2 = (JDBCFetchPlan) em2.getFetchPlan();
        assertEquals(17, plan2.getLockTimeout());
        assertEquals(JoinSyntax.TRADITIONAL, plan2.getJoinSyntax());
    }

    public void testInMemorySavepointsWithNewInstances() {
        OpenJPAEntityManagerFactory emf1 = createEMF(
            getManagedType(), getSecondaryType(),
            "openjpa.EntityManagerFactoryPool", "true",
            "openjpa.SavepointManager", "in-mem");
        OpenJPAEntityManager em = emf1.createEntityManager();
        OpenJPAEntityManager em2 = null;
        try {
            em.getTransaction().begin();
            T t = newManagedInstance();
            Object orig = getModifiedValue(t);
            em.persist(t);
            Object id = em.getObjectId(t);
            em.setSavepoint("foo");
            modifyInstance(t);
            assertNotEquals(orig, getModifiedValue(t));

            em2 = deserializeEM(serialize(em));
            T t2 = em2.find(getManagedType(), id);
            assertNotEquals(orig, getModifiedValue(t2));

            em.rollbackToSavepoint("foo");
            assertEquals(orig, getModifiedValue(t));

            em2.rollbackToSavepoint("foo");
            assertEquals(orig, getModifiedValue(t2));
        } finally {
            close(em);
            close(em2);
            clear(emf1);
            closeEMF(emf1);
        }
    }

    public void testInMemorySavepointsWithModifiedInstances() {
        OpenJPAEntityManagerFactory emf1 = createEMF(
            getManagedType(), getSecondaryType(),
            "openjpa.EntityManagerFactoryPool", "true",
            "openjpa.SavepointManager", "in-mem");
        OpenJPAEntityManager em = emf1.createEntityManager();
        OpenJPAEntityManager em2 = null;
        try {
            em.getTransaction().begin();
            T t = em.find(getManagedType(), id);
            Object orig = getModifiedValue(t);
            em.setSavepoint("foo");
            modifyInstance(t);
            assertNotEquals(orig, getModifiedValue(t));

            em2 = deserializeEM(serialize(em));
            T t2 = em2.find(getManagedType(), id);
            assertNotEquals(orig, getModifiedValue(t2));

            em.rollbackToSavepoint("foo");
            assertEquals(orig, getModifiedValue(t));

            em2.rollbackToSavepoint("foo");
            assertEquals(orig, getModifiedValue(t2));
        } finally {
            close(em);
            close(em2);
            clear(emf1);
            closeEMF(emf1);
        }
    }

    public void testEventManagers() {
        TxListener txListener = new TxListener();
        emf.addTransactionListener(txListener);
        LifeListener lifeListener = new LifeListener();
        emf.addLifecycleListener(lifeListener, null);

        OpenJPAEntityManager em = emf.createEntityManager();
        T t = em.find(getManagedType(), id);
        assertEquals(0, lifeListener.refreshCount);
        em.refresh(t);
        assertEquals(1*graphSize(), lifeListener.refreshCount);
        em.getTransaction().begin();
        em.getTransaction().commit();
        em.getTransaction().begin();
        em.getTransaction().commit();
        assertEquals(2, txListener.beginCount);

        OpenJPAEntityManager em2 = deserializeEM(serialize(em));
        assertNotNull(deserializedLifeListener);
        assertEquals(1* graphSize(),
            deserializedLifeListener.refreshCount);
        assertNotSame(lifeListener, deserializedLifeListener);
        T t2 = em2.find(getManagedType(), id);
        em2.refresh(t2);
        assertEquals(2* graphSize(),
            deserializedLifeListener.refreshCount);

        // if this is 3*refreshMultiplier(), that means that there are
        // extra registered listeners
        assertEquals(2* graphSize(), testGlobalRefreshCount);


        assertNotNull(deserializedTxListener);
        assertEquals(2, deserializedTxListener.beginCount);
        assertNotSame(txListener, deserializedTxListener);
        em2.getTransaction().begin();
        em2.getTransaction().rollback();
        assertEquals(3, deserializedTxListener.beginCount);

        // if this is 4, that means that there are extra registered listeners
        assertEquals(3, testGlobalBeginCount);
    }

    byte[] serialize(Object o) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    OpenJPAEntityManager deserializeEM(byte[] bytes) {
        return (OpenJPAEntityManager) deserialize(bytes);
    }

    private Object deserialize(byte[] bytes) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    void close(EntityManager em) {
        if (em != null && em.isOpen() && em.getTransaction().isActive())
            em.getTransaction().rollback();
        if (em != null && em.isOpen())
            em.close();
    }

    protected abstract Class<T> getManagedType();

    protected abstract T newManagedInstance();

    protected abstract void modifyInstance(T t);

    protected abstract Object getModifiedValue(T t);

    /**
     * The number of instances in the graph created
     * by {@link #newManagedInstance()} of type T.
     */
    protected int graphSize() {
        return 1;
    }

    /**
     * An additional type that must be available in this PC. May be null.
     */
    protected Class<?> getSecondaryType() {
        return null;
    }

    private static class TxListener
        extends AbstractTransactionListener
        implements Serializable {

        private int beginCount = 0;

        public TxListener() {

        }

        @Override
        public void afterBegin(TransactionEvent event) {
            beginCount++;
            testGlobalBeginCount++;
        }

        private void readObject(ObjectInputStream in)
            throws ClassNotFoundException, IOException {
            in.defaultReadObject();
            deserializedTxListener = this;
        }
    }

    private static class LifeListener
        extends AbstractLifecycleListener
        implements Serializable {

        private int refreshCount = 0;

        @Override
        public void afterRefresh(LifecycleEvent event) {
            refreshCount++;
            testGlobalRefreshCount++;
        }

        private void readObject(ObjectInputStream in)
            throws ClassNotFoundException, IOException {
            in.defaultReadObject();
            deserializedLifeListener = this;
        }
    }
}
