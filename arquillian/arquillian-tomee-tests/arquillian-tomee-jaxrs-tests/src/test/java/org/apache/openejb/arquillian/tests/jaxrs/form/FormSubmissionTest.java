/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxrs.form;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import java.net.URL;

//@Ignore
@RunWith(Arquillian.class)
public class FormSubmissionTest {

    @ArquillianResource
    private URL endpoint;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class);
        webArchive.addClasses(FormSubmission.class, SampleFilter.class);
        return webArchive;
    }

    @Test
    public void testPost() throws Exception {
        final String value = "hello+world";

        final Client client = ClientBuilder.newClient();
        final WebTarget target = client.target(endpoint.toURI()).path("api/form");

        final Form form = new Form().param("value", value);
        final String response = target.request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED), String.class);

        Assert.assertEquals(value, response);
    }
}
