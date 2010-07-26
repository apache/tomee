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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

import junit.framework.TestCase;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.junit.runner.RunWith;

@RunWith(ValidationRunner.class)
public class CheckInvalidInterceptorTest extends TestCase {
    @Keys( { @Key(value = "interceptor.callback.badReturnType", count = 2), @Key(value = "interceptor.callback.invalidArguments", count = 2),
            @Key(value = "aroundInvoke.badReturnType", count = 2), @Key(value = "aroundInvoke.invalidArguments", count = 2) })
    public EjbJar test() throws Exception {
        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        return ejbJar;
    }

    @Interceptors(Interceptor.class)
    public static class FooBean {}

    public static class Interceptor {
        @PostConstruct
        public Object interceptPostConstruct() {
            return null;
        }

        @PreDestroy
        public Object interceptPreDestroy() {
            return null;
        }

        @AroundInvoke
        public void interceptAroundInvoke() {}

        @AroundTimeout
        public void interceptAroundTimeout() {}
    }
}
