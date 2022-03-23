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
package org.apache.openejb.cdi;

import org.apache.openejb.testing.ApplicationComposers;
import org.apache.openejb.testing.Module;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContextsService;
import org.junit.Test;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.spi.Context;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class EnsureRequestScopeThreadLocalIsCleanUpTest {
    @Test
    public void runAndCheckThreadLocal() throws Exception {
        final ApplicationComposers composers = new ApplicationComposers(EnsureRequestScopeThreadLocalIsCleanUpTest.class);
        composers.before(this);
        final CdiAppContextsService contextsService = CdiAppContextsService.class.cast(WebBeansContext.currentInstance().getService(ContextsService.class));
        assertNotNull(contextsService.getCurrentContext(RequestScoped.class));
        assertNotNull(contextsService.getCurrentContext(SessionScoped.class));
        composers.after();
        assertNull(contextsService.getCurrentContext(RequestScoped.class));
        assertNull(contextsService.getCurrentContext(SessionScoped.class));
    }

    @Test
    public void ensureRequestContextCanBeRestarted() throws Exception {
        final ApplicationComposers composers = new ApplicationComposers(EnsureRequestScopeThreadLocalIsCleanUpTest.class);
        composers.before(this);
        final CdiAppContextsService contextsService = CdiAppContextsService.class.cast(WebBeansContext.currentInstance().getService(ContextsService.class));
        final Context req1 = contextsService.getCurrentContext(RequestScoped.class);
        assertNotNull(req1);
        final Context session1 = contextsService.getCurrentContext(SessionScoped.class);
        assertNotNull(session1);
        contextsService.endContext(RequestScoped.class, null);
        contextsService.startContext(RequestScoped.class, null);
        final Context req2 = contextsService.getCurrentContext(RequestScoped.class);
        assertNotSame(req1, req2);
        final Context session2 = contextsService.getCurrentContext(SessionScoped.class);
        assertSame(session1, session2);
        composers.after();
        assertNull(contextsService.getCurrentContext(RequestScoped.class));
        assertNull(contextsService.getCurrentContext(SessionScoped.class));
    }

    @Module
    public Class<?>[] clazz() {
        return new Class<?>[]{EnsureRequestScopeThreadLocalIsCleanUpTest.class};
    }
}
