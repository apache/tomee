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
package org.apache.tomee.application.composer;

import org.apache.tomee.application.composer.component.WebComponent;
import org.junit.Test;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import static org.junit.Assert.assertTrue;

public class ManualListenerTest {
    public static boolean init;
    public static boolean destroy;

    @WebComponent(urlPatterns = "/ManualFilterTest", loadOnStartup = 1)
    public ServletContextListener simpleServlet() {
        return new ServletContextListener() {
            @Override
            public void contextInitialized(final ServletContextEvent servletContextEvent) {
                init = servletContextEvent != null;
            }

            @Override
            public void contextDestroyed(final ServletContextEvent servletContextEvent) {
                destroy = servletContextEvent != null;
            }
        };
    }

    @Test
    public void checkItIsDeployed() throws Exception {
        init = false;
        destroy = false;
        try (final TomEEApplicationComposer runner = new TomEEApplicationComposer(ManualListenerTest.class)) {
            assertTrue(init);
        }
        assertTrue(destroy);
    }
}
