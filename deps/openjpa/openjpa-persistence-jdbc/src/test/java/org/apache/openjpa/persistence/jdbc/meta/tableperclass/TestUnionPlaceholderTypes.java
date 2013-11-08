/*
 * TestUnionPlaceholderTypes.java
 *
 * Created on October 5, 2006, 2:04 PM
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
package org.apache.openjpa.persistence.jdbc.meta.tableperclass;


import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.util.Id;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


public class TestUnionPlaceholderTypes
    extends org.apache.openjpa.persistence.jdbc.kernel.TestSQLListenerTestCase {
    
    /** Creates a new instance of TestUnionPlaceholderTypes */
    public TestUnionPlaceholderTypes(String name) 
    {
    	super(name);
    }

    private boolean _union = false;

    public void setUpTestCase() {
        JDBCConfiguration conf = (JDBCConfiguration) getConfiguration();
        //FIXME jthomas
        //_union = ((SQLFactory) conf.getSQLFactoryInstance())
        //.getAdvancedSQL().getSupportsUnion();
    }

    public void testUnion()
        throws Exception {
       deleteAll(PlaceholderTypesA.class);

        PlaceholderTypesA pta = new PlaceholderTypesA();
        pta.setIntA(1);
        PlaceholderTypesB ptb = new PlaceholderTypesB();
        ptb.setIntA(2);
        ptb.setIntB(3);
        ptb.setBooleanB(true);
        ptb.setByteB((byte) 64);
        ptb.setCharB('a');
        ptb.setFloatB(99.9F);
        ptb.setStringB("stringB");
        ptb.setClobB("clobB");
        ptb.setBlobB("blobB");

        Broker broker = getBrokerFactory().newBroker();
        broker.begin();
        broker.persist(pta, null);
        broker.persist(ptb, null);
        broker.commit();
        Object oida = broker.getObjectId(pta);
        long idb = ((Id) broker.getObjectId(ptb)).getId();
        broker.close();

        broker = getBrokerFactory().newBroker();
        broker.begin();
        sql.clear();
        pta = (PlaceholderTypesA) broker.find(oida, true, null);
        assertEquals(1, pta.getIntA());
        if (_union)
            assertEquals(1, sql.size());
        else
            assertNotSQL("UNION");
        broker.close();

        broker = getBrokerFactory().newBroker();
        broker.begin();
        sql.clear();
        ptb = (PlaceholderTypesB) broker.find(broker.newObjectId
            (PlaceholderTypesB.class, new Long(idb)), true, null);
        assertEquals(2, ptb.getIntA());
        assertEquals(3, ptb.getIntB());
        assertTrue(ptb.getBooleanB());
        assertEquals(64, ptb.getByteB());
        assertEquals('a', ptb.getCharB());
        assertEquals(99.9F, ptb.getFloatB(), .001);
        assertEquals("stringB", ptb.getStringB());
        assertEquals("clobB", ptb.getClobB());
        assertEquals("blobB", ptb.getBlobB());
        if (_union)
            assertEquals(1, sql.size());
        else
            assertNotSQL("UNION");
        broker.close();
    }
    
}
