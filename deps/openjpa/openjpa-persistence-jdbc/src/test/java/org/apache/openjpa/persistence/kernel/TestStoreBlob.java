/*
 * TestStoreBlob.java
 *
 * Created on October 13, 2006, 5:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

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
package org.apache.openjpa.persistence.kernel;



import org.apache.openjpa.persistence.kernel.common.apps.BlobTest;

import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestStoreBlob extends BaseKernelTest {

    /**
     * Creates a new instance of TestStoreBlob
     */
    public TestStoreBlob() {
    }

    public TestStoreBlob(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp(BlobTest.class);
    }

    public void testStoreBlob() {
        OpenJPAEntityManager pm;

        pm = getPM(false, false);
        startTx(pm);
        BlobTest blob = new BlobTest();
        byte[] bytes = new byte[2048];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = randomByte().byteValue();

        blob.setBlob(bytes);
        pm.persist(blob);
        int id = blob.getId();
        endTx(pm);

        byte[] b1 = blob.getBlob();
        endEm(pm);

        pm = getPM(false, false);
        startTx(pm);
        BlobTest blob2 = pm.find(BlobTest.class, id);

        byte[] b2 = blob2.getBlob();

        assertNotNull("Original blob was null", b1);
        assertNotNull("Retrieved blob was null", b2);
        assertEquals("Blob length was not the same", b1.length, b2.length);
        assertBytesEquals("Blob contents did not match", b1, b2);

        endTx(pm);
    }

    private void assertBytesEquals(String str,
        byte[] a, byte[] b) {
        for (int i = 0; i < a.length; i++) {
            assertEquals(str + " [" + i + "]", a[i], b[i]);
        }
    }
}
