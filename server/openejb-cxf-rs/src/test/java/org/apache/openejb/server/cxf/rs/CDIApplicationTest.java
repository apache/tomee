/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.server.cxf.rs.beans.MyFirstRestClass;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class CDIApplicationTest {

    private static int port = -1;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder().p("httpejbd.port", Integer.toString(port)).build();
    }

    @Module
    @Classes(cdi = true, value = {MyCdiRESTApplication.class, MyFirstRestClass.class, ACdiBeanInjectedInApp.class})
    public WebApp war() {
        return new WebApp()
            .contextRoot("foo")
            .addServlet("REST Application", Application.class.getName())
            .addInitParam("REST Application", "javax.ws.rs.Application", MyCdiRESTApplication.class.getName());
    }

    @Test
    public void isCdi() {
        assertTrue(MyCdiRESTApplication.injection.size() > 0);
        for (final Boolean b : MyCdiRESTApplication.injection) {
            assertTrue(b);
        }
        assertEquals("Hi from REST World!", WebClient.create("http://localhost:" + port + "/foo/").path("/first/hi").get(String.class));
    }

    public static class ACdiBeanInjectedInApp {
    }

    public static class MyCdiRESTApplication extends Application {
        public static Collection<Boolean> injection = new ArrayList<>();

        @Inject
        private ACdiBeanInjectedInApp cdi;

        public Set<Class<?>> getClasses() {
            // if no class are returned we use scanning, since we don't test rest deployment we put a single class
            return Collections.<Class<?>>singleton(MyFirstRestClass.class);
        }

        @Override
        public Set<Object> getSingletons() {
            injection.add(cdi != null);
            return super.getSingletons();
        }
    }
}
