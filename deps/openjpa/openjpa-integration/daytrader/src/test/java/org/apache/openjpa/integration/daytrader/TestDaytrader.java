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
package org.apache.openjpa.integration.daytrader;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.log.LogFactoryImpl;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

/**
 * Uses a modified version of Apache Geronimo Daytrader to stress test JPA.
 *
 * @version $Rev$ $Date$
 */
public class TestDaytrader extends AbstractPersistenceTestCase {

    private static final int TEST_USERS = 500;
    EntityManagerFactory emf = null;

    Log log = null;
    private TradeAction trade = null;
    
    @Override
    public void setUp() {
        // Mimic EM pooling
        boolean poolEm = true;
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true,"
            + "SchemaAction='add,deleteTableContents')");
        emf = createEmf("daytrader", map);
        assertNotNull(emf);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM orderejb").executeUpdate();
        em.createQuery("DELETE FROM quoteejb").executeUpdate();
        em.createQuery("DELETE FROM accountejb").executeUpdate();
        em.createQuery("DELETE FROM accountprofileejb").executeUpdate();
        em.getTransaction().commit();
        
        
        log = new LogFactoryImpl().getLog("test");
        TradeConfig.setRunTimeMode(TradeConfig.JPA);
        TradeConfig.setLog(log);
        trade = new TradeAction(log, emf, poolEm);
    }
    
    @Override
    public void tearDown() throws Exception {
        trade = null;
        log = null;
        emf.close();
        emf = null;
        super.tearDown();
    }
    
    /**
     * Scenario being tested:
     *   - Creates 1000 quotes
     *   - Creates 500 user accounts w/ 10 holdings each
     *   - Perform the following 15 tasks for the first 50 user ids:
     *     login, home, account, update, home, portfolio, sell, buy, home, portfolio, sell, buy, home, account, logout
     * @throws Exception 
     *   
     */
    public void testTrade() throws Exception {
        log.info("TestDaytrader.testTrade() started");
        // setup quotes, accounts and holdings in DB
        TradeBuildDB tradeDB = new TradeBuildDB(log, trade);
        tradeDB.setup(TradeConfig.getMAX_QUOTES(), TradeConfig.getMAX_USERS());
        // perform some operations per user
        TradeScenario scenario = new TradeScenario(trade);
        log.info("TestDaytrader.testTrade() calling TradeScenario.performUserTasks(" + TEST_USERS + ")");
        for (int i = 0; i < TEST_USERS; i++) {
            String userID = "uid:" + i;
            if (scenario.performUserTasks(userID) == false) {
                fail("TestDaytrader.testTrade() call to TradeScenario.performUserTask(" + userID + ") failed");
            }
        }
        log.info("TestDaytrader.testTrade() completed");
    }
    
    private EntityManagerFactory createEmf(final String pu, Object... props) {
        Map<String, Object> map = getPropertiesMap(props);
        EntityManagerFactory emf = null;
        Map<Object, Object> config = new HashMap<Object, Object>(System.getProperties());
        config.putAll(map);
        emf = Persistence.createEntityManagerFactory(pu, config);
        if (emf == null) {
            throw new NullPointerException("Expected an entity manager factory " + "for the persistence unit named: \""
                + pu + "\"");
        }
        return emf;
    }

}
