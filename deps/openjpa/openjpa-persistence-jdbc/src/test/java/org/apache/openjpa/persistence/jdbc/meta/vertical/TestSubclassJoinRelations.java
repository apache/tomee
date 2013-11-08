/*
 * TestSubclassJoinRelations.java
 *
 * Created on October 5, 2006, 4:28 PM
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
package org.apache.openjpa.persistence.jdbc.meta.vertical;

import java.util.*;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.Join;
import org.apache.openjpa.persistence.OpenJPAQuery;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestSubclassJoinRelations
    extends org.apache.openjpa.persistence.jdbc.kernel.TestSQLListenerTestCase {
    private String _outer = "OUTER";
    private Object _oid = null;
    
    /** Creates a new instance of TestSubclassJoinRelations */
    public TestSubclassJoinRelations(String name) 
    {
    	super(name);
    }
    
    public boolean skipTest() {
        //FIXME 
        /*
        return super.skipTest()
            || ((JDBCConfiguration) getConfiguration()).
            getDBDictionaryInstance().joinSyntax == Join.SYNTAX_TRADITIONAL;
         */
        return false;
    }

    public void setUpTestCase() {
        // make sure all classes are registered
        Class[] reg = new Class[]{
            Base.class, BaseSub1.class, BaseSub2.class,
            BaseSub1Sub1.class, BaseSub1Sub2.class,
        };

        if (((JDBCConfiguration) getConfiguration()).getDBDictionaryInstance().
            joinSyntax == Join.SYNTAX_DATABASE)
            _outer = "(+)";

       deleteAll(Relations.class);
       deleteAll(Base.class);

        Relations rel = new Relations();
        BaseSub2 sub2 = new BaseSub2();
        sub2.setBaseField(3);
        sub2.setBaseSub2Field(4);
        rel.setBase(sub2);

        BaseSub1 sub1 = new BaseSub1();
        sub1.setBaseField(2);
        sub1.setBaseSub1Field(3);
        rel.setBaseSub1(sub1);

        BaseSub1Sub2 sub1sub2 = new BaseSub1Sub2();
        sub1sub2.setBaseField(4);
        sub1sub2.setBaseSub1Field(5);
        sub1sub2.setBaseSub1Sub2Field(6);
        rel.setBaseSub1Sub2(sub1sub2);

        Base base = new Base();
        base.setBaseField(1);
        rel.getBaseList().add(base);
        rel.getBaseList().add(sub2);

        rel.getBaseSub1List().add(sub1);
        rel.getBaseSub1List().add(sub1sub2);

        rel.getBaseSub1Sub2List().add(sub1sub2);

        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);;
        pm.persist(rel);
        endTx(pm);;
        _oid = pm.getObjectId(rel);
        pm.close();
    }

    public void testNullOneOne()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(1, sql.size());
        assertNotSQL(_outer);
        sql.clear();

        assertNull(rel.getNullBase());
        pm.close();

        assertEquals(0, sql.size());
    }

    public void testEagerNullOneOne()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("nullBase");
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(1, sql.size());
        assertSQL(_outer);
        sql.clear();

        assertNull(rel.getNullBase());
        pm.close();

        assertEquals(0, sql.size());
    }

    public void testEmptyBaseList()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(1, sql.size());
        assertNotSQL(_outer);
        sql.clear();

        assertEquals(0, rel.getEmptyBaseList().size());
        pm.close();

        assertEquals(1, sql.size());
        assertSQL(_outer);
    }

    public void testEagerEmptyBaseList()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("emptyBaseList");
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(1, sql.size());
        assertSQL(_outer);
        sql.clear();

        assertEquals(0, rel.getEmptyBaseList().size());
        pm.close();

        assertEquals(0, sql.size());
    }

    public void testBaseOneOne()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(1, sql.size());
        assertNotSQL(_outer);
        sql.clear();

        BaseSub2 sub2 = (BaseSub2) rel.getBase();
        assertEquals(3, sub2.getBaseField());
        assertEquals(4, sub2.getBaseSub2Field());
        assertEquals(BaseSub2.class, sub2.getClass());
        pm.close();

        assertEquals(sql.toString(), 1, sql.size());
        assertSQL(_outer);
    }

    public void testEagerBaseOneOne()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("base");
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(1, sql.size());
        assertSQL(_outer);
        sql.clear();

        BaseSub2 sub2 = (BaseSub2) rel.getBase();
        assertEquals(3, sub2.getBaseField());
        assertEquals(4, sub2.getBaseSub2Field());
        assertEquals(BaseSub2.class, sub2.getClass());
        pm.close();

        assertEquals(0, sql.size());
    }

    public void testLeafOneOne()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(1, sql.size());
        assertNotSQL(_outer);
        sql.clear();

        BaseSub1Sub2 sub1sub2 = (BaseSub1Sub2) rel.getBaseSub1Sub2();
        assertEquals(4, sub1sub2.getBaseField());
        assertEquals(5, sub1sub2.getBaseSub1Field());
        assertEquals(6, sub1sub2.getBaseSub1Sub2Field());
        pm.close();

        assertEquals(1, sql.size());
        assertNotSQL(_outer);
    }

    public void testEagerLeafOneOne()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("baseSub1Sub2");
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(1, sql.size());
        assertSQL(_outer);
        sql.clear();

        BaseSub1Sub2 sub1sub2 = (BaseSub1Sub2) rel.getBaseSub1Sub2();
        assertEquals(4, sub1sub2.getBaseField());
        assertEquals(5, sub1sub2.getBaseSub1Field());
        assertEquals(6, sub1sub2.getBaseSub1Sub2Field());
        pm.close();

        assertEquals(0, sql.size());
    }

    public void testMidOneOne()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(1, sql.size());
        assertNotSQL(_outer);
        sql.clear();

        BaseSub1 sub1 = (BaseSub1) rel.getBaseSub1();
        assertEquals(2, sub1.getBaseField());
        assertEquals(3, sub1.getBaseSub1Field());
        assertEquals(BaseSub1.class, sub1.getClass());
        pm.close();

        assertEquals(1, sql.size());
        assertSQL(_outer);
    }

    public void testEagerMidOneOne()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("baseSub1");
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(1, sql.size());
        assertSQL(_outer);
        sql.clear();

        BaseSub1 sub1 = (BaseSub1) rel.getBaseSub1();
        assertEquals(2, sub1.getBaseField());
        assertEquals(3, sub1.getBaseSub1Field());
        assertEquals(BaseSub1.class, sub1.getClass());
        pm.close();

        assertEquals(0, sql.size());
    }

    public void testBaseList()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(1, sql.size());
        assertNotSQL(_outer);
        sql.clear();

        List list = rel.getBaseList();
        assertEquals(2, list.size());
        Base base = (Base) list.get(0);
        assertEquals(1, base.getBaseField());
        assertEquals(Base.class, base.getClass());
        BaseSub2 sub2 = (BaseSub2) list.get(1);
        assertEquals(3, sub2.getBaseField());
        assertEquals(4, sub2.getBaseSub2Field());
        assertEquals(BaseSub2.class, sub2.getClass());
        pm.close();

        assertEquals(1, sql.size());
        assertSQL(_outer);
    }

    public void testEagerBaseList()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("baseList");
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(1, sql.size());
        assertSQL(_outer);
        sql.clear();

        List list = rel.getBaseList();
        assertEquals(2, list.size());
        Base base = (Base) list.get(0);
        assertEquals(1, base.getBaseField());
        assertEquals(Base.class, base.getClass());
        BaseSub2 sub2 = (BaseSub2) list.get(1);
        assertEquals(3, sub2.getBaseField());
        assertEquals(4, sub2.getBaseSub2Field());
        assertEquals(BaseSub2.class, sub2.getClass());
        pm.close();

        assertEquals(0, sql.size());
    }

    public void testLeafList()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(1, sql.size());
        assertNotSQL(_outer);
        sql.clear();

        List list = rel.getBaseSub1Sub2List();
        assertEquals(1, list.size());
        BaseSub1Sub2 sub1sub2 = (BaseSub1Sub2) list.get(0);
        assertEquals(4, sub1sub2.getBaseField());
        assertEquals(5, sub1sub2.getBaseSub1Field());
        assertEquals(6, sub1sub2.getBaseSub1Sub2Field());
        pm.close();

        assertEquals(1, sql.size());
        assertNotSQL(_outer);
    }

    public void testEagerLeafList()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("baseSub1Sub2List");
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(2, sql.size());
        assertNotSQL(_outer);
        sql.clear();

        List list = rel.getBaseSub1Sub2List();
        assertEquals(1, list.size());
        BaseSub1Sub2 sub1sub2 = (BaseSub1Sub2) list.get(0);
        assertEquals(4, sub1sub2.getBaseField());
        assertEquals(5, sub1sub2.getBaseSub1Field());
        assertEquals(6, sub1sub2.getBaseSub1Sub2Field());
        pm.close();

        assertEquals(0, sql.size());
    }

    public void testMidList()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(1, sql.size());
        assertNotSQL(_outer);
        sql.clear();

        List list = rel.getBaseSub1List();
        assertEquals(2, list.size());
        BaseSub1 sub1 = (BaseSub1) list.get(0);
        assertEquals(2, sub1.getBaseField());
        assertEquals(3, sub1.getBaseSub1Field());
        assertEquals(BaseSub1.class, sub1.getClass());
        BaseSub1Sub2 sub1sub2 = (BaseSub1Sub2) list.get(1);
        assertEquals(4, sub1sub2.getBaseField());
        assertEquals(5, sub1sub2.getBaseSub1Field());
        assertEquals(6, sub1sub2.getBaseSub1Sub2Field());
        pm.close();

        assertEquals(1, sql.size());
        assertSQL(_outer);
    }

    public void testEagerMidList()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("baseSub1List");
        Relations rel = (Relations) pm.getObjectId(_oid);

        assertEquals(1, sql.size());
        assertSQL(_outer);
        sql.clear();

        List list = rel.getBaseSub1List();
        assertEquals(2, list.size());
        BaseSub1 sub1 = (BaseSub1) list.get(0);
        assertEquals(2, sub1.getBaseField());
        assertEquals(3, sub1.getBaseSub1Field());
        assertEquals(BaseSub1.class, sub1.getClass());
        BaseSub1Sub2 sub1sub2 = (BaseSub1Sub2) list.get(1);
        assertEquals(4, sub1sub2.getBaseField());
        assertEquals(5, sub1sub2.getBaseSub1Field());
        assertEquals(6, sub1sub2.getBaseSub1Sub2Field());
        pm.close();

        assertEquals(0, sql.size());
    }

    public void testProjections()
        throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createNativeQuery("",Relations.class);
        //FIXME jthomas
        /*
        q.setResult("base, baseSub1, baseSub1Sub2");
        q.setUnique(true);
        Object[] res = (Object[]) q.execute();
         */
        Object[] res=null;
        assertNotNull(res);
        assertEquals(3, res.length);

        BaseSub2 sub2 = (BaseSub2) res[0];
        assertEquals(3, sub2.getBaseField());
        assertEquals(4, sub2.getBaseSub2Field());
        assertEquals(BaseSub2.class, sub2.getClass());

        BaseSub1 sub1 = (BaseSub1) res[1];
        assertEquals(2, sub1.getBaseField());
        assertEquals(3, sub1.getBaseSub1Field());
        assertEquals(BaseSub1.class, sub1.getClass());

        BaseSub1Sub2 sub1sub2 = (BaseSub1Sub2) res[2];
        assertEquals(4, sub1sub2.getBaseField());
        assertEquals(5, sub1sub2.getBaseSub1Field());
        assertEquals(6, sub1sub2.getBaseSub1Sub2Field());
        pm.close();

        assertEquals(1, sql.size());
        assertSQL(_outer);
    }    
}
