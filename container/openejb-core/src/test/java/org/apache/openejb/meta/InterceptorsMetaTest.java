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
package org.apache.openejb.meta;

import org.junit.runner.RunWith;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @version $Rev$ $Date$
 */
@RunWith(MetaRunner.class)
public class InterceptorsMetaTest {

    @MetaTest(expected = ExpectedBean.class, actual = ActualBean.class)
    public void test() {
    }

    @Interceptors({LogUser.class, LogMethod.class, LogExceptions.class})
    @Metatype
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface CasualLogging {
    }

    @Interceptors({LogInvokeTime.class, LogArguments.class, LogReturnValue.class})
    @Metatype
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface CriticalLogging {
    }


    /**
     * Standard bean
     */
    @Interceptors({LogUser.class, LogMethod.class, LogExceptions.class})
    public static class ExpectedBean implements Bean {

        @Interceptors({LogInvokeTime.class, LogArguments.class, LogReturnValue.class})
        public void method() {
        }
    }

    /**
     * Meta bean
     */
    @CasualLogging
    public static class ActualBean implements Bean {

        @CriticalLogging
        public void method() {
        }
    }


    public static interface Bean {
    }


    public static class BaseInterceptor {

        @AroundInvoke
        public Object invoke(final InvocationContext context) throws Exception {
            return context.proceed();
        }
    }

    public static class LogUser extends BaseInterceptor {
    }

    public static class LogMethod extends BaseInterceptor {
    }

    public static class LogInvokeTime extends BaseInterceptor {
    }

    public static class LogArguments extends BaseInterceptor {
    }

    public static class LogReturnValue extends BaseInterceptor {
    }

    public static class LogExceptions extends BaseInterceptor {
    }
}