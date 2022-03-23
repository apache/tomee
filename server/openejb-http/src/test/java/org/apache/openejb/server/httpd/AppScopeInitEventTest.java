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
package org.apache.openejb.server.httpd;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Component;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SimpleLog
@EnableServices("http")
@Classes(cdi = true, innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class AppScopeInitEventTest {
    @Component
    public ServletContext context() { // default one doesnt read WebApp
        final EmbeddedServletContext servletContext = new EmbeddedServletContext();
        servletContext.setInitParameter("test", "start");
        return servletContext;
    }

    @Inject
    private Start start;

    @Inject
    private ServletContext context;

    @Test
    public void checkAccessAtStartup() {
        assertNotNull(start.getContext());
        assertEquals("start", start.getValue());
    }

    @Test
    public void checkAccessAtRuntime() {
        assertEquals("start", start.getContext().getInitParameter("test"));
        assertEquals("start", context.getInitParameter("test"));
    }

    @ApplicationScoped
    public static class Start {
        private volatile ServletContext context;
        private volatile String value;

        private void capture(@Observes @Initialized(ApplicationScoped.class) final ServletContext context) {
            if (this.context != null) {
                throw new IllegalStateException("app context started twice");
            }

            this.context = context;
            this.value = context.getInitParameter("test");
        }

        public ServletContext getContext() {
            return context;
        }

        public String getValue() {
            return value;
        }
    }
}
