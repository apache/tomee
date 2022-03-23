/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.junit.runner.RunWith;

import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ValidationRunner.class)
public class CheckInvalidAroundTimeoutTest extends TestCase {

    @Keys({@Key(value = "aroundInvoke.invalidArguments", count = 2), @Key(value = "aroundInvoke.badReturnType", count = 2)})
    public EjbJar testInvalidAroundTimeoutParameter() throws Exception {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean("TestInvalidAroundTimeoutParameterBean", TestInvalidAroundTimeoutParameterBean.class));
        ejbJar.addInterceptor(new Interceptor(TestInvalidAroundTimeoutParameterInterceptor.class));
        return ejbJar;
    }

    @Keys({@Key(value = "aroundInvoke.badReturnType", count = 2), @Key(value = "aroundInvoke.mustThrowException", count = 2)})
    public EjbJar testInvalidAroundTimeoutReturnValue() throws Exception {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean("TestInvalidAroundTimeoutReturnValueBean", TestInvalidAroundTimeoutReturnValueBean.class));
        ejbJar.addInterceptor(new Interceptor(TestInvalidAroundTimeoutReturnValueInterceptor.class));
        return ejbJar;
    }

    @Keys(@Key(value = "ignoredMethodAnnotation", type = KeyType.WARNING))
    public EjbJar testIgnoredAroundTimeout() throws Exception {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean("TestAroundTimeout", TestAroundTimeout.class));
        return ejbJar;
    }

    public static class TestInvalidAroundTimeoutParameterBean {

        @AroundTimeout
        public void aroundTimeout() {
        }
    }

    public static class TestInvalidAroundTimeoutParameterInterceptor {

        @AroundTimeout
        public void aroundTimeout() {
        }

    }

    public static class TestInvalidAroundTimeoutReturnValueBean {

        @AroundTimeout
        public void aroundTimeout(final InvocationContext context) {
        }
    }

    public static class TestInvalidAroundTimeoutReturnValueInterceptor {

        @AroundTimeout
        public void aroundTimeout(final InvocationContext context) {
        }
    }

    public static class TestAroundTimeout {

        @AroundTimeout
        public void aroundTimeout(final InvocationContext context) {
        }
    }
}
