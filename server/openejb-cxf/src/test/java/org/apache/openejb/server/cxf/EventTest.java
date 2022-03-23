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
package org.apache.openejb.server.cxf;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.server.cxf.event.ServerCreated;
import org.apache.openejb.server.cxf.event.ServerDestroyed;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.jws.WebService;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

@EnableServices("jaxws")
@RunWith(ApplicationComposer.class)
public class EventTest {
    @Module
    @Classes(innerClassesAsBean = true)
    public WebApp app() {
        return new WebApp();
    }

    @RandomPort("http")
    private int port;

    @Configuration
    public Properties config() {
        return new PropertiesBuilder().p("listener", "new://Service?class-name=" + Observer.class.getName()).build();
    }

    @Test
    public void run() {
        assertNotNull(Observer.created);
        assertNotNull(Observer.created.getServer());
        assertNotNull(Observer.created.getServer().getEndpoint());
    }

    @AfterClass
    public static void destroy() {
        assertNotNull(Observer.destroyed);
        assertNotNull(Observer.destroyed.getServer());
        assertNotNull(Observer.destroyed.getServer().getEndpoint());
    }

    @WebService
    public static class End {
        public String get() {
            return "end";
        }
    }

    public static class Observer {
        private static ServerCreated created;
        private static ServerDestroyed destroyed;

        public void created(@Observes final ServerCreated created) {
            Observer.created = created;
        }

        public void destroyed(@Observes final ServerDestroyed destroyed) {
            Observer.destroyed = destroyed;
        }
    }
}
