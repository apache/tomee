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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.filter;

import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp25.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webapp25.WebAppVersionType;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class AllFilterTest {
    @Deployment
    public static Archive<?> war() throws Exception {
        final String port = findPort();
        System.setProperty(AllFilterTest.class.getName() + ".port", port); // for embedded case

        return ShrinkWrap.create(WebArchive.class, "filter.war")
            .addClass(MyFilter.class)
            .addAsWebInfResource(new StringAsset(port), "classes/port.conf") // for remote case
            .addAsWebInfResource(new StringAsset(Descriptors.create(WebAppDescriptor.class)
                .version(WebAppVersionType._2_5)
                .createFilter()
                    .filterName("My")
                    .filterClass(MyFilter.class.getName())
                .up()
                .createFilterMapping()
                    .filterName("My")
                    .urlPattern("/*")
                .up()
            .exportAsString()), "web.xml");
    }

    @Test // needs to be executed on server side otherwise all is done with local executor and test obviously works
    public void requestShouldWorkEvenIfFilterInterceptsAllIncludingTheArquillianServlet() throws IOException {
        assertEquals("No problemo!", IO.slurp(new URL("http://localhost:" + httpPort() + "/filter/")));
    }

    private static String httpPort() throws IOException {
        final String property = System.getProperty(AllFilterTest.class.getName() + ".port");
        if (property != null) {
            return property;
        } // else remote
        return org.apache.openejb.loader.IO.slurp(Thread.currentThread().getContextClassLoader().getResourceAsStream("port.conf"));
    }

    private static String findPort() throws Exception {
        final String opts = System.getProperty("java.opts");
        if (opts != null) {
            return opts.substring(opts.indexOf("tomee.httpPort=") + "tomee.httpPort=".length());
        } // else embedded
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        final ObjectName on = server.queryMBeans(new ObjectName("Tomcat:type=ProtocolHandler,port=*"), null).iterator().next().getObjectName();
        return "" + server.getAttribute(on, "port");
    }
}
