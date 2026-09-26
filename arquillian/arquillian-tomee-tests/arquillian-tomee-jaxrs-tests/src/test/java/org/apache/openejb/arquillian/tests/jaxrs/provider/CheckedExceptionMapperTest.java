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
package org.apache.openejb.arquillian.tests.jaxrs.provider;

import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@Ignore("TOMEE-4707 EJB endpoints deployed by endpoint (ejb-deployment cxf.jaxrs.* config) answer 404: CxfRsHttpListener#isCXFResource never matches their empty InternalApplication")
@RunWith(Arquillian.class)
public class CheckedExceptionMapperTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "CheckedExceptionMapperTest.war")
            .addClass(CheckedExceptionMapperTest.class)
            .addAsWebInfResource(new StringAsset(
                "<ejb-jar>\n" +
                "  <enterprise-beans>\n" +
                "    <session>\n" +
                "      <ejb-name>ExampleRest</ejb-name>\n" +
                "      <ejb-class>" + ExampleRest.class.getName() + "</ejb-class>\n" +
                "      <session-type>Singleton</session-type>\n" +
                "    </session>\n" +
                "  </enterprise-beans>\n" +
                "</ejb-jar>\n"), "ejb-jar.xml")
            .addAsWebInfResource(new StringAsset(
                "<openejb-jar>\n" +
                "  <ejb-deployment ejb-name=\"ExampleRest\">\n" +
                "    <properties>\n" +
                "      cxf.jaxrs.providers = " + ExampleExceptionMapper.class.getName() + "\n" +
                "    </properties>\n" +
                "  </ejb-deployment>\n" +
                "</openejb-jar>\n"), "openejb-jar.xml");
    }

    @Test
    public void testThrowException() throws IOException {
        assertEquals("Exception!", IO.slurp(new URL(base.toExternalForm() + "example/throw/")));
    }

    public static class ExampleException extends Exception {
        public ExampleException(String message) {
            super(message);
        }
    }

    @Provider
    public static class ExampleExceptionMapper implements ExceptionMapper<ExampleException> {
        @Override
        public Response toResponse(ExampleException ex) {
            return Response.ok("Exception!").build();
        }
    }

    @Path("/example")
    public static class ExampleRest {
        @GET
        @Path(value = "/throw")
        public String throwException() throws ExampleException {
            throw new ExampleException("exception");
        }
    }
}
