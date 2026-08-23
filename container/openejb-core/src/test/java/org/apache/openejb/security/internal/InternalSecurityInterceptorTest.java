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

package org.apache.openejb.security.internal;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.ri.sp.PseudoSecurityService;
import org.apache.openejb.spi.Assembler;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jakarta.interceptor.InvocationContext;
import jakarta.transaction.TransactionManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class InternalSecurityInterceptorTest {
    private Assembler previousAssembler;

    @Before
    public void setUp() {
        previousAssembler = SystemInstance.get().setComponent(Assembler.class, new NoRoleAssembler());
    }

    @After
    public void tearDown() {
        SystemInstance.get().getProperties().remove(InternalSecurityInterceptor.OPENEJB_INTERNAL_BEANS_SECURITY_ENABLED);
        if (previousAssembler == null) {
            SystemInstance.get().removeComponent(Assembler.class);
        } else {
            SystemInstance.get().setComponent(Assembler.class, previousAssembler);
        }
    }

    @Test
    public void disabledCheckProceeds() throws Exception {
        SystemInstance.get().setProperty(InternalSecurityInterceptor.OPENEJB_INTERNAL_BEANS_SECURITY_ENABLED, "false");
        assertEquals("proceeded", new InternalSecurityInterceptor().invoke(new ProceedingContext()));
    }

    private static final class NoRoleAssembler implements Assembler {
        private final SecurityService<?> securityService = new PseudoSecurityService();

        @Override
        public void init(final Properties props) {
            // no-op
        }

        @Override
        public void build() {
            // no-op
        }

        @Override
        public ContainerSystem getContainerSystem() {
            return null;
        }

        @Override
        public TransactionManager getTransactionManager() {
            return null;
        }

        @Override
        public SecurityService getSecurityService() {
            return securityService;
        }

        @Override
        public void destroy() {
            // no-op
        }
    }

    private static final class ProceedingContext implements InvocationContext {
        @Override
        public Object getTarget() {
            return null;
        }

        @Override
        public Object getTimer() {
            return null;
        }

        @Override
        public Method getMethod() {
            return null;
        }

        @Override
        public Constructor<?> getConstructor() {
            return null;
        }

        @Override
        public Object[] getParameters() {
            return new Object[0];
        }

        @Override
        public void setParameters(final Object[] params) {
            // no-op
        }

        @Override
        public Map<String, Object> getContextData() {
            return new HashMap<>();
        }

        @Override
        public Object proceed() {
            return "proceeded";
        }
    }
}
