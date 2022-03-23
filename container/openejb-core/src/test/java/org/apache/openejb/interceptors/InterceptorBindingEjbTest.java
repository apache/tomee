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
package org.apache.openejb.interceptors;

import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InterceptorBinding;
import jakarta.interceptor.InvocationContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class InterceptorBindingEjbTest {
    @EJB
    private EJB2 ejb2;

    @Test
    public void test() {
        ejb2.foo();
        assertEquals(1, MarkedInterceptor.CLASSES.size());
        assertTrue(MarkedInterceptor.CLASSES.contains(EJB1.class.getSimpleName()));
    }

    /**
     * aims to test that method level cdi interceptors are well managed (and not all merged in class level interceptors).
     *
     * @return the needed module
     */
    @Module
    public EjbModule ejbJar() {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean("ejb1", EJB1.class));
        ejbJar.addEnterpriseBean(new StatelessBean("ejb2", EJB2.class));

        final Beans beans = new Beans();
        beans.addInterceptor(MarkedInterceptor.class);

        final EjbModule module = new EjbModule(ejbJar);
        module.setBeans(beans);
        return module;
    }

    @Inherited
    @InterceptorBinding
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Interception {
    }

    @Interception
    @jakarta.interceptor.Interceptor
    public static class MarkedInterceptor {
        public static final Collection<String> CLASSES = new ArrayList<String>();

        @AroundInvoke
        public Object intercept(final InvocationContext invocationContext) throws Exception {
            CLASSES.add(invocationContext.getTarget().getClass().getSimpleName());
            return invocationContext.proceed();
        }
    }

    public static class EJB1 {
        @Interception
        public void foo() {
        }
    }

    public static class EJB2 {
        @EJB
        private EJB1 ejb1;

        @Interception
        public void notCalled() {
        }

        public String foo() {
            ejb1.foo();
            return "ok";
        }
    }
}
