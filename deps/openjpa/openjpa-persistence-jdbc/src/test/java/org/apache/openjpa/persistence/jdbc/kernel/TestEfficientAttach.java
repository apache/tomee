/*
 * TestEfficientAttach.java
 *
 * Created on September 29, 2006, 6:00 PM
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
package org.apache.openjpa.persistence.jdbc.kernel;


import org.apache.openjpa.persistence.jdbc.common.apps.*;


import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.meta.ClassMetaData;

public class TestEfficientAttach extends TestSQLListenerTestCase {


//    private boolean  = true;//Boolean.valueOf(bool);
    
    /** Creates a new instance of TestEfficientAttach */
    public TestEfficientAttach(String name) 
    {
    	super(name);
    }
    
    public void testAttachWithDetachedStateManager() {
        Class[] clss = new Class[]{
            AttachA.class, AttachB.class, AttachC.class, AttachD.class,
            AttachE.class, AttachF.class,
        };
        String[] detachedStateFields = new String[clss.length];

        // null any declared detached state fields so we know we're using
        // synthetic state managers
        EntityManager em= currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast (em);
        JDBCConfiguration conf =
            (JDBCConfiguration) ((OpenJPAEntityManagerSPI) kem)
            .getConfiguration();

        MetaDataRepository repos = conf.getMappingRepositoryInstance();
        ClassMetaData meta;
        for (int i = 0; i < clss.length; i++) {
            meta = repos.getMetaData(clss[i], null, true);
            detachedStateFields[i] = meta.getDetachedState();
            meta.setDetachedState(null);

           deleteAll(clss[i]);
        }

        try {
            attachTest();
        } finally {
            // set detached state back
            for (int i = 0; i < clss.length; i++)
                repos.getMetaData(clss[i], null, true).setDetachedState
                    (detachedStateFields[i]);
        }
    }

    private void attachTest() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager) currentEntityManager();
        startTx(pm);;
        AttachE e = new AttachE();
        e.setB(new AttachB());
        pm.persist(e);
        endTx(pm);;
        //FIXME jthomas - could not find equivalent for detachCopy()
        //attachCopy() etc
/*      
        e = (AttachE) pm.detachCopy(e);
        pm.close();

        pm = getPM();
        pm.begin();
        sql.clear();
        e = (AttachE) pm.attachCopy(e, false);
        assertNotNull(e);
        assertNotNull(e.getB());
        assertSize(0, sql);
        pm.commit();
        assertSize(2, sql);
        pm.close();
 */
    }


    public static void main(String[] args) {
        //main();
    }
    
}
