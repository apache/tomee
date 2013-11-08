/*
 * TestMultiTableSelfCollection.java
 *
 * Created on October 4, 2006, 2:35 PM
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

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestMultiTableSelfCollection
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {    
    private Object a1oid;
    private Object a2oid;
    private Object a3oid;
    private Object a4oid;
    
    private Object c1oid;
    private Object c2oid;
    private Object c3oid;
    private Object c4oid;
    
    public TestMultiTableSelfCollection(String name) {
        super(name);
    }
    
    /** Creates a new instance of TestMultiTableSelfCollection */
    public TestMultiTableSelfCollection() {
    }
    
    public void setUp() {
       deleteAll(MultiA.class);
        
        OpenJPAEntityManager pm = (OpenJPAEntityManager)currentEntityManager();
        pm.getTransaction().begin();
        
        Collection os = new ArrayList();
        MultiA a1 = new MultiA();
        MultiA a2 = new MultiA();
        MultiA a3 = new MultiA();
        MultiA a4 = new MultiA();
        MultiC c1 = new MultiC();
        MultiC c2 = new MultiC();
        MultiC c3 = new MultiC();
        MultiC c4 = new MultiC();
        os.add(a1);
        os.add(a2);
        os.add(a3);
        os.add(a4);
        os.add(c1);
        os.add(c2);
        os.add(c3);
        os.add(c4);
        
        c1.getMultiAs().add(a1);
        c1.getMultiAs().add(a2);
        a1.getMultiCs().add(c1);
        a2.getMultiCs().add(c1);
        
        c2.getMultiAs().add(a1);
        c2.getMultiAs().add(a2);
        a1.getMultiCs().add(c2);
        a2.getMultiCs().add(c2);
        
        c3.getMultiAs().add(a3);
        c3.getMultiAs().add(a4);
        a3.getMultiCs().add(c3);
        a4.getMultiCs().add(c3);
        
        c4.getMultiAs().add(a3);
        c4.getMultiAs().add(a4);
        a3.getMultiCs().add(c4);
        a4.getMultiCs().add(c4);
        
        pm.persistAll(os);
        pm.getTransaction().commit();
        
        a1oid = pm.getObjectId(a1);
        a2oid = pm.getObjectId(a2);
        a3oid = pm.getObjectId(a3);
        a4oid = pm.getObjectId(a4);
        
        c1oid = pm.getObjectId(c1);
        c2oid = pm.getObjectId(c2);
        c3oid = pm.getObjectId(c3);
        c4oid = pm.getObjectId(c4);
        
        pm.close();
    }
    
    public void testSelfCollections() {
        OpenJPAEntityManager pm = (OpenJPAEntityManager)currentEntityManager();
        
        // check that all the sets are the right size, and equal where
        // they should be.
        
        MultiA a1 = (MultiA) pm.getObjectId(a1oid);
        assertEquals(2, a1.getMultiCs().size());
        
        MultiA a2 = (MultiA) pm.getObjectId(a2oid);
        assertEquals(2, a2.getMultiCs().size());
        
        assertEquals(a1.getMultiCs(), a2.getMultiCs());
        
        MultiA a3 = (MultiA) pm.getObjectId(a3oid);
        assertEquals(2, a3.getMultiCs().size());
        
        MultiA a4 = (MultiA) pm.getObjectId(a4oid);
        assertEquals(2, a4.getMultiCs().size());
        
        assertEquals(a3.getMultiCs(), a4.getMultiCs());
        
        MultiC c1 = (MultiC) pm.getObjectId(c1oid);
        assertEquals(2, c1.getMultiAs().size());
        
        MultiC c2 = (MultiC) pm.getObjectId(c2oid);
        assertEquals(2, c2.getMultiAs().size());
        
        assertEquals(c1.getMultiAs(), c2.getMultiAs());
        
        MultiC c3 = (MultiC) pm.getObjectId(c3oid);
        assertEquals(2, c3.getMultiAs().size());
        
        MultiC c4 = (MultiC) pm.getObjectId(c4oid);
        assertEquals(2, c4.getMultiAs().size());
        
        assertEquals(c3.getMultiAs(), c4.getMultiAs());
        
        // check that all the sets contain the correct values, and
        // don't contain the wrong values. Probably don't need to do
        // the above check as well.
        
        assertTrue(a1.getMultiCs().contains(c1));
        assertTrue(a1.getMultiCs().contains(c2));
        assertTrue(!a1.getMultiCs().contains(c3));
        assertTrue(!a1.getMultiCs().contains(c4));
        
        assertTrue(a2.getMultiCs().contains(c1));
        assertTrue(a2.getMultiCs().contains(c2));
        assertTrue(!a2.getMultiCs().contains(c3));
        assertTrue(!a2.getMultiCs().contains(c4));
        
        assertTrue(!a3.getMultiCs().contains(c1));
        assertTrue(!a3.getMultiCs().contains(c2));
        assertTrue(a3.getMultiCs().contains(c3));
        assertTrue(a3.getMultiCs().contains(c4));
        
        assertTrue(!a4.getMultiCs().contains(c1));
        assertTrue(!a4.getMultiCs().contains(c2));
        assertTrue(a4.getMultiCs().contains(c3));
        assertTrue(a4.getMultiCs().contains(c4));
        
        assertTrue(c1.getMultiAs().contains(a1));
        assertTrue(c1.getMultiAs().contains(a2));
        assertTrue(!c1.getMultiAs().contains(a3));
        assertTrue(!c1.getMultiAs().contains(a4));
        
        assertTrue(c2.getMultiAs().contains(a1));
        assertTrue(c2.getMultiAs().contains(a2));
        assertTrue(!c2.getMultiAs().contains(a3));
        assertTrue(!c2.getMultiAs().contains(a4));
        
        assertTrue(!c3.getMultiAs().contains(a1));
        assertTrue(!c3.getMultiAs().contains(a2));
        assertTrue(c3.getMultiAs().contains(a3));
        assertTrue(c3.getMultiAs().contains(a4));
        
        assertTrue(!c4.getMultiAs().contains(a1));
        assertTrue(!c4.getMultiAs().contains(a2));
        assertTrue(c4.getMultiAs().contains(a3));
        assertTrue(c4.getMultiAs().contains(a4));
        
        pm.close();
    }
    
    
}
