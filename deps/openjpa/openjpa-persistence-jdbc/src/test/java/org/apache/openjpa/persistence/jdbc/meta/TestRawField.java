/*
 * TestRawField.java
 *
 * Created on October 4, 2006, 2:57 PM
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

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestRawField
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
    
    /** Creates a new instance of TestRawField */
    public TestRawField(String name) 
    {
    	super(name);
    }
    
    public void setUp() {
       deleteAll(RawField.class);
    }

    public void testRaw() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pm.getTransaction().begin();
        RawField pc = new RawField();
        pc.setString("BAR");
        pm.persist(pc);
        Object oid = pm.getObjectId(pc);

        // batching?
        pc = new RawField();
        pc.setString("GOO");
        pm.persist(pc);
        pm.getTransaction().commit();
        pm.close();

        pm = (OpenJPAEntityManager)currentEntityManager();
        pc = (RawField) pm.getObjectId(oid);
        assertEquals(pc.getString(), "BARFOO", pc.getString());
        pm.close();
    }    
}
