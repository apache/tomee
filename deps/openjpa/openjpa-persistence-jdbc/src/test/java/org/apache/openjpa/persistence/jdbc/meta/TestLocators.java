/*
 * TestLocators.java
 *
 * Created on October 3, 2006, 4:37 PM
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
package org.apache.openjpa.persistence.jdbc.meta;

import java.util.*;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.persistence.Extent;

import org.apache.openjpa.persistence.jdbc.common.apps.*;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;


public class TestLocators
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
    
    private OpenJPAEntityManagerFactory pmf;

    public TestLocators(String name) {
        super(name);
    }    
    
    /** Creates a new instance of TestLocators */
    public TestLocators() {
    }
    public void setUp() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);
        
        Extent e = pm.createExtent(LocatorTestObject.class, true);
        for (Iterator i = e.iterator(); i.hasNext();) {
            pm.remove(i.next());
            
        }
        endTx(pm);
        pm.close();
    }

    public void tearDown()
        throws Exception {
        super.tearDown();

       deleteAll(LocatorTestObject.class);
    }

    public void testBLOBs() {
        doBlobTest(50000);
    }

    public void testSmallBLOBs() {
        doBlobTest(50);

        if (getCurrentPlatform() == AbstractTestCase.Platform.ORACLE) {
            OpenJPAEntityManager pm =
                (OpenJPAEntityManager)currentEntityManager();
            JDBCConfiguration conf = (JDBCConfiguration)
                ((OpenJPAEntityManagerSPI) pm).getConfiguration();
            DBDictionary dict = (DBDictionary)
                conf.getDBDictionaryInstance();

            int t = dict.maxEmbeddedBlobSize;
            doBlobTest(t - 1);
            doBlobTest(t);
            doBlobTest(t + 1);
        }
    }

    public void doBlobTest(int size) {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);
        LocatorTestObject o = new LocatorTestObject();
        byte[] bytes = new byte[size];
        Arrays.fill(bytes, (byte) 'b');
        o.setBytes(bytes);
        pm.persist(o);
        Object oid = pm.getObjectId(o);
        endTx(pm);
        pm.close();

        pm = (OpenJPAEntityManager)currentEntityManager();
        o = (LocatorTestObject) pm.getObjectId(oid);
        byte[] newbytes = o.getBytes();
        assertNotNull(newbytes);
        assertEquals(bytes.length, newbytes.length);
        for (int i = 0; i < bytes.length; i++) {
            assertEquals(bytes[i], newbytes[i]);
        }
    }

    public void testShrinkBLOB() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);
        LocatorTestObject o = new LocatorTestObject();
        byte[] bytes = new byte[50000];
        Arrays.fill(bytes, (byte) 'b');
        o.setBytes(bytes);
        pm.persist(o);
        Object oid = pm.getObjectId(o);
        endTx(pm);
        pm.close();

        pm = (OpenJPAEntityManager)currentEntityManager();
        o = (LocatorTestObject) pm.getObjectId(oid);

        startTx(pm);
        bytes = new byte[40000];
        Arrays.fill(bytes, (byte) 'c');
        o.setBytes(bytes);
        endTx(pm);
        pm.close();

        pm = (OpenJPAEntityManager)currentEntityManager();
        o = (LocatorTestObject) pm.getObjectId(oid);
        byte[] newbytes = o.getBytes();
        assertEquals(bytes.length, newbytes.length);
        for (int i = 0; i < bytes.length; i++) {
            assertEquals(bytes[i], newbytes[i]);
        }
    }

    public void testCLOBs() {
        doClobTest(50000);
    }

    public void testSmallCLOBs() {
        doClobTest(50);

        if (getCurrentPlatform() == AbstractTestCase.Platform.ORACLE) {
            OpenJPAEntityManager pm =
                (OpenJPAEntityManager)currentEntityManager();
            JDBCConfiguration conf = (JDBCConfiguration)
                ((OpenJPAEntityManagerSPI) pm).getConfiguration();
            DBDictionary dict = (DBDictionary)
                conf.getDBDictionaryInstance();

            int t = dict.maxEmbeddedClobSize;
            doClobTest(t - 1);
            doClobTest(t);
            doClobTest(t + 1);
        }
    }

    public void doClobTest(int size) {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);
        LocatorTestObject o = new LocatorTestObject();
        char[] chars = new char[size];
        Arrays.fill(chars, (char) 'c');
        o.setClobString(new String(chars));
        pm.persist(o);
        Object oid = pm.getObjectId(o);
        endTx(pm);
        pm.close();

        pm = (OpenJPAEntityManager)currentEntityManager();
        o = (LocatorTestObject) pm.getObjectId(oid);
        char[] newchars = o.getClobString().toCharArray();

        assertNotNull(newchars);
        assertEquals(chars.length, newchars.length);
        for (int i = 0; i < chars.length; i++) {
            assertEquals(chars[i], newchars[i]);
        }
    }

    public void testShrinkCLOB() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);
        LocatorTestObject o = new LocatorTestObject();
        char[] chars = new char[50000];
        Arrays.fill(chars, (char) 'c');
        o.setClobString(new String(chars));
        pm.persist(o);
        Object oid = pm.getObjectId(o);
        endTx(pm);
        pm.close();

        pm = (OpenJPAEntityManager)currentEntityManager();
        o = (LocatorTestObject) pm.getObjectId(oid);

        startTx(pm);
        chars = new char[40000];
        Arrays.fill(chars, (char) 'd');
        o.setClobString(new String(chars));
        endTx(pm);
        pm.close();

        pm = (OpenJPAEntityManager)currentEntityManager();
        o = (LocatorTestObject) pm.getObjectId(oid);
        char[] newchars = o.getClobString().toCharArray();
        assertEquals(chars.length, newchars.length);
        for (int i = 0; i < chars.length; i++) {
            assertEquals(chars[i], newchars[i]);
        }
    }
    
}
