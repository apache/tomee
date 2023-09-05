/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id$
 */
package ee.jakarta.tck.ws.rs.ee.rs.cookieparam;

import java.io.InputStream;
import java.io.IOException;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityPrototype;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingExceptionGivenByName;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingWebApplicationException;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithConstructor;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithFromString;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithValueOf;
import ee.jakarta.tck.ws.rs.ee.rs.JaxrsParamClient;
import ee.jakarta.tck.ws.rs.ee.rs.ParamTest;
import ee.jakarta.tck.ws.rs.ee.rs.RuntimeExceptionMapper;
import ee.jakarta.tck.ws.rs.ee.rs.WebApplicationExceptionMapper;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.core.Response.Status;

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

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsParamClient {
  private static final long serialVersionUID = 1L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_rs_cookieparam_web/CookieParamTest");
  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  @Deployment(testable = false, name = "cookieparam")
  public static WebArchive createDeployment() throws IOException{

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/cookieparam/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_cookieparam_web.war");
    archive.addClasses(TSAppConfig.class, CookieParamTest.class,
      ParamEntityPrototype.class,
      ParamEntityWithConstructor.class,
      ParamEntityWithValueOf.class,
      ParamEntityWithFromString.class,
      ParamTest.class,
      JaxrsParamClient.CollectionName.class,
      ParamEntityThrowingWebApplicationException.class,
      ParamEntityThrowingExceptionGivenByName.class,
      RuntimeExceptionMapper.class,
      WebApplicationExceptionMapper.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }


  /* Run test */
  /*
   * @testName: cookieParamTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4; JAXRS:JAVADOC:145; JAXRS:JAVADOC:2;
   * 
   * @test_Strategy: Client invokes GET on root resource at /CookieParamTest;
   * Resource will respond with a cookie; Client verify the cookie is received;
   * Client send request again with the cookie; Verify that the cookie is
   * received using CookieParam in the resource; Verify on the client side from
   * response.
   */
  @Test
  public void cookieParamTest() throws Fault {
    StringBuffer sb = new StringBuffer();
    boolean pass = true;

    setProperty(Property.REQUEST, buildRequest("setcookie"));
    setProperty(Property.SEARCH_STRING, "setCookie=done");
    setProperty(Property.EXPECTED_HEADERS, "Set-Cookie:name1=value1");
    setProperty(Property.SAVE_STATE, "true");
    try {
      invoke();
    } catch (Exception ex) {
      pass = false;
      sb.append("Test failed with: " + ex.getMessage());
    }

    setProperty(Property.REQUEST, buildRequest("verifycookie"));
    setProperty(Property.SEARCH_STRING, "name1=value1|verifyCookie=done");
    setProperty(Property.USE_SAVED_STATE, "true");
    try {
      invoke();
    } catch (Exception ex) {
      pass = false;
      sb.append("Test failed with: " + ex.getMessage());
    }

    assertTrue(pass, "At least one assertion failed:"+ sb);
  }

  /*
   * @testName: cookieParamEntityWithConstructorTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4; JAXRS:SPEC:5.2; JAXRS:JAVADOC:12;
   * JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named CookieParam is handled properly
   */
  @Test
  public void cookieParamEntityWithConstructorTest() throws Fault {
    super.paramEntityWithConstructorTest();
  }

  /*
   * @testName: cookieParamEntityWithValueOfTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4; JAXRS:SPEC:5.3; JAXRS:JAVADOC:12;
   * JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named CookieParam is handled properly
   */
  @Test
  public void cookieParamEntityWithValueOfTest() throws Fault {
    super.paramEntityWithValueOfTest();
  }

  /*
   * @testName: cookieParamEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4; JAXRS:SPEC:5.3; JAXRS:JAVADOC:12;
   * JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named CookieParam is handled properly
   */
  @Test
  public void cookieParamEntityWithFromStringTest() throws Fault {
    super.paramEntityWithFromStringTest();
  }

  /*
   * @testName: cookieParamSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4; JAXRS:SPEC:5.4; JAXRS:JAVADOC:12;
   * JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named CookieParam is handled properly
   */
  @Test
  public void cookieParamSetEntityWithFromStringTest() throws Fault {
    super.paramCollectionEntityWithFromStringTest(CollectionName.SET);
  }

  /*
   * @testName: cookieParamListEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4; JAXRS:SPEC:5.4; JAXRS:JAVADOC:12;
   * JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named CookieParam is handled properly
   */
  @Test
  public void cookieParamListEntityWithFromStringTest() throws Fault {
    super.paramCollectionEntityWithFromStringTest(CollectionName.LIST);
  }

  /*
   * @testName: cookieParamSortedSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4; JAXRS:SPEC:5.4; JAXRS:JAVADOC:12;
   * JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named CookieParam is handled properly
   */
  @Test
  public void cookieParamSortedSetEntityWithFromStringTest() throws Fault {
    super.paramCollectionEntityWithFromStringTest(CollectionName.SORTED_SET);
  }

  /*
   * @testName: cookieFieldParamEntityWithConstructorTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4; JAXRS:SPEC:5.2; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named CookieParam is handled properly
   */
  @Test
  public void cookieFieldParamEntityWithConstructorTest() throws Fault {
    super.fieldEntityWithConstructorTest();
  }

  /*
   * @testName: cookieFieldParamEntityWithValueOfTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4; JAXRS:SPEC:5.3; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named CookieParam is handled properly
   */
  @Test
  public void cookieFieldParamEntityWithValueOfTest() throws Fault {
    super.fieldEntityWithValueOfTest();
  }

  /*
   * @testName: cookieFieldParamEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4; JAXRS:SPEC:5.3; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named CookieParam is handled properly
   */
  @Test
  public void cookieFieldParamEntityWithFromStringTest() throws Fault {
    super.fieldEntityWithFromStringTest();
  }

  /*
   * @testName: cookieFieldParamSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4; JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named CookieParam is handled properly
   */
  @Test
  public void cookieFieldParamSetEntityWithFromStringTest() throws Fault {
    super.fieldCollectionEntityWithFromStringTest(CollectionName.SET);
  }

  /*
   * @testName: cookieFieldParamListEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4; JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named CookieParam is handled properly
   */
  @Test
  public void cookieFieldParamListEntityWithFromStringTest() throws Fault {
    super.fieldCollectionEntityWithFromStringTest(CollectionName.LIST);
  }

  /*
   * @testName: cookieFieldSortedSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4; JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named CookieParam is handled properly
   */
  @Test
  public void cookieFieldSortedSetEntityWithFromStringTest() throws Fault {
    super.fieldCollectionEntityWithFromStringTest(CollectionName.SORTED_SET);
  }

  /*
   * @testName: cookieParamThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4; JAXRS:SPEC:12.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see section 3.2.
   * 
   */
  @Test
  public void cookieParamThrowingWebApplicationExceptionTest() throws Fault {
    super.paramThrowingWebApplicationExceptionTest();
  }

  /*
   * @testName: cookieFieldThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4; JAXRS:SPEC:8;
   * 
   * @test_Strategy: A WebApplicationException thrown during construction of
   * field or property values using 2 or 3 above is processed directly as
   * described in section 3.3.4.
   */
  @Test
  public void cookieFieldThrowingWebApplicationExceptionTest() throws Fault {
    super.fieldThrowingWebApplicationExceptionTest();
  }

  /*
   * @testName: cookieParamThrowingIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:9; JAXRS:SPEC:9.2; JAXRS:SPEC:10;
   * 
   * @test_Strategy: Other exceptions thrown during construction of field or
   * property values using 2 or 3 above are treated as client errors:
   *
   * if the field or property is annotated with @HeaderParam or @CookieParam
   * then an implementation MUST generate a WebApplicationException that wraps
   * the thrown exception with a client error response (400 status) and no
   * entity.
   */
  @Test
  public void cookieParamThrowingIllegalArgumentExceptionTest() throws Fault {
    super.paramThrowingIllegalArgumentExceptionTest();
  }

  /*
   * @testName: cookieFieldParamThrowingIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see section 3.2.
   */
  @Test
  public void cookieFieldParamThrowingIllegalArgumentExceptionTest()
      throws Fault {
    super.fieldThrowingIllegalArgumentExceptionTest();
  }

  @Override
  protected void paramEntityThrowingAfterRequestSet(String request)
      throws Fault {
    createAndCheckCookie(request);
    // this is for illegal argument exception tests only
    if (request.contains(IllegalArgumentException.class.getSimpleName()))
      setProperty(Property.UNORDERED_SEARCH_STRING, Status.BAD_REQUEST.name());
    super.paramEntityThrowingAfterRequestSet(request);
  }

  private void createAndCheckCookie(String request) throws Fault {
    // create cookie
    setProperty(Property.REQUEST, buildRequest(request));
    setProperty(Property.SAVE_STATE, "true");
    invoke();
    checkCookie(request);
    // check cookie
    setProperty(Property.USE_SAVED_STATE, "true");
  }

  @Override
  protected void paramEntity(String request) throws Fault {
    createAndCheckCookie(request);
    super.paramEntity(request);
  }

  @Override
  protected String buildRequest(String param) {
    StringBuilder sb = new StringBuilder();
    sb.append(Request.GET).append(" ").append(_contextRoot);
    sb.append("?todo=").append(param.replace("=", "%3d")).append(HTTP11);
    return sb.toString();
  }

  @Override
  protected String getDefaultValueOfParam(String param) {
    StringBuilder sb = new StringBuilder();
    sb.append(param).append("=");
    sb.append(CookieParamTest.class.getSimpleName());
    return sb.toString();
  }

  private void checkCookie(String cookie) throws Fault {
    boolean found = false;
    String lowCookie = stripQuotesSpacesAndLowerCase(cookie);
    String[] headers = getResponseHeaders();
    for (String h : headers) {
      String header = stripQuotesSpacesAndLowerCase(h);
      if (header.startsWith("set-cookie"))
        if (header.contains(lowCookie))
          found = true;
    }
    assertTrue(found, "Could not find cookie"+ cookie+ "in response headers:"+
        JaxrsUtil.iterableToString(";", headers));
    logMsg("Found cookie", cookie, "as expected");
  }

  private static String stripQuotesSpacesAndLowerCase(String cookie) {
    return cookie.toLowerCase().replace("\"", "").replace("'", "").replace(" ",
        "");
  }
}
