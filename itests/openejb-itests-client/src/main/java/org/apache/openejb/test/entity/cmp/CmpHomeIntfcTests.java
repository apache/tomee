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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.entity.cmp;


/**
 * [2] Should be run as the second test suite of the BasicCmpTestClients
 */
public class CmpHomeIntfcTests extends BasicCmpTestClient {

    public CmpHomeIntfcTests() {
        super("HomeIntfc.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        final Object obj = initialContext.lookup("client/tests/entity/cmp/BasicCmpHome");
        ejbHome = (BasicCmpHome) obj;
    }

    //===============================
    // Test home interface methods
    //
    public void test01_create() {
        try {
            ejbObject = ejbHome.createObject("First Bean");
            assertNotNull("The EJBObject is null", ejbObject);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test02_findByPrimaryKey() {
        try {
            ejbPrimaryKey = ejbObject.getPrimaryKey();
            ejbObject = ejbHome.findByPrimaryKey((Integer) ejbPrimaryKey);
            assertNotNull("The EJBObject is null", ejbObject);
        } catch (final Exception e) {
            e.printStackTrace();
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test03_findByLastName() {
        final Integer[] keys = new Integer[3];
        try {
            ejbObject = ejbHome.createObject("David Blevins");
            keys[0] = (Integer) ejbObject.getPrimaryKey();

            ejbObject = ejbHome.createObject("Dennis Blevins");
            keys[1] = (Integer) ejbObject.getPrimaryKey();

            ejbObject = ejbHome.createObject("Claude Blevins");
            keys[2] = (Integer) ejbObject.getPrimaryKey();
        } catch (final Exception e) {
            fail("Received exception while preparing the test: " + e.getClass() + " : " + e.getMessage());
        }

        try {
            final java.util.Collection objects = ejbHome.findByLastName("Blevins");
            assertNotNull("The Collection is null", objects);
            assertEquals("The Collection is not the right size.", keys.length, objects.size());
            final Object[] objs = objects.toArray();
            for (int i = 0; i < objs.length; i++) {
                ejbObject = (BasicCmpObject) objs[i];
                // This could be problematic, it assumes the order of the collection.
                assertEquals("The primary keys are not equal.", keys[i], ejbObject.getPrimaryKey());
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void test04_homeMethod() {
        try {
            final int expected = 8;
            final int actual = ejbHome.sum(5, 3);
            assertEquals("home method returned wrong result", expected, actual);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
    //
    // Test home interface methods
    //===============================

}
