/*
 * TestNamedSQLQueries.java
 *
 * Created on October 5, 2006, 5:26 PM
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
package org.apache.openjpa.persistence.jdbc.query;

import java.util.*;
import org.apache.openjpa.persistence.OpenJPAQuery;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestNamedSQLQueries
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
     
    
    /** Creates a new instance of TestNamedSQLQueries */
    public TestNamedSQLQueries() {
    }
    
    public TestNamedSQLQueries(String test) {
        super(test);
    }

    public void setUp() {
       deleteAll(NamedSQL.class);
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);;
        NamedSQL pc;
        for (int i = 0; i < 10; i++) {
            pc = new NamedSQL();
            if (i < 5)
                pc.setNum(4);
            else
                pc.setNum(i + 10);
            pm.persist(pc);
        }
        endTx(pm);;
        pm.close();
    }

    public void testNamedQuery() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createNativeQuery("cls",NamedSQL.class);

        assertEquals("javax.jdo.query.SQL", ((OpenJPAQuery) q).getLanguage());
        Collection results = (Collection) q.getCandidateCollection();
        for (Iterator i = results.iterator(); i.hasNext();)
            assertEquals(4, ((NamedSQL) i.next()).getNum());
        assertEquals(5, results.size());
        q.closeAll();
        pm.close();
    }

    public void testSystem() {
        // test top-level package.jdoquery
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        //FIXME jthomas
        /*
        OpenJPAQuery query = pm.newNamedQuery(null, "system2");
        query.setUnique(true);
        query.setResultClass(int.class);
        Integer count = (Integer) query.execute();
        assertEquals(20, count.intValue());
        query.closeAll();

        // test sql query in local package.jdoquery
        query = pm.newNamedQuery(NamedSQL.class, "cls");
        query.closeAll(); // force parsing of package query file
        query = pm.newNamedQuery(null, "system");
        query.setUnique(true);
        query.setResultClass(int.class);
        count = (Integer) query.execute();
        assertEquals(20, count.intValue());
        query.closeAll();
        pm.close();
         */
    }
    
}
