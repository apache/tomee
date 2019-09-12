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


package org.superbiz.service;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.openejb.arquillian.common.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.dao.PersonDAO;
import org.superbiz.domain.Person;
import org.superbiz.init.Initializer;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertTrue;


@RunWith(Arquillian.class)
public class JerseyApplicationTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "jersey-application.war")
                .addPackage(JerseyApplication.class.getPackage())
                .addPackage(Person.class.getPackage())
                .addPackage(PersonDAO.class.getPackage())
                .addAsManifestResource(new FileAsset(new File("src/main/webapp/WEB-INF/web.xml")), "web.xml")
                .addAsManifestResource(new ClassLoaderAsset("META-INF/persistence.xml"), "persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @ArquillianResource
    private URL webapp;

    @Test
    public void test() {
        get("person/create/TestPerson");

        String allPersons = get("person/all");

        assertTrue(allPersons.contains("<name>TestPerson</name>"));
    }

    private String get(String url) {
        final CloseableHttpClient client = HttpClients.custom().build();
        final HttpHost httpHost = new HttpHost(webapp.getHost(), webapp.getPort(), webapp.getProtocol());
        final HttpClientContext context = HttpClientContext.create();

        final HttpGet get = new HttpGet(webapp.toExternalForm() + url);
        CloseableHttpResponse response = null;
        try {
            response = client.execute(httpHost, get, context);
            return EntityUtils.toString(response.getEntity());
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
