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
package org.apache.openejb.arquillian.tests.jaxrs.johnzon;

import org.apache.johnzon.mapper.JohnzonIgnore;
import org.apache.johnzon.mapper.JohnzonProperty;
import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class JsonbJaxrsProviderTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "JsonbJaxrsProviderTest.war")
            .addClass(JsonbJaxrsProviderTest.class)
            .addAsWebInfResource(new StringAsset(
                "<ejb-jar>\n" +
                "  <enterprise-beans>\n" +
                "    <session>\n" +
                "      <ejb-name>Endpoint</ejb-name>\n" +
                "      <ejb-class>" + Endpoint.class.getName() + "</ejb-class>\n" +
                "      <session-type>Singleton</session-type>\n" +
                "    </session>\n" +
                "  </enterprise-beans>\n" +
                "</ejb-jar>\n"), "ejb-jar.xml");
    }

    @Test
    public void run() throws IOException {
        assertEquals("{\"johnzon\":\"johnzon\",\"jsonb\":\"jsonb\",\"value\":\"value\"}",
                     IO.slurp(new URL(base.toExternalForm() + "test")));
    }

    @Path("test")
    public static class Endpoint {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Model get() {
            final Model model = new Model();
            model.setValue("value");
            model.setJohnzonIgnore("ignoreJohnzon");
            model.setJsonbIgnore("ignoreJsonb");
            model.setJohnzonProperty("johnzon");
            model.setJsonbProperty("jsonb");
            return model;
        }
    }

    public static class Model {
        private String value;
        @JohnzonIgnore
        private String johnzonIgnore;
        @JsonbTransient
        private String jsonbIgnore;
        @JohnzonProperty("johnzon")
        private String johnzonProperty;
        @JsonbProperty("jsonb")
        private String jsonbProperty;

        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }

        public String getJohnzonIgnore() {
            return johnzonIgnore;
        }

        public void setJohnzonIgnore(final String johnzonIgnore) {
            this.johnzonIgnore = johnzonIgnore;
        }

        public String getJsonbIgnore() {
            return jsonbIgnore;
        }

        public void setJsonbIgnore(final String jsonbIgnore) {
            this.jsonbIgnore = jsonbIgnore;
        }

        public String getJohnzonProperty() {
            return johnzonProperty;
        }

        public void setJohnzonProperty(final String johnzonProperty) {
            this.johnzonProperty = johnzonProperty;
        }

        public String getJsonbProperty() {
            return jsonbProperty;
        }

        public void setJsonbProperty(final String jsonbProperty) {
            this.jsonbProperty = jsonbProperty;
        }
    }
}
