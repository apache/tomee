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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.moviefun;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.io.FileUtils;
import org.apache.tomee.embedded.EmbeddedTomEEContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class MoviesHtmlUnitTest {

    private static EJBContainer container;
    private static File webApp;

    @BeforeClass
    public static void start() throws IOException {
        webApp = createWebApp();
        Properties p = new Properties();
        p.setProperty(EJBContainer.APP_NAME, "moviefun");
        p.setProperty(EJBContainer.PROVIDER, "tomee-embedded"); // need web feature
        p.setProperty(EJBContainer.MODULES, webApp.getAbsolutePath());
        p.setProperty(EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT, "9999");
        container = EJBContainer.createEJBContainer(p);
    }

    @AfterClass
    public static void stop() {
        if (container != null) {
            container.close();
        }
        if (webApp != null) {
            try {
                FileUtils.forceDelete(webApp);
            } catch (IOException e) {
                FileUtils.deleteQuietly(webApp);
            }
        }
    }

    private static File createWebApp() throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir") + "/tomee-" + Math.random());
        if (!file.mkdirs() && !file.exists()) {
            throw new RuntimeException("can't create " + file.getAbsolutePath());
        }

        FileUtils.copyDirectory(new File("target/classes"), new File(file, "WEB-INF/classes"));
        FileUtils.copyDirectory(new File("target/test-libs"), new File(file, "WEB-INF/lib"));
        FileUtils.copyDirectory(new File("src/main/webapp"), file);

        return file;
    }

    @Test
    @Ignore("This test fails due to a change in OWB-4 to pass the TCK related to dotted EL names: " +
        "https://github.com/apache/openwebbeans/commit/4e4962a69064585d146c71bb387a8395455e88b5" +
        "Until this is fixed, we disable this test. Related stacktrace is added below.")
    /*
     jakarta.el.PropertyNotFoundException: The class 'org.apache.webbeans.el22.WrappedValueExpressionNode' does not have the property 'title'.
	    at jakarta.el.BeanELResolver.getBeanProperty(BeanELResolver.java:626)
	    at jakarta.el.BeanELResolver.getValue(BeanELResolver.java:338)
	    at org.apache.jasper.el.JasperELResolver.getValue(JasperELResolver.java:129)
	    at org.apache.el.parser.AstValue.getValue(AstValue.java:169)
    	at org.apache.el.ValueExpressionImpl.getValue(ValueExpressionImpl.java:190)
    	at org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate(PageContextImpl.java:701)
    	at org.apache.jsp.WEB_002dINF.setup_jsp._jspx_meth_c_005fout_005f0(setup_jsp.java:247)
    	at org.apache.jsp.WEB_002dINF.setup_jsp._jspx_meth_c_005fforEach_005f0(setup_jsp.java:198)
    	at org.apache.jsp.WEB_002dINF.setup_jsp._jspService(setup_jsp.java:150)
     */
    public void testShouldMakeSureWebappIsWorking() throws Exception {
        WebClient webClient = new WebClient();
        HtmlPage page = webClient.getPage("http://localhost:9999/moviefun/setup");

        assertMoviesPresent(page);

        try {
            page = webClient.getPage("http://localhost:9999/moviefun/faces/movie/List.xhtml");
        } catch (Exception e) {
            e.printStackTrace(); // just to get it in the console in debug phase
            throw e;
        }

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
        assertTrue(pageAsText.contains("Zoolander"));
        assertTrue(pageAsText.contains("Shanghai Noon"));
    }
}
