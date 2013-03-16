/*
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
package org.apache.openejb.core.stateless;

import org.apache.openejb.jee.Beans;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class StatelessEjbCdiBeanInterceptorsTest extends Assert {

    @EJB
    private OrangeBean orangeBean;

    @Module
    public Class[] module() {
        return new Class[]{CdiInterceptor.class, CdiInterceptorBinding.class, EjbInterceptor.class, OrangeBean.class};
    }

    @Module
    public Beans beans() {
        final Beans beans = new Beans();
        beans.addInterceptor(CdiInterceptor.class);
        return beans;
    }


    @Test
    public void test() throws Exception {
        assertEquals(0, calls.size());

        orangeBean.hello();

        assertEquals(new ArrayList<Call>(), calls);
    }

    public static List<Call> calls = new ArrayList<Call>();

    public static enum Call {
        CDI,
        EJB,
        BEAN
    }


    @InterceptorBinding
    @Target({TYPE, METHOD})
    @Retention(RUNTIME)
    public @interface CdiInterceptorBinding {
    }

    @Interceptor
    @CdiInterceptorBinding
    public static class CdiInterceptor {

        @AroundInvoke
        public Object invoke(InvocationContext context) throws Exception {
            calls.add(Call.CDI);
            return context.proceed();
        }
    }

    public static class EjbInterceptor {

        @AroundInvoke
        public Object invoke(InvocationContext context) throws Exception {
            calls.add(Call.EJB);
            return context.proceed();
        }
    }

    @Stateless
    @Interceptors(EjbInterceptor.class)
    @CdiInterceptorBinding
    public static class OrangeBean {

        @AroundInvoke
        public Object invoke(InvocationContext context) throws Exception {
            calls.add(Call.BEAN);
            return context.proceed();
        }

        public void hello() {

        }
    }
}
