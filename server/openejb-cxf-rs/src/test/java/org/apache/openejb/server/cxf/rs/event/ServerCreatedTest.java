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
package org.apache.openejb.server.cxf.rs.event;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.Path;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

@EnableServices("jaxrs")
@RunWith(ApplicationComposer.class)
public class ServerCreatedTest {
    private static int port = -1;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder()
                .p("httpejbd.port", Integer.toString(port))
                .p("observer", "new://Service?class-name=" + Observer.class.getName())
                .build();
    }

    @Module
    @Classes(ServerCreatedEndpoint.class)
    public WebApp war() {
        return new WebApp().contextRoot("foo");
    }

    @Test
    public void checkEvent() {
        assertNotNull(Observer.event);
        assertNotNull(Observer.event.getAppContext());
        assertNotNull(Observer.event.getWebContext());
        assertNotNull(Observer.event.getServer());
    }

    @Path("ServerCreatedTest")
    public static class ServerCreatedEndpoint {
        @HEAD
        public void useless() {}
    }

    public static class Observer {
        public static ServerCreated event;

        public void obs(@Observes final ServerCreated event) {
            Observer.event = event;
        }
    }
}
