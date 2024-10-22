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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxrs.spring;

import org.apache.ziplock.WebModule;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.ResolutionException;
import org.jboss.shrinkwrap.resolver.api.ResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.loader.tools.Libraries;
import org.springframework.boot.loader.tools.Library;
import org.springframework.boot.loader.tools.LibraryCallback;
import org.springframework.boot.loader.tools.LibraryScope;
import org.springframework.boot.loader.tools.Repackager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class SpringWebappTest {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive getArchive() throws Exception {
        MavenResolvedArtifact[] dependencies;
        try { // try offline first since it is generally faster
            dependencies = Maven.configureResolver()
                    .workOffline()
                    .loadPomFromFile("src/test/resources/spring-webmvc-pom.xml")
                    .importCompileAndRuntimeDependencies().resolve().withTransitivity()
                    .asResolvedArtifact();
        } catch (ResolutionException re) { // try on central
            dependencies = Maven.resolver()
                    .loadPomFromFile("src/test/resources/spring-webmvc-pom.xml")
                    .importCompileAndRuntimeDependencies().resolve().withTransitivity()
                    .asResolvedArtifact();
        }


        final WebArchive archive = new WebModule(SpringWebappTest.class.getSimpleName()).getArchive();
        archive.addClasses(AlternativeGreeter.class, DemoApplication.class, GreetingController.class, ServletInitializer.class);
        archive.addAsLibraries(toFiles(dependencies));

        // repackage as a Spring Boot WAR file
        final File originalWarFile = File.createTempFile("test", ".war");
        archive.as(ZipExporter.class).exportTo(originalWarFile, true);

        final Repackager repackager = new Repackager(originalWarFile);
        final File repackagedWarFile = File.createTempFile("repackaged", ".war");

        repackager.repackage(repackagedWarFile, new MavenResolvedArtifactLibraries(dependencies));

        final WebArchive repackaged = ShrinkWrap.createFromZipFile(WebArchive.class, repackagedWarFile);

        System.out.println(repackaged.toString(true));
        return repackaged;
    }

    private static File[] toFiles(MavenResolvedArtifact[] dependencies) {
        return Arrays.stream(dependencies)
                .map(ResolvedArtifact::asFile)
                .collect(Collectors.toList())
                .toArray(new File[dependencies.length]);
    }

    @Test
    public void validate() throws Exception {
        final String launchProfile = System.getProperty("arquillian.launch");
        if ("tomee-embedded".equals(launchProfile)) {
            System.out.println("Skipping this test in TomEE embedded");
            return;
        }

        final InputStream is = new URL(url.toExternalForm() + "hello").openStream();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bytesRead;
        byte[] buffer = new byte[8192];
        while ((bytesRead = is.read(buffer)) > -1) {
            os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();

        final String output = new String(os.toByteArray(), "UTF-8");
        assertNotNull("Response shouldn't be null", output);
        assertTrue("Output should contain: " + "Hello World!" + "\n" + output, output.contains("Hello World!"));
    }

    public static class MavenResolvedArtifactLibraries implements Libraries {

        private final MavenResolvedArtifact[] artifacts;

        public MavenResolvedArtifactLibraries(final MavenResolvedArtifact[] artifacts) {
            this.artifacts = artifacts;
        }

        @Override
        public void doWithLibraries(final LibraryCallback callback) throws IOException {
            for (final MavenResolvedArtifact artifact : artifacts) {
                callback.library(new Library(artifact.asFile(), toScope(artifact)));
            }
        }

        private LibraryScope toScope(MavenResolvedArtifact artifact) {
            final ScopeType scope = artifact.getScope();
            if (ScopeType.COMPILE.equals(scope)) {
                return LibraryScope.COMPILE;
            } else if (ScopeType.RUNTIME.equals(scope)) {
                return LibraryScope.COMPILE;
            } else if (ScopeType.TEST.equals(scope)) {
                return LibraryScope.PROVIDED;
            } else if (ScopeType.PROVIDED.equals(scope)) {
                return LibraryScope.PROVIDED;
            } else if (ScopeType.SYSTEM.equals(scope)) {
                return LibraryScope.CUSTOM;
            } else if (ScopeType.IMPORT.equals(scope)) {
                return LibraryScope.CUSTOM;
            }

            throw new IllegalStateException("Unsupported scope: " + scope);
        }
    }
}
