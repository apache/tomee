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
package org.apache.openejb.alt.containers.castor_cmp11;

import java.util.Properties;
import javax.transaction.TransactionManager;

import org.castor.transactionmanager.TransactionManagerFactory;
import org.castor.transactionmanager.TransactionManagerAcquireException;

/**
 * @version $Revision$ $Date$
 */
public class ThreadLocalTransactionManagerFactory implements TransactionManagerFactory {
    public static ThreadLocal<TransactionManager> transactionManager = new ThreadLocal<TransactionManager>();
    public static final String NAME = "threadLocal";

    /**
     * {@inheritDoc}
     */
    public String getName() { return NAME; }

    /**
     * {@inheritDoc}
     */
    public TransactionManager getTransactionManager(Properties properties) throws TransactionManagerAcquireException {
        return transactionManager.get();
    }
}
