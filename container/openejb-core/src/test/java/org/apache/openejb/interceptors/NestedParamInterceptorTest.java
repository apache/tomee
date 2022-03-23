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

import static org.junit.Assert.assertEquals;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
@Classes(NestedParamInterceptorTest.Intercepted.class)
public class NestedParamInterceptorTest {
    @EJB
    private Intercepted bean;

    @Test
    public void run() {
        assertEquals("success", bean.value(null));
    }

    @Stateless
    public static class Intercepted {
        @Interceptors(InterceptorImpl.class)
        public String value(final Nested param) {
            return "failed";
        }
    }

    public static class InterceptorImpl {
        @AroundInvoke
        public Object invoke(final InvocationContext ctx) throws Exception {
            return "success";
        }
    }

    public static class Nested {
    }
}
