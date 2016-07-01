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
package org.apache.openejb.arquillian.tests.jaxrs.staticresources;

import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class AvoidConflictTest {
    @Deployment(testable = false)
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, "AvoidConflictTest.war")
                    .addClasses(TheResource.class, SimpleServlet.class, PreviousFilter.class)
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                    .addAsWebResource(new StringAsset("static"), "index.html")
                    .addAsWebResource(new StringAsset("JSP <%= 5 %>"), "sample.jsp");
    }

    @ArquillianResource
    private URL url;

    @Test
    public void jaxrs() throws IOException {
        assertEquals("resource", IO.slurp(new URL(url.toExternalForm() + "the")));
    }

    @Test
    public void staticResource() throws IOException {
        assertEquals("static", IO.slurp(url));
    }

    @Test
    public void servlet() throws IOException {
        assertEquals("Servlet!", IO.slurp(new URL(url + "servlet")));
    }

    @Test
    public void jsp() throws IOException {
        assertEquals("JSP 5", IO.slurp(new URL(url + "sample.jsp")).trim());
    }

    @Test
    public void filterOrder() throws IOException {
        assertEquals("I'm the first", IO.slurp(new URL(url.toExternalForm() + "gotFilter")));
    }
}
