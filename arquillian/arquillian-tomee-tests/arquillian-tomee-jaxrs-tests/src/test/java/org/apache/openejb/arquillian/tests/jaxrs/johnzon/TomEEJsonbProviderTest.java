/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.apache.openejb.arquillian.tests.jaxrs.johnzon;

import org.apache.johnzon.mapper.access.FieldAccessMode;
import org.apache.openejb.loader.IO;
import org.apache.openejb.server.cxf.rs.johnzon.TomEEJsonbProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class TomEEJsonbProviderTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "TomEEJsonbProviderTest.war")
            .addClass(TomEEJsonbProviderTest.class)
            .addAsWebInfResource(new StringAsset(
                "<ejb-jar>\n" +
                "  <enterprise-beans>\n" +
                "    <session>\n" +
                "      <ejb-name>Endpoint</ejb-name>\n" +
                "      <local-bean/>\n" +
                "      <ejb-class>" + Endpoint.class.getName() + "</ejb-class>\n" +
                "      <session-type>Singleton</session-type>\n" +
                "    </session>\n" +
                "  </enterprise-beans>\n" +
                "</ejb-jar>\n"), "ejb-jar.xml")
            .addAsWebInfResource(new StringAsset(
                "<resources>\n" +
                // Locale.GERMANY and new FieldAccessMode(true, true) as services to reference them
                "  <Service id=\"germany\" class-name=\"" + Locale.class.getName() + "\" constructor=\"language, country\">\n" +
                "    language = de\n" +
                "    country = DE\n" +
                "  </Service>\n" +
                "  <Service id=\"fieldAccess\" class-name=\"" + FieldAccessMode.class.getName() + "\" constructor=\"useConstructor, acceptHiddenConstructor\">\n" +
                "    useConstructor = true\n" +
                "    acceptHiddenConstructor = true\n" +
                "  </Service>\n" +
                "  <Service id=\"configuredTomEEJsonbProvider\" class-name=\"" + TomEEJsonbProvider.class.getName() + "\">\n" +
                "    dateFormat = MMM-yyyy\n" +
                "    locale = $germany\n" +
                "    accessMode = $fieldAccess\n" +
                "  </Service>\n" +
                "</resources>\n"), "resources.xml")
            .addAsWebInfResource(new StringAsset(
                "<openejb-jar>\n" +
                "  <pojo-deployment class-name=\"jaxrs-application\">\n" +
                "    <properties>\n" +
                "      cxf.jaxrs.providers = configuredTomEEJsonbProvider\n" +
                "    </properties>\n" +
                "  </pojo-deployment>\n" +
                "</openejb-jar>\n"), "openejb-jar.xml");
    }

    @Test
    public void run() throws IOException {
        assertEquals("{\"date\":\"" + localizedFormattedDate() + "\",\"fieldAccessOnly\":\"access-via-field\"}", IO.slurp(new URL(base.toExternalForm() + "test")));
    }

    private String localizedFormattedDate() { // same configuredTomEEJsonbProvider should have done
        return new SimpleDateFormat("MMM-yyyy", Locale.GERMANY).format(new Date());
    }

    @Path("test")
    public static class Endpoint {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Model get() {
            final Model model = new Model();
            model.setDate(new Date());
            return model;
        }
    }

    public static class Model {
        private Date date;
        private String fieldAccessOnly = "access-via-field";

        public Date getDate() {
            return date;
        }

        public void setDate(final Date date) {
            this.date = date;
        }

        public String getSomeField() {
            throw new UnsupportedOperationException("This Getter should not be invoked as we configured field-access.");
        }
    }
}
