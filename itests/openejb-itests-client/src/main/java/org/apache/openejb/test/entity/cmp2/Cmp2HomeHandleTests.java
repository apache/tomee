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

import javax.ejb.EJBHome;
import javax.ejb.HomeHandle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.MarshalledObject;

/**
 * [6] Should be run as the sixth test suite of the BasicCmpTestClients
 */
public class Cmp2HomeHandleTests extends BasicCmp2TestClient {

    public Cmp2HomeHandleTests() {
        super("HomeHandle.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        final Object obj = initialContext.lookup("client/tests/entity/cmp2/BasicCmpHome");
        ejbHome = (BasicCmpHome) obj;
        ejbHomeHandle = ejbHome.getHomeHandle();
    }

    //=================================
    // Test home handle methods
    //
    public void test01_getEJBHome() {
        try {
            final EJBHome home = ejbHomeHandle.getEJBHome();
            assertNotNull("The EJBHome is null", home);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void Xtest02_copyHandleByMarshalledObject() {
        try {
            final MarshalledObject obj = new MarshalledObject(ejbHomeHandle);
            final HomeHandle copy = (HomeHandle) obj.get();

            assertNotNull("The HomeHandle copy is null", copy);
            final EJBHome home = copy.getEJBHome();
            assertNotNull("The EJBHome is null", home);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void Xtest03_copyHandleBySerialize() {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(ejbHomeHandle);
            oos.flush();
            oos.close();
            final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            final ObjectInputStream ois = new ObjectInputStream(bais);
            final HomeHandle copy = (HomeHandle) ois.readObject();

            assertNotNull("The HomeHandle copy is null", copy);
            final EJBHome home = copy.getEJBHome();
            assertNotNull("The EJBHome is null", home);
        } catch (final Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }
    //
    // Test home handle methods
    //=================================

}
