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
package org.apache.openejb.test.entity.cmp2;

import org.apache.openejb.test.entity.cmp.BasicCmpHome;
import org.apache.openejb.test.entity.cmp.BasicCmpObject;


/**
 * [2] Should be run as the second test suite of the BasicCmpTestClients
 */
public class Cmp2HomeIntfcTests extends BasicCmp2TestClient {

    public Cmp2HomeIntfcTests() {
        super("HomeIntfc.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/cmp2/BasicCmpHome");
        ejbHome = (BasicCmpHome) javax.rmi.PortableRemoteObject.narrow(obj, BasicCmpHome.class);
    }

    //===============================
    // Test home interface methods
    //
    public void test01_create() throws Exception {
        ejbObject = ejbHome.createObject("First Bean");
        assertNotNull("The EJBObject is null", ejbObject);
    }

    public void test02_findByPrimaryKey() throws Exception {
        ejbPrimaryKey = ejbObject.getPrimaryKey();
        ejbObject = ejbHome.findByPrimaryKey((Integer) ejbPrimaryKey);
        assertNotNull("The EJBObject is null", ejbObject);
    }

    public void test03_findByLastName() throws Exception {
        Integer[] keys = new Integer[3];
        ejbObject = ejbHome.createObject("David Blevins");
        keys[0] = (Integer) ejbObject.getPrimaryKey();

        ejbObject = ejbHome.createObject("Dennis Blevins");
        keys[1] = (Integer) ejbObject.getPrimaryKey();

        ejbObject = ejbHome.createObject("Claude Blevins");
        keys[2] = (Integer) ejbObject.getPrimaryKey();

        java.util.Collection objects = ejbHome.findByLastName("Blevins");
        assertNotNull("The Collection is null", objects);
        assertEquals("The Collection is not the right size.", keys.length, objects.size());
        Object[] objs = objects.toArray();
        for (int i = 0; i < objs.length; i++) {
            ejbObject = (BasicCmpObject) javax.rmi.PortableRemoteObject.narrow(objs[i], BasicCmpObject.class);
            // This could be problematic, it assumes the order of the collection.
            assertEquals("The primary keys are not equal.", keys[i], ejbObject.getPrimaryKey());
        }
    }

    public void test04_homeMethod() {
        try {
            int expected = 8;
            int actual = ejbHome.sum(5, 3);
            assertEquals("home method returned wrong result", expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
    //
    // Test home interface methods
    //===============================

}
