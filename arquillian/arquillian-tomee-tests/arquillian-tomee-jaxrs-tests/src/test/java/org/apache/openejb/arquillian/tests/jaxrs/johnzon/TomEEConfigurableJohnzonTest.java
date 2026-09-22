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

import org.apache.johnzon.mapper.Converter;
import org.apache.openejb.loader.IO;
import org.apache.openejb.server.cxf.rs.johnzon.TomEEConfigurableJohnzon;
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
import java.util.Comparator;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class TomEEConfigurableJohnzonTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "TomEEConfigurableJohnzonTest.war")
            .addClass(TomEEConfigurableJohnzonTest.class)
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
                "  <Service id=\"testSorter\" class-name=\"" + Sorter.class.getName() + "\" />\n" +
                "  <Service id=\"customerConverter\" class-name=\"" + MyConverter.class.getName() + "\" />\n" +
                "  <Service id=\"johnzon\" class-name=\"" + TomEEConfigurableJohnzon.class.getName() + "\">\n" +
                "    datePattern = yyyy\n" +
                // "    converter = $customerConverter\n" + // or the collection syntax
                "    converters = collection:$customerConverter,$customerConverter\n" +
                "    attributeOrder = $testSorter\n" +
                "  </Service>\n" +
                "</resources>\n"), "resources.xml")
            .addAsWebInfResource(new StringAsset(
                "<openejb-jar>\n" +
                "  <pojo-deployment class-name=\"jaxrs-application\">\n" +
                "    <properties>\n" +
                "      cxf.jaxrs.providers = johnzon\n" +
                "    </properties>\n" +
                "  </pojo-deployment>\n" +
                "</openejb-jar>\n"), "openejb-jar.xml");
    }

    @Test
    public void run() throws IOException {
        assertEquals("{\"date\":\"" + year() + "\",\"horrible\":\"awesome\"}", IO.slurp(new URL(base.toExternalForm() + "test")));
    }

    private String year() { // same johnzon should have done
        return new SimpleDateFormat("yyyy").format(new Date());
    }

    @Path("test")
    public static class Endpoint {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Model get() {
            final Model model = new Model();
            model.setDate(new Date());
            model.setHorrible(new Horrible());
            return model;
        }
    }

    public static class Model {
        private Horrible horrible;
        private Date date;

        public Horrible getHorrible() {
            return horrible;
        }

        public void setHorrible(final Horrible horrible) {
            this.horrible = horrible;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(final Date date) {
            this.date = date;
        }
    }

    public static class Horrible {
    }


    public static class MyConverter implements Converter<Horrible> {
        @Override
        public String toString(final Horrible instance) {
            return "awesome";
        }

        @Override
        public Horrible fromString(final String text) {
            return new Horrible();
        }
    }

    public static class Sorter implements Comparator<String> {
        @Override
        public int compare(final String o1, final String o2) {
            return o1.compareTo(o2);
        }
    }
}
