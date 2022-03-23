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

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InterceptorBinding;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;

// ensure @AroundConstruct doesnt fail and prevent to use EJB in its both flavors/signatures
@RunWith(ApplicationComposer.class)
@Classes(cdi = true, innerClassesAsBean = true, cdiInterceptors = {
        AroundConstructCdiTest.AC1.class, AroundConstructCdiTest.AC2.class
})
public class AroundConstructCdiTest {
    @Inject
    private Bean bean;

    @Test
    public void run() {
        assertEquals("truetruetrueget", bean.get());
    }

    @B1 @B2
    @Singleton
    @Interceptors(AC3.class)
    public static class Bean {
        public String get() {
            return "get";
        }
    }

    @Interceptor
    public static class AC3 {
        private boolean constructured = false;

        @AroundConstruct
        public Object ac(InvocationContext ic) throws Exception {
            constructured = true;
            return ic.proceed();
        }

        @AroundInvoke
        public Object ai(final InvocationContext ic) throws Exception {
            return constructured + ic.proceed().toString();
        }
    }

    @B1
    @Interceptor
    public static class AC1 {
        private boolean constructured = false;

        @AroundConstruct
        public Object ac(InvocationContext ic) throws Exception {
            constructured = true;
            return ic.proceed();
        }

        @AroundInvoke
        public Object ai(final InvocationContext ic) throws Exception {
            return constructured + ic.proceed().toString();
        }
    }

    @InterceptorBinding
    @Target(TYPE)
    @Retention(RUNTIME)
    public @interface B1 {
    }

    @B2
    @Interceptor
    public static class AC2 {
        private boolean constructured = false;

        @AroundConstruct
        public void ac(final InvocationContext ic) {
            constructured = true;
        }

        @AroundInvoke
        public Object ai(final InvocationContext ic) throws Exception {
            return constructured + ic.proceed().toString();
        }
    }

    @InterceptorBinding
    @Target(TYPE)
    @Retention(RUNTIME)
    public @interface B2 {
    }
}
