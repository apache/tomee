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
package org.apache.openjpa.slice;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import junit.framework.TestResult;
import org.apache.openjpa.kernel.AbstractBrokerFactory;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.JPAFacadeHelper;

/**
 * Base test class providing persistence utilities.
 */
public abstract class PersistenceTestCase
    extends TestCase {

    /**
     * Marker object you an pass to {@link #setUp} to indicate that the
     * database tables should be cleared.
     */
    protected static final Object CLEAR_TABLES = new Object();

    /**
     * The {@link TestResult} instance for the current test run.
     */
    protected TestResult testResult;

    /**
     * Create an entity manager factory. Put {@link #CLEAR_TABLES} in
     * this list to tell the test framework to delete all table contents
     * before running the tests.
     *
     * @param props list of persistent types used in testing and/or
     * configuration values in the form key,value,key,value...
     */
    protected OpenJPAEntityManagerFactorySPI createEMF(Object... props) {
        return createNamedEMF(getPersistenceUnitName(), props);
    }

    /**
     * The name of the persistence unit that this test class should use
     * by default. This defaults to "test".
     */
    protected String getPersistenceUnitName() {
        return "test";
    }

    /**
     * Create an entity manager factory for persistence unit <code>pu</code>.
     * Put {@link #CLEAR_TABLES} in
     * this list to tell the test framework to delete all table contents
     * before running the tests.
     *
     * @param props list of persistent types used in testing and/or
     * configuration values in the form key,value,key,value...
     */
    protected OpenJPAEntityManagerFactorySPI createNamedEMF(String pu,
        Object... props) {
        Map map = new HashMap(System.getProperties());
        List<Class> types = new ArrayList<Class>();
        boolean prop = false;
        for (int i = 0; i < props.length; i++) {
            if (prop) {
                map.put(props[i - 1], props[i]);
                prop = false;
            } else if (props[i] == CLEAR_TABLES) {
                map.put("openjpa.jdbc.SynchronizeMappings",
                    "buildSchema(ForeignKeys=true," 
                    + "SchemaAction='add,deleteTableContents')");
            } else if (props[i] instanceof Class)
                types.add((Class) props[i]);
            else if (props[i] != null)
                prop = true;
        }

        if (!types.isEmpty()) {
            StringBuffer buf = new StringBuffer();
            for (Class c : types) {
                if (buf.length() > 0)
                    buf.append(";");
                buf.append(c.getName());
            }
            map.put("openjpa.MetaDataFactory",
                "jpa(Types=" + buf.toString() + ")");
        }
        if (!map.containsKey("openjpa.ConnectionFactoryProperties")) {
            map.put("openjpa.ConnectionFactoryProperties", "PrintParameters=true");
        }
        return (OpenJPAEntityManagerFactorySPI) Persistence.
            createEntityManagerFactory(pu, map);
    }

    @Override
    public void run(TestResult testResult) {
        this.testResult = testResult;
        super.run(testResult);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            super.tearDown();
        } catch (Exception e) {
            // if a test failed, swallow any exceptions that happen
            // during tear-down, as these just mask the original problem.
            if (testResult.wasSuccessful())
                throw e;
        }
    }

    /**
     * Safely close the given factory.
     */
    protected boolean closeEMF(EntityManagerFactory emf) {
        if (emf == null || !emf.isOpen())
            return false;
        
        closeAllOpenEMs(emf);
        emf.close();
        return !emf.isOpen();
    }

    /**
     * Closes all open entity managers after first rolling back any open
     * transactions.
     */
    protected void closeAllOpenEMs(EntityManagerFactory emf) {
        if (emf == null || !emf.isOpen())
            return;

        for (Iterator iter = ((AbstractBrokerFactory) JPAFacadeHelper
            .toBrokerFactory(emf)).getOpenBrokers().iterator();
            iter.hasNext(); ) {
            Broker b = (Broker) iter.next();
            if (b != null && !b.isClosed()) {
                EntityManager em = JPAFacadeHelper.toEntityManager(b);
                if (em.getTransaction().isActive())
                    em.getTransaction().rollback();
                em.close();
            }
        }
    }

    /**
     * Delete all instances of the given types using bulk delete queries,
     * but do not close any open entity managers.
     */
    protected void clear(EntityManagerFactory emf, Class... types) {
        if (emf == null || types.length == 0)
            return;

        List<ClassMetaData> metas = new ArrayList<ClassMetaData>(types.length);
        for (Class c : types) {
            ClassMetaData meta = JPAFacadeHelper.getMetaData(emf, c);
            if (meta != null)
                metas.add(meta);
        }
        clear(emf, false, metas.toArray(new ClassMetaData[metas.size()]));
    }

    /**
     * Delete all instances of the persistent types registered with the given
     * factory using bulk delete queries, after first closing all open entity
     * managers (and rolling back any open transactions).
     */
    protected void clear(EntityManagerFactory emf) {
        if (emf == null)
            return;
        clear(emf, true, ((OpenJPAEntityManagerFactorySPI) emf).
            getConfiguration().getMetaDataRepositoryInstance().getMetaDatas());
    }

    /**
     * Delete all instances of the given types using bulk delete queries.
     * @param closeEMs TODO
     */
    private void clear(EntityManagerFactory emf, boolean closeEMs,
            ClassMetaData... types) {
        if (emf == null || types.length == 0)
            return;
        
        // prevent deadlock by closing the open entity managers 
        // and rolling back any open transactions 
        // before issuing delete statements on a new entity manager.
        if (closeEMs)
            closeAllOpenEMs(emf);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (ClassMetaData meta : types) {
            if (!meta.isMapped() || meta.isEmbeddedOnly() 
                || Modifier.isAbstract(meta.getDescribedType().getModifiers()))
                continue;
//            em.createQuery("DELETE FROM " + meta.getTypeAlias() + " o").
//                executeUpdate();
        }
        em.getTransaction().commit();
        em.close();
    }

    /**
     * Return the entity name for the given type.   
     */
    protected String entityName(EntityManagerFactory emf, Class c) {
        ClassMetaData meta = JPAFacadeHelper.getMetaData(emf, c);
        return (meta == null) ? null : meta.getTypeAlias();
    }

    public static void assertNotEquals(Object o1, Object o2) {
        if (o1 == o2)
            fail("expected args to be different; were the same instance.");
        else if (o1 == null || o2 == null)
            return;
        else if (o1.equals(o2))
            fail("expected args to be different; compared equal.");
    }
}
