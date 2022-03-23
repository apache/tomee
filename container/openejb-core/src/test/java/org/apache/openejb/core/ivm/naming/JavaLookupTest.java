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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.ivm.naming;

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.UserTransaction;

/**
 * @version $Rev$ $Date$
 */
public class JavaLookupTest extends TestCase {

    public void test() throws Exception {

        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));


        final InitialContext context = new InitialContext();
        assertTrue(context.lookup("java:openejb/TransactionManager") instanceof TransactionManager);

        assertTrue(context.lookup("java:comp/TransactionManager") instanceof TransactionManager);

        assertTrue(context.lookup("java:comp/UserTransaction") instanceof UserTransaction);

        assertTrue(context.lookup("java:comp/TransactionSynchronizationRegistry") instanceof TransactionSynchronizationRegistry);
    }


    public void testLinking() throws Exception {

        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final InitialContext context = new InitialContext();

        final Context javaContext = (Context) context.lookup("java:");

        javaContext.bind("java:TransactionManager", new JndiUrlReference("java:comp/TransactionManager"));
        javaContext.bind("java:TransactionManagerLink", new LinkRef("java:comp/TransactionManager"));

        assertTrue(context.lookup("java:TransactionManager") instanceof TransactionManager);
        assertTrue(context.lookup("java:TransactionManagerLink") instanceof TransactionManager);

        new InitialContext().bind("java:foo", new LinkRef("java:comp/TransactionManager"));

        assertTrue(context.lookup("java:foo") instanceof TransactionManager);


    }

}
