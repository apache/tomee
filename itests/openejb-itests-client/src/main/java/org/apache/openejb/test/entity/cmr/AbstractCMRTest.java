/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.entity.cmr;


import javax.transaction.SystemException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.TransactionManager;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Properties;

import org.apache.openejb.test.TestManager;

/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public abstract class AbstractCMRTest extends org.apache.openejb.test.NamedTestCase {
    private TransactionManager transactionManager;
    protected DataSource ds;
    protected InitialContext initialContext;

    public AbstractCMRTest(String name){
        super("Entity.CMR."+name);
    }

    protected void beginTransaction() throws Exception {
        transactionManager.begin();
    }

    protected void completeTransaction() throws SystemException, HeuristicMixedException, HeuristicRollbackException, RollbackException {
        int status = transactionManager.getStatus();
        if (status == Status.STATUS_ACTIVE) {
            transactionManager.commit();
        } else if (status != Status.STATUS_NO_TRANSACTION) {
            transactionManager.rollback();
        }
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        super.setUp();

        Properties properties = TestManager.getServer().getContextEnvironment();
        properties.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        properties.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");

        initialContext = new InitialContext(properties);

        InitialContext jndiContext = new InitialContext( );
        transactionManager = (TransactionManager) jndiContext.lookup("java:openejb/TransactionManager");
        ds = (DataSource) jndiContext.lookup("java:openejb/connector/Default JDBC Database");
    }
}

