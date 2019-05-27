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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.openejb.loader.IO;
import org.apache.tomee.arquillian.remote.RemoteTomEEConfiguration;
import org.apache.tomee.arquillian.remote.RemoteTomEEContainer;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertTrue;

@Ignore("Not implemented yet")
/*
 * This test deploys the ear in a manual fashion. Normally the remote adapter deploys using the deployer.
 * Here we'll use the arquillian adapter to control the lifecycle of the server, but we'll do the deploy
 * by hand into the webapps directory directly. The EAR deployment was fixed in TOMEE-2145.
 *
 */
public class DeployInWebAppsDirectoryTest {

    @Test
    public void test() throws Exception {
        final RemoteTomEEConfiguration configuration = new RemoteTomEEConfiguration();
        configuration.setGroupId("org.apache.tomee");
        configuration.setArtifactId("apache-tomee");
        configuration.setClassifier("plus");
        configuration.setVersion(System.getProperty("tomee.version"));
        configuration.setHttpPort(-1);

        final RemoteTomEEContainer container = new RemoteTomEEContainer();
        container.setup(configuration);

        try {
            container.start();

            final File webapps = new File(configuration.getDir(), "apache-tomee-" + configuration.getClassifier() + "-" + configuration.getVersion() + "/webapps");
            webapps.mkdirs();

            final File enterpriseArchive = Maven.resolver().resolve("org.superbiz:moviefun-ear:ear:1.1.0-SNAPSHOT")
                    .withoutTransitivity().asSingleFile();

            IO.copy(enterpriseArchive, new File(webapps, "moviefun-ear.ear"));
            final String appUrl = "http://localhost:" + configuration.getHttpPort() + "/moviefun";

            runTests(appUrl);

            container.stop();

            // restart the container, make sure all is still well
            container.start();

        } finally {
            container.stop();
        }
    }

    private void runTests(final String appUrl) throws IOException {
        final WebClient webClient = new WebClient();

        HtmlPage page = attempt(new Callable<HtmlPage>() {

            @Override
            public HtmlPage call() throws Exception {
                return getPage(webClient, appUrl);
            }
        }, 30);

        assertMoviesPresent(page);

        page = webClient.getPage(appUrl + "/moviefun");

        assertMoviesPresent(page);
        webClient.closeAllWindows();
    }

    private <T> T attempt(final Callable<T> callable, int numberOfAttempts) {
        int tries = 0;

        while (tries < numberOfAttempts) {
            try {
                return callable.call();
            } catch (final Exception e) {
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

    private HtmlPage getPage(final WebClient webClient, final String url) throws IOException {
        return webClient.getPage(url + "/setup.jsp");
    }

    private void assertMoviesPresent(HtmlPage page) {
        String pageAsText = page.asText();
        assertTrue(pageAsText.contains("Wedding Crashers"));
        assertTrue(pageAsText.contains("Starsky & Hutch"));
        assertTrue(pageAsText.contains("Shanghai Knights"));
        assertTrue(pageAsText.contains("I-Spy"));
        assertTrue(pageAsText.contains("The Royal Tenenbaums"));
    }
}
