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

package ee.jakarta.tck.ws.rs.ee.rs.formparam;

import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityPrototype;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingExceptionGivenByName;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingWebApplicationException;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithConstructor;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithFromString;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithValueOf;
import ee.jakarta.tck.ws.rs.ee.rs.JaxrsParamClient;

import ee.jakarta.tck.ws.rs.ee.rs.RuntimeExceptionMapper;
import ee.jakarta.tck.ws.rs.ee.rs.WebApplicationExceptionMapper;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import java.io.InputStream;
import java.io.IOException;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

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
  private static final String ENCODED = "_%60%27%24X+Y%40%22a+a%22";

  private static final String DECODED = "_`'$X Y@\"a a\"";

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_formparam_web/FormParamTest");
  }

  private static final long serialVersionUID = 1L;

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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/formparam/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_formparam_web.war");
    archive.addClasses(TSAppConfig.class, FormParamTest.class,
      ParamEntityPrototype.class,
      ParamEntityWithConstructor.class,
      ParamEntityWithValueOf.class,
      ParamEntityWithFromString.class,
      ParamEntityThrowingWebApplicationException.class,
      ParamEntityThrowingExceptionGivenByName.class,
      RuntimeExceptionMapper.class,
      WebApplicationExceptionMapper.class
);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }



  /*
   * @testName: nonDefaultFormParamNothingSentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Test sending no content;
   */
  @Test
  public void nonDefaultFormParamNothingSentTest() throws Fault {
    defaultFormParamAndInvoke(Request.POST, "PostNonDefParam", null);
  }

  /*
   * @testName: defaultFormParamSentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Test sending override of default argument content;
   */
  @Test
  public void defaultFormParamSentTest() throws Fault {
    setProperty(Property.CONTENT, "default_argument=" + ENCODED);
    defaultFormParamAndInvoke(Request.POST, "PostDefParam", ENCODED);
  }

  /*
   * @testName: defaultFormParamNoArgSentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Test sending no argument content, receiving default;
   */
  @Test
  public void defaultFormParamNoArgSentTest() throws Fault {
    defaultFormParamAndInvoke(Request.POST, "PostDefParam", "default");
  }

  /*
   * @testName: defaultFormParamPutNoArgSentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Test sending no argument content, PUT, receiving default;
   */
  @Test
  public void defaultFormParamPutNoArgSentTest() throws Fault {
    defaultFormParamAndInvoke(Request.PUT, "DefParam", "DefParam");
  }

  /*
   * @testName: defaultFormParamPutArgSentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Test sending argument content, PUT;
   */
  @Test
  public void defaultFormParamPutArgSentTest() throws Fault {
    setProperty(Property.CONTENT, "default_argument=" + ENCODED);
    defaultFormParamAndInvoke(Request.PUT, "DefParam", DECODED);
  }

  /*
   * @testName: defaultFormParamValueOfTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Test creating a ParamEntityWithValueOf from default;
   */
  @Test
  public void defaultFormParamValueOfTest() throws Fault {
    defaultFormParamAndInvoke(Request.POST, "ParamEntityWithValueOf",
        "ValueOf");
  }

  /*
   * @testName: nonDefaultFormParamValueOfTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Test creating a ParamEntityWithValueOf from sending a
   * String;
   */
  @Test
  public void nonDefaultFormParamValueOfTest() throws Fault {
    setProperty(Property.CONTENT, "default_argument=" + ENCODED);
    defaultFormParamAndInvoke(Request.POST, "ParamEntityWithValueOf", DECODED);
  }

  /*
   * @testName: defaultFormParamFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Test creating a ParamEntityWithFromString from default;
   */
  @Test
  public void defaultFormParamFromStringTest() throws Fault {
    defaultFormParamAndInvoke(Request.POST, "ParamEntityWithFromString",
        "FromString");
  }

  /*
   * @testName: nonDefaultFormParamFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Test creating a ParamEntityWithFromString from sending a
   * String;
   */
  @Test
  public void nonDefaultFormParamFromStringTest() throws Fault {
    setProperty(Property.CONTENT, "default_argument=" + ENCODED);
    defaultFormParamAndInvoke(Request.POST, "ParamEntityWithFromString",
        ENCODED);
  }

  /*
   * @testName: defaultFormParamFromConstructorTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Test creating a ParamEntityWithFromString from default;
   */
  @Test
  public void defaultFormParamFromConstructorTest() throws Fault {
    defaultFormParamAndInvoke(Request.POST, "Constructor", "Constructor");
  }

  /*
   * @testName: nonDefaultFormParamFromConstructorTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Test creating a ParamEntityWithConstructor from sending a
   * String;
   */
  @Test
  public void nonDefaultFormParamFromConstructorTest() throws Fault {
    setProperty(Property.CONTENT, "default_argument=" + ENCODED);
    defaultFormParamAndInvoke(Request.POST, "Constructor", DECODED);
  }

  /*
   * @testName: defaultFormParamFromListConstructorTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Test creating a ParamEntityWithConstructor from default;
   */
  @Test
  public void defaultFormParamFromListConstructorTest() throws Fault {
    defaultFormParamAndInvoke(Request.POST, "ListConstructor",
        "ListConstructor");
  }

  /*
   * @testName: nonDefaultFormParamFromListConstructorTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Test creating a ParamEntityWithConstructor from sending a
   * String;
   */
  @Test
  public void nonDefaultFormParamFromListConstructorTest() throws Fault {
    setProperty(Property.CONTENT, "default_argument=" + ENCODED);
    defaultFormParamAndInvoke(Request.POST, "ListConstructor", DECODED);
  }

  /*
   * @testName: defaultFormParamFromSetFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Test creating a ParamEntityWithFromString from default;
   */
  @Test
  public void defaultFormParamFromSetFromStringTest() throws Fault {
    defaultFormParamAndInvoke(Request.POST, "SetFromString", "SetFromString");
  }

  /*
   * @testName: nonDefaultFormParamFromSetFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Test creating a ParamEntityWithListConstructor from sending
   * a String;
   */
  @Test
  public void nonDefaultFormParamFromSetFromStringTest() throws Fault {
    setProperty(Property.CONTENT, "default_argument=" + ENCODED);
    defaultFormParamAndInvoke(Request.POST, "SetFromString", ENCODED);
  }

  /*
   * @testName: defaultFormParamFromSortedSetFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Test creating a ParamEntityWithFromString from default;
   */
  @Test
  public void defaultFormParamFromSortedSetFromStringTest() throws Fault {
    defaultFormParamAndInvoke(Request.POST, "SortedSetFromString",
        "SortedSetFromString");
  }

  /*
   * @testName: nonDefaultFormParamFromSortedSetFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Test creating a ParamEntityWithListConstructor from sending
   * a String;
   */
  @Test
  public void nonDefaultFormParamFromSortedSetFromStringTest() throws Fault {
    setProperty(Property.CONTENT, "default_argument=" + ENCODED);
    defaultFormParamAndInvoke(Request.POST, "SortedSetFromString", ENCODED);
  }

  /*
   * @testName: defaultFormParamFromListFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Test creating a ParamEntityWithFromString from default;
   */
  @Test
  public void defaultFormParamFromListFromStringTest() throws Fault {
    defaultFormParamAndInvoke(Request.POST, "ListFromString", "ListFromString");
  }

  /*
   * @testName: nonDefaultFormParamFromListFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Test creating a ParamEntityWithListConstructor from sending
   * a String;
   */
  @Test
  public void nonDefaultFormParamFromListFromStringTest() throws Fault {
    setProperty(Property.CONTENT, "default_argument=" + ENCODED);
    defaultFormParamAndInvoke(Request.POST, "ListFromString", ENCODED);
  }

  /*
   * @testName: formParamEntityWithEncodedTest
   * 
   * @assertion_ids: JAXRS:SPEC:7; JAXRS:SPEC:12;JAXRS:SPEC:12.2;
   * 
   * @test_Strategy: Verify that named FormParam @Encoded is handled
   */
  @Test
  public void formParamEntityWithEncodedTest() throws Fault {
    searchEqualsEncoded = true;
    super.paramEntityWithEncodedTest();
  }

  /*
   * @testName: formParamThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see Section 3.2.
   */
  @Test
  public void formParamThrowingWebApplicationExceptionTest() throws Fault {
    super.paramThrowingWebApplicationExceptionTest();
  }

  /*
   * @testName: formParamThrowingIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see section 3.2. Exceptions thrown during
   * construction of @FormParam annotated parameter values are treated the same
   * as if the parameter were annotated with @HeaderParam.
   */
  @Test
  public void formParamThrowingIllegalArgumentExceptionTest() throws Fault {
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.BAD_REQUEST.name());
    super.paramThrowingIllegalArgumentExceptionTest();
  }

  // ///////////////////////////////////////////////////////////////////////

  private void defaultFormParamAndInvoke(Request request, String method,
      String arg) throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE));
    setProperty(Property.REQUEST, buildRequest(request, method));
    setProperty(Property.SEARCH_STRING, FormParamTest.response(arg));
    invoke();
  }

  @Override
  protected String buildRequest(String param) {
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE));
    setProperty(Property.CONTENT,
        "default_argument=" + param.replace("=", "%3d"));
    return buildRequest(Request.POST, segmentFromParam(param));
  }

  // not used at the moment
  @Override
  protected String getDefaultValueOfParam(String param) {
    return null;
  }
}
