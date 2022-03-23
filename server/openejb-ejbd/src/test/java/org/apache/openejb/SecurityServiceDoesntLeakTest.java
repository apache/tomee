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
package org.apache.openejb;

import org.apache.openejb.client.RemoteInitialContextFactory;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.testing.SimpleLog;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.reflection.Reflections;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.Properties;
import jakarta.ejb.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SimpleLog
@EnableServices("ejbd")
@Classes(innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class SecurityServiceDoesntLeakTest {
    @RandomPort("ejbd")
    private int port;

    @Test
    public void run() throws NamingException {
        final SecurityService ss = SystemInstance.get().getComponent(SecurityService.class);
        assertNotNull(ss);

        final Map<Object, Object> identities = (Map<Object, Object>) Reflections.get(ss, "identities");
        assertEquals(0, identities.size());

        final Properties p = new PropertiesBuilder()
                .p("java.naming.factory.initial", RemoteInitialContextFactory.class.getName())
                .p("java.naming.provider.url", "ejbd://localhost:" + port)
                .p("java.naming.security.principal", "foo")
                .p("java.naming.security.credentials", "bar")
                .p("openejb.authentication.realmName", "PropertiesLogin")
                .build();
        final Context ctx = new InitialContext(p);

        final CallMeRemotely handle = CallMeRemotely.class.cast(
                ctx.lookup("java:global/openejb/CallMe!org.apache.openejb.SecurityServiceDoesntLeakTest$CallMeRemotely"));
        assertNotNull(handle);
        assertEquals("remote!", handle.remote());
        assertEquals(1, identities.size());

        ctx.close();
        assertEquals(0, identities.size());
    }

    @AfterClass
    public static void resetJAAS() {
        System.clearProperty("java.security.auth.login.config");
    }

    @Singleton
    public static class CallMe implements CallMeRemotely {
        @Override
        public String remote() {
            return "remote!";
        }
    }

    public interface CallMeRemotely {
        String remote();
    }
}
