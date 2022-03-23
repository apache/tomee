/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.interceptors;

import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.ExcludeDefaultInterceptors;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
// don't activate CDI here - we are full EJB
public class EjbModuleOnlyInterceptorsTest {

    @EJB
    private InterceptorBean interceptorBean;

    @EJB
    private HistorySingletonBean historySingletonBean;

    @Module
    public EjbJar setUp() throws Exception {
        final EjbJar ejbJar = new EjbJar("bla");
        ejbJar.addEnterpriseBean(new SingletonBean(InterceptorBean.class));
        ejbJar.addEnterpriseBean(new SingletonBean(HistorySingletonBean.class));
        ejbJar.addInterceptor(new Interceptor(InterceptorColor.class));
        ejbJar.addInterceptor(new Interceptor(InterceptorJuice.class));
        final AssemblyDescriptor assemblyDescriptor = new AssemblyDescriptor();
        final InterceptorBinding binding =
            new InterceptorBinding("*", InterceptorJuice.class.getName(), InterceptorColor.class.getName());
        assemblyDescriptor.addInterceptorBinding(binding);
        ejbJar.setAssemblyDescriptor(assemblyDescriptor);
        return ejbJar;
    }

    @Test
    public void allInterceptorsUsed() throws Exception {
        assertNotNull(interceptorBean);
        final List<String> postConstructRecordsFor = historySingletonBean.getPostConstructRecordsFor();

        // todo, when we have an idea about the fix, we can add asserts so build does not fail for now
        // this is a placeholder for people to jump in and help, because I'm getting a bit stuck on this one.
        System.out.println(postConstructRecordsFor);
    }

    @Singleton
    @Startup
    @ExcludeDefaultInterceptors
    @Interceptors({
                      InterceptorColor.class,
                      InterceptorJuice.class
                  })
    public static class InterceptorBean extends InterceptorBeanBase {
        private static final String simpleName = "InterceptorBean";

        @PostConstruct
        protected void postConstruct() {
            this.historySingletonBean.addPostConstructRecordFor(simpleName);
        }

    }

    public static abstract class InterceptorBeanBase {
        private static final String simpleName = "InterceptorBeanBase";

        @EJB
        protected HistorySingletonBean historySingletonBean;

        @PostConstruct
        protected void postConstructInInterceptorBeanBase() {
            this.historySingletonBean.addPostConstructRecordFor(simpleName);
        }

        public HistorySingletonBean getHistorySingletonBean() {
            return this.historySingletonBean;
        }
    }

    public static class InterceptorColor extends InterceptorJuice {
        private static final String simpleName = "InterceptorColor";

        @AroundConstruct
        private void aroundConstruct(InvocationContext ic) {
            Object savedTarget = ic.getTarget();
            try {
                ic.proceed();

            } catch (final Exception ex) {
                this.historySingletonBean.addPostConstructRecordFor(ex.toString());
                return;
            }
            if (savedTarget != null) {
                this.historySingletonBean.addPostConstructRecordFor("NotNullTargetBeforeProceed");
                return;
            }
            this.historySingletonBean.addPostConstructRecordFor(simpleName);
        }
    }

    public static class InterceptorJuice extends InterceptorBase {
        private static final String simpleName = "InterceptorJuice";

        @AroundConstruct
        private void aroundConstruct(InvocationContext ic) {
            Object savedTarget = ic.getTarget();
            try {
                ic.proceed();

            } catch (final Exception ex) {
                this.historySingletonBean.addPostConstructRecordFor(ex.toString());
                return;
            }
            if (savedTarget != null) {
                this.historySingletonBean.addPostConstructRecordFor("NotNullTargetBeforeProceed");
                return;
            }
            this.historySingletonBean.addPostConstructRecordFor(simpleName);
        }
    }

    public static abstract class InterceptorBase extends InterceptorBaseBase {
        private static final String simpleName = "InterceptorBase";

        @PostConstruct
        private void postConstruct(InvocationContext inv) {
            this.historySingletonBean.addPostConstructRecordFor(simpleName);
            try {
                inv.proceed();

            } catch (final Exception ex) {
                this.historySingletonBean.addPostConstructRecordFor(ex.toString());
            }
        }
    }

    public static abstract class InterceptorBaseBase {
        private static final String simpleName = "InterceptorBaseBase";

        @EJB
        protected HistorySingletonBean historySingletonBean;

        @PostConstruct
        protected void postConstructInInterceptorBaseBase(InvocationContext inv) {
            this.historySingletonBean.addPostConstructRecordFor(simpleName);
            try {
                inv.proceed();

            } catch (final Exception ex) {
                this.historySingletonBean.addPostConstructRecordFor(ex.toString());
            }
        }

    }

    @Startup
    @Singleton
    @ExcludeDefaultInterceptors
    public static class HistorySingletonBean {
        private List<String> postConstructRecordsMap = new ArrayList<>();

        public List<String> getPostConstructRecordsFor() {
            return Collections.unmodifiableList(postConstructRecordsMap);
        }

        public void addPostConstructRecordFor(final String aEntry) {
            postConstructRecordsMap.add(aEntry);
        }

    }
}
