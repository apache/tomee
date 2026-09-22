/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxrs.provider;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(Arquillian.class)
public class IgnoreMandatoryProviderTest {
    private static final String DEACTIVATION = "org.apache.cxf.jaxrs.validation.ValidationExceptionMapper.activated";

    // mandatory providers are only deactivated by container properties (CxfRSService#isActive), so the property is
    // set around this class' deployment only; it needs the container in this JVM
    @ClassRule
    public static final ExternalResource DEACTIVATED_MAPPER = new ExternalResource() {
        @Override
        protected void before() {
            assumeTrue("needs the container in the test JVM",
                    System.getProperty("openejb.arquillian.adapter", "embedded").contains("embedded"));
            SystemInstance.get().setProperty(DEACTIVATION, "false");
        }

        @Override
        protected void after() {
            SystemInstance.get().getProperties().remove(DEACTIVATION);
        }
    };

    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "IgnoreMandatoryProviderTest.war")
            .addClass(IgnoreMandatoryProviderTest.class);
    }

    @Test
    public void noHandler() throws IOException {
        final Response response = WebClient.create(base.toExternalForm()).path("ignore-mandatory").get();
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.getStatus()); // 400 with the handler
        assertTrue(IO.slurp(InputStream.class.cast(response.getEntity())).contains("Internal Server Error</h1>")); // Tomcat error report
    }

    @Path("ignore-mandatory")
    public static class Thrower {
        @GET
        public String throwIt() {
            throw new ConstraintViolationException(Collections.<ConstraintViolation<?>>emptySet());
        }
    }
}
