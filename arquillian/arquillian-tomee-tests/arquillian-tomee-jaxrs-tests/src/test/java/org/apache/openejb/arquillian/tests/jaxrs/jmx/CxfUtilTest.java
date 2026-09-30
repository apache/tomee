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
package org.apache.openejb.arquillian.tests.jaxrs.jmx;

import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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

// needs openejb.cxf.monitoring.jmx=true on the container, see arquillian.xml
@RunWith(Arquillian.class)
public class CxfUtilTest {
    @ArquillianResource
    private URL root;

    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(CxfUtilTest.class, Endpoint.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void checkMonitoring() throws IOException, MalformedObjectNameException {
        // need a call to get something
        IO.slurp(new URL(root.toExternalForm() + "monitoring/cxf"));
        // the counters of the apps deployed before in this JVM stay registered, only count the ones of this endpoint
        final Set<ObjectInstance> mbeans = ManagementFactory.getPlatformMBeanServer().queryMBeans(new ObjectName("*:*,type=Performance.Counter.Server,service="
                + ObjectName.quote("{http://jmx.jaxrs.tests.arquillian.openejb.apache.org/}Endpoint")), null);
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
