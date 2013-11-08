/*
 * TestLockGroupsWithHorizontalBaseType.java
 *
 * Created on October 4, 2006, 5:03 PM
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
package org.apache.openjpa.persistence.jdbc.meta.horizontal;


import org.apache.openjpa.persistence.jdbc.common.apps.*;
import org.apache.openjpa.persistence.common.utils.*;
import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestLockGroupsWithHorizontalBaseType extends AbstractTestCase
{   
    /** Creates a new instance of TestLockGroupsWithHorizontalBaseType */
    public TestLockGroupsWithHorizontalBaseType(String name) 
    {
    	super(name, "jdbccactusapp");
    }


    public void setUp() {
       deleteAll(LockGroupSubclass.class);
    }

    public void testHorizontalLockGroup() {
        LockGroupSubclass pc = new LockGroupSubclass();
        pc.setDefaultLockGroupField("foo");
        pc.setNonDefaultLockGroupField("bar");
        OpenJPAEntityManager pm = getEm(true, true);
        pm.getTransaction().begin();
        pm.persist(pc);
        pm.getTransaction().commit();
        pm.getTransaction().begin();

        Object oid = pm.getObjectId(pc);

        OpenJPAEntityManager  pm2 = getEm(true, true);
        LockGroupSubclass pc2 = (LockGroupSubclass) pm2.getObjectId(oid);
        pm2.getTransaction().begin();
        pc2.setNonDefaultLockGroupField("baz");
        pm2.getTransaction().commit();

        pc.setDefaultLockGroupField("wookie");
        pm.getTransaction().commit();
    }    
    
   private OpenJPAEntityManager getEm(boolean optimistic,boolean retainValues) {
        OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
        em.setNontransactionalRead(true);
        em.setRetainState(retainValues);
        em.setOptimistic(optimistic);
        return em;
    }    
    
}
