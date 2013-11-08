/*
 * TestQueryAgainstEntireMappedHierarchy.java
 *
 * Created on October 5, 2006, 10:46 AM
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


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestQueryAgainstEntireMappedHierarchy
    extends org.apache.openjpa.persistence.jdbc.kernel.TestSQLListenerTestCase {
	
    /** Creates a new instance of TestQueryAgainstEntireMappedHierarchy */
    public TestQueryAgainstEntireMappedHierarchy(String name) 
    {
    	super(name);
    }
    
    public void setUpTestCase() 
    {        
        // this test depends on this fact
        assertTrue(HorizJ.class.getSuperclass() == HorizD.class);
        assertTrue(HorizK.class.getSuperclass() == HorizJ.class);

       deleteAll(HorizD.class);
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pm.getTransaction().begin();

        HorizD d = new HorizD();
        d.setStringA("stringa-d");
        d.setStringC("stringc-d");
        pm.persist(d);

        HorizJ j = new HorizJ();
        j.setStringA("stringa-j");
        j.setStringC("stringc-j");
        pm.persist(j);

        HorizK k = new HorizK();
        k.setStringA("stringa-k");
        k.setStringC("stringc-k");
        pm.persist(k);

        pm.getTransaction().commit();
        pm.close();
    }

    public void testQueryAgainstEntireMappedHierarchy() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        sql.clear();
        //FIXME jthomas
        //pm.newQuery(HorizD.class).execute();
        String lastStatement = (String) sql.get(sql.size() - 1);
        assertTrue(lastStatement.toLowerCase().indexOf("in (") == -1);
        pm.close();
    }
    
}
