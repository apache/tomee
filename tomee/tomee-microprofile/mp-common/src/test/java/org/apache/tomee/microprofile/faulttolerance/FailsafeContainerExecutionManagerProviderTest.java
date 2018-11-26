package org.apache.tomee.microprofile.faulttolerance;

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

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.apache.safeguard.api.ExecutionManager;
import org.apache.safeguard.impl.cdi.FailsafeExecutionManagerProvider;
import org.apache.safeguard.impl.cdi.SafeguardInterceptor;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.Singleton;
import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class FailsafeContainerExecutionManagerProviderTest {

    @Inject
    private FailsafeExecutionManagerProvider manager;

    @Inject
    private FTClass ftClass;

    @Module
    @Classes(value = {FailsafeContainerExecutionManagerProvider.class,
            FailsafeExecutionManagerProvider.class,
            ExecutionManager.class,
            SafeguardInterceptor.class,
            FTClass.class},
            cdi = true)
    public WebApp app() {
        return new WebApp();
    }

    @Test
    public void testManagerInjection() throws Exception {

        assertEquals("called", ftClass.validateInjectedClass());

        assertTrue("We must override the original FailsafeExecutionManagerProvider, was:" + manager.getClass(),
                manager instanceof FailsafeContainerExecutionManagerProvider);
    }

    @Singleton
    public static class FTClass {

        @CircuitBreaker
        public String validateInjectedClass() {
            return "called";
        }
    }

}