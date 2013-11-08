/*
 * TestDataStoreTrips.java
 *
 * Created on September 29, 2006, 4:48 PM
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

import java.util.*;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.lib.jdbc.AbstractJDBCListener;
import org.apache.openjpa.lib.jdbc.JDBCEvent;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.persistence.Extent;



public class TestDataStoreTrips extends BaseJDBCTest{


//    private boolean  = true;//Boolean.valueOf(bool);
	
    private OpenJPAEntityManagerFactory _factory = null;
    private Object _oid = null;
    private SelectCounter _counter = null;
    
    /** Creates a new instance of TestDataStoreTrips */
    public TestDataStoreTrips() {
    }
    public TestDataStoreTrips(String name) {
        super(name);
    }
    
    public void setUp()
        throws Exception {
       deleteAll(DataStoreTripsPC.class);

        // figure out what table to look for in SQL statements
        EntityManager em= currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast (em);
        JDBCConfiguration conf =
            (JDBCConfiguration) ((OpenJPAEntityManagerSPI) kem)
            .getConfiguration();        

        ClassMapping mapping = conf.getMappingRepositoryInstance().getMapping
            (DataStoreTripsPC.class, null, true);
        String table = conf.getDBDictionaryInstance()
            .getFullName(mapping.getTable(), false);

        // get factory with counter listener
        _factory = getEmf();
/*                getPMFactory(new String[]{
            "openjpa.jdbc.JDBCListeners",
            SelectCounter.class.getName() + "(Table=" + table + ")",
            "openjpa.Compatibility",
            "ValidateTrueChecksStore=true"
        });
*/
        
        OpenJPAEntityManager pm = _factory.createEntityManager();
        startTx(pm);
        
        DataStoreTripsPC pc = new DataStoreTripsPC(0);
        pm.persist(pc);
        _oid = pm.getObjectId(pc);

        for (int i = 1; i < 10; i++)
              pm.persist(new DataStoreTripsPC(i));
        endTx(pm);
        em.close();

        // do a query so that the subclass list will get initialized and
        // won't count as a select
        pm = _factory.createEntityManager();
        pm.getObjectId(_oid);
        pm.close();

        _counter = (SelectCounter) ((JDBCConfiguration)
            ((OpenJPAEntityManagerFactorySPI) _factory).getConfiguration())
            .getJDBCListenerInstances()[0];
        _counter.selects = 0;
        _counter.counts = 0;
    }

    public void testTrips() {
        OpenJPAEntityManager pm = _factory.createEntityManager();
        Extent ext = pm.createExtent(DataStoreTripsPC.class, true);
        for (Iterator itr = ext.iterator(); itr.hasNext();)
            itr.next();
        ext.closeAll();
        pm.close();

        // there might be more than 1 trip if subclasses need to be
        // initialized and so forth, but make sure there isn't more than
        // 1 + # objects trips to the DB
        assertTrue(_counter.selects > 0);
        assertTrue("Should have been 1-3 trips to the data store; got "
            + _counter.selects, _counter.selects < 5);
    }

    public void testExistsCalls() {
        OpenJPAEntityManager pm = _factory.createEntityManager();

        // first time there shouldn't be any call to exists b/c the data
        // needs to be loaded
        pm.getObjectId(_oid);
        assertEquals(0, _counter.counts);
        assertEquals(1, _counter.selects);

        // this time there should be a call b/c data is already loaded but
        // we're still asking to validate
        pm.getObjectId(_oid);
        assertEquals(1, _counter.counts);
        assertEquals(2, _counter.selects);
        pm.getObjectId(_oid);
        assertEquals(2, _counter.counts);
        assertEquals(3, _counter.selects);

        // shouldn't be a call if validate is false; no select b/c data loaded
        pm.getObjectId(_oid);
        assertEquals(2, _counter.counts);
        assertEquals(3, _counter.selects);

        // this will hollow the object
        //FIXME jthomas ...need to find a substitute
        //pm.currentTransaction().setOptimistic(false);
        startTx(pm);
        

        // no count call b/c loading data
        pm.getObjectId(_oid);
        assertEquals(2, _counter.counts);
        assertEquals(4, _counter.selects);

        // no count call b/c transactional; no select b/c data loaded
        pm.getObjectId(_oid);
        assertEquals(2, _counter.counts);
        assertEquals(4, _counter.selects);

        rollbackTx(pm);
        pm.close();
    }

    public static class SelectCounter
        extends AbstractJDBCListener {

        public int selects = 0;
        public int counts = 0;

        private String _table = null;

        public void setTable(String table) {
            _table = table;
        }

        public void beforeExecuteStatement(JDBCEvent event) {
            if (event.getSQL().indexOf(_table) != -1)
                selects++;
            {
                if (event.getSQL().indexOf(" COUNT(") != -1)
                    counts++;
            }
        }
    }
    
    
}
