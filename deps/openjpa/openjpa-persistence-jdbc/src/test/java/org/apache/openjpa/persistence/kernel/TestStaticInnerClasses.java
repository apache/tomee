/*
 * TestStaticInnerClasses.java
 *
 * Created on October 13, 2006, 5:45 PM
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
package org.apache.openjpa.persistence.kernel;

import java.util.LinkedList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestStaticInnerClasses extends BaseKernelTest {

    private Object _oid = null;

    /**
     * Creates a new instance of TestStaticInnerClasses
     */
    public TestStaticInnerClasses() {
    }

    public TestStaticInnerClasses(String name) {
        super(name);
    }

    public void setUp()
        throws Exception {
        super.setUp(Inner.class);
        Inner inner = new Inner("foo");
        inner.addTwin();
        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        pm.persist(inner);
        _oid = pm.getObjectId(inner);
        endTx(pm);
        endEm(pm);
    }

    public void testGetById() {
        OpenJPAEntityManager pm = getPM();
        Inner inner = pm.find(Inner.class, _oid);
        assertNotNull(inner);
        assertEquals("foo", inner.getString());
        endEm(pm);
    }

    public void testGetByQuery() {
//        OpenJPAEntityManager pm = getPM();
//
//        OpenJPAQuery q = pm.createNativeQuery("",Inner.class);
//        //FIXME jthomas
//        /*
//        q.ssetCandidates(pm.createExtent(Inner.class, false));
//        q.declareVariables(Inner.class.getName() + " inner;");
//        q.setFilter("twins.contains (inner) && inner.string == \"foo\"");
//        q.setOrdering("string ascending, num descending");
//        Iterator iter = null;
//        try {
//            iter = ((Collection) q.execute()).iterator();
//        } catch (JDOException jdoe) {
//            if (jdoe.getMessage().indexOf("is ambiguous") != -1)
//                bug(AbstractTestCase.Platform.POSTGRESQL, 74, jdoe,
//                        "Sorts in PostgreSQL may result"
//                        + "in \"ORDER BY 'my_sort_key' is ambiguous\"");
//            else
//                throw jdoe;
//        }
//
//        assertNotNull(iter);
//        assertTrue(iter.hasNext());
//        assertEquals("foo", ((Inner) iter.next()).getString());
//        */
//        endEm(pm,());
        /*OpenJPAEntityManager pm = getPM();
          OpenJPAQuery q = pm.createQuery(
              "SELECT c FROM TestStaticInnerClasses.Inner c "
              + "WHERE c.string = 'foo' ORDER BY c.string ASC");
          q.setCandidateCollection((Collection)pm.createExtent(
              TestStaticInnerClasses.Inner.class, false));

          try
          {
              Iterator iter = ((Collection) q.getResultList()).iterator();
          }
          catch (Exception jdoe)
          {
              if (jdoe.getMessage().indexOf("is ambiguous") != -1)
                  bug(AbstractTestCase.Platform.POSTGRESQL, 74, jdoe,
                          "Sorts in PostgreSQL may result"
                          + "in \"ORDER BY 'my_sort_key' is ambiguous\"");
              else
                  throw jdoe;
          }*/

    }

    @Entity
    @Table(name="StaticInner")
    public static class Inner {

        @SuppressWarnings("unused")
        private int num = 0;
        private String string = null;
        private List twins = new LinkedList();

        protected Inner() {
        }

        public Inner(String string) {
            this.string = string;
        }

        public void addTwin() {
            twins.add(new Inner(string));
        }

        public String getString() {
            return string;
        }
    }
}
