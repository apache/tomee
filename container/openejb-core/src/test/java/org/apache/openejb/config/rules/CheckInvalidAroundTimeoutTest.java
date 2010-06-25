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

import static org.apache.openejb.config.rules.ValidationAssertions.assertFailures;
import static org.apache.openejb.config.rules.ValidationAssertions.assertWarnings;

import java.util.ArrayList;
import java.util.List;

import javax.interceptor.AroundTimeout;
import javax.interceptor.InvocationContext;

import junit.framework.TestCase;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.ValidationFailedException;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.junit.Test;

/**
 * @version $Rev$ $Date$
 */
public class CheckInvalidAroundTimeoutTest extends TestCase {

    @Test
    public void testInvalidAroundTimeoutParameter() throws Exception {
        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();
        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean("TestInvalidAroundTimeoutParameterBean", TestInvalidAroundTimeoutParameterBean.class));
        ejbJar.addInterceptor(new Interceptor(TestInvalidAroundTimeoutParameterInterceptor.class));

        List<String> expectedFailureKeys = new ArrayList<String>();
        expectedFailureKeys.add("aroundInvoke.invalidArguments");
        expectedFailureKeys.add("aroundInvoke.invalidArguments");
        try {
            config.configureApplication(ejbJar);
        } catch (ValidationFailedException e) {
            assertFailures(expectedFailureKeys, e);
        }
    }

    @Test
    public void testInvalidAroundTimeoutReturnValue() throws Exception {
        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();
        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean("TestInvalidAroundTimeoutReturnValueBean", TestInvalidAroundTimeoutReturnValueBean.class));
        ejbJar.addInterceptor(new Interceptor(TestInvalidAroundTimeoutReturnValueInterceptor.class));

        List<String> expectedFailureKeys = new ArrayList<String>();
        //For Bean Class
        expectedFailureKeys.add("aroundInvoke.badReturnType");
        expectedFailureKeys.add("aroundInvoke.mustThrowException");
        //For Interceptor
        expectedFailureKeys.add("aroundInvoke.badReturnType");
        expectedFailureKeys.add("aroundInvoke.mustThrowException");
        try {
            config.configureApplication(ejbJar);
        } catch (ValidationFailedException e) {
            assertFailures(expectedFailureKeys, e);
        }
    }

    @Test
    public void testIgnoredAroundTimeout() throws Exception {
        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();
        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean("TestAroundTimeout", TestAroundTimeout.class));
        List<String> expectedWarningKeys = new ArrayList<String>();
        expectedWarningKeys.add("ignoredAnnotation");
        try {
            config.configureApplication(ejbJar);
        } catch (ValidationFailedException e) {
            assertWarnings(expectedWarningKeys, e);

        }
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
        public void aroundTimeout(InvocationContext context) {
        }
    }

    public static class TestInvalidAroundTimeoutReturnValueInterceptor {

        @AroundTimeout
        public void aroundTimeout(InvocationContext context) {
        }
    }

    public static class TestAroundTimeout {

        @AroundTimeout
        public void aroundTimeout(InvocationContext context) {
        }
    }
}
