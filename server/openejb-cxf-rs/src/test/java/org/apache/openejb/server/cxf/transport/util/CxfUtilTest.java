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
package org.apache.openejb.server.cxf.transport.util;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@EnableServices("jaxrs")
@ContainerProperties({
        @ContainerProperties.Property(name = "openejb.cxf.monitoring.jmx", value = "true"),
        @ContainerProperties.Property(name = "openejb.cxf.monitoring.jmx.clear-on-undeploy", value = "true")
})
@Classes(cdi = true, innerClassesAsBean = true, context = "test")
@RunWith(ApplicationComposer.class)
public class CxfUtilTest {
    @RandomPort("http")
    private URL root;

    @Test
    public void checkMonitoring() throws IOException, MalformedObjectNameException {
        // need a call to get something
        IO.slurp(new URL(root.toExternalForm() + "test/monitoring/cxf"));
        final Set<ObjectInstance> mbeans = ManagementFactory.getPlatformMBeanServer().queryMBeans(new ObjectName("*:*,type=Performance.Counter.Server"), null);
        assertNotNull(mbeans);
        assertEquals(2, mbeans.size());
    }

    @Path("monitoring/cxf")
    public static class Endpoint {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            return "ok";
        }
    }
}
