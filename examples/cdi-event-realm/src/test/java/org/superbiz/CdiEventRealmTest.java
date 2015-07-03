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
package org.superbiz;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class CdiEventRealmTest {

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "event-realm.war")
                .addClasses(AuthBean.class, HelloServlet.class, LoginServlet.class)
                .addAsManifestResource(new FileAsset(new File("src/main/webapp/META-INF/context.xml")), "context.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @ArquillianResource
    private URL webapp;

    @Test
    public void notAuthenticated() throws IOException {
        final CloseableHttpClient client = HttpClients.createDefault();

        final HttpGet httpGet = new HttpGet(webapp.toExternalForm() + "hello");
        final CloseableHttpResponse resp = client.execute(httpGet);
        try {
            // Without login, it fails with a 403, not authorized
            assertEquals(403, resp.getStatusLine().getStatusCode());

        } finally {
            resp.close();
        }
    }

    @Test
    public void badAuthentication() throws IOException {
        final CloseableHttpClient client = HttpClients.createDefault();

        // first authenticate with the login servlet
        final HttpPost httpPost = new HttpPost(webapp.toExternalForm() + "login");
        final List<NameValuePair> data = new ArrayList<NameValuePair>() {{
            add(new BasicNameValuePair("username", "userB"));
            add(new BasicNameValuePair("password", "bla bla"));
        }};
        httpPost.setEntity(new UrlEncodedFormEntity(data));
        final CloseableHttpResponse respLogin = client.execute(httpPost);
        try {
            assertEquals(401, respLogin.getStatusLine().getStatusCode());

        } finally {
            respLogin.close();
        }
    }

    @Test
    public void notAuthorized() throws IOException {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        final CloseableHttpClient client = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();

        // first authenticate with the login servlet
        final HttpPost httpPost = new HttpPost(webapp.toExternalForm() + "login");
        final List<NameValuePair> data = new ArrayList<NameValuePair>() {{
            add(new BasicNameValuePair("username", "userB"));
            add(new BasicNameValuePair("password", "secret"));
        }};
        httpPost.setEntity(new UrlEncodedFormEntity(data));
        final CloseableHttpResponse respLogin = client.execute(httpPost);
        try {
            assertEquals(200, respLogin.getStatusLine().getStatusCode());

        } finally {
            respLogin.close();
        }

        // then we can just call the hello servlet
        final HttpGet httpGet = new HttpGet(webapp.toExternalForm() + "hello");
        final CloseableHttpResponse resp = client.execute(httpGet);
        try {
            assertEquals(403, resp.getStatusLine().getStatusCode());

        } finally {
            resp.close();
        }
    }

    @Test
    public void success() throws IOException {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        final CloseableHttpClient client = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();

        // first authenticate with the login servlet
        final HttpPost httpPost = new HttpPost(webapp.toExternalForm() + "login");
        final List<NameValuePair> data = new ArrayList<NameValuePair>() {{
            add(new BasicNameValuePair("username", "userA"));
            add(new BasicNameValuePair("password", "secret"));
        }};
        httpPost.setEntity(new UrlEncodedFormEntity(data));
        final CloseableHttpResponse respLogin = client.execute(httpPost);
        try {
            assertEquals(200, respLogin.getStatusLine().getStatusCode());

        } finally {
            respLogin.close();
        }

        // then we can just call the hello servlet
        final HttpGet httpGet = new HttpGet(webapp.toExternalForm() + "hello");
        final CloseableHttpResponse resp = client.execute(httpGet);
        try {
            assertEquals(200, resp.getStatusLine().getStatusCode());
            System.out.println(EntityUtils.toString(resp.getEntity()));

        } finally {
            resp.close();
        }
    }


}
