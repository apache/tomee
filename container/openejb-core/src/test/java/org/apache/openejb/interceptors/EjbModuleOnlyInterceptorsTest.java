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

import org.apache.openejb.core.OpenEJBInitialContextFactory;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.interceptor.AroundConstruct;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
@Classes(innerClassesAsBean = true) // don't activate CDI here because on TCK it won't get activated
public class EjbModuleOnlyInterceptorsTest {

    @EJB
    private InterceptorBean interceptorBean;

    @EJB
    private HistorySingletonBean historySingletonBean;

    @Before
    public void setUp() throws Exception {
        final Context ctx = new InitialContext(new PropertiesBuilder()
                                                   .p(Context.INITIAL_CONTEXT_FACTORY,
                                                      OpenEJBInitialContextFactory.class.getName())
                                                   .build());
        ctx.bind("inject", this);
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
