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
package org.apache.tomee.embedded;

import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.NetworkUtil;
import org.apache.openejb.util.reflection.Reflections;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FailingJspTest {
    @Test
    public void run() throws MalformedURLException { // this test passes just cause we skip container tags
        final Collection<Object> tracked1;
        final WebContext ctx;
        try (final Container container = new Container(
                    new Configuration()
                            .http(NetworkUtil.getNextAvailablePort())
                            .property("openejb.container.additional.exclude", "org.apache.tomee.embedded.")
                            .property("openejb.additional.include", "tomee-")
                            .user("tomee", "tomeepwd")
                            .loginConfig(new LoginConfigBuilder().basic())
                            .securityConstaint(new SecurityConstaintBuilder().addAuthRole("**").authConstraint(true).addCollection("api", "/api/resource2/")))
                .deployPathsAsWebapp(JarLocation.jarLocation(FailingJspTest.class))
                .inject(this)) {

            ctx = SystemInstance.get().getComponent(ContainerSystem.class).getWebContextByHost("", "localhost");

            tracked1 = getTrackedContexts(ctx);

            for (int i = 0; i < 5; i++) {
                try {
                    IO.slurp(new URL("http://localhost:" + container.getConfiguration().getHttpPort() + "/fail.jsp"));
                } catch (final IOException e) {
                    // no-op
                }
            }

            final Collection<Object> tracked2 = getTrackedContexts(ctx);

            // bug in org.apache.jasper.servlet.JspServletWrapper.destroy()
            tracked2.removeAll(tracked1);
            assertEquals(String.valueOf(tracked2), 1, tracked2.size());
        }

        final Collection<Object> tracked2 = getTrackedContexts(ctx);

        // bug in org.apache.jasper.servlet.JspServletWrapper.destroy()
        tracked2.removeAll(tracked1);
        assertEquals(String.valueOf(tracked2), 0, tracked2.size());
    }

    private Collection<Object> getTrackedContexts(final WebContext ctx) {
        return new ArrayList<>(Map.class.cast(Reflections.get(ctx, "creationalContexts")).keySet());
    }
}
