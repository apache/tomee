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
import org.apache.openejb.server.cxf.event.ServerDestroyed;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import jakarta.jws.WebService;

import static org.junit.Assert.assertNotNull;

// client side: the observer lives in the test JVM like a container level service, the embedded container shares this JVM
@RunWith(Arquillian.class)
public class EventTest {
    // a class rule wraps the Arquillian deployment: the observer is there before the deployment,
    // the checks run after the undeployment (Arquillian runs @AfterClass before undeploying)
    @ClassRule
    public static final TestRule LISTENER = new ExternalResource() {
        private final Observer observer = new Observer();

        @Override
        protected void before() {
            SystemInstance.get().addObserver(observer);
        }

        @Override
        protected void after() {
            SystemInstance.get().removeObserver(observer);
            destroy();
        }
    };

    @Deployment(testable = false)
    public static WebArchive app() {
        return ShrinkWrap.create(WebArchive.class, "event.war").addClass(End.class);
    }

    @Test
    public void run() {
        assertNotNull(Observer.created);
        assertNotNull(Observer.created.getServer());
        assertNotNull(Observer.created.getServer().getEndpoint());
    }

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
