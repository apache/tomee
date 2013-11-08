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
package org.apache.openjpa.jta;

import javax.transaction.TransactionManager;

import org.apache.openjpa.ee.ManagedRuntime;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Simulates a container transaction around a test method. 
 * <br>
 * The concrete tests derived from this class must adhere to the following guidelines:
 * <LI>They must configure openjpa.ManagedRuntime in setUp() properties as
 * to <code>org.apache.openjpa.jta.JTAManagedRuntime</code> 
 * 
 * <LI>The tests themselves must not try to access the transaction directly 
 * i.e. not call EntityManager.getTransaction(). Attempt to do so will raise
 * exception.
 * 
 * <LI>The test methods must use  EntityManager.joinTransaction() to use a transaction.
 * This test harness ensures that a transaction is active before running each
 * test method.
 * 
 * <LI>The test methods may call commit() and rollback() methods of this class
 * to demarcate transaction.  
 * 
 * @author Pinaki Poddar
 *
 */
public abstract class ContainerTest extends SingleEMFTestCase {
    ManagedRuntime runtime;
    
    @Override
    public void setUp(Object...props) {
        super.setUp(props);
        runtime = emf.getConfiguration().getManagedRuntimeInstance();
        assertNotNull(runtime);
    }
    
    @Override
    public void runTest() throws Throwable {
        TransactionManager txm = runtime.getTransactionManager();
        txm.begin();
        try {
            super.runTest();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }
    
    @Override
    public void tearDown() throws Exception {
        try {
            if (emf != null && emf.isOpen()) {
                // can't use AbstractPersistenceTestCase.closeEMF() due to using managed transactions
                // closeAllOpenEMs(emf);
                emf.close();
            }
        } catch (Exception e) {
            // if a test failed, swallow any exceptions that happen
            // during tear-down, as these just mask the original problem.
            if (testResult.wasSuccessful()) {
                throw e;
            }
        } finally {
            emf = null;
        }
    }
    
    protected void commit() {
        try {
            runtime.getTransactionManager().commit();
        } catch (Throwable e) {
            e.printStackTrace();
        } 
    }
    
    protected void rollback() {
        try {
            runtime.getTransactionManager().rollback();
        } catch (Throwable e) {
            e.printStackTrace();
        } 
    }
    

}
