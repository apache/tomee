/**
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
package org.apache.openejb.test.stateful;

import org.apache.openejb.test.TestFailureException;
import org.apache.openejb.test.TestManager;

public class StatefulPersistenceContextTests extends StatefulTestClient {

    protected PersistenceContextStatefulObject ejbObject;
    protected PersistenceContextStatefulHome ejbHome;

    public StatefulPersistenceContextTests() {
        super("PERSISTENCE_CONTEXT.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        ejbHome = (PersistenceContextStatefulHome) initialContext.lookup("client/tests/stateful/PersistenceContextStatefulBean");
        ejbObject = ejbHome.create();

        /*[2] Create database table */
        TestManager.getDatabase().createEntityTable();
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        try {
            /*[1] Drop database table */
            TestManager.getDatabase().dropEntityTable();
        } catch (final Exception e) {
            throw e;
        } finally {
            super.tearDown();
        }
    }

    public void test01_persistenceContext() {
        try {
            ejbObject.testPersistenceContext();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test02_extendedPersistenceContext() {
        try {
            ejbObject.testExtendedPersistenceContext();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test03_propagatedPersistenceContext() {
        try {
            ejbObject.testPropagatedPersistenceContext();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test04_propogation() {
        try {
            ejbObject.testPropgation();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
}
