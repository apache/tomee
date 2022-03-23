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
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.junit.AfterClass;

import jakarta.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @version $Rev$ $Date$
 */
public class MappedNameTest extends TestCase {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(GreenBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(RedBean.class));

        final EjbModule ejbModule = new EjbModule(ejbJar, new OpenejbJar());

        ejbModule.getOpenejbJar().addEjbDeployment(new EjbDeployment(null, "foo/bar/baz/Green", "GreenBean"));
        ejbModule.getOpenejbJar().addEjbDeployment(new EjbDeployment(null, "foo/bar/baz/Red", "RedBean"));

        final EjbJarInfo info = config.configureApplication(ejbModule);
        assembler.createApplication(info);

        final InitialContext initialContext = new InitialContext();
        final Color green = (Color) initialContext.lookup("foo/bar/baz/GreenLocal");
        final Color red = (Color) initialContext.lookup("foo/bar/baz/RedLocal");

        red.test();
    }

    public static interface Color {

        public void test() throws NamingException;

    }

    public static class GreenBean implements Color {
        public void test() throws NamingException {
        }
    }

    @EJB(name = "green", beanName = "GreenBean", beanInterface = Color.class)
    public static class RedBean implements Color {

        public void test() throws NamingException {
            final InitialContext initialContext = new InitialContext();
            initialContext.lookup("java:comp/env/green");
        }
    }
}
