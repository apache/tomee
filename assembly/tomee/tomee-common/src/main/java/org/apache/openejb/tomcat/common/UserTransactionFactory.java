/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.tomcat.common;

import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.loader.SystemInstance;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.util.Hashtable;

public class UserTransactionFactory implements ObjectFactory {
    public Object getObjectInstance(Object object, Name name, Context context, Hashtable environment) throws Exception {
        // get the transaction manager
        TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
        if (transactionManager == null) {
            throw new NamingException("transaction manager not found");
        }

        // if transaction manager implements user transaction we are done
        if (transactionManager instanceof UserTransaction) {
            return transactionManager;
        }

        // wrap transaction manager with user transaction
        return new CoreUserTransaction(transactionManager);
    }
}
