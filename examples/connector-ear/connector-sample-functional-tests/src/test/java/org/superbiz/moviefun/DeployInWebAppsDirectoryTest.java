/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.moviefun;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.loader.IO;
import org.apache.tomee.arquillian.remote.RemoteTomEEConfiguration;
import org.apache.tomee.arquillian.remote.RemoteTomEEContainer;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.application6.ApplicationDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Test;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;

/*
 * This test deploys the ear in a manual fashion. Normally the remote adapter deploys using the deployer.
 * Here we'll use the arquillian adapter to control the lifecycle of the server, but we'll do the deploy
 * by hand into the webapps directory directly. The EAR deployment was fixed in TOMEE-2145.
 *
 */
public class DeployInWebAppsDirectoryTest {

    public static EnterpriseArchive createDeployment() {

        final JavaArchive apiJar = ShrinkWrap.create(JavaArchive.class, "connector-sample-api.jar");
        apiJar.addPackage("org.superbiz.connector.api");
        System.out.println("API JAR:\n" + apiJar.toString(true));

        final JavaArchive implJar = ShrinkWrap.create(JavaArchive.class, "connector-sample-impl.jar");
        implJar.addPackage("org.superbiz.connector.adapter");
        System.out.println("IMPL JAR:\n" + implJar.toString(true));

        final ResourceAdapterArchive rar = ShrinkWrap.create(ResourceAdapterArchive.class,"connector-sample-ra.rar");
        rar.addAsLibraries(implJar);

        final File raXml = Basedir.basedir("../connector-sample-rar/src/main/rar/META-INF/ra.xml");
        rar.setResourceAdapterXML(raXml);
        System.out.println("RAR:\n" + rar.toString(true));

        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "connector-sample-war.war");
        webArchive.addPackage("org.superbiz.application");

        final WebAppDescriptor webAppDescriptor = Descriptors.create(WebAppDescriptor.class);
        webAppDescriptor.version("3.0");

        final File resourcesXml = Basedir.basedir("../connector-sample-war/src/main/webapp/WEB-INF/resources.xml");
        webArchive.addAsWebInfResource(resourcesXml);
        webArchive.setWebXML(new StringAsset(webAppDescriptor.exportAsString()));
        webArchive.addAsWebInfResource(resourcesXml);
        webArchive.addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml");
        System.out.println("Webapp:\n" + webArchive.toString(true));

        final EnterpriseArchive enterpriseArchive = ShrinkWrap.create(EnterpriseArchive.class, "connector-sample.ear");
        enterpriseArchive.addAsLibraries(apiJar);
        enterpriseArchive.addAsModule(rar);
        enterpriseArchive.addAsModule(webArchive);

        ApplicationDescriptor applicationXml = Descriptors.create(ApplicationDescriptor.class);
        applicationXml.displayName("connector-sample-ear");
        applicationXml.createModule()
                .getOrCreateWeb()
                .webUri("connector-sample-war.war")
                .contextRoot("/sample")
                .up().up()
                .createModule().connector("connector-sample-ra.rar")
                .up().libraryDirectory("lib");

        enterpriseArchive.setApplicationXML(new StringAsset(applicationXml.exportAsString()));
        System.out.println(enterpriseArchive.toString(true));

        return enterpriseArchive;
    }

    @Test
    public void test() throws Exception {
        final RemoteTomEEConfiguration configuration = new RemoteTomEEConfiguration();
        configuration.setGroupId("org.apache.tomee");
        configuration.setArtifactId("apache-tomee");
        configuration.setClassifier("plus");
        configuration.setVersion(System.getProperty("tomee.version"));
//        configuration.setDebug(true);
        configuration.setHttpPort(-1);

        final RemoteTomEEContainer container = new RemoteTomEEContainer();
        container.setup(configuration);

        try {
            container.start();

            final File webapps = new File(configuration.getDir(), "apache-tomee-" + configuration.getClassifier() + "-" + configuration.getVersion() + "/webapps");
            webapps.mkdirs();

            final InputStream is = createDeployment().as(ZipExporter.class).exportAsInputStream();

            IO.copy(is, new File(webapps, "connector-sample.ear"));
            final String appUrl = "http://localhost:" + configuration.getHttpPort() + "/sample/";

            attempt(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    runTests(appUrl);
                    return null;
                }
            }, 30);

            container.stop();

            // restart the container, make sure all is still well
            container.start();

        } finally {
            container.stop();
        }
    }

    public void runTests(final String appUrl) throws Exception {
        final WebClient webClient = WebClient.create(appUrl);
        final Response response = webClient.path("").type(MediaType.TEXT_PLAIN_TYPE).post("Hello, world");

        assertEquals(204, response.getStatus());
        final String result = webClient.path("").accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);

        assertEquals("Hello, world", result);
    }

    private <T> T attempt(final Callable<T> callable, int numberOfAttempts) {
        int tries = 0;

        while (tries < numberOfAttempts) {
            try {
                return callable.call();
            } catch (final Throwable e) {
                // ignore the exception and try again
                tries++;
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException ie) {
                    // ignore
                }
            }
        }

        throw new IllegalStateException("Unable to invoke callable successfully after " + numberOfAttempts + " attempts");
    }
}
