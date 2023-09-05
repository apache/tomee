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
package ee.jakarta.tck.ws.rs.ee.rs.queryparam;

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
    setContextRoot("/jaxrs_ee_rs_queryparam_web/QueryParamTest");
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/queryparam/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_queryparam_web.war");
    archive.addClasses(TSAppConfig.class, QueryParamTest.class,
      ParamEntityPrototype.class,
      ParamEntityWithConstructor.class,
      ParamEntityWithValueOf.class,
      ParamEntityWithFromString.class,
      ParamTest.class,
      JaxrsParamClient.CollectionName.class,
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
   * @testName: queryParamStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:SPEC:12.1;
   * JAXRS:JAVADOC:11; JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:JAVADOC:22;
   * 
   * @test_Strategy: Client invokes HEAD on root resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamStringTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest("stringtest=cts"));
    setProperty(Property.SEARCH_STRING, "stringtest=cts");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("stringtest=cts1&stringtest=cts2"));
    setProperty(Property.SEARCH_STRING, "stringtest=cts1");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("stringtest1=cts&stringtest2=ri_impl"));
    setProperty(Property.SEARCH_STRING,
        "stringtest=abc|stringtest1=cts|stringtest2=ri_impl");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("stringtest1=cts&stringtest2=ri_impl&stringtest1=newone"));
    setProperty(Property.SEARCH_STRING,
        "stringtest=abc|stringtest1=cts|stringtest2=ri_impl");
    invoke();
  }

  /*
   * @testName: queryParamNoQueryTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:SPEC:14;
   * JAXRS:JAVADOC:11; JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:JAVADOC:22;
   * 
   * @test_Strategy: Client invokes HEAD on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamNoQueryTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS, "Accept: text/plain");
    setProperty(Property.REQUEST, buildRequest(""));
    setProperty(Property.SEARCH_STRING, "stringtest=abc|No QueryParam");
    invoke();
  }

  /*
   * @testName: queryParamIntTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:JAVADOC:11;
   * JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:JAVADOC:22;
   * 
   * @test_Strategy: Client invokes HEAD on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamIntTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest("inttest1=2147483647"));
    setProperty(Property.SEARCH_STRING, "inttest1=2147483647");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("inttest=-2147483648&inttest=2147483647"));
    setProperty(Property.SEARCH_STRING, "inttest=-2147483648");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("inttest1=2147483647&inttest2=-2147483648"));
    setProperty(Property.SEARCH_STRING,
        "inttest1=2147483647|inttest2=-2147483648");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("inttest1=-2147483648&inttest2=2147483647&inttest1=1234"));
    setProperty(Property.SEARCH_STRING,
        "inttest1=-2147483648|inttest2=2147483647");
    invoke();
  }

  /*
   * @testName: queryParamDoubleTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:JAVADOC:11;
   * JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:JAVADOC:22;
   * 
   * @test_Strategy: Client invokes HEAD on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamDoubleTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest("doubletest1=123"));
    setProperty(Property.SEARCH_STRING, "doubletest1=123.0");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("doubletest=123.1&doubletest=345"));
    setProperty(Property.SEARCH_STRING, "doubletest=123.1");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("doubletest1=12.345&doubletest2=34.567"));
    setProperty(Property.SEARCH_STRING,
        "doubletest1=12.345|doubletest2=34.567");
    invoke();

    setProperty(Property.REQUEST, buildRequest(
        "doubletest1=23.456&doubletest2=0.56789&doubletest1=1.234"));
    setProperty(Property.SEARCH_STRING,
        "doubletest1=23.456|doubletest2=0.56789");
    invoke();
  }

  /*
   * @testName: queryParamFloatTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:JAVADOC:11;
   * JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:JAVADOC:22;
   *
   * @test_Strategy: Client invokes HEAD on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamFloatTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest("floattest1=123"));
    setProperty(Property.SEARCH_STRING, "floattest1=123.0");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("floattest=123.1&floattest=345"));
    setProperty(Property.SEARCH_STRING, "floattest=123.1");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("floattest1=12.345&floattest2=34.567"));
    setProperty(Property.SEARCH_STRING, "floattest1=12.345|floattest2=34.567");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("floattest1=23.456&floattest2=0.56789&floattest1=1.234"));
    setProperty(Property.SEARCH_STRING, "floattest1=23.456|floattest2=0.56789");
    invoke();
  }

  /*
   * @testName: queryParamLongTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:JAVADOC:11;
   * JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:JAVADOC:22;
   * 
   * @test_Strategy: Client invokes GET on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamLongTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest("longtest=-9223372036854775808"));
    setProperty(Property.SEARCH_STRING, "longtest=-9223372036854775808");
    invoke();

    setProperty(Property.REQUEST, buildRequest(
        "longtest=9223372036854775807&longtest=-9223372036854775808"));
    setProperty(Property.SEARCH_STRING, "longtest=9223372036854775807");
    invoke();

    setProperty(Property.REQUEST, buildRequest(
        "longtest1=-9223372036854775808&longtest2=9223372036854775807"));
    setProperty(Property.SEARCH_STRING,
        "longtest1=-9223372036854775808|longtest2=9223372036854775807");
    invoke();

    setProperty(Property.REQUEST, buildRequest(
        "longtest1=-9223372036854775808&longtest2=9223372036854775807&longtest1=1234"));
    setProperty(Property.SEARCH_STRING,
        "longtest1=-9223372036854775808|longtest2=9223372036854775807");
    invoke();
  }

  /*
   * @testName: queryParamShortTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:JAVADOC:11;
   * JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:JAVADOC:22;
   * 
   * @test_Strategy: Client invokes GET on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamShortTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest("shorttest=-32768"));
    setProperty(Property.SEARCH_STRING, "shorttest=-32768");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("shorttest=32767&shorttest=-32768"));
    setProperty(Property.SEARCH_STRING, "shorttest=32767");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("shorttest1=32767&shorttest2=-32768"));
    setProperty(Property.SEARCH_STRING, "shorttest1=32767|shorttest2=-32768");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("shorttest1=32767&shorttest2=-32768&shorttest1=1234"));
    setProperty(Property.SEARCH_STRING, "shorttest1=32767|shorttest2=-32768");
    invoke();
  }

  /*
   * @testName: queryParamByteTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:JAVADOC:11;
   * JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:JAVADOC:22;
   * 
   * @test_Strategy: Client invokes GET on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamByteTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest("bytetest=127"));
    setProperty(Property.SEARCH_STRING, "bytetest=127");
    invoke();

    setProperty(Property.REQUEST, buildRequest("bytetest=-128&bytetest=26"));
    setProperty(Property.SEARCH_STRING, "bytetest=-128");
    invoke();

    setProperty(Property.REQUEST, buildRequest("bytetest1=123&bytetest2=-128"));
    setProperty(Property.SEARCH_STRING, "bytetest1=123|bytetest2=-128");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("bytetest1=0&bytetest2=-128&bytetest1=127"));
    setProperty(Property.SEARCH_STRING, "bytetest2=-128");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, "bytetest1");
    invoke();
  }

  /*
   * @testName: queryParamBooleanTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:JAVADOC:11;
   * JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:JAVADOC:22;
   * 
   * @test_Strategy: Client invokes GET on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamBooleanTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest("booleantest=true"));
    setProperty(Property.SEARCH_STRING, "booleantest=true");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("booleantest=true&booleantest=false"));
    setProperty(Property.SEARCH_STRING, "booleantest=true");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("booleantest1=false&booleantest2=true"));
    setProperty(Property.SEARCH_STRING, "booleantest2=true");
    invoke();

    setProperty(Property.REQUEST,
        buildRequest("booleantest1=true&booleantest2=false&booleantest1=true"));
    setProperty(Property.SEARCH_STRING, "booleantest1=true");
    invoke();
  }

  /*
   * @testName: queryParamEntityWithConstructorTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.2; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamEntityWithConstructorTest() throws Fault {
    super.paramEntityWithConstructorTest();
  }

  /*
   * @testName: queryParamEntityWithValueOfTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.3; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamEntityWithValueOfTest() throws Fault {
    super.paramEntityWithValueOfTest();
  }

  /*
   * @testName: queryParamEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.3; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamEntityWithFromStringTest() throws Fault {
    super.paramEntityWithFromStringTest();
  }

  /*
   * @testName: queryParamSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamSetEntityWithFromStringTest() throws Fault {
    super.paramCollectionEntityWithFromStringTest(CollectionName.SET);
  }

  /*
   * @testName: queryParamSortedSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamSortedSetEntityWithFromStringTest() throws Fault {
    super.paramCollectionEntityWithFromStringTest(CollectionName.SORTED_SET);
  }

  /*
   * @testName: queryParamListEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamListEntityWithFromStringTest() throws Fault {
    super.paramCollectionEntityWithFromStringTest(CollectionName.LIST);
  }

  /*
   * @testName: queryFieldParamEntityWithConstructorTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.2; JAXRS:SPEC:6;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryFieldParamEntityWithConstructorTest() throws Fault {
    super.fieldEntityWithConstructorTest();
  }

  /*
   * @testName: queryFieldParamEntityWithValueOfTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.3; JAXRS:SPEC:6;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryFieldParamEntityWithValueOfTest() throws Fault {
    super.fieldEntityWithValueOfTest();
  }

  /*
   * @testName: queryFieldParamEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.3; JAXRS:SPEC:6;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryFieldParamEntityWithFromStringTest() throws Fault {
    super.fieldEntityWithFromStringTest();
  }

  /*
   * @testName: queryFieldParamSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:6;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryFieldParamSetEntityWithFromStringTest() throws Fault {
    super.fieldCollectionEntityWithFromStringTest(CollectionName.SET);
  }

  /*
   * @testName: queryFieldParamSortedSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:6;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryFieldParamSortedSetEntityWithFromStringTest() throws Fault {
    super.fieldCollectionEntityWithFromStringTest(CollectionName.SORTED_SET);
  }

  /*
   * @testName: queryFieldParamListEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:6;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryFieldParamListEntityWithFromStringTest() throws Fault {
    super.fieldCollectionEntityWithFromStringTest(CollectionName.LIST);
  }

  /*
   * @testName: queryParamEntityWithEncodedTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:7; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.2;
   * 
   * @test_Strategy: Verify that named QueryParam @Encoded is handled
   */
  @Test
  public void queryParamEntityWithEncodedTest() throws Fault {
    super.paramEntityWithEncodedTest();
  }

  /*
   * @testName: queryFieldParamEntityWithEncodedTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:7;
   * 
   * @test_Strategy: Verify that named QueryParam @Encoded is handled
   */
  @Test
  public void queryFieldParamEntityWithEncodedTest() throws Fault {
    super.fieldEntityWithEncodedTest();
  }

  /*
   * @testName: queryParamThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see Section 3.2.
   */
  @Test
  public void queryParamThrowingWebApplicationExceptionTest() throws Fault {
    super.paramThrowingWebApplicationExceptionTest();
  }

  /*
   * @testName: queryFieldThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:8;
   * 
   * @test_Strategy: A WebApplicationException thrown during construction of
   * field or property values using 2 or 3 above is processed directly as
   * described in section 3.3.4.
   */
  @Test
  public void queryFieldThrowingWebApplicationExceptionTest() throws Fault {
    super.fieldThrowingWebApplicationExceptionTest();
  }

  /*
   * @testName: queryParamThrowingIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see section 3.2.
   */
  @Test
  public void queryParamThrowingIllegalArgumentExceptionTest() throws Fault {
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.NOT_FOUND.name());
    super.paramThrowingIllegalArgumentExceptionTest();
  }

  /*
   * @testName: queryFieldThrowingIllegalArgumentExceptionTest
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
  public void queryFieldThrowingIllegalArgumentExceptionTest() throws Fault {
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.NOT_FOUND.name());
    super.fieldThrowingIllegalArgumentExceptionTest();
  }

  @Override
  protected String buildRequest(String param) {
    StringBuilder sb = new StringBuilder();
    sb.append(GET).append(_contextRoot).append("?");
    sb.append(param).append(HTTP11);
    return sb.toString();
  }

  @Override
  protected String getDefaultValueOfParam(String param) {
    StringBuilder sb = new StringBuilder();
    sb.append(param).append("=");
    sb.append(QueryParamTest.class.getSimpleName());
    return sb.toString();
  }

}
