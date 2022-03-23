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
package org.apache.openejb.test.entity.cmp2;

import org.apache.openejb.test.entity.cmp.ComplexCmpHome;

import jakarta.ejb.EJBObject;
import jakarta.ejb.Handle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.MarshalledObject;

/**
 * [7] Should be run as the seventh test suite of the BasicCmpTestClients
 */
public class Complex2HandleTests extends ComplexCmp2TestClient {
    public Complex2HandleTests() {
        super("Handle.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        final Object obj = initialContext.lookup("client/tests/entity/cmp2/ComplexCmpHome");
        ejbHome = (ComplexCmpHome) obj;
        ejbObject = ejbHome.createObject("Fifth Bean");
        ejbHandle = ejbObject.getHandle();
    }

    protected void tearDown() throws Exception {
        if (ejbObject != null) {
            ejbObject.remove();
        }
        super.tearDown();
    }

    //=================================
    // Test handle methods
    //
    public void test01_getEJBObject() {

        try {
            final EJBObject object = ejbHandle.getEJBObject();
            assertNotNull("The EJBObject is null", object);
            // Wait until isIdentical is working.
            //assertTrue("EJBObjects are not identical", object.isIdentical(ejbObject));
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void Xtest02_copyHandleByMarshalledObject() {
        try {
            final MarshalledObject obj = new MarshalledObject(ejbHandle);
            final Handle copy = (Handle) obj.get();

            final EJBObject object = copy.getEJBObject();
            assertNotNull("The EJBObject is null", object);
            assertTrue("EJBObjects are not identical", object.isIdentical(ejbObject));
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void Xtest03_copyHandleBySerialize() {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(ejbHandle);
            oos.flush();
            oos.close();
            final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            final ObjectInputStream ois = new ObjectInputStream(bais);
            final Handle copy = (Handle) ois.readObject();

            final EJBObject object = copy.getEJBObject();
            assertNotNull("The EJBObject is null", object);
            assertTrue("EJBObjects are not identical", object.isIdentical(ejbObject));
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * This remove method of the EJBHome is placed hear as it
     * is more a test on the handle then on the remove method
     * itself.
     */
    public void test04_EJBHome_remove() {
        try {
            ejbHome.remove(ejbHandle);
            try {
                ejbObject.businessMethod("Should throw an exception");
                assertTrue("Calling business method after removing the EJBObject does not throw an exception", false);
            } catch (final Exception e) {
                assertTrue(true);
            }
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        } finally {
            ejbObject = null;
        }
    }

    //
    // Test handle methods
    //=================================
}
