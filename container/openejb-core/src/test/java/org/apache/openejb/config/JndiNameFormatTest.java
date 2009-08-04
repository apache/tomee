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

package org.apache.openejb.config;

import junit.framework.TestCase;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;
import java.io.IOException;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;

public class JndiNameFormatTest extends TestCase {

    public void setUp() throws OpenEJBException, NamingException, IOException {
        System.setProperty("openejb.jndiname.format", "{ejbName}/{interfaceType.annotationNameLC}");

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        AppModule app = new AppModule(this.getClass().getClassLoader(), "test-app");

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(EchoImpl.class));
        app.getEjbModules().add(new EjbModule(ejbJar));

        assembler.createApplication(config.configureApplication(app));
    }

    public void testShouldLookupDeployBeanWithLowercaseInterfaceName() throws Exception {
        Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");

        InitialContext context = new InitialContext(p);
        Echo echo = (Echo) context.lookup("EchoImpl/remote");

        assertEquals("Echoing: This is a test", echo.echo("This is a test"));
    }

    @Remote
    public static interface Echo {
        String echo(String input);
    }

    @Stateless
    public static class EchoImpl implements Echo {

        public EchoImpl() {
        }

        public String echo(String input) {
            return "Echoing: " + input;
        }
    }

}
