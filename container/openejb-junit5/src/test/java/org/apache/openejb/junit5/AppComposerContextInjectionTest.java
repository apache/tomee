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
package org.apache.openejb.junit5;

import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.rest.ThreadLocalContextManager;
import org.apache.openejb.testing.AppResource;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.rest.ContextProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.naming.Context;
import javax.naming.NamingException;
import jakarta.ws.rs.core.SecurityContext;
import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@RunWithApplicationComposer(mode = ExtensionMode.PER_ALL)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AppComposerContextInjectionTest {
    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(MyBean.class).localBean();
    }

    @AppResource
    private Context context;

    @AppResource
    private ContextProvider provider;

    @Test
    public void lookupShouldWorkOnOpenEJBNames() throws NamingException {
        assertEquals("ok", MyBean.class.cast(context.lookup("MyBeanLocalBean")).ok());
    }

    @Test
    public void lookupShouldWorkOnGlobalNames() throws NamingException {
        assertEquals("ok", MyBean.class.cast(context.lookup("java:global/AppComposerContextInjectionTest/bean/MyBean")).ok());
    }

    @Test
    public void jaxrs() throws NamingException {
        assertNotNull(provider);
        assertNull(provider.find(SecurityContext.class));
        final SecurityContext securityContext = new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return null;
            }

            @Override
            public boolean isUserInRole(final String s) {
                return "foo".equals(s);
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public String getAuthenticationScheme() {
                return null;
            }
        };
        provider.register(SecurityContext.class, securityContext);
        assertNotNull(provider.find(SecurityContext.class));
        assertTrue(SecurityContext.class.cast(ThreadLocalContextManager.findThreadLocal(SecurityContext.class)).isUserInRole("foo"));
        assertFalse(SecurityContext.class.cast(ThreadLocalContextManager.findThreadLocal(SecurityContext.class)).isUserInRole("bar"));
    }

    public static class MyBean {
        public String ok() {
            return "ok";
        }
    }
}
