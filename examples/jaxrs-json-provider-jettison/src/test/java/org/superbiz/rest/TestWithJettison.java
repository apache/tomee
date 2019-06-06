/*
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

package org.superbiz.rest;

import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.BookResource;
import org.superbiz.Item;
import org.superbiz.JAXRSApplication;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;


@RunWith(Arquillian.class)
public class TestWithJettison {
    private final static Logger LOGGER = Logger.getLogger(TestWithJettison.class.getName());

    @Deployment()
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                                                .addClass(Item.class)
                                                .addClass(BookResource.class)
                                                .addClass(JAXRSApplication.class)
                                                .addAsWebInfResource(new File("src/main/webapp/WEB-INF", "web.xml"))
                                                .addAsWebInfResource(
                                                        new File("src/main/webapp/WEB-INF", "openejb-jar.xml"))
                                                .addAsWebInfResource(
                                                        new File("src/main/webapp/WEB-INF", "resources.xml"))
                                                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml");
        return webArchive;
    }

    @ArquillianResource
    private URL base;


    @Test
    public void testBookResource() {

        final WebClient webClient = WebClient
                .create(base.toExternalForm());

        String responsePayload = webClient.reset().path("/api/catalog/books/").get(String.class);

        LOGGER.info(responsePayload);

        assertTrue(responsePayload.equals(
                "{\"book\":{\"@id\":\"134\",\"@availableSince\":\"2019-05-27 15:27:16.878\",\"@available\":\"false\",\"$\":\"TomEE Tutorial\"}}"));

    }

}
