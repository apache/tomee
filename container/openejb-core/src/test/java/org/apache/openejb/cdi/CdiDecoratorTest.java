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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.cdi;

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.junit.Before;

import jakarta.annotation.PostConstruct;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.ejb.Local;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InterceptorBinding;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("deprecation")
public class CdiDecoratorTest extends TestCase {

    private InitialContext ctx;

    @Before
    public void setUp() throws Exception {

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        assembler.createContainer(config.configureService(StatelessSessionContainerInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean("HelloOne", RedBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean("HelloTwo", RedBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(OrangeBean.class));

        final Beans beans = new Beans();
        beans.addInterceptor(OrangeCdiInterceptor.class);
        beans.addDecorator(OrangeOneDecorator.class);
        beans.addDecorator(OrangeTwoDecorator.class);
        beans.addManagedClass(YellowBean.class);

        final EjbModule module = new EjbModule(ejbJar);
        module.setBeans(beans);

        assembler.createApplication(config.configureApplication(module));

        final Properties properties = new Properties(System.getProperties());
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());
        ctx = new InitialContext(properties);
    }

    public void testSimple() {
        try {

            final Color color = (Color) ctx.lookup("HelloOneLocal");
            color.hello();

            for (final String call : callback) {
                System.out.println("callback = " + call);
            }

            for (final String call : businessMethod) {
                System.out.println("call = " + call);
            }


            assertTrue(YellowBean.RUN);
            assertTrue(OrangeBean.RUN);
            assertTrue(RedBean.RUN);
            assertTrue(OrangeCdiInterceptor.RUN);
            assertTrue(OrangeOneDecorator.RUN);

        } catch (final NamingException e) {
            e.printStackTrace();
        }
    }

    private static final List<String> businessMethod = new ArrayList<String>();
    private static final List<String> callback = new ArrayList<String>();

    @Local
    public static interface Color {
        public void hello();
    }

    @InterceptorBinding
    @Target(value = {ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface OrangeInterceptorBinding {

    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
    public static @interface OrangeQualifier {

    }

    @Stateless
    @Interceptors(RedInterceptor.class)
    public static class RedBean implements Color {

        @Inject
        private YellowBean cdiBean;

        public static boolean RUN = false;

        @PostConstruct
        public void postConstruct() {
            callback.add(this.getClass().getSimpleName());
        }

        @Override
        public void hello() {
            businessMethod.add(this.getClass().getSimpleName());
            RUN = true;
            System.out.println("In EJB : " + RedBean.class.getName());
            cdiBean.sayHelloWorld();
        }
    }

    public static class RedInterceptor {
        @PostConstruct
        public void postConstruct(final InvocationContext ctx) throws Exception {
            callback.add(this.getClass().getSimpleName());
            ctx.proceed();
        }

        @AroundInvoke
        public Object aroundInvoke(final InvocationContext ctx) throws Exception {
            businessMethod.add(this.getClass().getSimpleName());
            return ctx.proceed();
        }

    }

    public static class YellowBean {

        @Inject
        @OrangeQualifier
        private Color colorEjb;

        public static boolean RUN = false;

        @PostConstruct
        public void postConstruct() {
            callback.add(this.getClass().getSimpleName());
        }

        public void sayHelloWorld() {
            businessMethod.add(this.getClass().getSimpleName());
            RUN = true;
            System.out.println("In Managed Bean : " + YellowBean.class.getName());
            this.colorEjb.hello();
        }
    }

    @Decorator
    public static class OrangeOneDecorator implements Color {

        public static boolean RUN = false;

        @Inject
        @Delegate
        @OrangeQualifier
        private Color color;

        @Override
        public void hello() {
            businessMethod.add(this.getClass().getSimpleName());
            System.out.println("In CDI Style Decorator  : " + OrangeOneDecorator.class.getName());
            RUN = true;
            this.color.hello();
        }
    }

    @Decorator
    public static class OrangeTwoDecorator implements Color {

        public static boolean RUN = false;

        @Inject
        @Delegate
        @OrangeQualifier
        private Color color;

        @Override
        public void hello() {
            businessMethod.add(this.getClass().getSimpleName());
            System.out.println("In CDI Style Decorator  : " + OrangeOneDecorator.class.getName());
            RUN = true;
            this.color.hello();
        }
    }

    @Interceptor
    @OrangeInterceptorBinding
    public static class OrangeCdiInterceptor {

        public static boolean RUN = false;

        @PostConstruct
        public void postConstruct(final InvocationContext ctx) throws Exception {
            callback.add(this.getClass().getSimpleName());
            ctx.proceed();

        }

        @AroundInvoke
        public Object aroundInvoke(final InvocationContext ctx) throws Exception {
            businessMethod.add(this.getClass().getSimpleName());
            System.out.println("In CDI Style Interceptor  : " + OrangeCdiInterceptor.class.getName());
            RUN = true;
            return ctx.proceed();
        }
    }

    @LocalBean
    @OrangeQualifier
    @OrangeInterceptorBinding
    @Interceptors(OrangeEjbInterceptor.class)
    public static class OrangeBean implements Color {

        public static boolean RUN = false;

        @PostConstruct
        public void postConstruct() {
            callback.add(this.getClass().getSimpleName());
        }

        @Override
        public void hello() {
            businessMethod.add(this.getClass().getSimpleName());
            System.out.println("In EJB : " + OrangeBean.class.getName());
            RUN = true;
        }
    }

    public static class OrangeEjbInterceptor {

        @PostConstruct
        public void postConstruct(final InvocationContext ctx) throws Exception {
            callback.add(this.getClass().getSimpleName());
            ctx.proceed();
        }

        @AroundInvoke
        public Object aroundInvoke(final InvocationContext ctx) throws Exception {
            businessMethod.add(this.getClass().getSimpleName());
            return ctx.proceed();
        }

    }
}
