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

package org.apache.openejb.arquillian.session;

import java.net.URL;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

@RunWith(Arquillian.class)
public class SessionScopeTest {
    @ArquillianResource
    private URL webappUrl;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "SessionScopeTest.war")
            .addClass(PojoSessionScoped.class).addClass(PojoSessionScopedServletWrapper.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
            .setWebXML(new StringAsset(Descriptors.create(WebAppDescriptor.class)
                .version("3.0")
                    .createServlet().servletName("servlet").servletClass(PojoSessionScopedServletWrapper.class.getName()).up()
                    .createServletMapping().servletName("servlet").urlPattern("/session").up()
                .exportAsString()));
    }

    @Test
    public void testShouldBeAbleToAccessServletAndEjb() throws Exception {
        final String sessionUrl = webappUrl.toExternalForm() + "session";

        String[] sessionResult = new String[2];
        for (int i = 0; i < sessionResult.length; i++) {
            // a client of its own, so that each iteration starts with an empty cookie store
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                String[] contents = new String[2];
                for (int j = 0; j < contents.length; j++) {
                    try (CloseableHttpResponse response = client.execute(new HttpGet(sessionUrl))) {
                        final int out = response.getStatusLine().getStatusCode();
                        if (out != 200) {
                            throw new RuntimeException("get " + sessionUrl + " returned " + out);
                        }
                        contents[j] = EntityUtils.toString(response.getEntity());
                    }
                }

                assertEquals(contents[0], contents[1]);
                sessionResult[i] = contents[0];
            }
        }

        assertNotSame(sessionResult[0], sessionResult[1]);
    }
}
