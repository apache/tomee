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
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.loader.SystemInstance;

import jakarta.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
public class GlobalLookupScopesTest extends TestCase {

    public void testNothing() {
    }

    // TODO  We need this for https://issues.apache.org/jira/browse/OPENEJB-1140
    public void _test() throws Exception {

        SystemInstance.get().setProperty("openejb.deploymentId.format", "{appId}/{moduleId}/{ejbName}");

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));


        final AppContext circleApp = createApp("circle", config, assembler);
        final AppContext squareApp = createApp("square", config, assembler);

        {
            final BeanContext bean = squareApp.getBeanContexts().get(0);

            final Context context = bean.getJndiContext();

            assertTrue(context.lookup("global/square") instanceof Context);
            assertTrue(context.lookup("global/square/Bean") instanceof Bean);
            assertTrue(context.lookup("global/square/Bean!" + Bean.class.getName()) instanceof Bean);
            assertTrue(context.lookup("global/square/Other") instanceof Bean);
            assertTrue(context.lookup("global/square/Other!" + Bean.class.getName()) instanceof Bean);
            assertTrue(context.lookup("global/circle") instanceof Context);
            assertTrue(context.lookup("global/circle/Bean") instanceof Bean);
            assertTrue(context.lookup("global/circle/Bean!" + Bean.class.getName()) instanceof Bean);
            assertTrue(context.lookup("global/circle/Other") instanceof Bean);
            assertTrue(context.lookup("global/circle/Other!" + Bean.class.getName()) instanceof Bean);

        }
    }

    private AppContext createApp(final String name, final ConfigurationFactory config, final Assembler assembler) throws OpenEJBException, IOException, javax.naming.NamingException {
        // Setup the descriptor information

        final EjbJar ejbJar = new EjbJar(name);
        ejbJar.addEnterpriseBean(new SingletonBean(Bean.class));

        // Deploy the bean a second time to simulate situations
        // where the same java:module java:app java:global names
        // are re-declared in a compatible way
        ejbJar.addEnterpriseBean(new SingletonBean("Other", Bean.class));

        final EjbModule ejbModule = new EjbModule(ejbJar);
        final AppModule module = new AppModule(ejbModule);
        return assembler.createApplication(config.configureApplication(module));
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