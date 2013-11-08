/*
 * TestJDBCListener.java
 *
 * Created on October 6, 2006, 1:38 PM
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
package org.apache.openjpa.persistence.jdbc.schema;

import java.util.*;
import org.apache.openjpa.lib.jdbc.AbstractJDBCListener;
import org.apache.openjpa.lib.jdbc.JDBCEvent;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestJDBCListener
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
    
    public static boolean commitOccurred;    
    
    /** Creates a new instance of TestJDBCListener */
    public TestJDBCListener() {
    }

    public TestJDBCListener(String name) {
        super(name);
    }

    public void testJDBCListener() {
        Map props=new HashMap();
        props.put("openjpa.jdbc.JDBCListeners", Listener.class.getName());
        
        OpenJPAEntityManagerFactory factory =(OpenJPAEntityManagerFactory)
                getEmf(props);

        commitOccurred = false;
        OpenJPAEntityManager pm = factory.createEntityManager();     
        
        pm.getTransaction().begin();
        assertFalse(commitOccurred);
        pm.persist(new RuntimeTest1("Listener test", 99));
        pm.getTransaction().commit();
        assertTrue("Commit event should have occurred, but did not",
            commitOccurred);
        pm.close();
    }
    
    public static class Listener
        extends AbstractJDBCListener {

        public void beforeCommit(JDBCEvent event) {
            commitOccurred = true;
        }
    }    
}
