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
import static org.apache.openejb.core.stateful.CallbackOverridesTest.Callback.POST_CONSTRUCT;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.ejb.Remove;
import javax.interceptor.InvocationContext;
import javax.interceptor.Interceptors;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import static java.util.Arrays.asList;

/**
 * @version $Rev$ $Date$
 */
public class CallbackOverridesTest extends TestCase {

    @Override
    protected void setUp() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean(ChildBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));
    }

    public void test() throws Exception {
        InitialContext context = new InitialContext();

        callbacks.clear();

        ChildBean childBean = (ChildBean) context.lookup("ChildBeanLocalBean");


        assertEquals(asList(ParentInterceptor.class, ChildInterceptor.class, ParentBean.class, ChildBean.class), callbacks);

        callbacks.clear();

        childBean.remove();

        assertEquals(Collections.EMPTY_LIST, callbacks);
    }

    public static enum Callback {
        POST_CONSTRUCT,
        PRE_PASSIVATE,
        POST_ACTIVATE,
        PRE_DESTROY,
    }

    public static List<Class> callbacks = new ArrayList<Class>();

    public static class ParentInterceptor {

        @PostConstruct
        private void construct(InvocationContext context) throws Exception {
            callbacks.add(ParentInterceptor.class);
            context.proceed();
        }

        @PreDestroy
        protected void destroy(InvocationContext context) throws Exception {
            callbacks.add(ParentInterceptor.class);
            context.proceed();
        }

    }

    public static class ChildInterceptor extends ParentInterceptor {

        @PostConstruct
        private void construct(InvocationContext context) throws Exception {
            callbacks.add(ChildInterceptor.class);
            context.proceed();
        }

        // callback is disabled
        protected void destroy(InvocationContext context) throws Exception {
            callbacks.add(ParentInterceptor.class);
            context.proceed();
        }
    }

    public static class ParentBean {

        @PostConstruct
        private void construct()  {
            callbacks.add(ParentBean.class);
        }

        @PreDestroy
        protected void destroy() {
            callbacks.add(ParentBean.class);
        }

    }

    @Stateful
    @Interceptors(ChildInterceptor.class)
    public static class ChildBean extends ParentBean {


        @PostConstruct
        private void construct()  {
            callbacks.add(ChildBean.class);
        }

        protected void destroy() {
            callbacks.add(ChildBean.class);
        }

        @Remove
        public void remove() {}
    }

}
