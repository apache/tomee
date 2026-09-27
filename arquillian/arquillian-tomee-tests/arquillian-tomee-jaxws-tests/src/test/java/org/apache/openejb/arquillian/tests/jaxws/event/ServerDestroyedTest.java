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
package org.apache.openejb.arquillian.tests.jaxws.event;

import org.apache.openejb.arquillian.common.ArquillianUtil;
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
import static org.junit.Assume.assumeTrue;

// client side: ServerDestroyed fires on undeploy, so the observer lives in the test JVM,
// which is the server's one with tomee-embedded only
@RunWith(Arquillian.class)
public class ServerDestroyedTest {
    private static final Observer OBSERVER = new Observer();

    // a class rule wraps the Arquillian deployment: the observer is there before the deployment,
    // the check runs after the undeployment (Arquillian runs @AfterClass before undeploying)
    @ClassRule
    public static final TestRule EMBEDDED_ONLY_AND_AFTER = new ExternalResource() {
        @Override
        protected void before() {
            assumeTrue("needs the container in the test JVM",
                    ArquillianUtil.isCurrentAdapter("tomee-embedded"));
            SystemInstance.get().addObserver(OBSERVER);
        }

        @Override
        protected void after() {
            SystemInstance.get().removeObserver(OBSERVER);
            assertNotNull(OBSERVER.destroyed);
            assertNotNull(OBSERVER.destroyed.getServer());
            assertNotNull(OBSERVER.destroyed.getServer().getEndpoint());
        }
    };

    @Deployment(testable = false)
    public static WebArchive app() {
        return ShrinkWrap.create(WebArchive.class, "destroyed.war").addClass(End.class);
    }

    @Test
    public void created() {
        assertNotNull(OBSERVER.created);
    }

    @WebService
    public static class End {
        public String get() {
            return "end";
        }
    }

    public static class Observer {
        private volatile ServerCreated created;
        private volatile ServerDestroyed destroyed;

        public void created(@Observes final ServerCreated created) {
            this.created = created;
        }

        public void destroyed(@Observes final ServerDestroyed destroyed) {
            this.destroyed = destroyed;
        }
    }
}
