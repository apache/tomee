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


package org.apache.tomee.itests.jaxrs.applogging;

import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.junit.Ignore;
import org.junit.Test;
import org.tomitribe.util.Join;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApplicationLoggingTest {

    @Test
    public void discovered() throws Exception {

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.webprofile()
                .add("webapps/test/WEB-INF/lib/app.jar", Archive.archive()
                        .add(DiscoveredResources.class)
                        .add(SquareResource.class)
                        .add(TriangleResource.class)
                        .add(AnnotatedWriter.class)
                        .asJar())
                .watch("org.apache.openejb.server.cxf.rs.CxfRsHttpListener.logApplication ", "\n", output::add)
                .build();

        Collections.sort(output);

        final String join = Join.join("\n", output);
        assertEquals("Application{path='http://localhost:0/test/blue', class=org.apache.tomee.itests.jaxrs.applogging.DiscoveredResources, resources=2, providers=1, invalids=0}\n" +
                "Provider{clazz=org.apache.tomee.itests.jaxrs.applogging.AnnotatedWriter, discovered=true, singleton=false}\n" +
                "Resource{clazz=org.apache.tomee.itests.jaxrs.applogging.SquareResource, discovered=true, singleton=false}\n" +
                "Resource{clazz=org.apache.tomee.itests.jaxrs.applogging.TriangleResource, discovered=true, singleton=false}", normalize(join));
    }

    @Test
    public void getClasses() throws Exception {

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.webprofile()
                .add("webapps/test/WEB-INF/lib/app.jar", Archive.archive()
                        .add(GetClasses.class)
                        .add(SquareResource.class)
                        .add(TriangleResource.class)
                        .add(AnnotatedWriter.class)
                        .asJar())
                .watch("org.apache.openejb.server.cxf.rs.CxfRsHttpListener.logApplication ", "\n", output::add)
                .build();

        Collections.sort(output);

        final String join = Join.join("\n", output);
        assertEquals("Application{path='http://localhost:0/test/red', class=org.apache.tomee.itests.jaxrs.applogging.GetClasses, resources=2, providers=1, invalids=0}\n" +
                "Provider{clazz=org.apache.tomee.itests.jaxrs.applogging.AnnotatedWriter, discovered=false, singleton=false}\n" +
                "Resource{clazz=org.apache.tomee.itests.jaxrs.applogging.SquareResource, discovered=false, singleton=false}\n" +
                "Resource{clazz=org.apache.tomee.itests.jaxrs.applogging.TriangleResource, discovered=false, singleton=false}", normalize(join));
    }

    @Test
    public void getSingletons() throws Exception {

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.webprofile()
                .add("webapps/test/WEB-INF/lib/app.jar", Archive.archive()
                        .add(GetSingletons.class)
                        .add(SquareResource.class)
                        .add(TriangleResource.class)
                        .add(AnnotatedWriter.class)
                        .asJar())
                .watch("org.apache.openejb.server.cxf.rs.CxfRsHttpListener.logApplication ", "\n", output::add)
                .build();

        Collections.sort(output);

        final String join = Join.join("\n", output);
        assertEquals("Application{path='http://localhost:0/test/red', class=org.apache.tomee.itests.jaxrs.applogging.GetSingletons, resources=2, providers=1, invalids=0}\n" +
                "Provider{clazz=org.apache.tomee.itests.jaxrs.applogging.AnnotatedWriter, discovered=false, singleton=true}\n" +
                "Resource{clazz=org.apache.tomee.itests.jaxrs.applogging.SquareResource, discovered=false, singleton=true}\n" +
                "Resource{clazz=org.apache.tomee.itests.jaxrs.applogging.TriangleResource, discovered=false, singleton=true}", normalize(join));
    }


    /**
     * We appear to be breaking the second sentence of this requirement of the JAX-RS specification:
     *
     * ----
     * When an Application subclass is present in the archive, if both Application.getClasses
     * and Application.getSingletons return an empty collection then all root resource classes and
     * providers packaged in the web application MUST be included and the JAX-RS implementation is
     * REQUIRED to discover them automatically by scanning a .war file as described above. If either
     * getClasses or getSingletons returns a non-empty collection then only those classes or singletons
     * returned MUST be included in the published JAX-RS application.
     * ----
     *
     * Despite there being a getClasses() method, we still scan for @Provider implementations in the classpath
     * add them to the application.
     */
    @Ignore()
    @Test
    public void getClassesNoProviders() throws Exception {

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.webprofile()
                .add("webapps/test/WEB-INF/lib/app.jar", Archive.archive()
                        .add(GetClassesNoProviders.class)
                        .add(SquareResource.class)
                        .add(TriangleResource.class)
                        .add(CircleResource.class)
                        .add(AnnotatedWriter.class)
                        .add(NotAnnotatedWriter.class)
                        .asJar())
                .watch("org.apache.openejb.server.cxf.rs.CxfRsHttpListener.logApplication ", "\n", output::add)
                .build();

        Collections.sort(output);
        final String join = Join.join("\n", output);
        assertEquals("Application{path='http://localhost:0/test/red', class=org.apache.tomee.itests.jaxrs.applogging.GetClassesNoProviders, resources=2, providers=1, invalids=0}\n" +
                "Resource{clazz=org.apache.tomee.itests.jaxrs.applogging.SquareResource, discovered=false, singleton=false}\n" +
                "Resource{clazz=org.apache.tomee.itests.jaxrs.applogging.TriangleResource, discovered=false, singleton=false}", normalize(join));
    }

    @Test
    public void getClassesNonAnnotatedProvider() throws Exception {

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.webprofile()
                .add("webapps/test/WEB-INF/lib/app.jar", Archive.archive()
                        .add(GetClassesNonAnnotatedProvider.class)
                        .add(SquareResource.class)
                        .add(TriangleResource.class)
                        .add(CircleResource.class)
                        .add(NotAnnotatedWriter.class)
                        .asJar())
                .watch("org.apache.openejb.server.cxf.rs.CxfRsHttpListener.logApplication ", "\n", output::add)
                .build();

        Collections.sort(output);
        final String join = Join.join("\n", output);
        assertEquals("Application{path='http://localhost:0/test/red', class=org.apache.tomee.itests.jaxrs.applogging.GetClassesNonAnnotatedProvider, resources=2, providers=1, invalids=0}\n" +
                "Provider{clazz=org.apache.tomee.itests.jaxrs.applogging.NotAnnotatedWriter, discovered=false, singleton=false}\n" +
                "Resource{clazz=org.apache.tomee.itests.jaxrs.applogging.SquareResource, discovered=false, singleton=false}\n" +
                "Resource{clazz=org.apache.tomee.itests.jaxrs.applogging.TriangleResource, discovered=false, singleton=false}", normalize(join));
    }

    private String normalize(final String join) {
        return join.replaceAll("localhost:[0-9]+", "localhost:0");
    }

    public void assertPresent(final ArrayList<String> output, final String s) {
        final Optional<String> actual = output.stream()
                .filter(line -> line.contains(s))
                .findFirst();

        assertTrue(actual.isPresent());
    }

    public void assertNotPresent(final ArrayList<String> output, final String s) {
        final Optional<String> actual = output.stream()
                .filter(line -> line.contains(s))
                .findFirst();

        assertTrue(!actual.isPresent());
    }

}