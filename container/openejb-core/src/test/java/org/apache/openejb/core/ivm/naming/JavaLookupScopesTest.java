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
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ModuleContext;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;

import jakarta.annotation.Resource;
import jakarta.ejb.EJBContext;
import jakarta.ejb.TimerService;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.xml.ws.WebServiceContext;

/**
 * @version $Rev$ $Date$
 */
public class JavaLookupScopesTest extends TestCase {

    public void test() throws Exception {

        final AppContext app;
        {
            final ConfigurationFactory config = new ConfigurationFactory();
            final Assembler assembler = new Assembler();

            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

            // Setup the descriptor information

            final EjbJar ejbJar = new EjbJar("testmodule");
            ejbJar.addEnterpriseBean(new SingletonBean(Bean.class));

            // Deploy the bean a second time to simulate situations
            // where the same java:module java:app java:global names
            // are re-declared in a compatible way
            ejbJar.addEnterpriseBean(new SingletonBean("Other", Bean.class));

            final EjbModule ejbModule = new EjbModule(ejbJar);
            final AppModule module = new AppModule(ejbModule);
            app = assembler.createApplication(config.configureApplication(module));
        }

        final BeanContext bean = app.getBeanContexts().get(0);

        final ModuleContext module = bean.getModuleContext();


        { // app context lookups
            final Context context = app.getAppJndiContext();

            assertTrue(context.lookup("app") instanceof Context);
            assertTrue(context.lookup("app/AppName") instanceof String);
            assertTrue(context.lookup("app/green") instanceof DataSource);
            assertTrue(context.lookup("app/testmodule") instanceof Context);
            assertTrue(context.lookup("app/testmodule/Bean") instanceof Bean);
            assertTrue(context.lookup("app/testmodule/Bean!" + Bean.class.getName()) instanceof Bean);
            assertTrue(context.lookup("app/testmodule/Other") instanceof Bean);
            assertTrue(context.lookup("app/testmodule/Other!" + Bean.class.getName()) instanceof Bean);

            assertEquals("testmodule", context.lookup("app/AppName"));
        }

        { // module context lookups
            final Context context = module.getModuleJndiContext();

            assertTrue(context.lookup("module") instanceof Context);
            assertTrue(context.lookup("module/ModuleName") instanceof String);
            assertTrue(context.lookup("module/blue") instanceof DataSource);
            assertTrue(context.lookup("module/Bean") instanceof Bean);
            assertTrue(context.lookup("module/Bean!" + Bean.class.getName()) instanceof Bean);

            assertEquals("testmodule", context.lookup("module/ModuleName"));

            // TODO the Module JNDI context *should* be able to see the App context
        }

        {
            final Context context = bean.getJndiContext();

            assertTrue(context.lookup("comp") instanceof Context);
            assertTrue(context.lookup("comp/EJBContext") instanceof EJBContext);
            assertTrue(context.lookup("comp/TimerService") instanceof TimerService);
            assertTrue(context.lookup("comp/TransactionManager") instanceof TransactionManager);
            assertTrue(context.lookup("comp/TransactionSynchronizationRegistry") instanceof TransactionSynchronizationRegistry);
            assertTrue(context.lookup("comp/WebServiceContext") instanceof WebServiceContext);

            assertTrue(context.lookup("comp/env") instanceof Context);
            assertTrue(context.lookup("comp/env") instanceof Context);
            assertTrue(context.lookup("comp/env/orange") instanceof DataSource);
            assertTrue(context.lookup("comp/env/" + Bean.class.getName()) instanceof Context);
            assertTrue(context.lookup("comp/env/" + Bean.class.getName() + "/red") instanceof DataSource);

            assertTrue(context.lookup("module") instanceof Context);
            assertTrue(context.lookup("module/ModuleName") instanceof String);
            assertTrue(context.lookup("module/blue") instanceof DataSource);
            assertTrue(context.lookup("module/Bean") instanceof Bean);
            assertTrue(context.lookup("module/Bean!" + Bean.class.getName()) instanceof Bean);
            assertTrue(context.lookup("module/Other") instanceof Bean);
            assertTrue(context.lookup("module/Other!" + Bean.class.getName()) instanceof Bean);

            assertTrue(context.lookup("app") instanceof Context);
            assertTrue(context.lookup("app/AppName") instanceof String);
            assertTrue(context.lookup("app/green") instanceof DataSource);
            assertTrue(context.lookup("app/testmodule") instanceof Context);
            assertTrue(context.lookup("app/testmodule/Bean") instanceof Bean);
            assertTrue(context.lookup("app/testmodule/Bean!" + Bean.class.getName()) instanceof Bean);
            assertTrue(context.lookup("app/testmodule/Other") instanceof Bean);
            assertTrue(context.lookup("app/testmodule/Other!" + Bean.class.getName()) instanceof Bean);

            assertTrue(context.lookup("global") instanceof Context);
            assertTrue(context.lookup("global/yellow") instanceof DataSource);

            assertTrue(context.lookup("global/testmodule") instanceof Context);
            assertTrue(context.lookup("global/testmodule/Bean") instanceof Bean);
            assertTrue(context.lookup("global/testmodule/Bean!" + Bean.class.getName()) instanceof Bean);
            assertTrue(context.lookup("global/testmodule/Other") instanceof Bean);
            assertTrue(context.lookup("global/testmodule/Other!" + Bean.class.getName()) instanceof Bean);


            assertEquals("testmodule", context.lookup("app/AppName"));
            assertEquals("testmodule", context.lookup("module/ModuleName"));
        }
    }

    public static class Bean {

        @Resource()
        private DataSource red;

        @Resource(name = "orange")
        private DataSource orange;

        @Resource(name = "java:module/blue")
        private DataSource blue;

        @Resource(name = "java:app/green")
        private DataSource green;

        @Resource(name = "java:global/yellow")
        private DataSource yellow;


        public Object lookup(final String s) throws javax.naming.NamingException {
            final InitialContext context = new InitialContext();
            return context.lookup(s);
        }
    }
}
