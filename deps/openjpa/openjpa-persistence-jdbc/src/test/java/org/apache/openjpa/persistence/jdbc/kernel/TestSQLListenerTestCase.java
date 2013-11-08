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


import org.apache.openjpa.lib.jdbc.*;

/**
 * Test case that provides access to all the SQL that is executed.
 *
 */
public abstract class TestSQLListenerTestCase
    extends BaseJDBCTest {

    protected static final Collection brokers = new HashSet();
    public static List sql = new ArrayList();

    public TestSQLListenerTestCase() {
    }

    public TestSQLListenerTestCase(String name) {
        super(name);
    }

    public void setUp()
        throws Exception {
        super.setUp();
        setUpTestCase();
        sql.clear();
    }

    /**
     * Override for setUp() behavior.
     */
    public void setUpTestCase() {
    }

    public final void tearDown()
        throws Exception {
        super.tearDown();
        tearDownTestCase();
        sql.clear();
    }

    /**
     * Override for tearDown() behavior.
     */
    public void tearDownTestCase() {
    }

    /**
     * Confirm that the specified SQL has been executed.
     *
     * @param sqlExp the SQL expression. E.g., "SELECT FOO .*"
     */
    public void assertSQL(String sqlExp)
        throws Exception {
        for (Iterator i = sql.iterator(); i.hasNext();) {
            String statement = (String) i.next();
            if (matches(sqlExp, statement))
                return;
        }

        fail("Expected regular expression <" + sqlExp + "> to have"
            + " existed in SQL statements: " + sql);
    }

    /**
     * Confirm that the specified SQL has not been executed.
     *
     * @param sqlExp the SQL expression. E.g., "SELECT BADCOLUMN .*"
     */
    public void assertNotSQL(String sqlExp)
        throws Exception {
        boolean failed = false;

        for (Iterator i = sql.iterator(); i.hasNext();) {
            String statement = (String) i.next();
            if (matches(sqlExp, statement))
                failed = true;
        }

        if (failed)
            fail("Regular expression <" + sqlExp + ">"
                + " should not have been executed in SQL statements: " + sql);
    }
/*

    public KodoPersistenceManager getPM() {
        KodoPersistenceManager pm = (KodoPersistenceManager) getPMFactory().
            getPersistenceManager();
        brokers.add(KodoJDOHelper.toBroker(pm));
        return pm;
    }

    public KodoPersistenceManagerFactory getPMFactory() {
        return getPMFactory(null);
    }
    public BrokerFactory getBrokerFactory(String[] props) {
        String[] builtin = new String[]{
            "openjpa.jdbc.JDBCListeners", Listener.class.getName()
        };

        if (props == null)
            props = builtin;
        else {
            String[] tmp = new String[props.length + builtin.length];
            System.arraycopy(props, 0, tmp, 0, props.length);
            System.arraycopy(builtin, 0, tmp, props.length, builtin.length);
            props = tmp;
        }

        return super.getBrokerFactory(props);
    }
    public KodoPersistenceManagerFactory getPMFactory(String[] props) {
        String[] builtin = new String[]{
            "openjpa.jdbc.JDBCListeners", Listener.class.getName()
        };

        if (props == null)
            props = builtin;
        else {
            String[] tmp = new String[props.length + builtin.length];
            System.arraycopy(props, 0, tmp, 0, props.length);
            System.arraycopy(builtin, 0, tmp, props.length, builtin.length);
            props = tmp;
        }

        return super.getPMFactory(props);
    }

    public Broker getBroker() {
        Broker broker = getBrokerFactory().newBroker();
        brokers.add(broker);
        return broker;
    }

    public BrokerFactory getBrokerFactory() {
        return getBrokerFactory(null);
    }

*/



    public static class Listener
        extends AbstractJDBCListener {

        public void beforeExecuteStatement(JDBCEvent event) {
            if (event.getSQL() != null)
                sql.add(event.getSQL());
		}
	}
}
