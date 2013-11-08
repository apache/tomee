
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

import java.sql.*;
import java.util.*;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.sql.ResultSetResult;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.jdbc.kernel.GenericResultObjectProvider;
import org.apache.openjpa.lib.rop.ResultList;
import org.apache.openjpa.lib.rop.EagerResultList;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.kernel.AbstractPCResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.FetchConfiguration;
/**
 *
 */


public class TestRawSQL extends BaseJDBCTest {
    
    /** Creates a new instance of TestRawSQL */
    private Object[] _oids = new Object[3];
    private String[] _strings = new String[]{ "a", "b", "c" };
    private int[] _ints = new int[]{ 1, 2, 3 };

    public TestRawSQL(String test) {
        super(test);
    }

    public void setUp() {
       deleteAll(RawSQL.class);
        EntityManager em = currentEntityManager();
        Broker broker = JPAFacadeHelper.toBroker(em);

        broker.begin();
        RawSQL raw;
        for (int i = 0; i < _oids.length; i++) {
            raw = new RawSQL(_strings[i], _ints[i]);
            broker.persist(raw, null);
            _oids[i] = broker.getUserObject(raw);
        }
        broker.commit();
        broker.close();
        em.close();
    }

    public void testLoadWithResult()
        throws Exception {
        
        EntityManager em = currentEntityManager();
        Broker broker = JPAFacadeHelper.toBroker(em);
       
        JDBCStore store = (JDBCStore)broker.getStoreManager();

        Connection conn = store.getConnection();
        Statement stmnt = conn.createStatement();
        ResultSet rs = stmnt.executeQuery("SELECT * FROM RAWSQL "
        + "ORDER BY INTFIELD");
        ResultSetResult res = new ResultSetResult(rs,
        store.getConfiguration().getDBDictionaryInstance());
        ResultObjectProvider rop = new GenericResultObjectProvider
        (RawSQL.class, store, null, res);
        ResultList rl = new EagerResultList(rop);
        check(rl, broker);
        rl.close();
        broker.close();
        em.close();
    }

        public void testLoadWithPCROP()
        throws Exception {
            
        EntityManager em = currentEntityManager();    
        Broker broker = JPAFacadeHelper.toBroker(em);
        ResultObjectProvider rop = new AbstractPCResultObjectProvider(broker) {
            private int _row = -1;

            public boolean supportsRandomAccess() {
                return true;
            }

            public boolean next() {
                return ++_row < _oids.length;
            }

            public boolean absolute(int pos) {
                _row = pos;
                return _row < _oids.length;
            }

            public int size() {
                return _oids.length;
            }

            public void close() {
            }

            public void handleCheckedException(Exception e) {
                throw new RuntimeException(e.toString());
            }

            protected Object getObjectId(ClassMetaData meta) {
                return _oids[_row];
            }

            protected Class getPCType() {
                return RawSQL.class;
            }

            protected void load(OpenJPAStateManager sm, 
                FetchConfiguration fetch) {
                ClassMetaData meta = sm.getMetaData();
                sm.storeStringField(meta.getField("stringField").getIndex(),
                    _strings[_row]);
                sm.storeIntField(meta.getField("intField").getIndex(),
                    _ints[_row]);

                // note that we're not loading version info here, so the system
                // will go out and get it after this intial load; kinda neat
            }
        };
        ResultList rl = new EagerResultList(rop);
        check(rl, broker);
        rl.close();
        broker.close();
    }

    
    
    private void check(ResultList rl, Broker broker) {
        RawSQL raw;
        int i = 0;
        for (Iterator itr = rl.iterator(); itr.hasNext(); i++) {
            raw = (RawSQL) itr.next();
            assertTrue(broker.getStateManager(raw).getLoaded().get(0));
            assertTrue(broker.getStateManager(raw).getLoaded().get(1));
            if (i < _oids.length) {
                assertEquals(_strings[i], raw.getStringField());
                assertEquals(_ints[i], raw.getIntField());
            }
        }
        assertEquals(_oids.length, i);
        assertEquals(_oids.length, rl.size());
    }
    
    
}
