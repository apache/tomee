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
package org.superbiz.security;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.tomee.bootstrap.Archive;
import org.apache.tomee.bootstrap.Server;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;

public class MovieTest {

    private static URI serverURI;

    @BeforeClass
    public static void setup() {
        // Add any classes you need to an Archive
        // or add them to a jar via any means
        final Archive classes = Archive.archive()
                                       .add(MovieServlet.class)
                                       .add(TestIdentityStore.class);

        // Place the classes where you would want
        // them in a Tomcat install
        final Server server = Server.builder()
                                    // This effectively creates a webapp called ROOT
                                    .add("webapps/ROOT/WEB-INF/classes", classes)
                                    .add("webapps/ROOT/WEB-INF/beans.xml", "")
                                    .build();

        serverURI = server.getURI();
    }

    @Test
    public void getWithoutAuthentication() throws Exception {
        System.out.println("\n\nCalling MovieServlet without any credentials provided.");
        try (final WebClient webClient = new WebClient()) {
            webClient.getPage(serverURI.toString() + "/movies");

        } catch (final FailingHttpStatusCodeException e) {
            Assert.assertEquals(401, e.getStatusCode());
        }
    }

    @Test
    public void getWrongUser() throws Exception {
        System.out.println("\n\nCalling MovieServlet with the wrong credentials.");
        try (final WebClient webClient = new WebClient()) {
            //set proxy username and password
            final DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) webClient.getCredentialsProvider();
            credentialsProvider.addCredentials("username", "password");

            webClient.getPage(serverURI.toString() + "/movies");

        } catch (final FailingHttpStatusCodeException e) {
            Assert.assertEquals(401, e.getStatusCode());
        }
    }

    @Test
    public void getWrongPermission() throws Exception {
        System.out.println("\n\nCalling MovieServlet with a valid user but without required permissions.");
        try (final WebClient webClient = new WebClient()) {

            //set proxy username and password
            final DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) webClient.getCredentialsProvider();
            credentialsProvider.addCredentials("iron", "man");

            webClient.getPage(serverURI.toString() + "/movies");

        } catch (final FailingHttpStatusCodeException e) {
            Assert.assertEquals(403, e.getStatusCode());
        }
    }

    @Test
    public void getRightPermissions() throws Exception {
        System.out.println("\n\nCalling MovieServlet with a valid user and valid permissions.");
        try (final WebClient webClient = new WebClient()) {
            //set proxy username and password
            final DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) webClient.getCredentialsProvider();
            credentialsProvider.addCredentials("jon", "doe");

            // should not throw any exception now
            final HtmlPage htmlPage = webClient.getPage(serverURI.toString() + "/movies");
            Assert.assertTrue(htmlPage.asNormalizedText().contains("web username: jon"));
        }
    }

}
