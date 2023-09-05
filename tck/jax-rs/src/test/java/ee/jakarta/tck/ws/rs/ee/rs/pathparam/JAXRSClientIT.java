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

package ee.jakarta.tck.ws.rs.ee.rs.pathparam;

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

  private static final long serialVersionUID = 1L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_rs_pathparam_web/PathParamTest");
    useDefaultValue = false;
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/pathparam/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_pathparam_web.war");
    archive.addClasses(TSAppConfig.class, PathParamTest.class,
      ParamEntityPrototype.class,
      ParamEntityWithConstructor.class,
      ParamEntityWithValueOf.class,
      ParamEntityWithFromString.class,
      ParamTest.class,
      ParamEntityThrowingWebApplicationException.class,
      ParamEntityThrowingExceptionGivenByName.class,
      RuntimeExceptionMapper.class,
      WebApplicationExceptionMapper.class
    );
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }



  /* Run test */
  /*
   * @testName: test1
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:JAVADOC:9;
   * 
   * @test_Strategy: Client invokes Request.GET on root resource at
   * /PathParamTest; Verify that right Method is invoked while using PathParam
   * with primitive type String.
   */
  @Test
  public void test1() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_HTML_TYPE));
    setProperty(Property.REQUEST, buildRequest(Request.GET, "a"));
    setProperty(Property.SEARCH_STRING, "single=a");
    invoke();
  }

  /*
   * @testName: test2
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:JAVADOC:9; JAXRS:JAVADOC:114;
   * 
   * @test_Strategy: Client invokes Request.GET on root resource at
   * /PathParamTest; Verify that right Method is invoked while using PathParam
   * primitive type String and PathSegment.
   */
  @Test
  public void test2() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "a/b"));
    setProperty(Property.SEARCH_STRING, "double=ab");
    invoke();
  }

  /*
   * @testName: test3
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:JAVADOC:9; JAXRS:JAVADOC:114;
   * 
   * @test_Strategy: Client invokes Request.GET on root resource at
   * /PathParamTest; Verify that right Method is invoked while using PathParam
   * primitive type int, float and PathSegment.
   */
  @Test
  public void test3() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "2147483647/b/12.345"));
    setProperty(Property.SEARCH_STRING, "triple=2147483647b12.345");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "-2147483648/b/123.0"));
    setProperty(Property.SEARCH_STRING, "triple=-2147483648b123.0");
    invoke();
  }

  /*
   * @testName: test4
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:JAVADOC:9; JAXRS:JAVADOC:114;
   * 
   * @test_Strategy: Client invokes Request.GET on root resource at
   * /PathParamTest; Verify that right Method is invoked using PathParam
   * primitive type double, boolean, byte, and PathSegment.
   */
  @Test
  public void test4() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "123.1/true/127/tmp"));
    setProperty(Property.SEARCH_STRING, "quard=123.1true127tmp");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "345/false/-128/xyz"));
    setProperty(Property.SEARCH_STRING, "quard=345.0false-128xyz");
    invoke();
  }

  /*
   * @testName: test5
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:JAVADOC:9; JAXRS:JAVADOC:114;
   * 
   * @test_Strategy: Client invokes Request.GET on root resource at
   * /PathParamTest; Verify that right Method is invoked using PathParam
   * primitive type long, String, short, boolean and PathSegment.
   */
  @Test
  public void test5() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "-9223372036854775808/b/32767/true/abc"));
    setProperty(Property.SEARCH_STRING,
        "penta=-9223372036854775808b32767trueabc");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "9223372036854775807/b/-32768/false/xyz"));
    setProperty(Property.SEARCH_STRING,
        "penta=9223372036854775807b-32768falsexyz");
    invoke();
  }

  /*
   * @testName: test6
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:JAVADOC:9;
   * 
   * @test_Strategy: Client invokes Request.GET on root resource at
   * /PathParamTest; Verify that right Method is invoked using PathParam
   * primitive type List<String>.
   */
  @Test
  public void test6() throws Fault {
    String[] headers = { "list=abcdef", "list=fedcba" };

    for (String header : headers) {
      setProperty(Property.REQUEST_HEADERS, "Accept: text/plain");
      setProperty(Property.REQUEST, buildRequest(Request.GET, "a/b/c/d/e/f"));
      setProperty(Property.SEARCH_STRING, header);
      try {
        invoke();
        return;
      } catch (Exception ex) {
        TestUtil
            .logTrace("Header " + header + " didnt work out, try another one");
      }
    }
    throw new Fault("If you get to here means test failed.");
  }

  /*
   * @testName: test7
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:JAVADOC:9;JAXRS:JAVADOC:113;
   * JAXRS:JAVADOC:114;
   * 
   * @test_Strategy: Client invokes Request.GET on root resource at
   * /PathParamTest with Matrix parameter; Verify that right Method is invoked
   * using PathParam PathSegment.
   */
  @Test
  public void test7() throws Fault {
    String[] headers = { "matrix=/a;boolean1=false;boolean2=true",
        "matrix=/a;boolean2=true;boolean1=false" };
    for (String header : headers) {
      setProperty(Property.REQUEST_HEADERS, "Accept:text/plain");
      setProperty(Property.REQUEST,
          buildRequest(Request.GET, "matrix/a;boolean1=false;boolean2=true"));
      setProperty(Property.SEARCH_STRING, header);
      try {
        invoke();
        return;
      } catch (Exception ex) {
        TestUtil
            .logTrace("Header " + header + " didnt work out, try another one");
      }
    }
    throw new Fault("If you get to here means test failed.");
  }

  /*
   * @testName: pathParamEntityWithConstructorTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:5.2; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Verify that named PathParam is handled properly
   */
  @Test
  public void pathParamEntityWithConstructorTest() throws Fault {
    super.paramEntityWithConstructorTest();
  }

  /*
   * @testName: pathParamEntityWithValueOfTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:5.3; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Verify that named PathParam is handled properly
   */
  @Test
  public void pathParamEntityWithValueOfTest() throws Fault {
    super.paramEntityWithValueOfTest();
  }

  /*
   * @testName: pathParamEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:5.3; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Verify that named PathParam is handled properly
   */
  @Test
  public void pathParamEntityWithFromStringTest() throws Fault {
    searchEqualsEncoded = true;
    super.paramEntityWithFromStringTest();
  }

  /*
   * @testName: pathParamSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:5.4; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Verify that named PathParam is handled properly
   */
  @Test
  public void pathParamSetEntityWithFromStringTest() throws Fault {
    super.paramCollectionEntityWithFromStringTest(CollectionName.SET);
  }

  /*
   * @testName: pathParamSortedSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:5.4; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Verify that named PathParam is handled properly
   */
  @Test
  public void pathParamSortedSetEntityWithFromStringTest() throws Fault {
    super.paramCollectionEntityWithFromStringTest(CollectionName.SORTED_SET);
  }

  /*
   * @testName: pathParamListEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:5.4; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Verify that named PathParam is handled properly
   */
  @Test
  public void pathParamListEntityWithFromStringTest() throws Fault {
    super.paramCollectionEntityWithFromStringTest(CollectionName.LIST);
  }

  /*
   * pathFieldParamEntityWithConstructorTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:5.2;
   * 
   * @test_Strategy: Verify that named PathParam is handled properly
   */
  public void pathFieldParamEntityWithConstructorTest() throws Fault {
    super.fieldEntityWithConstructorTest();
  }

  /*
   * pathFieldParamEntityWithValueOfTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:5.3;
   * 
   * @test_Strategy: Verify that named PathParam is handled properly
   */
  public void pathFieldParamEntityWithValueOfTest() throws Fault {
    super.fieldEntityWithValueOfTest();
  }

  /*
   * pathFieldParamEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:5.3;
   * 
   * @test_Strategy: Verify that named PathParam is handled properly
   */
  public void pathFieldParamEntityWithFromStringTest() throws Fault {
    super.fieldEntityWithFromStringTest();
  }

  /*
   * pathFieldParamSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:5.4;
   * 
   * @test_Strategy: Verify that named PathParam is handled properly
   */
  public void pathFieldParamSetEntityWithFromStringTest() throws Fault {
    super.fieldCollectionEntityWithFromStringTest(CollectionName.SET);
  }

  /*
   * pathFieldParamSortedSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:5.4;
   * 
   * @test_Strategy: Verify that named PathParam is handled properly
   */
  public void pathFieldParamSortedSetEntityWithFromStringTest() throws Fault {
    super.fieldCollectionEntityWithFromStringTest(CollectionName.SORTED_SET);
  }

  /*
   * pathFieldParamListEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:5.4;
   * 
   * @test_Strategy: Verify that named PathParam is handled properly
   */
  public void pathFieldParamListEntityWithFromStringTest() throws Fault {
    super.fieldCollectionEntityWithFromStringTest(CollectionName.LIST);
  }

  /*
   * @testName: pathParamEntityWithEncodedTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:7;
   * 
   * @test_Strategy: Verify that named PathParam @Encoded is handled
   */
  @Test
  public void pathParamEntityWithEncodedTest() throws Fault {
    searchEqualsEncoded = true;
    super.paramEntityWithEncodedTest();
  }

  /*
   * @testName: pathParamThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:8;
   * 
   * @test_Strategy: A WebApplicationException thrown during construction of
   * field or property values using 2 or 3 above is processed directly as
   * described in section 3.3.4.
   */
  @Test
  public void pathParamThrowingWebApplicationExceptionTest() throws Fault {
    super.paramThrowingWebApplicationExceptionTest();
  }

  /*
   * @testName: pathParamThrowingIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:9; JAXRS:SPEC:9.1; JAXRS:SPEC:10;
   * 
   * @test_Strategy: Other exceptions thrown during construction of field or
   * property values using 2 or 3 above are treated as client errors:
   * 
   * if the field or property is annotated with @MatrixParam,
   * 
   * @QueryParam or @PathParam then an implementation MUST generate a
   * WebApplicationException that wraps the thrown exception with a not found
   * response (404 status) and no entity;
   */
  @Test
  public void pathParamThrowingIllegalArgumentExceptionTest() throws Fault {
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.NOT_FOUND.name());
    super.paramThrowingIllegalArgumentExceptionTest();
  }

  @Override
  protected String buildRequest(String param) {
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_PLAIN_TYPE));

    StringBuilder sb = new StringBuilder();
    sb.append(Request.GET.name()).append(" ").append(_contextRoot);
    sb.append(SL).append(segmentFromParam(param)).append(SL);
    sb.append(param.replace("=", "%3d")).append(SL);
    return sb.append(HTTP11).toString();
  }

  @Override
  protected String getDefaultValueOfParam(String param) {
    StringBuilder sb = new StringBuilder();
    sb.append(param).append("=");
    sb.append(PathParamTest.class.getSimpleName());
    return sb.toString();
  }
}
