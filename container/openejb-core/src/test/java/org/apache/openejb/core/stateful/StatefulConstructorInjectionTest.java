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
package org.apache.openejb.core.stateful;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import javax.naming.InitialContext;

/**
 * @version $Revision$ $Date$
 */
public class StatefulConstructorInjectionTest extends TestCase {

    public void test() throws Exception {
        final InitialContext ctx = new InitialContext();

        final Widget widget = (Widget) ctx.lookup("WidgetBeanLocal");

        final Foo foo = (Foo) ctx.lookup("FooBeanLocal");

//        assertEquals("Widget.getCount()", 10, widget.getCount());
        assertEquals("Widget.getFoo()", foo, widget.getFoo());
    }

    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // Setup the descriptor information

        final EjbJar ejbJar = new EjbJar();

        ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));

        final StatefulBean bean = ejbJar.addEnterpriseBean(new StatefulBean(WidgetBean.class));
        bean.getEnvEntry().add(new EnvEntry("count", Integer.class.getName(), "10"));

        final EjbModule module = new EjbModule(ejbJar);
        module.setBeans(new Beans());

        assembler.createApplication(config.configureApplication(module));

    }

    @Override
    protected void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    public static interface Foo {
    }

    public static class FooBean implements Foo {
    }

    public static interface Widget {
        public int getCount();

        public Foo getFoo();
    }

    public static class WidgetBean implements Widget {

        @EJB(beanName = "FooBean")
        private final Foo foo;

        @Resource(name = "count")
        private int count;

        //        @Resource
//        private final DataSource ds;
        //TODO OPENEJB-1578 use producer fields or methods to inject count and datasource
        @Inject
        public WidgetBean(/*Integer count,*/ final Foo foo/*, DataSource ds*/) {
//            this.count = count;
            this.foo = foo;
//            this.ds = ds;
        }

        public int getCount() {
            return count;
        }

        public Foo getFoo() {
            return foo;
        }
    }
}
