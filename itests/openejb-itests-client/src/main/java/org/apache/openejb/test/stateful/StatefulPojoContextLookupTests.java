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

/**
 * [4] Should be run as the fourth test suite of the StatefulTestClients
 */
public class StatefulPojoContextLookupTests extends StatefulTestClient {

    protected EncStatefulHome ejbHome;
    protected EncStatefulObject ejbObject;

    public StatefulPojoContextLookupTests() {
        super("JNDI_ENC.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        final Object obj = initialContext.lookup("client/tests/stateful/ContextLookupStatefulPojoBean");
        ejbHome = (EncStatefulHome) obj;
        ejbObject = ejbHome.create("Enc Bean");

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

    public void test01_lookupStringEntry() {
        try {
            ejbObject.lookupStringEntry();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test02_lookupDoubleEntry() {
        try {
            ejbObject.lookupDoubleEntry();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test03_lookupLongEntry() {
        try {
            ejbObject.lookupLongEntry();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test04_lookupFloatEntry() {
        try {
            ejbObject.lookupFloatEntry();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test05_lookupIntegerEntry() {
        try {
            ejbObject.lookupIntegerEntry();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test06_lookupShortEntry() {
        try {
            ejbObject.lookupShortEntry();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test07_lookupBooleanEntry() {
        try {
            ejbObject.lookupBooleanEntry();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test08_lookupByteEntry() {
        try {
            ejbObject.lookupByteEntry();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test09_lookupCharacterEntry() {
        try {
            ejbObject.lookupCharacterEntry();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test10_lookupEntityBean() {
        try {
            ejbObject.lookupEntityBean();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test11_lookupStatefulBean() {
        try {
            ejbObject.lookupStatefulBean();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test12_lookupStatelessBean() {
        try {
            ejbObject.lookupStatelessBean();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test13_lookupResource() {
        try {
            ejbObject.lookupResource();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test14_lookupPersistenceUnit() {
        try {
            ejbObject.lookupPersistenceUnit();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test15_lookupPersistenceContext() {
        try {
            ejbObject.lookupPersistenceContext();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test18_lookupSessionContext() {
        try {
            ejbObject.lookupSessionContext();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test23_lookupJMSConnectionFactory() {
        try {
            ejbObject.lookupJMSConnectionFactory();
        } catch (final TestFailureException e) {
            throw e.error;
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
}
