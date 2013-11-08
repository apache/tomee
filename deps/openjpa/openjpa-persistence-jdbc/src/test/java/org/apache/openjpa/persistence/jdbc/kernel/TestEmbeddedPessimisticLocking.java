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
package org.apache.openjpa.persistence.jdbc.kernel;


import java.util.*;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.FetchPlan;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.jdbc.kernel.TestSQLListenerTestCase; 


/**
 * Test various bits of embedded-field functionality.
 * <p/>
 * ##### this should test embedded-element collections, maps, at least to ensure
 * ##### that the state managers of values in embedded collections, maps do not
 * ##### have owners. Not relevant in 3.x; will be important in 4.0.
 */
public class TestEmbeddedPessimisticLocking
    extends TestSQLListenerTestCase {


    private boolean supportsLocking;
    private Object oid;
    private OpenJPAEntityManagerFactory emf;
    
    public TestEmbeddedPessimisticLocking(String name)
    {
    	super(name);
    }

	public void setUp() throws Exception{
		super.setUp();
		emf = (OpenJPAEntityManagerFactory)getEmf(getProps());
	}



    public void setUpTestCase() {

        JDBCConfiguration conf =
            (JDBCConfiguration) ((OpenJPAEntityManagerFactorySPI)
                    OpenJPAPersistence.cast(emf)).getConfiguration();

        supportsLocking =
            conf.getDBDictionaryInstance().supportsSelectForUpdate;

       deleteAll(EmbeddedOwnerPC.class);

        OpenJPAEntityManager em =(OpenJPAEntityManager)currentEntityManager();
        startTx(em);
        EmbeddedOwnerPC owner = new EmbeddedOwnerPC(10, 20);
        em.persist(owner);

        EmbeddedPC embedded = new EmbeddedPC();
        embedded.setIntField(4);
        embedded.setStringField("foo");
        owner.setEmbedded(embedded);

        EmbeddedPC embedded2 = new EmbeddedPC();
        embedded2.setIntField(8);
        embedded2.setStringField("bar");
        ComplexEmbeddedPC complex = new ComplexEmbeddedPC();
        complex.setStringField("complex");
        complex.setEmbedded(embedded2);
        owner.setComplexEmbedded(complex);

        endTx(em);

        oid = em.getObjectId(owner);
        em.close();
    }

    private void prepareEMF(OpenJPAEntityManagerFactory emf) {
        // do this to ensure that the SELECT DISTINCT gets performed here.
        OpenJPAEntityManager em = emf.createEntityManager();

        //FIXME jthomas commenting this since setOptimistic is not available for
        //userTx
        //em.getTransaction().setOptimistic(false);
        startTx(em);

        try {
            EmbeddedOwnerPC pc = (EmbeddedOwnerPC) em.getObjectId(oid);
        } finally {
            rollbackTx(em);
            em.close();
        }
    }

    public void testEmbeddedFieldsWithLockedParent() {
        OpenJPAEntityManager em =(OpenJPAEntityManager)currentEntityManager();
        prepareEMF(em.getEntityManagerFactory());
        ((FetchPlan) em.getFetchPlan()).addField(EmbeddedOwnerPC.class,
                "embedded");
//FIXME jthomas commenting this since setOptimistic is not available for userTx
//        pm.currentTransaction().setOptimistic(false);
        startTx(em);
        try {
            sql.clear();
            EmbeddedOwnerPC pc = (EmbeddedOwnerPC) em.getObjectId(oid);
            assertEquals(1, sql.size());

            pc.getEmbedded().setStringField
                (pc.getEmbedded().getStringField() + "bar");
            // should not go to the db for another lock; we use <=, since
            // some databases (like HSQL) don't support locking at all
            assertTrue(sql.size() <= 1);
        } finally {
            rollbackTx(em);
            em.close();
        }
    }

    public void testEmbeddedFieldsWithUnlockedParent() {
        OpenJPAEntityManager em =(OpenJPAEntityManager)currentEntityManager();
		prepareEMF(em.getEntityManagerFactory());
        ((FetchPlan) em.getFetchPlan()).addField(EmbeddedOwnerPC.class,
                "embedded");


//FIXME jthomas commenting this since setOptimistic is not available for userTx
//        pm.currentTransaction().setOptimistic(false);
        startTx(em);

        try {

            //FIXME jthomas - no equivalent found for LockLevels.LOCK_NONE
            //((FetchPlan) pm.getFetchPlan()).setReadLockMode(
            //    LockLevels.LOCK_NONE);

            sql.clear();
            EmbeddedOwnerPC pc = (EmbeddedOwnerPC) em.getObjectId(oid);
            EmbeddedPC embedded = pc.getEmbedded();

            assertNull(getStateManager(pc, em).getLock());
            assertNull(getStateManager(embedded, em).getLock());
            assertEquals(1, sql.size());
            sql.clear();

            embedded.setStringField(embedded.getStringField() + "bar");
            // should not go to the db for another lock -- should have gotten
            // one along with the embedded field's lock
            assertTrue(sql.size() <= 1);
            assertNotNull(getStateManager(pc, em).getLock());

            // embeddeds don't get locks at all.
            assertNull(getStateManager(embedded, em).getLock());

            // owner is dirtied when embedded record is changed
            assertTrue(getStateManager(pc, em).isDirty());
            assertTrue(getStateManager(embedded, em).isDirty());

            pc.setStringField(pc.getStringField() + "bar");
            // should not go to the db for another lock.
            assertTrue(sql.size() <= 1);
        } finally {
            rollbackTx(em);
            em.close();
        }
    }

    public void testComplexEmbeddedFieldsWithLockedParent() {

        OpenJPAEntityManager em =(OpenJPAEntityManager)currentEntityManager();
		prepareEMF(em.getEntityManagerFactory());
		em.getFetchPlan().setMaxFetchDepth(-1);
        ((FetchPlan) em.getFetchPlan()).addField(EmbeddedOwnerPC.class,
                "complexEmbedded");
        ((FetchPlan) em.getFetchPlan()).addField(RecursivelyEmbeddedPC.class,
                "embedded");

//FIXME jthomas commenting this since setOptimistic is not available for userTx
//        pm.currentTransaction().setOptimistic(false);
        startTx(em);
        try {
            sql.clear();
            EmbeddedOwnerPC pc = (EmbeddedOwnerPC) em.getObjectId(oid);
            assertEquals(1, sql.size());

            pc.getComplexEmbedded().getEmbedded().setStringField
                (pc.getComplexEmbedded().getEmbedded().getStringField() +
                    "bar");
            // should not go to the db for another lock.
            assertTrue(sql.size() <= 1);
        } finally {
            rollbackTx(em);
            em.close();
        }
    }

    public void testComplexEmbeddedFieldsWithUnlockedParent() {
        // doing this because setting the read lock level
        // does not seem to be disabling FOR UPDATE.


        OpenJPAEntityManager em =(OpenJPAEntityManager)currentEntityManager();
		prepareEMF(em.getEntityManagerFactory());
		em.getFetchPlan().setMaxFetchDepth(-1);
        ((FetchPlan) em.getFetchPlan()).addField(EmbeddedOwnerPC.class,
                "complexEmbedded");
        ((FetchPlan) em.getFetchPlan()).addField(RecursivelyEmbeddedPC.class,
                "embedded");



//FIXME jthomas commenting this since setOptimistic is not available for userTx
//        pm.currentTransaction().setOptimistic(false);
        startTx(em);

        try {
            //FIXME jthomas FetchPlan.LOCK_NONE??
            //((FetchPlan) em.getFetchPlan()).setReadLockLevel
            //    (FetchPlan.LOCK_NONE);

            sql.clear();
            EmbeddedOwnerPC pc = (EmbeddedOwnerPC) em.getObjectId(oid);
            ComplexEmbeddedPC complex = pc.getComplexEmbedded();
            EmbeddedPC embedded = complex.getEmbedded();

            assertNull(getStateManager(pc, em).getLock());
            assertNull(getStateManager(complex, em).getLock());
            assertNull(getStateManager(embedded, em).getLock());
            assertEquals(1, sql.size());
            sql.clear();

            embedded.setStringField(embedded.getStringField() + "bar");
            // should not go to the db for another lock -- should have gotten
            // one along with the embedded field's lock
            assertTrue(sql.size() <= 1);
            assertNotNull(getStateManager(pc, em).getLock());

            // embeddeds don't get locks at all.
            assertNull(getStateManager(complex, em).getLock());
            assertNull(getStateManager(embedded, em).getLock());

            // owner is dirtied when embedded record is changed
            assertTrue(getStateManager(pc, em).isDirty());
            assertTrue(getStateManager(complex, em).isDirty());
            assertTrue(getStateManager(embedded, em).isDirty());

            complex.setStringField(complex.getStringField() + "bar");
            // should not go to the db for another lock.
            assertTrue(sql.size() <= 1);

            pc.setStringField(pc.getStringField() + "bar");
            // should not go to the db for another lock.
            assertTrue(sql.size() <= 1);
        } finally {
            rollbackTx(em);
            em.close();
        }
    }

	private Map getProps() {
		Map props=new HashMap();
		props.put("openjpa.DataCache", "true");
		props.put("openjpa.RemoteCommitProvider", "sjvm");
		props.put("openjpa.FlushBeforeQueries", "true");
		props.put("javax.jdo.option.IgnoreCache", "false");
        //propsMap.put("openjpa.BrokerImpl", "kodo.datacache.CacheTestBroker");
        //CacheTestBroker.class.getName());
		return props;
	}


}
