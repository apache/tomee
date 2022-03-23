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

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import java.util.concurrent.Future;

/**
 * @author <a href="mailto:tommy@diabol.se">Tommy Tynj&auml;</a>
 */
@RunWith(ValidationRunner.class)
public class CheckInvalidAsynchronousAnnotationsTest {

    private static final String VALIDATION_OUTPUT_LEVEL = "openejb.validation.output.level";

    @BeforeClass
    public static void setupTestCase() {
        SystemInstance.reset();
        final SystemInstance system = SystemInstance.get();
        system.setProperty(VALIDATION_OUTPUT_LEVEL, "VERBOSE");
    }

    @AfterClass
    public static void cleanupTestCase() {
        SystemInstance.reset();
    }

    @Keys({@Key("asynchronous.badReturnType")})
    public EjbJar shouldRecognizeBadReturnTypeForAnnotatedMethod() throws Exception {
        return ejbJarWithStatelessBean(AsyncBeanMethodWithWrongReturnType.class);
    }

    @Keys
    public EjbJar shouldAcceptAsyncMethodWithVoidReturnType() {
        return ejbJarWithStatelessBean(AsyncBeanMethodWithVoidReturnType.class);
    }

    @Keys
    public EjbJar shouldAcceptAsyncMethodWithFutureReturnType() {
        return ejbJarWithStatelessBean(AsyncBeanMethodWithFutureReturnType.class);
    }

    @Keys({@Key("asynchronous.badReturnType")})
    public EjbJar shouldRecognizeBadReturnTypeForAnnotatedBean() throws Exception {
        return ejbJarWithStatelessBean(AsyncBeanWithWrongReturnType.class);
    }

    @Keys
    public EjbJar shouldAcceptAsyncBeanWithVoidReturnTypeMethod() {
        return ejbJarWithStatelessBean(AsyncBeanWithVoidReturnType.class);
    }

    @Keys
    public EjbJar shouldAcceptAsyncBeanWithFutureReturnTypeMethod() {
        return ejbJarWithStatelessBean(AsyncBeanWithFutureReturnType.class);
    }

    @Keys({@Key("asynchronous.badExceptionType")})
    public EjbJar shouldRecognizeBadExceptionTypeForAsyncMethodWithVoidReturnType() {
        return ejbJarWithStatelessBean(AsyncBeanWithIllegalExceptionType.class);
    }

    @Keys({@Key(value = "ignoredMethodAnnotation", type = KeyType.WARNING)})
    public EjbJar shouldWarnForAsyncAnnotationOnNonSessionBeanMethod() {
        return ejbJarWithMessageDrivenBean(MessageDrivenBeanWithAsyncMethod.class);
    }

    @Keys({@Key(value = "ignoredClassAnnotation", type = KeyType.WARNING)})
    public EjbJar shouldWarnForAsyncAnnotationOnNonSessionBean() {
        return ejbJarWithMessageDrivenBean(MessageDrivenBeanWithAsyncClassLevelAnnotation.class);
    }

    private static EjbJar ejbJarWithStatelessBean(final Class<?> beanClass) {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(beanClass));
        return ejbJar;
    }

    private static EjbJar ejbJarWithMessageDrivenBean(final Class<?> beanClass) {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new MessageDrivenBean(beanClass));
        return ejbJar;
    }

    public static class AsyncBeanMethodWithWrongReturnType {
        @Asynchronous
        public Object asyncMethodWithWrongReturnType() {
            return "async invocation";
        }
    }

    public static class AsyncBeanMethodWithVoidReturnType {
        @Asynchronous
        public void validReturnType() {
        }
    }

    public static class AsyncBeanMethodWithFutureReturnType {
        @Asynchronous
        public Future<String> validReturnType() {
            return new AsyncResult<>("returning from async call");
        }
    }

    @Asynchronous
    public static class AsyncBeanWithWrongReturnType {
        public String asyncMethodWithWrongReturnType() {
            return "should not be valid for an async bean";
        }
    }

    @Asynchronous
    public static class AsyncBeanWithVoidReturnType {
        public void validReturnType() {
        }
    }

    @Asynchronous
    public static class AsyncBeanWithFutureReturnType {
        public Future<String> validReturnType() {
            return new AsyncResult<>("returning from async call");
        }
    }

    @Asynchronous
    public static class AsyncBeanWithIllegalExceptionType {
        private class ArbitraryApplicationException extends Exception {
        }

        public void businessMethod() throws ArbitraryApplicationException {
        }
    }

    @Asynchronous
    @MessageDriven
    public static class MessageDrivenBeanWithAsyncClassLevelAnnotation implements MessageListener {
        public void onMessage(final Message message) {
        }
    }

    @MessageDriven
    public static class MessageDrivenBeanWithAsyncMethod implements MessageListener {
        public void onMessage(final Message message) {
        }

        @Asynchronous
        public void asyncMethod() {
        }
    }
}
