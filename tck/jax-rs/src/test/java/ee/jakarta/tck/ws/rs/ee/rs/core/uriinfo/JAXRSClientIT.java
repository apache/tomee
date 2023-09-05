/*
 * Copyright (c) 2007, 2022 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.core.uriinfo;

import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import java.io.InputStream;
import java.io.IOException;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = -5479757659703717839L;

  protected static final String ROOT = "jaxrs_ee_core_uriinfo_web";

  protected static final String RESOURCE = "resource";

  public JAXRSClientIT() {
    setup();
    setContextRoot("/" + ROOT + "/" + RESOURCE);
  }

  
  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/core/uriinfo/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_core_uriinfo_web.war");
    archive.addClasses(TSAppConfig.class, URIInfoTest.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }


  /* Run test */

  /*
   * @testName: queryTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:249; JAXRS:JAVADOC:97;
   * 
   * @test_Strategy: Client send a request with query parameters to a resource,
   * which handles the request using UriInfo. Verify that
   * UriInfo.getQueryParameters() works.
   */
  @Test
  public void queryTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(GET, "query?stringtest=cts&inttest=-2147483648?"));
    setProperty(Property.UNORDERED_SEARCH_STRING,
        "stringtest=cts|inttest=-2147483648?");
    invoke();
  }

  /*
   * @testName: queryTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:250; JAXRS:JAVADOC:97;
   * 
   * @test_Strategy: Client send a request with query parameters to a resource,
   * which handles the request using UriInfo. Verify that
   * UriInfo.getQueryParameters(true) works.
   */
  @Test
  public void queryTest1() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET,
        "query1?stringtest=cts%20&inttest=-2147483648?%2010"));
    setProperty(Property.UNORDERED_SEARCH_STRING,
        "stringtest=cts |inttest=-2147483648? 10");
    invoke();
  }

  /*
   * @testName: queryTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:250; JAXRS:JAVADOC:97;
   * 
   * @test_Strategy: Client send a request with query parameters to a resource,
   * which handles the request using UriInfo. Verify that
   * UriInfo.getQueryParameters(false) works.
   */
  @Test
  public void queryTest2() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(GET, "query2?stringtest=cts%20&inttest=-2147483648%2010"));
    setProperty(Property.UNORDERED_SEARCH_STRING,
        "stringtest=cts%20|inttest=-2147483648%2010");
    invoke();
  }

  /*
   * @testName: aPathTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:236; JAXRS:JAVADOC:237;
   * 
   * @test_Strategy: Client send a request to a resource, which handles the
   * request using UriInfo. Verify that UriInfo.getAbsolutePath() and
   * getAbsolutePathBuilder() work.
   */
  @Test
  public void aPathTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "apath"));
    setProperty(Property.SEARCH_STRING,
        "http://" + _hostname + ":" + _port + getContextRoot() + "/apath");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, "FAILED");
    invoke();
  }

  /*
   * @testName: baseUriTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:238; JAXRS:JAVADOC:239;
   * 
   * @test_Strategy: Client send a request to a resource, which handles the
   * request using UriInfo. Verify that UriInfo.getBaseUri() and
   * getBaseUriBuilder() work.
   */
  @Test
  public void baseUriTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "baseuri"));
    setProperty(Property.SEARCH_STRING,
        "http://" + _hostname + ":" + _port + "/" + ROOT);
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, "FAILED");
    invoke();
  }

  /*
   * @testName: pathTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:243;
   * 
   * @test_Strategy: Client send a request to a resource, which handles the
   * request using UriInfo. Verify that UriInfo.getPath() work.
   */
  @Test
  public void pathTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "path"));
    setProperty(Property.SEARCH_STRING, RESOURCE + "/path");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, ROOT);
    invoke();
  }

  /*
   * @testName: pathTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:244;
   * 
   * @test_Strategy: Client send a request to a resource, which handles the
   * request using UriInfo. Verify that UriInfo.getPath(true) work.
   */
  @Test
  public void pathTest1() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "path1%20/%2010"));
    setProperty(Property.SEARCH_STRING, RESOURCE + "/path1 / 10");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, ROOT);
    invoke();
  }

  /*
   * @testName: pathTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:244;
   * 
   * @test_Strategy: Client send a request to a resource, which handles the
   * request using UriInfo. Verify that UriInfo.getPath(false) work.
   */
  @Test
  public void pathTest2() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "path2%20/%2010"));
    setProperty(Property.SEARCH_STRING, RESOURCE + "/path2%20/%2010");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, ROOT);
    invoke();
  }

  /*
   * @testName: pathSegTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:247;
   * 
   * @test_Strategy: Client send a request to a resource, which handles the
   * request using UriInfo. Verify that UriInfo.getPathSegments() work.
   */
  @Test
  public void pathSegTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "pathseg"));
    setProperty(Property.UNORDERED_SEARCH_STRING, RESOURCE);
    setProperty(Property.UNORDERED_SEARCH_STRING, "pathseg");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, ROOT);
    invoke();
  }

  /*
   * @testName: pathSegTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:248;
   * 
   * @test_Strategy: Client send a request to a resource, which handles the
   * request using UriInfo. Verify that UriInfo.getPathSegments(true) work.
   */
  @Test
  public void pathSegTest1() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "pathseg1%20/%2010"));
    setProperty(Property.UNORDERED_SEARCH_STRING, RESOURCE);
    setProperty(Property.UNORDERED_SEARCH_STRING, "pathseg1 / 10/");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, ROOT);
    invoke();
  }

  /*
   * @testName: pathSegTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:248;
   * 
   * @test_Strategy: Client send a request to a resource, which handles the
   * request using UriInfo. Verify that UriInfo.getPathSegments(false) work.
   */
  @Test
  public void pathSegTest2() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "pathseg2%20/%2010"));
    setProperty(Property.UNORDERED_SEARCH_STRING, RESOURCE);
    setProperty(Property.UNORDERED_SEARCH_STRING, "pathseg2%20/%2010/");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, ROOT);
    invoke();
  }

  /*
   * @testName: pathParamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:245; JAXRS:JAVADOC:97;
   * 
   * @test_Strategy: Client send a request to a resource, which handles the
   * request using UriInfo. Verify that UriInfo.getPathParameters() work.
   */
  @Test
  public void pathParamTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "pathparam/a/b"));
    setProperty(Property.UNORDERED_SEARCH_STRING, "a=a|b=b");
    invoke();
  }

  /*
   * @testName: pathParamTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:246; JAXRS:JAVADOC:97;
   * 
   * @test_Strategy: Client send a request to a resource, which handles the
   * request using UriInfo. Verify that UriInfo.getPathParameters(true) work.
   */
  @Test
  public void pathParamTest1() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "pathparam1/%20/%2010"));
    setProperty(Property.UNORDERED_SEARCH_STRING, "a= |b= 10");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, ROOT);
    invoke();
  }

  /*
   * @testName: pathParamTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:246; JAXRS:JAVADOC:97;
   * 
   * @test_Strategy: Client send a request to a resource, which handles the
   * request using UriInfo. Verify that UriInfo.getPathParameters(false) work.
   */
  @Test
  public void pathParamTest2() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "pathparam2/%20/%2010"));
    setProperty(Property.UNORDERED_SEARCH_STRING, "a=%20|b=%2010");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, ROOT);
    invoke();
  }

  /*
   * @testName: requestURITest
   * 
   * @assertion_ids: JAXRS:JAVADOC:251; JAXRS:JAVADOC:252;
   * 
   * @test_Strategy: Client send a request with query parameters to a resource,
   * which handles the request using UriInfo. Verify that getRequestUri() and
   * getRequestUriBuilder() work.
   */
  @Test
  public void requestURITest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(GET, "request?stringtest=cts&inttest=-2147483648"));
    setProperty(Property.UNORDERED_SEARCH_STRING,
        "http://" + _hostname + ":" + _port + getContextRoot()
            + "/request?stringtest=cts&inttest=-2147483648");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, "FAILED");
    invoke();
  }

  /*
   * @testName: getMatchedResourcesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:240; JAXRS:SPEC:40;
   * 
   * @test_Strategy: Client send a request with query parameters to a resource,
   * which handles the request using UriInfo. Verify that getMatchedResources()
   * work.
   */
  @Test
  public void getMatchedResourcesTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "resource"));
    setProperty(Property.SEARCH_STRING, URIInfoTest.class.getName());
    invoke();
  }

  /*
   * @testName: getMatchedURIsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:241;
   * 
   * @test_Strategy: Client send a request with query parameters to a resource,
   * which handles the request using UriInfo. Verify that getMatchedURIs() work.
   */
  @Test
  public void getMatchedURIsTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "uri"));
    setProperty(Property.SEARCH_STRING, RESOURCE + "/uri");
    setProperty(Property.SEARCH_STRING, RESOURCE);
    setProperty(Property.SEARCH_STRING, "number=2");
    invoke();
  }

  /*
   * @testName: getMatchedURIsTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:242;
   * 
   * @test_Strategy: Client send a request with query parameters to a resource,
   * which handles the request using UriInfo. Verify that getMatchedURIs(true)
   * work.
   */
  @Test
  public void getMatchedURIsTest1() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "uri1"));
    setProperty(Property.SEARCH_STRING, RESOURCE + "/uri1");
    setProperty(Property.SEARCH_STRING, RESOURCE);
    setProperty(Property.SEARCH_STRING, "number=2");
    invoke();
  }

  /*
   * @testName: getMatchedURIsTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:242;
   * 
   * @test_Strategy: Client send a request with query parameters to a resource,
   * which handles the request using UriInfo. Verify that getMatchedURIs(false)
   * work.
   */
  @Test
  public void getMatchedURIsTest2() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "uri2"));
    setProperty(Property.SEARCH_STRING, RESOURCE + "/uri2");
    setProperty(Property.SEARCH_STRING, RESOURCE);
    setProperty(Property.SEARCH_STRING, "number=2");
    invoke();
  }

  /*
   * @testName: getNormalizedUriTest
   * 
   * @assertion_ids: JAXRS:SPEC:61;
   * 
   * @test_Strategy: The normalized request URI MUST be reflected in the URIs
   * obtained from an injected UriInfo
   */
  @Test
  @Disabled
  public void getNormalizedUriTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, URIInfoTest.DECODED));
    invoke();
    assertBodyGreaterThanOne();

    setProperty(Property.REQUEST, buildRequest(GET, URIInfoTest.ENCODED));
    invoke();
    assertBodyGreaterThanOne();
  }

  private void assertBodyGreaterThanOne() throws Fault {
    int i = Integer.parseInt(getResponseBody());
    assertTrue(i > 1, "Got unexpected response body"+ getResponseBody());
  }
}
