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
package org.apache.openejb.server.ejbd;

import org.apache.openejb.client.RemoteInitialContextFactory;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ServiceManager;
import org.apache.openejb.server.SimpleServiceManager;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.util.reflection.Reflections;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@EnableServices("ejbd")
@RunWith(ApplicationComposer.class)
public class ZEjbdTest {
    @RandomPort("ejbd")
    private int ejbd;

    @Configuration
    public Properties configuration() {
        final Properties configuration = new Properties();
        configuration.setProperty("ejbd.gzip", "true");
        configuration.setProperty("openejb.client.connection.socket.read", "1000");
        return configuration;
    }

    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(AppClientTest.Orange.class).localBean();
    }

    @Test
    public void checkZipIsOn() throws Exception {
        int checked = 0;
        for (final ServerService daemon : SimpleServiceManager.class.cast(ServiceManager.get()).getDaemons()) {
            if (ServiceDaemon.class.isInstance(daemon) && daemon.getName().equals("ejbd")) {
                assertTrue(EjbDaemon.class.cast(Reflections.get(Reflections.get(Reflections.get(Reflections.get(Reflections.get(Reflections.get(Reflections.get(daemon, "next"),
                        "service"), "service"), "service"),
                    "service"), "service"), "server")).isGzip());
                checked++;
            }
        }
        assertEquals(1, checked);
    }

    @Test
    public void checkItWorks() throws Exception {
        remoteCall("zejbd");
    }

    @Test(expected = NamingException.class)
    public void checkEjbdFailWithAGzipServer() throws Exception {
        remoteCall("ejbd");
    }

    private void remoteCall(final String scheme) throws NamingException {
        final Context ctx = new InitialContext(new Properties() {{
            setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
            setProperty(Context.PROVIDER_URL, scheme + "://localhost:" + ejbd + "?connectTimeout=1000&readTimeout=1000");
        }});
        assertEquals("hello", ((AppClientTest.OrangeBusinessRemote) ctx.lookup("OrangeRemote")).echo("olleh"));
    }
}
