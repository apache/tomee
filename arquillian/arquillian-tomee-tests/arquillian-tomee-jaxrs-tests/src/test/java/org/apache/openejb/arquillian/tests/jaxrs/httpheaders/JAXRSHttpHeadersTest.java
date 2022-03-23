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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.openejb.arquillian.tests.jaxrs.JaxrsTest;
import org.apache.ziplock.WebModule;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link jakarta.ws.rs.core.HttpHeaders} methods.
 */
@RunWith(Arquillian.class)
public class JAXRSHttpHeadersTest extends JaxrsTest {

    @Deployment(testable = false)
    public static WebArchive archive() {
        return new WebModule(JAXRSHttpHeadersTest.class, JAXRSHttpHeadersTest.class).getArchive();
    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getAcceptableLanguages()} that if given no
     * acceptable languages, that it will return the server default locale back.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testAcceptableLanguagesNoneGiven() throws IOException {
        final String response = get("context/httpheaders/acceptablelanguages");
        assertEquals("acceptablelanguages:*:", response);
    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getAcceptableLanguages()} that if given a
     * language, it will be the only language in the list.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testAcceptableLanguagesOneGiven() throws IOException {
        final Map<String, String> headers = headers("Accept-Language", "de");
        final String actual = get(headers, "context/httpheaders/acceptablelanguages");
        assertEquals("acceptablelanguages:de:", actual);
    }


    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getAcceptableLanguages()} that if given multiple
     * languages, all will be returned in the list.                             
     *
     * @throws java.io.IOException
     */
    @Test
    public void testAcceptableLanguagesManyGiven() throws IOException {
        final Map<String, String> headers = headers("Accept-Language", "de, en, zh");
        final String responseBody = get(headers, "/context/httpheaders/acceptablelanguages");
        assertTrue(responseBody, responseBody.startsWith("acceptablelanguages:"));
        assertTrue(responseBody, responseBody.contains(":de:"));
        assertTrue(responseBody, responseBody.contains(":en:"));
        assertTrue(responseBody, responseBody.contains(":zh:"));
    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getAcceptableLanguages()} that if given multiple
     * languages, all will be returned in the list sorted by their quality
     * value.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testAcceptableLanguagesManyGivenQSort() throws IOException {
        final Map<String, String> headers = headers("Accept-Language", "de;q=0.6, en;q=0.8, zh;q=0.7");
        final String responseBody = get(headers, "/context/httpheaders/acceptablelanguages");
        assertEquals("acceptablelanguages:en:zh:de:", responseBody);
    }


    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getAcceptableMediaTypes()} that if given a
     * single Accept header value, it is returned.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testAcceptableMediaTypesOneGiven() throws IOException {
        final Map<String, String> headers = headers("Accept", "text/plain");
        final String responseBody = get(headers, "/context/httpheaders/acceptablemediatypes");

        assertEquals("acceptablemediatypes:text/plain:", responseBody);
        // TODO assert assertEquals("text/plain", getResponseHeader("Content-Type").getValue());
    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getAcceptableMediaTypes()} that if given
     * multiple Accept header values, the values are sorted by q-value.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testAcceptableMediaTypesManyGiven() throws IOException {
        final Map<String, String> headers = headers("Accept", "text/plain;q=1.0,*/*;q=0.6, application/json;q=0.7,text/xml;q=0.8");
        final String responseBody = get(headers, "/context/httpheaders/acceptablemediatypes");

        assertEquals("acceptablemediatypes:text/plain:text/xml:application/json:*/*:", responseBody);
        //TODO assertEquals("text/plain;q=1.0", getMethod.getResponseHeader("Content-Type").getValue());
    }


    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getMediaType()} that if given a text/plain, the
     * method will return text/plain.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testMediaTypesRequestTextPlain() throws IOException {
        HttpPost post = new HttpPost(uri("context/httpheaders/requestmediatype"));
        post.setHeader("Content-Type", "text/plain");
        post.setEntity(new StringEntity("Hello world!", "UTF-8"));

        final HttpResponse response = client.execute(post);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        String responseBody = asString(response);
        assertEquals("mediatype:text/plain:", responseBody);

    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getMediaType()} when a non-standard content type
     * is sent in.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testMediaTypesRequestCustomContentType() throws IOException {
        HttpPost post = new HttpPost(uri("context/httpheaders/requestmediatype"));
        post.setHeader("Content-Type", "defg/abcd");
        post.setEntity(new StringEntity("Hello world!", "UTF-8"));

        final HttpResponse response = client.execute(post);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        String responseBody = asString(response);
        assertEquals("mediatype:defg/abcd:", responseBody);

    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getMediaType()} when no request entity is given.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testMediaTypesRequestNoRequestEntity() throws IOException {
        HttpPost post = new HttpPost(uri("context/httpheaders/requestmediatype"));

        final HttpResponse response = client.execute(post);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        String responseBody = asString(response);
        assertEquals("mediatype:null:", responseBody);

    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getLanguage()} when no language is given in the
     * request.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testLanguageNoneGiven() throws IOException {
        HttpPost post = new HttpPost(uri("context/httpheaders/language"));
        post.setHeader("Content-Type", "text/plain");
        post.setEntity(new StringEntity("Hello world!", "UTF-8"));

        final HttpResponse response = client.execute(post);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        String responseBody = asString(response);
        assertEquals("language:null:", responseBody);

    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getLanguage()} when English language is given in
     * the request.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testLanguageEnglishGiven() throws IOException {
        HttpPost post = new HttpPost(uri("context/httpheaders/language"));
        post.setHeader("Content-Type", "text/plain");
        post.setEntity(new StringEntity("Hello world!", "UTF-8"));
        post.addHeader("Content-Language", "en");

        final HttpResponse response = client.execute(post);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        String responseBody = asString(response);
        assertEquals("language:en:", responseBody);

    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getLanguage()} when Chinese language is given in
     * the request.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testLanguageChineseGiven() throws IOException {
        HttpPost post = new HttpPost(uri("context/httpheaders/language"));
        post.setHeader("Content-Type", "text/plain");
        post.setEntity(new StringEntity("Hello world!", "UTF-8"));
        post.addHeader("Content-Language", "zh");

        final HttpResponse response = client.execute(post);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        String responseBody = asString(response);
        assertEquals("language:zh:", responseBody);

    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getCookies()} when no cookies are given.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testCookiesNone() throws IOException {
        HttpPost HttpPost = new HttpPost(uri("context/httpheaders/cookies"));

        final HttpResponse response = client.execute(HttpPost);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        String responseBody = asString(response);
        assertEquals("cookies:", responseBody);

    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getCookies()} when given a single cookie.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testCookiesOneGiven() throws IOException {
        final HttpPost HttpPost = new HttpPost(uri("context/httpheaders/cookies"));
        HttpPost.addHeader("Cookie", "foo=bar");
        final HttpResponse response = client.execute(HttpPost);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        String responseBody = asString(response);
        assertEquals("cookies:foo=bar:", responseBody);
    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getCookies()} when given multiple cookies.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testCookiesManyGiven() throws IOException {
        final HttpPost post = new HttpPost(uri("context/httpheaders/cookies"));
        post.addHeader("Cookie", "foo=bar");
        post.addHeader("Cookie", "foo2=bar2");

        final HttpResponse response = client.execute(post);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        String responseBody = asString(response);
        assertEquals("cookies:foo=bar:foo2=bar2:", responseBody);
    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getRequestHeader(String)} when given a null
     * value.
     *
     * @throws java.io.IOException
     */
    public void testRequestHeaderNoneGivenIllegalArgument() throws IOException {
        HttpGet get = new HttpGet(uri("context/httpheaders/"));
        final HttpResponse response = client.execute(get);
        assertStatusCode(200, response);
        String responseBody = asString(response);
        assertEquals("requestheader:null:", responseBody);
    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getRequestHeader(String)} when requesting header
     * values for a non-existent header name.
     *
     * @throws java.io.IOException
     */
    public void testRequestHeaderNonexistentHeader() throws IOException {
        HttpGet get = new HttpGet(uri("context/httpheaders/?name=foo"));
        final HttpResponse response = client.execute(get);
        assertStatusCode(200, response);
        String responseBody = asString(response);
        assertEquals("requestheader:null:", responseBody);
    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getRequestHeader(String)} when requesting header
     * value for a single header name.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testRequestHeaderSingleValue() throws IOException {
        HttpGet getMethod = new HttpGet(uri("context/httpheaders/?name=foo"));
        getMethod.addHeader("foo", "bar");
        final HttpResponse response = client.execute(getMethod);
        assertStatusCode(200, response);
        String responseBody = asString(response);
        assertEquals("requestheader:[bar]", responseBody);
    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getRequestHeader(String)} when requesting
     * multiple header value for a single header name.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testRequestHeaderMultipleValue() throws IOException {
        HttpGet getMethod = new HttpGet(uri("context/httpheaders/?name=foo"));
        getMethod.addHeader("foo", "bar");
        getMethod.addHeader("foo", "bar2");
        final HttpResponse response = client.execute(getMethod);
        assertStatusCode(200, response);
        String responseBody = asString(response);
        assertEquals("requestheader:[bar, bar2]", responseBody);
    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getRequestHeader(String)} when requesting
     * multiple header value for a single header name when using
     * case-insensitive names.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testRequestHeaderCaseInsensitive() throws IOException {
        HttpGet getMethod = new HttpGet(uri("context/httpheaders/?name=foo"));
        getMethod.addHeader("FOO", "bar");
        getMethod.addHeader("FoO", "bar2");
        final HttpResponse response = client.execute(getMethod);
        assertStatusCode(200, response);
        String responseBody = asString(response);
        assertEquals("requestheader:[bar, bar2]", responseBody);
    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getRequestHeaders()} when making a basic
     * HttpClient request.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testRequestHeadersBasicHeader() throws IOException {
        HttpGet getMethod = new HttpGet(uri("context/httpheaders/requestheaders"));
        final HttpResponse response = client.execute(getMethod);
        assertStatusCode(200, response);
        String responseBody = asString(response);
        assertTrue(responseBody, responseBody.contains("requestheaders:"));
        assertTrue(responseBody, responseBody.contains(":host=") || responseBody
                .contains(":Host="));
        assertTrue(responseBody, responseBody.contains(":user-agent=") || responseBody
                .contains(":User-Agent="));
    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getRequestHeaders()} when having a custom
     * header.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testRequestHeadersSingleValue() throws IOException {
        HttpGet getMethod = new HttpGet(uri("context/httpheaders/requestheaders"));
        getMethod.addHeader("fOo", "bAr");
        final HttpResponse response = client.execute(getMethod);
        assertStatusCode(200, response);
        String responseBody = asString(response);
        assertTrue(responseBody, responseBody.contains("requestheaders:"));
        assertTrue(responseBody, responseBody.contains(":fOo=[bAr]") || responseBody.contains(":foo=[bAr]"));
    }

    /**
     * Tests {@link jakarta.ws.rs.core.HttpHeaders#getRequestHeaders()} when having multiple values
     * and multiple custom headers.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testRequestHeadersMultipleValues() throws IOException {
        HttpGet getMethod = new HttpGet(uri("context/httpheaders/requestheaders"));
        getMethod.addHeader("fOo", "bAr");
        getMethod.addHeader("abc", "xyz");
        getMethod.addHeader("fOo", "2bAr");
        final HttpResponse response = client.execute(getMethod);
        assertStatusCode(200, response);
        String responseBody = asString(response);
        assertTrue(responseBody, responseBody.contains("requestheaders:"));
        assertTrue(responseBody, responseBody.contains(":fOo=[2bAr, bAr]") || responseBody.contains(":foo=[2bAr, bAr]"));
        assertTrue(responseBody, responseBody.contains(":abc=[xyz]"));
    }
}
