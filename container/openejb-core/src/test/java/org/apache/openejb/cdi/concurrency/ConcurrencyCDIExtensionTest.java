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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.cdi.concurrency;

import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that the {@link ConcurrencyCDIExtension} correctly registers
 * concurrency resources as CDI beans, both with default and custom qualifiers.
 */
@RunWith(ApplicationComposer.class)
public class ConcurrencyCDIExtensionTest {

    @Inject
    private DefaultInjectionBean defaultBean;

    @Inject
    private QualifiedInjectionBean qualifiedBean;

    @Module
    public EnterpriseBean ejb() {
        return new SingletonBean(DummyEjb.class).localBean();
    }

    @Module
    public Class<?>[] beans() {
        return new Class<?>[]{
                DefaultInjectionBean.class,
                QualifiedInjectionBean.class,
                AppConfig.class
        };
    }

    @Test
    public void defaultManagedExecutorServiceIsInjectable() {
        assertNotNull("Default ManagedExecutorService should be injectable via @Inject",
                defaultBean.getMes());
    }

    @Test
    public void defaultManagedScheduledExecutorServiceIsInjectable() {
        assertNotNull("Default ManagedScheduledExecutorService should be injectable via @Inject",
                defaultBean.getMses());
    }

    @Test
    public void defaultManagedThreadFactoryIsInjectable() {
        assertNotNull("Default ManagedThreadFactory should be injectable via @Inject",
                defaultBean.getMtf());
    }

    @Test
    public void defaultContextServiceIsInjectable() {
        assertNotNull("Default ContextService should be injectable via @Inject",
                defaultBean.getCs());
    }

    @Test
    public void qualifiedManagedExecutorServiceIsInjectable() {
        assertNotNull("Qualified ManagedExecutorService should be injectable via @Inject @TestQualifier",
                qualifiedBean.getMes());
    }

    @Test
    public void qualifiedManagedExecutorServiceExecutesTask() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        qualifiedBean.getMes().execute(latch::countDown);
        assertTrue("Task should complete on qualified MES",
                latch.await(5, TimeUnit.SECONDS));
    }

    // --- Dummy EJB to trigger full resource deployment ---

    @jakarta.ejb.Singleton
    public static class DummyEjb {
    }

    // --- Qualifier ---

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
    public @interface TestQualifier {
    }

    // --- App config with qualifier-enabled definition ---

    @ManagedExecutorDefinition(
            name = "java:comp/env/concurrent/TestQualifiedExecutor",
            qualifiers = {TestQualifier.class}
    )
    @ApplicationScoped
    public static class AppConfig {
    }

    // --- Bean that injects default concurrency resources ---

    @ApplicationScoped
    public static class DefaultInjectionBean {

        @Inject
        private ManagedExecutorService mes;

        @Inject
        private ManagedScheduledExecutorService mses;

        @Inject
        private ManagedThreadFactory mtf;

        @Inject
        private ContextService cs;

        public ManagedExecutorService getMes() {
            return mes;
        }

        public ManagedScheduledExecutorService getMses() {
            return mses;
        }

        public ManagedThreadFactory getMtf() {
            return mtf;
        }

        public ContextService getCs() {
            return cs;
        }
    }

    // --- Bean that injects qualified concurrency resources ---

    @ApplicationScoped
    public static class QualifiedInjectionBean {

        @Inject
        @TestQualifier
        private ManagedExecutorService mes;

        public ManagedExecutorService getMes() {
            return mes;
        }
    }
}
