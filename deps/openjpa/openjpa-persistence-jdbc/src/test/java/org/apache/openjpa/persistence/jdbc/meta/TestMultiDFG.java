/*
 * TestMultiDFG.java
 *
 * Created on October 4, 2006, 1:50 PM
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

import javax.persistence.EntityTransaction;

import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.meta.FieldMetaData;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestMultiDFG
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {    
    private Object boid;
    
    public TestMultiDFG(String name) {
        super(name);
    }
    
    
    /** Creates a new instance of TestMultiDFG */
    public TestMultiDFG() {
    }
    
    public void setUp() {
        
       deleteAll(MultiB.class);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        EntityTransaction t = pm.getTransaction();
        t.begin();
        MultiB b = new MultiB();
        pm.persist(b);
        t.commit();
        boid = pm.getObjectId(b);
        pm.close();
    }
    
    public void testDFG() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        MultiB b = (MultiB) pm.getObjectId(boid);
        
        // check the non-dfg fields
        OpenJPAStateManager sm = getStateManager(b, pm);
        FieldMetaData fmd = sm.getMetaData().getField("bString");
        assertTrue("bString should not be loaded",
                !sm.getLoaded().get(fmd.getIndex()));
        
        fmd = sm.getMetaData().getField("bInt");
        assertTrue("bInt should not be loaded",
                !sm.getLoaded().get(fmd.getIndex()));
        
        System.out.println("### getting values");
        b.getBString();
        
        System.out.println("### getting values again");
        b.getString0();
        b.getBString();
        
        pm.close();
    }
    
    
}
