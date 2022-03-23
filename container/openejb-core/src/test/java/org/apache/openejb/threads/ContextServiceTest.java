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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.threads;

import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.enterprise.concurrent.ContextService;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class ContextServiceTest {
    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(ContextServiceFacade.class).localBean();
    }

    @EJB
    private ContextServiceFacade facade;

    @Test
    public void checkContext() throws Exception {
        final Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return ThreadContext.getThreadContext().getBeanContext().getBeanClass() == ContextServiceFacade.class;
            }
        };
        assertFalse(callable.call());
        assertTrue(facade.proxy(callable).call());
    }

    @Singleton
    public static class ContextServiceFacade {
        @Resource
        private ContextService cs;

        public Callable<Boolean> proxy(final Callable<Boolean> callable) {
            return cs.createContextualProxy(callable, Callable.class);
        }

    }
}
