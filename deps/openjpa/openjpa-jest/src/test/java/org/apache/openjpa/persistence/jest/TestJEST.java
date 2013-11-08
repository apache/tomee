/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.openjpa.persistence.jest;

import java.io.InputStream;
import java.net.HttpURLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

/**
 * Tests JEST Servlet using <A href="http://httpunit.sourceforge.net/doc/servletunit-intro.html">ServletUnit</A>.
 * 
 * Sets up a class-level Servlet Runner (an in-process Servlet Engine).
 * 
 * Recognizes following JVM system property
 * <OL>
 * <LI><tt>jest.web.xml</tt> : web descriptor resource name looked up as a resource in the current
 * thread context. Defaults to <tt>WEB-INF/web.xml</tt>
 * <LI><tt>jest.base.uri</tt> : base uri for all request. Defaults to <tt>http://localhost/jest</tt>
 *  
 * 
 * @author Pinaki Poddar
 *
 */
public class TestJEST extends TestCase {
    private static ServletRunner container;
    private static String baseURI;
    private static String DEFAULT_WEB_XML  = "WEB-INF/web.xml";
    private static String DEFAULT_BASE_URI = "http://localhost/jest";
    private static DocumentBuilder _xmlParser;
    private static XPathFactory _xpathFactory;
    
    /**
     * Sets up a class-wide Servlet Engine.
     */
    protected void setUp() throws Exception {
        super.setUp();
        if (container == null) {
            String resource = System.getProperty("jest.web.xml", DEFAULT_WEB_XML);
            System.err.println("Starting Servlet Container from " + resource);
            InputStream wdesc = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            assertNotNull(resource + " not loadable at thread context classpath", wdesc);
            container = new ServletRunner(wdesc);
            assertNotNull("Servlet engine could not be started", container);
            
            baseURI = System.getProperty("jest.base.uri", DEFAULT_BASE_URI);
            System.err.println("Base URI  " + baseURI);
            
            _xmlParser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            _xpathFactory = XPathFactory.newInstance();
        }
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
        HttpUnitOptions.setScriptingEnabled(false);
        HttpUnitOptions.setExceptionsThrownOnScriptError(false);
    }
    
    public void testBadURL() throws Exception {
        assertError(HttpURLConnection.HTTP_NOT_FOUND, uri("some+bad+url"));
    }
    
    public void testDomain() throws Exception {
        WebResponse response = getResponse(uri("domain"));
        assertNotNull(response);
        System.err.println(response.getText());
        assertEquals("text/xml", response.getContentType());
        Document doc = _xmlParser.parse(response.getInputStream());
        assertNotNull(doc);
        Node metamodel = getNode(doc, "/metamodel");
        assertNotNull(metamodel);
        NodeList entities = getNodes(doc, "/metamodel/entity");
        assertEquals(2, ((NodeList)entities).getLength());
    }
    
    /**
     * Gets the response for the given URL.
     */
    WebResponse getResponse(String url) {
        try {
            ServletUnitClient client = container.newClient();
            return client.getResponse(url);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to get response on " + url + ". Error: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Create a URI string for the given path with the base URI prepended.
     */
    protected String uri(String path) {
        return baseURI + '/' + path;
    }
    
    /**
     * Asserts that the given URL generates the given error code.
     * @param error HTTP error code
     * @param url URL string 
     */
    void assertError(int error, String url) throws Exception {
        ServletUnitClient client = container.newClient();
        try {
            client.getResponse(url);
            fail("expected HTTP error " + error + " on " + url);
        } catch (HttpException e) {
            assertEquals("Unexpected HTTP Error code for " + url, error, e.getResponseCode());
        }
    }
    
    NodeList getNodes(Document doc, String path) throws Exception {
        XPath xpath = _xpathFactory.newXPath();
        Object nodes = xpath.compile(path).evaluate(doc, XPathConstants.NODESET);
        assertTrue(nodes instanceof NodeList);
        return (NodeList)nodes;
    }
    
    Node getNode(Document doc, String path) throws Exception {
        XPath xpath = _xpathFactory.newXPath();
        Object node = xpath.compile(path).evaluate(doc, XPathConstants.NODE);
        assertTrue(node instanceof Node);
        return (Node)node;
    }
}
