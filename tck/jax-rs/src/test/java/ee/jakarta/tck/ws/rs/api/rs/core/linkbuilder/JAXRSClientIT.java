/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.ws.rs.api.rs.core.linkbuilder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Link.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriBuilderException;
import jakarta.ws.rs.ext.RuntimeDelegate;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

    private static final long serialVersionUID = -4301370250466608838L;

    @BeforeEach
    void logStartTest(TestInfo testInfo) {
        TestUtil.logMsg("STARTING TEST : " + testInfo.getDisplayName());
    }

    @AfterEach
    void logFinishTest(TestInfo testInfo) {
        TestUtil.logMsg("FINISHED TEST : " + testInfo.getDisplayName());
    }

    /* Run test */

    /*
     * @testName: buildNoArgTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:804;
     * 
     * @test_Strategy: Finish building this link and return the instance
     */
    @Test
    public void buildNoArgTest() throws Fault {
        Link link = builderFromResource("get").build();
        assertTrue(link != null, "#build should return an instance");
        assertTrue(link.getUri().toASCIIString().length() > 0, "Link is empty");
        logMsg("#build() finished building a link and returned the instance");
    }

    /*
     * @testName: buildNoArgsThrowsUriBuilderExceptionTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:804;
     * 
     * @test_Strategy: throws UriBuilderException if a URI cannot be constructed
     * based on the current state of the underlying URI builder.
     */
    @Test
    public void buildNoArgsThrowsUriBuilderExceptionTest() throws Fault {
        Link.Builder builder = Link.fromUri("http://:@");
        try {
            Link link = builder.build();
            assertTrue(false, "No exception has been thrown for link " + link);
        } catch (UriBuilderException e) {
            logMsg("#build() throw UriBuilderException as expected");
        }
    }

    /*
     * @testName: buildObjectsTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:804;
     * 
     * @test_Strategy: Finish building this link using the supplied values as URI
     * parameters.
     */
    @Test
    public void buildObjectsTest() throws Fault {
        StringBuilder path1 = new StringBuilder().append("p1");
        ByteArrayInputStream path2 = new ByteArrayInputStream("p2".getBytes(Charset.defaultCharset())) {
            @Override
            public String toString() {
                return "p2";
            }
        };
        URI path3;
        try {
            path3 = new URI("p3");
        } catch (URISyntaxException e) {
            throw new Fault(e);
        }
        String expected = "<" + url() + "p1/p2/p3" + ">";
        Link.Builder builder = Link.fromUri(url() + "{x1}/{x2}/{x3}");
        Link link = builder.build(path1, path2, path3);
        assertTrue(link != null, "#build should return an instance");
        assertTrue(link.toString().equals(expected), "Link " + link + " differs from expected " + expected);
        logMsg("#build() finished building a link and returned the instance", link);
    }

    /*
     * @testName: buildThrowsIAEWhenSuppliedJustOneValueOutOfThreeTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:804;
     * 
     * @test_Strategy: Throws: java.lang.IllegalArgumentException - if there are any
     * URI template parameters without a supplied value
     */
    @Test
    public void buildThrowsIAEWhenSuppliedJustOneValueOutOfThreeTest() throws Fault {
        Builder linkBuilder = Link.fromUri(url() + "{x1}/{x2}/{x3}"); // rfc6570
        try {
            Link link = linkBuilder.build("p");
            fault("IllegalArgumentException has not been thrown when value is not supplied, link=", link.toString());
        } catch (IllegalArgumentException iae) {
            logMsg("IllegalArgumentException has been thrown as expected when a value has not been supplied");
        }
    }

    /*
     * @testName: buildObjectsThrowsUriBuilderExceptionTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:804;
     * 
     * @test_Strategy: throws UriBuilderException if a URI cannot be constructed
     * based on the current state of the underlying URI builder.
     */
    @Test
    public void buildObjectsThrowsUriBuilderExceptionTest() throws Fault {
        Link.Builder builder = Link.fromUri("http://:@");
        try {
            Link link = builder.build("aaa");
            assertTrue(false, "No exception has been thrown for link " + link);
        } catch (UriBuilderException e) {
            logMsg("#build(someNonURIObjects) throw UriBuilderException as expected");
        }
    }

    /*
     * @testName: paramTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:807; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Set an arbitrary parameter on this link.
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void paramTest() throws Fault {
        String[] params = { "param1", "param2" };
        String[] values = { "param1value1", "param1value2" };
        Link.Builder builder = RuntimeDelegate.getInstance().createLinkBuilder().uri(url());
        builder = builder.param(params[0], values[0]).param(params[1], values[1]);
        Link link = builder.build();
        assertParams(link, params, values);

        logMsg("#param set correct parameters");
    }

    /*
     * @testName: paramThrowsExceptionWhenNullNameTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:807; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: throws IlleagalArgumentException - if either the name or
     * value are null
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void paramThrowsExceptionWhenNullNameTest() throws Fault {
        Link.Builder builder = RuntimeDelegate.getInstance().createLinkBuilder().uri(url());
        try {
            builder.param((String) null, "value");
            throw new Fault("No exception has been thrown when null name");
        } catch (IllegalArgumentException e) {
            logMsg("#param throws IllegalArgumentException as expected");
        }
    }

    /*
     * @testName: paramThrowsExceptionWhenNullValueTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:807; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: throws IlleagalArgumentException - if either the name or
     * value are null
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder *
     */
    @Test
    public void paramThrowsExceptionWhenNullValueTest() throws Fault {
        Link.Builder builder = RuntimeDelegate.getInstance().createLinkBuilder().uri(url());
        try {
            builder.param((String) null, "value");
            throw new Fault("No exception has been thrown when null name");
        } catch (IllegalArgumentException e) {
            logMsg("#param throws IllegalArgumentException as expected");
        }
    }

    /*
     * @testName: relTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:809; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Convenience method to set a link relation. More than one rel
     * value can be specified using this method.
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void relTest() throws Fault {
        String[] names = { "name1", "name2" };
        Link.Builder builder = RuntimeDelegate.getInstance().createLinkBuilder().uri(url());
        for (String name : names) {
            Link link = builder.rel(name).build();
            assertTrue(link.getRel().contains(name), "Rel " + name + " not found in " + link);
        }
        logMsg("#rel added expected relations");
    }

    /*
     * @testName: relMoreNamesTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:809; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: More than one "rel" value can be specified by using one or
     * more whitespace characters as delimiters according to RFC 5988. The effect of
     * calling this method is cumulative; relations are appended using a single
     * space character as separator.
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void relMoreNamesTest() throws Fault {
        String[] names = { "name1", "name2" };
        Link.Builder builder = RuntimeDelegate.getInstance().createLinkBuilder().uri(url());
        for (String name : names)
            builder = builder.rel(name);
        Link link = builder.build();
        String search = JaxrsUtil.iterableToString(" ", (Object[]) names);
        assertTrue(link.getRel().contains(search), "rel " + search + " not found in " + link);
        logMsg("#rel added expected relations");
    }

    /*
     * @testName: titleTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:810; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Convenience method to set a title on this link. If called
     * more than once, the previous value of title is overwritten.
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void titleTest() throws Fault {
        String[] titles = { "tiTle1", "titlE2", "titLe3" };
        Link.Builder builder = RuntimeDelegate.getInstance().createLinkBuilder().uri(url());
        for (String title : titles) {
            builder = builder.title(title);
            Link link = builder.build();
            assertTrue(link.getTitle().equals(title), "Title " + title + " not found in " + link);
        }
        logMsg("#title set expected title");
    }

    /*
     * @testName: typeTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:811; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Convenience method to set a type on this link. If called more
     * than once, the previous value of title is overwritten.
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void typeTest() throws Fault {
        String[] types = { "type1", "type2", "type3" };
        Link.Builder builder = RuntimeDelegate.getInstance().createLinkBuilder().uri(url());
        for (String type : types) {
            Link link = builder.type(type).build();
            assertTrue(link.getType().equals(type), "type " + type + " not found in " + link);
        }
        logMsg("#type set correct types");
    }

    /*
     * @testName: uriUriTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:812; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Set underlying URI template for the link being constructed.
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void uriUriTest() throws Fault {
        URI uri = uri("get");
        Link.Builder builder = RuntimeDelegate.getInstance().createLinkBuilder().uri(uri);
        Link link = builder.build();
        assertTrue(link.toString().contains(uri.toASCIIString()), "uri(URI) " + uri + " not used in " + link);
        logMsg("#uri(URI) affected link", link, "as expected");
    }

    /*
     * @testName: uriStringTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:813; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Set underlying string representing URI template for the link
     * being constructed.
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void uriStringTest() throws Fault {
        Link.Builder builder = RuntimeDelegate.getInstance().createLinkBuilder().uri(url());
        Link link = builder.build();
        assertTrue(link.toString().contains(url()), "uri(String) " + url() + " not used in " + link);
        logMsg("#uri(String) affected link", link, "as expected");
    }

    /*
     * @testName: uriStringThrowsIAETest
     * 
     * @assertion_ids: JAXRS:JAVADOC:813; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: throws IllegalArgumentException - if string representation of
     * URI is invalid
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void uriStringThrowsIAETest() throws Fault {
        try {
            RuntimeDelegate.getInstance().createLinkBuilder().uri((String) null).build();
            fault("IllegalArgumentException has not been thrown for uri(null)");
        } catch (IllegalArgumentException e) {
            logMsg("#uri(nonUriString) throws IllegalArgumentException as expected");
        }
    }

    /*
     * @testName: uriBuilderTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1006; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Set underlying URI builder representing the URI template for
     * the link being constructed.
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void uriBuilderTest() throws Fault {
        String segment = "goto/label/ten/";
        Link link = Link.fromUri(uri(segment)).build();
        UriBuilder uriBuilder = link.getUriBuilder();

        Builder linkBuilder = RuntimeDelegate.getInstance().createLinkBuilder().uriBuilder(uriBuilder);
        String sBuilder = uriBuilder.build().toASCIIString();
        String sFromBuilder = linkBuilder.build().getUri().toASCIIString();
        assertContains(sFromBuilder, sBuilder, "Original builder", sBuilder, "not found in #fromUriBuilder",
                sFromBuilder);
        logMsg("#fromUriBuilder", sFromBuilder, "contains the original", sBuilder);
    }

    /*
     * @testName: baseUriURITest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1125; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Set the base URI for resolution of relative URIs.
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void baseUriURITest() throws Fault {
        URI uri = null;
        try {
            uri = new URI(url());
        } catch (URISyntaxException use) {
            fault(use);
        }
        Builder linkBuilder = RuntimeDelegate.getInstance().createLinkBuilder();
        linkBuilder = linkBuilder.baseUri(uri);
        URI createdUri = linkBuilder.uri("/a/b/c").build().getUri();
        logMsg("Created URI", createdUri.toASCIIString());
        assertContains(createdUri.toASCIIString(), uri.toASCIIString());
    }

    /*
     * @testName: baseUriIsNotJustBaseURITest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1125; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Set the base URI for resolution of relative URIs. Provide a
     * URI that is not just base, i.e. schema and authority, but also a path
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void baseUriIsNotJustBaseURITest() throws Fault {
        URI uri = uri("something");
        Builder linkBuilder = RuntimeDelegate.getInstance().createLinkBuilder();
        linkBuilder = linkBuilder.baseUri(uri);
        URI createdUri = linkBuilder.uri("/a/b/c").build().getUri();
        logMsg("Created URI", createdUri.toASCIIString());
        assertFalse(createdUri.toASCIIString().contains(uri.toASCIIString()), "Base Uri " + uri.toASCIIString() +
                " is not a base uri built as " + createdUri.toASCIIString());
        assertContains(createdUri.toASCIIString(), url());
    }

    /*
     * @testName: baseUriIsIgnoredURITest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1125; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: If the underlying URI is already absolute, the base URI is
     * ignored.
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void baseUriIsIgnoredURITest() throws Fault {
        String ignored = "http://ignored.com";
        URI ignoredUri = null;
        try {
            ignoredUri = new URI(ignored);
        } catch (URISyntaxException e) {
            fault(ignored);
        }
        URI uri = uri("something");
        Builder linkBuilder = RuntimeDelegate.getInstance().createLinkBuilder();
        linkBuilder = linkBuilder.uri(uri);
        linkBuilder = linkBuilder.baseUri(ignoredUri);
        URI createdUri = linkBuilder.build().getUri();
        logMsg("Created URI", createdUri.toASCIIString());
        assertFalse(createdUri.toASCIIString().contains(ignored), "Base Uri " + ignored + " is not ignored, though " +
                uri.toASCIIString() + " is absolute");
        assertContains(createdUri.toASCIIString(), url());
        logMsg("The base uri", ignored, "has been ignored as expected");
    }

    /*
     * @testName: baseUriStringTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1126; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Set the base URI as a string for resolution of relative URIs.
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void baseUriStringTest() throws Fault {
        URI uri = null;
        try {
            uri = new URI(url());
        } catch (URISyntaxException use) {
            fault(use);
        }
        Builder linkBuilder = RuntimeDelegate.getInstance().createLinkBuilder();
        linkBuilder = linkBuilder.baseUri(uri.toASCIIString());
        URI createdUri = linkBuilder.uri("/a/b/c").build().getUri();
        logMsg("Created URI", createdUri.toASCIIString());
        assertContains(createdUri.toASCIIString(), uri.toASCIIString());
    }

    /*
     * @testName: baseUriStringThrowsIAETest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1126; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: throws java.lang.IllegalArgumentException - if string
     * representation of URI is invalid.
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void baseUriStringThrowsIAETest() throws Fault {
        Builder linkBuilder = RuntimeDelegate.getInstance().createLinkBuilder();
        try {
            linkBuilder.baseUri("?:!@#$%^&*()");
            fault("IllegalArgumentException has not been thrown");
        } catch (IllegalArgumentException iae) {
            logMsg("IllegalArgumentException has been thrown as expected");
        }
    }

    /*
     * @testName: baseUriIsNotJustBaseStringTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1126; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Set the base URI as a string for resolution of relative URIs.
     * Provide a URI that is not just base, i.e. schema and authority, but also a
     * path
     * 
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void baseUriIsNotJustBaseStringTest() throws Fault {
        URI uri = uri("something");
        Builder linkBuilder = RuntimeDelegate.getInstance().createLinkBuilder();
        linkBuilder = linkBuilder.baseUri(uri.toASCIIString());
        URI createdUri = linkBuilder.uri("/a/b/c").build().getUri();
        logMsg("Created URI", createdUri.toASCIIString());
        assertFalse(createdUri.toASCIIString().contains(uri.toASCIIString()), "Base Uri " + uri.toASCIIString() +
                " is not a base uri built as " + createdUri.toASCIIString());
        assertContains(createdUri.toASCIIString(), url());
    }

    /*
     * @testName: baseUriIsIgnoredStringTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1126; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: If the underlying URI is already absolute, the base URI is
     * ignored. jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void baseUriIsIgnoredStringTest() throws Fault {
        String ignored = "http://ignored.com";
        URI uri = uri("something");
        Builder linkBuilder = RuntimeDelegate.getInstance().createLinkBuilder();
        linkBuilder = linkBuilder.uri(uri);
        linkBuilder = linkBuilder.baseUri(ignored);
        URI createdUri = linkBuilder.build().getUri();
        logMsg("Created URI", createdUri.toASCIIString());
        assertFalse(createdUri.toASCIIString().contains(ignored), "Base Uri " + ignored + " is not ignored, though " +
                uri.toASCIIString() + "is absolute");
        assertContains(createdUri.toASCIIString(), url());
        logMsg("The base uri", ignored, "has been ignored as expected");
    }

    /*
     * @testName: buildRelativizedTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1054; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Finish building this link using the supplied values as URI
     * parameters and relativize the result with respect to the supplied URI.
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void buildRelativizedTest() throws Fault {
        String relative = "a/b/c";
        URI underlay = uri(relative), respect = uri("");
        Builder linkBuilder = RuntimeDelegate.getInstance().createLinkBuilder();
        linkBuilder = linkBuilder.uri(underlay);
        Link link = linkBuilder.buildRelativized(respect);
        assertFalse(link.toString().contains(url()), "Found unexpected absolute path " + url());
        assertContains(link.toString(), relative);
        logMsg("Absolute", url(), "has not been found as expected in link", link.toString());
    }

    /*
     * @testName: buildRelativizedDoesNotSharePrefixTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1054;
     * 
     * @test_Strategy: If the underlying link is absolute but does not share a
     * prefix with the supplied URI, this method is equivalent to calling
     * build(java.lang.Object[])
     */
    @Test
    public void buildRelativizedDoesNotSharePrefixTest() throws Fault {
        String relative = "a/b/c";
        String prefix = "ssh";
        URI underlay = null, respect = null;
        try {
            underlay = new URI(url() + relative);
            respect = new URI(url().replace("http", prefix));
        } catch (URISyntaxException e) {
            fault(e);
        }
        Builder linkBuilder = RuntimeDelegate.getInstance().createLinkBuilder();
        linkBuilder = linkBuilder.uri(underlay);
        Link link = linkBuilder.buildRelativized(respect);
        Link build = linkBuilder.build(respect);
        assertContains(link.toString(), relative);
        assertContains(link.toString(), url());
        assertContains(link.toString(), build.getUri().toASCIIString()); // |=|
        logMsg("When a prefix is not shared, the methods is equivalent to build() as expected");
    }

    /*
     * @testName: buildRelativizedThrowsIAEWhenNotSuppliedValuesTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1054;
     * 
     * @test_Strategy: Throws: java.lang.IllegalArgumentException - if there are any
     * URI template parameters without a supplied value
     */
    @Test
    public void buildRelativizedThrowsIAEWhenNotSuppliedValuesTest() throws Fault {
        Builder linkBuilder = Link.fromUri(url() + "{x1}/{x2}/{x3}"); // rfc6570
        URI respect = null;
        try {
            respect = new URI(url());
        } catch (URISyntaxException e) {
            fault(e);
        }
        try {
            Link link = linkBuilder.buildRelativized(respect);
            fault("IllegalArgumentException has not been thrown when value is not supplied, link=", link.toString());
        } catch (IllegalArgumentException iae) {
            logMsg("IllegalArgumentException has been thrown as expected when a value has not been supplied");
        }
    }

    /*
     * @testName: buildRelativizedThrowsIAEWhenSuppliedJustOneValueOutOfThreeTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1054;
     * 
     * @test_Strategy: Throws: java.lang.IllegalArgumentException - if there are any
     * URI template parameters without a supplied value
     */
    @Test
    public void buildRelativizedThrowsIAEWhenSuppliedJustOneValueOutOfThreeTest() throws Fault {
        Builder linkBuilder = Link.fromUri(url() + "{x1}/{x2}/{x3}"); // rfc6570
        URI respect = null;
        try {
            respect = new URI(url());
        } catch (URISyntaxException e) {
            fault(e);
        }
        try {
            Link link = linkBuilder.buildRelativized(respect, "p");
            fault("IllegalArgumentException has not been thrown when value is not supplied, link=", link.toString());
        } catch (IllegalArgumentException iae) {
            logMsg("IllegalArgumentException has been thrown as expected when a value has not been supplied");
        }
    }

    /*
     * @testName: buildRelativizedThrowsIAEWhenSuppliedValueIsNullTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1054;
     * 
     * @test_Strategy: Throws: java.lang.IllegalArgumentException - if a value is
     * null.
     */
    @Test
    public void buildRelativizedThrowsIAEWhenSuppliedValueIsNullTest() throws Fault {
        Builder linkBuilder = Link.fromUri(url() + "{x1}/{x2}/{x3}"); // rfc6570
        URI respect = null;
        try {
            respect = new URI(url());
        } catch (URISyntaxException e) {
            fault(e);
        }
        try {
            Link link = linkBuilder.buildRelativized(respect, new Object[] { (String) null });
            fault("IllegalArgumentException has not been thrown when value is not supplied, link=", link.toString());
        } catch (IllegalArgumentException iae) {
            logMsg("IllegalArgumentException has been thrown as expected when a supplied value is null");
        }
    }

    /*
     * @testName: buildRelativizedThrowsUriBuilderExceptionTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1054; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Throws: UriBuilderException - if a URI cannot be constructed
     * based on the current state of the underlying URI builder.
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void buildRelativizedThrowsUriBuilderExceptionTest() throws Fault {
        Builder linkBuilder = RuntimeDelegate.getInstance().createLinkBuilder();
        URI respect = null;
        try {
            respect = new URI(url());
        } catch (URISyntaxException e) {
            fault(e);
        }
        try {
            Link link = linkBuilder.uri("http://@").buildRelativized(respect);
            fault("UriBuilderException has not been thrown, link=", link.toString());
        } catch (UriBuilderException iae) {
            logMsg("UriBuilderException has been thrown as expected");
        }
    }

    /*
     * @testName: linkLinkTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1042; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Initialize builder using another link. Sets underlying URI
     * and copies all parameters.
     * jakarta.ws.rs.ext.RuntimeDelegate.createLinkBuilder
     */
    @Test
    public void linkLinkTest() throws Fault {
        String title = "Ttttlll";
        String rel = "RlrL";
        String[] params = { "Param1", "parAM2", "paRam3" };
        String[] values = { "vAlUe", "ValuEe", "VVallue" };
        Builder lb = RuntimeDelegate.getInstance().createLinkBuilder();
        lb = lb.baseUri(url()).title(title).rel(rel).type(MediaType.TEXT_XML);
        for (int i = 0; i != params.length; i++)
            lb = lb.param(params[i], values[i]);
        Link link = lb.build();
        Builder lb2 = RuntimeDelegate.getInstance().createLinkBuilder();
        lb2 = lb2.link(link);
        link = lb2.build();
        // rel & title is param of Link
        assertContains(link.getRel(), rel, "link(Link) does not pass relation");
        assertContains(link.getTitle(), title, "link(Link) does noot pass title");
        assertContains(link.getType(), MediaType.TEXT_XML, "link(Link) does not pass type");
        assertParams(link, params, values);
        assertContains(link.toString(), url());
        logMsg("parameters and underlaying URI were copied as expected to a new link", link);
    }

    /*
     * @testName: linkStringTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1043; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Initialize builder using another link represented as a
     * string. Uses simple parser to convert string representation into a link.
     */
    @Test
    public void linkStringTest() throws Fault {
        String title = "Ttttlll";
        String rel = "RlrL";
        String[] params = { "Param1", "parAM2", "paRam3" };
        String[] values = { "vAlUe", "ValuEe", "VVallue" };
        StringBuilder sb = new StringBuilder().append("<").append(url()).append(">;").append("rel=\"").append(rel)
                .append("\";");
        for (int i = 0; i != params.length; i++)
            sb = sb.append(params[i]).append("=\"").append(values[i]).append("\";");
        sb = sb.append("title=\"").append(title).append("\";");
        sb = sb.append("type=\"").append(MediaType.TEXT_XML).append("\"");
        String originalLink = sb.toString();
        Builder lb = RuntimeDelegate.getInstance().createLinkBuilder();
        lb = lb.link(originalLink);
        Link link = lb.build();
        // rel & title is param of Link
        assertContains(link.getRel(), rel, "link(Link) does not pass relation");
        assertContains(link.getTitle(), title, "link(Link) does noot pass title");
        assertContains(link.getType(), MediaType.TEXT_XML, "link(Link) does not pass type");
        assertParams(link, params, values);
        assertContains(link.toString(), url());
        logMsg("parameters and underlaying URI were copied as expected to a new link", link);
    }

    /*
     * @testName: linkStringThrowsIAETest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1043; JAXRS:JAVADOC:1053;
     * 
     * @test_Strategy: Throws: java.lang.IllegalArgumentException - if string
     * representation of URI is invalid.
     */
    @Test
    public void linkStringThrowsIAETest() throws Fault {
        Builder lb = RuntimeDelegate.getInstance().createLinkBuilder();
        try {
            lb.link("<>>");
            fault("IllegalArgumentException has not been throw when invalid uri");
        } catch (IllegalArgumentException e) {
            logMsg("IllegalArgumentException has been thrown as expected");
        }
    }

    // ///////////////////////////////////////////////////////////////////
    protected static Link.Builder builderFromResource(String method) {
        Builder builder = Link.fromMethod(Resource.class, method);
        return builder;
    }

    private static//
    void assertParams(Link link, String[] params, String[] values) throws Fault {
        Map<String, String> map = link.getParams();
        for (int i = 0; i != params.length; i++) {
            String list = map.get(params[i]);
            assertContains(list, values[i], "Link.getParams", map, "does not contain", values[i]);
            logMsg("Found", values[i], "in map", map, "as expected");
        }
    }

    protected void assertContains(String string, String substring) throws Fault {
        assertTrue(string.toLowerCase().contains(substring.toLowerCase()), string + " does not contain expected " +
                substring);
        logMsg("Found expected", substring);
    }

    protected URI uri(String method) throws Fault {
        URI uri = null;
        try {
            uri = new URI(url() + "resource/" + method);
        } catch (URISyntaxException e) {
            throw new Fault(e);
        }
        return uri;
    }

    protected static String url() {
        return "http://oracle.com:888/";
    }

}
