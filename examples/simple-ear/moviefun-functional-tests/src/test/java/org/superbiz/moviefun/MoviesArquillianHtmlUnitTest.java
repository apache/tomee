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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.ResourceBundle;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
public class MoviesArquillianHtmlUnitTest {

    @Deployment
    public static EnterpriseArchive createDeployment() {
        ResourceBundle mavenVersion = ResourceBundle.getBundle("version");

        final EnterpriseArchive enterpriseArchive = Maven.resolver().resolve("org.superbiz:moviefun-ear:ear:" + mavenVersion.getString("version"))
                .withoutTransitivity().asSingle(EnterpriseArchive.class);

        System.out.println(enterpriseArchive.toString(true));

        return enterpriseArchive;
    }

    @ArquillianResource
    private URL deploymentUrl;

    @Test
    public void testShouldMakeSureWebappIsWorking() throws Exception {
        final String url = "http://" + deploymentUrl.getHost() + ":" + deploymentUrl.getPort() + "/moviefun";

        WebClient webClient = new WebClient();
        HtmlPage page = webClient.getPage(url + "/setup.jsp");

        assertMoviesPresent(page);

        page = webClient.getPage(url + "/moviefun");

        assertMoviesPresent(page);
        webClient.closeAllWindows();
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
