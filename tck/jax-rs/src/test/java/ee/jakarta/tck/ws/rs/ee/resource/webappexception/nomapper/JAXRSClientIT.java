/*
 * Copyright (c) 2011, 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.resource.webappexception.nomapper;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.ws.rs.core.Response.Status;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JAXRSCommonClient {
  private static final long serialVersionUID = 1L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_resource_webappexception_nomapper_web/resource");
  }

  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException {

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/resource/webappexception/nomapper/web.xml.template");
    // Replace the servlet_adaptor in web.xml.template with the System variable set as servlet adaptor
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_resource_webappexception_nomapper_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class);
    archive.setWebXML(new StringAsset(webXml));
    //archive.addAsWebInfResource(JAXRSClientIT.class.getPackage(), "web.xml.template", "web.xml"); //can use if the web.xml.template doesn't need to be modified.    
    
    return archive;
  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  /*
   * @testName: emptyConstructorTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:13;
   * 
   * @test_Strategy: Client invokes GET on root resource at /resource; Verify
   * that WebApplicationException() works.
   */
  @Test
  public void emptyConstructorTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "EmptyConstructor"));
    setProperty(STATUS_CODE, getStatusCode(Status.INTERNAL_SERVER_ERROR));
    invoke();
  }

  /*
   * @testName: statusCode404Test
   * 
   * @assertion_ids: JAXRS:JAVADOC:15;
   * 
   * @test_Strategy: Client invokes GET on root resource at /resource; Verify
   * that WebApplicationException(404) works.
   */
  @Test
  public void statusCode404Test() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "StatusCode404"));
    setProperty(STATUS_CODE, getStatusCode(Status.NOT_FOUND));
    invoke();
  }

  /*
   * @testName: statusCode401Test
   * 
   * @assertion_ids: JAXRS:JAVADOC:15;
   * 
   * @test_Strategy: Client invokes GET on root resource at /resource; Verify
   * that WebApplicationException(401) works.
   */
  @Test
  public void statusCode401Test() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "StatusCode401"));
    setProperty(STATUS_CODE, getStatusCode(Status.UNAUTHORIZED));
    invoke();
  }

  /*
   * @testName: status503Test
   * 
   * @assertion_ids: JAXRS:JAVADOC:16;
   * 
   * @test_Strategy: Client invokes GET on root resource at /resource; Verify
   * that WebApplicationException(Status) works.
   */
  @Test
  public void status503Test() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "Status503"));
    setProperty(STATUS_CODE, getStatusCode(Status.SERVICE_UNAVAILABLE));
    invoke();
  }

  /*
   * @testName: status415Test
   * 
   * @assertion_ids: JAXRS:JAVADOC:16;
   * 
   * @test_Strategy: Client invokes GET on root resource at /resource; Verify
   * that WebApplicationException(Status) works.
   */
  @Test
  public void status415Test() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "Status415"));
    setProperty(STATUS_CODE, getStatusCode(Status.UNSUPPORTED_MEDIA_TYPE));
    invoke();
  }

  /*
   * @testName: responseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:14;
   * 
   * @test_Strategy: Client invokes GET on root resource at /resource; Verify
   * that WebApplicationException(Response) works.
   */
  @Test
  public void responseTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "ResponseTest"));
    setProperty(SEARCH_STRING, Resource.TESTID);
    setProperty(Property.EXPECTED_HEADERS, "CTS-HEAD: " + Resource.TESTID);
    invoke();
  }

  /*
   * @testName: nullResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:14;
   * 
   * @test_Strategy: Client invokes GET on root resource at /resource; Verify
   * that WebApplicationException(Response null) works.
   */
  @Test
  public void nullResponseTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "NullResponseTest"));
    setProperty(STATUS_CODE, getStatusCode(Status.INTERNAL_SERVER_ERROR));
    invoke();
  }

  /*
   * @testName: getResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Client invokes GET on root resource at /resource; Verify
   * that WebApplicationException.getResponse works.
   */
  @Test
  public void getResponseTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "getResponseTest"));
    setProperty(SEARCH_STRING, Resource.TESTID);
    setProperty(Property.EXPECTED_HEADERS, "CTS-HEAD: " + Resource.TESTID);
    invoke();
  }

  /*
   * @testName: throwableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:17;
   * 
   * @test_Strategy: Client invokes GET on root resource at /resource; Verify
   * that WebApplicationException(Throwable) works.
   */
  @Test
  public void throwableTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "ThrowableTest"));
    setProperty(STATUS_CODE, getStatusCode(Status.INTERNAL_SERVER_ERROR));
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH,
        Resource.id("-throwableTest"));
    invoke();
  }

  /*
   * @testName: throwableResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:18;
   * 
   * @test_Strategy: Client invokes GET on root resource at /resource; Verify
   * that WebApplicationException(Throwable, Response) works.
   */
  @Test
  public void throwableResponseTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "ThrowableResponseTest"));
    setProperty(STATUS_CODE, getStatusCode(Status.ACCEPTED));
    setProperty(Property.EXPECTED_HEADERS, "CTS-HEAD: " + Resource.TESTID);
    setProperty(SEARCH_STRING, Resource.TESTID);
    setProperty(SEARCH_STRING, "throwableResponseTest");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, Resource.id("-FAIL"));
    invoke();
  }

  /*
   * @testName: throwableResponseTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:18;
   * 
   * @test_Strategy: Client invokes GET on root resource at /resource; Verify
   * that WebApplicationException(Throwable, Response Null) works.
   */
  @Test
  public void throwableResponseTest1() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "ThrowableResponseTest1"));
    setProperty(STATUS_CODE, getStatusCode(Status.INTERNAL_SERVER_ERROR));
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH,
        Resource.id("-throwableResponseTest1-FAIL"));
    invoke();
  }

  /*
   * @testName: throwableStatusTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:20;
   * 
   * @test_Strategy: Client invokes GET on root resource at /resource; Verify
   * that WebApplicationException(Throwable, Status) works.
   */
  @Test
  public void throwableStatusTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "ThrowableStatusTest"));
    setProperty(STATUS_CODE, getStatusCode(Status.SEE_OTHER));
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH,
        Resource.id("-throwableStatusTest"));
    invoke();
  }

  /*
   * @testName: throwableStatusTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:20;
   * 
   * @test_Strategy: Client invokes GET on root resource at /resource; Verify
   * that WebApplicationException(Throwable, Status Null) works.
   */
  @Test
  public void throwableStatusTest1() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "ThrowableNullStatusTest"));
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH,
        Resource.id("-throwableNullStatusTest-FAIL"));
    setProperty(SEARCH_STRING, Resource.id("-throwableNullStatusTest-PASS"));
    invoke();
  }

  /*
   * @testName: throwableStatusCodeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:19;
   * 
   * @test_Strategy: Client invokes GET on root resource at /resource; Verify
   * that WebApplicationException(Throwable, int) works.
   */
  @Test
  public void throwableStatusCodeTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "ThrowableStatusCodeTest"));
    setProperty(STATUS_CODE, getStatusCode(Status.NO_CONTENT));
    invoke();
  }
}