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

import java.sql.*;
import java.util.*;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.kernel.StoreManager;
import org.apache.openjpa.kernel.DelegatingStoreManager;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;

public class TestIncrementalJDBCFlushes
        extends BaseJDBCTest {
    
    private EntityManagerFactory emf;
    

//    private boolean  = true;
//    
    
    public TestIncrementalJDBCFlushes(String str) {
        super(str);
    }
    
    
    public void setUp() {
        emf = getEmf(getProps());
        
        EntityManager em =emf.createEntityManager();
       deleteAll(RuntimeTest1.class,em);
        
    }
    
    
    public void testFlushHappened() throws java.sql.SQLException{
        
        
        
        EntityManager em =emf.createEntityManager();
        startTx(em);
        RuntimeTest1 a = new RuntimeTest1("a-name", 10);
        em.persist(a);
        em.flush();
        JDBCStore store = (JDBCStore) getStoreManager(em, true);
        Connection conn = store.getConnection();
        ClassMapping mapping = store.getConfiguration().
                getMappingRepositoryInstance().getMapping(RuntimeTest1.class,
                null, true);
        FieldMapping fm = mapping.getFieldMapping("stringField");
        String tableName =
            store.getConfiguration().getDBDictionaryInstance().getFullName(
                    fm.getTable(), false);
        String colName = fm.getColumns()[0].getName();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT " + colName + " FROM "
                + tableName + " WHERE " + colName + " = 'a-name'");
        int count = 0;
        while (rs.next())
            count++;
        assertEquals(1, count);
        rollbackTx(em);
        endEm(em);
        
        
    }
    
    private StoreManager getStoreManager(EntityManager em, boolean innermost) {
        DelegatingStoreManager mgr =
            JPAFacadeHelper.toBroker(em).getStoreManager();
        if (innermost)
            return mgr.getInnermostDelegate();
        return mgr;
    }
    
    private Map getProps() {
        Map props=new HashMap();
        props.put("openjpa.DataCache", "true");
        props.put("openjpa.RemoteCommitProvider", "sjvm");
        props.put("openjpa.FlushBeforeQueries", "true");
        props.put("javax.jdo.option.IgnoreCache", "false");
        //propsMap.put("openjpa.BrokerImpl", "kodo.datacache.CacheTestBroker");
        //CacheTestBroker.class.getName());
        return props;
    }
    
}
