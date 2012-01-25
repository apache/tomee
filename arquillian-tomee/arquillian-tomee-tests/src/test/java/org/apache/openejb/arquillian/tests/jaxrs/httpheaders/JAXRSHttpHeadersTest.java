/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openejb.arquillian.tests.jaxrs.httpheaders;

import org.apache.openejb.arquillian.tests.jaxrs.JaxrsTest;
import org.apache.ziplock.WebModule;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Tests the {@link javax.ws.rs.core.HttpHeaders} methods.
 */
@RunWith(Arquillian.class)
public class JAXRSHttpHeadersTest extends JaxrsTest {

    @Deployment(testable = false)
    public static WebArchive archive() {
        return new WebModule(JAXRSHttpHeadersTest.class).getArchive();
    }

    /**
     * Tests {@link javax.ws.rs.core.HttpHeaders#getAcceptableLanguages()} that if given no
     * acceptable languages, that it will return the server default locale back.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testAcceptableLanguagesNoneGiven() throws IOException {
        final String response = get("/context/httpheaders/acceptablelanguages");
        assertEquals("acceptablelanguages:", response);
    }

    /**
     * Tests {@link javax.ws.rs.core.HttpHeaders#getAcceptableLanguages()} that if given a
     * language, it will be the only language in the list.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testAcceptableLanguagesOneGiven() throws IOException {
        final Map<String, String> headers = headers("Accept-Language", "de");
        final String actual = get(headers, "/context/httpheaders/acceptablelanguages");
        assertEquals("acceptablelanguages:de:", actual);
    }


    /**
     * Tests {@link javax.ws.rs.core.HttpHeaders#getAcceptableLanguages()} that if given multiple
     * languages, all will be returned in the list.                             
     *
     * @throws IOException
     */
    public void testAcceptableLanguagesManyGiven() throws IOException {
        final Map<String, String> headers = headers("Accept-Language", "de, en, zh");
        final String responseBody = get(headers, "/context/httpheaders/acceptablelanguages");
        assertEquals("acceptablelanguages:de:", responseBody);
        assertTrue(responseBody, responseBody.startsWith("acceptablelanguages:"));
        assertTrue(responseBody, responseBody.contains(":de:"));
        assertTrue(responseBody, responseBody.contains(":en:"));
        assertTrue(responseBody, responseBody.contains(":zh:"));
    }

    /**
     * Tests {@link javax.ws.rs.core.HttpHeaders#getAcceptableLanguages()} that if given multiple
     * languages, all will be returned in the list sorted by their quality
     * value.
     *
     * @throws IOException
     */
    public void testAcceptableLanguagesManyGivenQSort() throws IOException {
        final Map<String, String> headers = headers("Accept-Language", "de;q=0.6, en;q=0.8, zh;q=0.7");
        final String responseBody = get(headers, "/context/httpheaders/acceptablelanguages");
        assertEquals("acceptablelanguages:de:", responseBody);
    }

    /**
     * Tests {@link javax.ws.rs.core.HttpHeaders#getAcceptableMediaTypes()} that if given no
     * Accept header, wildcard/wildcard is returned.
     *
     * @throws IOException
     */
    public void testAcceptableMediaTypesNoneGiven() throws IOException {
        final String responseBody = get("/context/httpheaders/acceptablemediatypes");
        assertEquals("acceptablemediatypes:*/*:", responseBody);
    }

    /**
     * Tests {@link javax.ws.rs.core.HttpHeaders#getAcceptableMediaTypes()} that if given a
     * single Accept header value, it is returned.
     *
     * @throws IOException
     */
    public void testAcceptableMediaTypesOneGiven() throws IOException {
        final Map<String, String> headers = headers("Accept", "text/plain");
        final String responseBody = get(headers, "/context/httpheaders/acceptablemediatypes");

        assertEquals("acceptablemediatypes:text/plain:", responseBody);
        // TODO assert assertEquals("text/plain", getResponseHeader("Content-Type").getValue());
    }

    /**
     * Tests {@link javax.ws.rs.core.HttpHeaders#getAcceptableMediaTypes()} that if given
     * multiple Accept header values, the values are sorted by q-value.
     *
     * @throws IOException
     */
    public void testAcceptableMediaTypesManyGiven() throws IOException {
        final Map<String, String> headers = headers("Accept", "text/plain;q=1.0,*/*;q=0.6, application/json;q=0.7,text/xml;q=0.8");
        final String responseBody = get(headers, "/context/httpheaders/acceptablemediatypes");

        assertEquals("acceptablemediatypes:text/plain:text/xml:application/json:*/*:", responseBody);
        //TODO assertEquals("text/plain;q=1.0", getMethod.getResponseHeader("Content-Type").getValue());
    }

}
