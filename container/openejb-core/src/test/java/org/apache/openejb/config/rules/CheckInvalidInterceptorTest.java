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
package org.apache.openejb.config.rules;

import junit.framework.TestCase;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.LifecycleCallback;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.StatelessBean;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;
import java.util.List;

@RunWith(ValidationRunner.class)
public class CheckInvalidInterceptorTest extends TestCase {
    @Keys({@Key(value = "interceptor.callback.badReturnType", count = 2), @Key(value = "interceptor.callback.invalidArguments", count = 2),
        @Key(value = "aroundInvoke.badReturnType", count = 2), @Key(value = "aroundInvoke.invalidArguments", count = 2), @Key("interceptor.callback.missing"),
        @Key("aroundInvoke.missing"), @Key("interceptorBinding.noSuchEjbName"), @Key("interceptorBinding.ejbNameRequiredWithMethod"),
        @Key("interceptor.callback.missing.possibleTypo")})
    public EjbJar test() throws Exception {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        final Interceptor interceptor = ejbJar.addInterceptor(new org.apache.openejb.jee.Interceptor(CallbackMissingInterceptor.class));
        interceptor.addAroundInvoke("wrongMethod");
        interceptor.addPostConstruct("foo");
        interceptor.addPostConstruct("wrongMethod");
        final List<InterceptorBinding> interceptorBindings = ejbJar.getAssemblyDescriptor().getInterceptorBinding();
        final InterceptorBinding binding = new InterceptorBinding("wrongEjbName");
        // binding.setMethod(new NamedMethod("wrongMethod"));
        interceptorBindings.add(binding);
        final InterceptorBinding binding1 = new InterceptorBinding();
        binding1.setMethod(new NamedMethod("aMethod"));
        interceptorBindings.add(binding1);
        return ejbJar;
    }

    @Keys({@Key(value = "interceptor.unused", count = 2, type = KeyType.WARNING), @Key("aroundInvoke.invalidArguments")})
    public EjbJar test1() {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(BarBean.class));
        final Interceptor unused = ejbJar.addInterceptor(new org.apache.openejb.jee.Interceptor(UnusedInterceptor.class));
        final Interceptor unused1 = ejbJar.addInterceptor(new org.apache.openejb.jee.Interceptor(UnusedInterceptor1.class));
        final Interceptor used = ejbJar.addInterceptor(new org.apache.openejb.jee.Interceptor(UsedInterceptor.class));
        final List<InterceptorBinding> interceptorBindings = ejbJar.getAssemblyDescriptor().getInterceptorBinding();
        final InterceptorBinding binding = new InterceptorBinding("BarBean", used);
        interceptorBindings.add(binding);
        return ejbJar;
    }

    /**
     * Should not get any failure message, as we explicitly configure the methods in the base class
     *
     * @return
     */
    @Keys
    public EjbJar testDeclaringInterceptorClass() {
        final EjbJar ejbJar = new EjbJar();
        final Interceptor subInterceptor = ejbJar.addInterceptor(new org.apache.openejb.jee.Interceptor(SubInterceptor.class));
        subInterceptor.getPostConstruct().add(new LifecycleCallback(BaseInterceptor.class.getName(), "interceptPostConstruct"));
        subInterceptor.getPreDestroy().add(new LifecycleCallback(BaseInterceptor.class.getName(), "interceptPreDestroy"));
        subInterceptor.getAroundInvoke().add(new org.apache.openejb.jee.AroundInvoke(BaseInterceptor.class.getName(), "interceptAroundInvoke"));
        subInterceptor.getAroundTimeout().add(new org.apache.openejb.jee.AroundTimeout(BaseInterceptor.class.getName(), "interceptAroundTimeout"));
        return ejbJar;
    }

    @Interceptors(Interzeptor.class)
    public static class FooBean {
    }

    public static class Interzeptor {

        @PostConstruct
        public Object interceptPostConstruct() {
            return null;
        }

        @PreDestroy
        public Object interceptPreDestroy() {
            return null;
        }

        @AroundInvoke
        public void interceptAroundInvoke() {
        }

        @AroundTimeout
        public void interceptAroundTimeout() {
        }
    }

    public static class BaseInterceptor {

        public void interceptPostConstruct(final InvocationContext context) throws Exception {
        }

        public void interceptPreDestroy(final InvocationContext context) throws Exception {
        }

        public Object interceptAroundInvoke(final InvocationContext context) throws Exception {
            return null;
        }

        public Object interceptAroundTimeout(final InvocationContext context) throws Exception {
            return null;
        }
    }

    public static class SubInterceptor extends BaseInterceptor {

        private final Object interceptPostConstruct() {
            return null;
        }

        private final Object interceptPreDestroy() {
            return null;
        }

        private final void interceptAroundInvoke() {
        }

        private final void interceptAroundTimeout() {
        }
    }

    public static class CallbackMissingInterceptor {
        public void interceptSomething() {
        }

        public void foo(final String str) {
        }

        public void foo(final int x) {
        }
    }

    public static class UnusedInterceptor {
        // need this to cause validation failure
        @AroundInvoke
        public Object interceptAroundInvoke() {
            return null;
        }
    }

    public static class UnusedInterceptor1 {
    }

    public static class UsedInterceptor {
    }

    public static class BarBean {
    }
}
