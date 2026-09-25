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
package org.apache.openejb.arquillian.tests.jaxws.event;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.server.cxf.event.ServerCreated;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.jws.WebService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import static org.junit.Assert.assertNotNull;

// in-container: the observer is registered server side by a listener of the webapp,
// POJO web services are deployed (and ServerCreated fired) once the webapp context started
@RunWith(Arquillian.class)
public class EventTest {
    @Deployment
    public static WebArchive app() {
        return ShrinkWrap.create(WebArchive.class, "event.war")
                .addClasses(EventTest.class, End.class, Observer.class, ObserverRegistration.class);
    }

    @Test
    public void run() {
        assertNotNull(Observer.created);
        assertNotNull(Observer.created.getServer());
        assertNotNull(Observer.created.getServer().getEndpoint());
    }

    @WebService
    public static class End {
        public String get() {
            return "end";
        }
    }

    public static class Observer {
        private static volatile ServerCreated created;

        public void created(@Observes final ServerCreated created) {
            Observer.created = created;
        }
    }

    // registers the observer before the JAX-WS deployment of this webapp, removes it on undeploy
    @WebListener
    public static class ObserverRegistration implements ServletContextListener {
        private final Observer observer = new Observer();

        @Override
        public void contextInitialized(final ServletContextEvent sce) {
            SystemInstance.get().addObserver(observer);
        }

        @Override
        public void contextDestroyed(final ServletContextEvent sce) {
            SystemInstance.get().removeObserver(observer);
        }
    }
}
