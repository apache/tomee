/*
 * TestStringFunctions.java
 *
 * Created on October 5, 2006, 5:20 PM
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


public class TestStringFunctions
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
    private OpenJPAEntityManager _pm = null;
    private QueryTest1 _match = null;
    
    /** Creates a new instance of TestStringFunctions */
    public TestStringFunctions(String name) 
    {
    	super(name);
    }
    
    public void setUp() {
       deleteAll(QueryTest1.class);

        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pm.getTransaction().begin();
        QueryTest1 match = new QueryTest1();
        match.setString("foobarbiz");
        pm.persist(match);
        QueryTest1 fail = new QueryTest1();
        fail.setString("barbizraz");
        pm.persist(fail);
        pm.getTransaction().commit();
        Object oid = pm.getObjectId(match);
        pm.close();

        _pm = (OpenJPAEntityManager)currentEntityManager();
        _match = (QueryTest1) _pm.getObjectId(oid);
    }

    public void tearDown()
        throws Exception {
        if (_pm != null)
            _pm.close();

        super.tearDown();
    }

    public void testSubstring() {
        assertMatch("string.substring (3) == 'barbiz'");
        assertMatch("string.substring (3, 6) == 'bar'");
    }

    public void testIndexOf() {
        assertMatch("string.indexOf ('bar') == 3");
        assertMatch("string.indexOf (\"b\", 4) == 6");
        assertMatch("string.indexOf ('b', 4) == 6");
    }

    public void testToLowerCase() {
        assertMatch("string.toLowerCase () == 'foobarbiz'");
        assertMatch("'FOOBARBIZ'.toLowerCase () == string");
    }

    public void testToUpperCase() {
        assertMatch("string.toUpperCase () == 'FOOBARBIZ'");
    }

    public void testStartsWith() {
        assertMatch("string.startsWith ('foobar')");
        assertMatch("'foobarbizbaz'.startsWith (string)");
    }

    public void testEndsWith() {
        assertMatch("string.endsWith ('barbiz')");
        assertMatch("'bazfoobarbiz'.endsWith (string)");
    }

    public void testMatches() {
        assertMatch("string.matches ('.oobar.*')");
        assertMatch("string.matches ('FOO.AR.IZ(?i)')");
    }

    private void assertMatch(String filter) {
        OpenJPAQuery q = _pm.createNativeQuery(filter,QueryTest1.class);
        Collection res = (Collection) q.getCandidateCollection();
        assertEquals(1, res.size());
        assertEquals(_match, res.iterator().next());
        q.closeAll();
    }
    
}
