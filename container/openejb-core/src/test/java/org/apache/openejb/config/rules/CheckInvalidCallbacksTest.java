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

import static org.apache.openejb.config.rules.ValidationAssertions.assertWarnings;
import static org.apache.openejb.config.rules.ValidationAssertions.assertFailures;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.AfterBegin;
import javax.ejb.AfterCompletion;
import javax.ejb.BeforeCompletion;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.interceptor.AroundInvoke;

import junit.framework.TestCase;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.ValidationFailedException;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatelessBean;
import org.junit.Test;

/**
 * @version $Rev$ $Date$
 */
public class CheckInvalidCallbacksTest extends TestCase {

    @Test
    public void test() throws Exception {
        Assembler assemler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean("TestStateless", TestBean.class));
        ejbJar.addEnterpriseBean(new SingletonBean("TestSingleton", TestBean.class));

        List<String> expectedWarningKeys = new ArrayList<String>();
        //For StatelessBean
        expectedWarningKeys.add("ignoredAnnotation");
        expectedWarningKeys.add("ignoredAnnotation");
        expectedWarningKeys.add("ignoredAnnotation");
        expectedWarningKeys.add("ignoredAnnotation");
        expectedWarningKeys.add("ignoredAnnotation");
        //For SingletionBean
        expectedWarningKeys.add("ignoredAnnotation");
        expectedWarningKeys.add("ignoredAnnotation");
        expectedWarningKeys.add("ignoredAnnotation");
        expectedWarningKeys.add("ignoredAnnotation");
        expectedWarningKeys.add("ignoredAnnotation");

        List<String> expectedFailureKeys = new ArrayList<String>();
        //For StatelessBean
        expectedFailureKeys.add("aroundInvoke.invalidArguments");
        //For SingletionBean
        expectedFailureKeys.add("aroundInvoke.invalidArguments");

        try {
            config.configureApplication(ejbJar);
        } catch (ValidationFailedException e) {
            assertWarnings(expectedWarningKeys, e);
            assertFailures(expectedFailureKeys,e);
        }
    }

    public static class TestBean implements Callable {

        public Object call() throws Exception {
            return null;
        }

        @AroundInvoke
        public void invoke(){}

        @PostConstruct
        public void myConstruct() {
        }

        @PreDestroy
        public void myDestroy() {
        }

        @PostActivate
        public void myActivate() {
        }

        @PrePassivate
        public void myPassivate() {
        }

        @AfterBegin
        public void myAfterBegin() {
        }

        @BeforeCompletion
        public void beforeCompletion() {
        }

        @AfterCompletion
        public void afterCompletion() {
        }
    }

}