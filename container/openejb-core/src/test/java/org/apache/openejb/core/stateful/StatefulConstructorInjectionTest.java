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
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * @version $Revision$ $Date$
 */
public class StatefulConstructorInjectionTest extends TestCase {

    public void test() throws Exception {
        InitialContext ctx = new InitialContext();

        Widget widget = (Widget) ctx.lookup("WidgetBeanLocal");

        Foo foo = (Foo) ctx.lookup("FooBeanLocal");

//        assertEquals("Widget.getCount()", 10, widget.getCount());
        assertEquals("Widget.getFoo()", foo, widget.getFoo());
    }

    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // Setup the descriptor information

        EjbJar ejbJar = new EjbJar();

        ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));

        StatefulBean bean = ejbJar.addEnterpriseBean(new StatefulBean(WidgetBean.class));
        bean.getEnvEntry().add(new EnvEntry("count", Integer.class.getName(), "10"));


        assembler.createApplication(config.configureApplication(ejbJar));

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
        public WidgetBean(/*Integer count,*/ Foo foo/*, DataSource ds*/) {
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
