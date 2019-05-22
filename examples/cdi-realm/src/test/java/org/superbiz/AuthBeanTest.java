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
package org.superbiz;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.openejb.arquillian.common.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class AuthBeanTest {
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "low-typed-realm.war")
                .addClasses(SecuredServlet.class, AuthBean.class)
                .addAsManifestResource(new FileAsset(new File("src/main/webapp/META-INF/context.xml")), "context.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @ArquillianResource
    private URL webapp;

    @Test
    public void success() throws IOException {
        assertEquals("200 Servlet!", get("userA", "test"));
    }

    @Test
    public void failure() throws IOException {
        assertThat(get("userA", "oops, wrong password"), startsWith("401"));
    }

    private String get(final String user, final String password) {
        final BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
        basicCredentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
        final CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(basicCredentialsProvider).build();

        final HttpHost httpHost = new HttpHost(webapp.getHost(), webapp.getPort(), webapp.getProtocol());
        final AuthCache authCache = new BasicAuthCache();
        final BasicScheme basicAuth = new BasicScheme();
        authCache.put(httpHost, basicAuth);
        final HttpClientContext context = HttpClientContext.create();
        context.setAuthCache(authCache);

        final HttpGet get = new HttpGet(webapp.toExternalForm() + "servlet");
        CloseableHttpResponse response = null;
        try {
            response = client.execute(httpHost, get, context);
            return response.getStatusLine().getStatusCode() + " " + EntityUtils.toString(response.getEntity());
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                IO.close(response);
            } catch (final IOException e) {
                // no-op
            }
        }
    }
}
