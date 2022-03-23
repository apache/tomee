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
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;

import javax.naming.Context;
import javax.naming.InitialContext;
import jakarta.transaction.TransactionManager;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class OpenejbLookupTest extends TestCase {

    public void testPlainInitialContext() throws Exception {

        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        assembler.createApplication(config.configureApplication(ejbJar));

        final Context context = new InitialContext();

        assertOpenejbUrlLookups(context);
    }

    public void testLocalInitialContext() throws Exception {

        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        assembler.createApplication(config.configureApplication(ejbJar));

        final Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        Context context = new InitialContext(properties);

        // This is still the jvm InitialContext delegating to the openejbURLContextFactory
        assertTrue(context instanceof InitialContext);
        assertOpenejbUrlLookups(context);

        // Now we have effectively unwrapped the InitalContext, openejb: lookups should still work.
        context = (Context) context.lookup("");
        assertTrue(context instanceof IvmContext);
        assertOpenejbUrlLookups(context);


        // Test that an EJB can lookup items from openejb:
        final FooLocal fooLocal = (FooLocal) context.lookup("FooBeanLocal");

        fooLocal.test();
    }

    private static void assertOpenejbUrlLookups(Context context) throws javax.naming.NamingException {
        Object o = context.lookup("openejb:");

        assertTrue(o instanceof Context);

        context = (Context) o;

        o = context.lookup("TransactionManager");

        assertTrue(o instanceof TransactionManager);
    }


    public static class FooBean implements FooLocal {

        public void test() throws javax.naming.NamingException {
            Context context = new InitialContext();
            assertOpenejbUrlLookups(context);

            context = (Context) context.lookup("java:");
            assertOpenejbUrlLookups(context);

            context = (Context) context.lookup("");
            assertOpenejbUrlLookups(context);
        }

    }

    public static interface FooLocal {
        public void test() throws javax.naming.NamingException;
    }

}