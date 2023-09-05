/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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
package ee.jakarta.tck.ws.rs.ee.rs.queryparam.sub;

import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.ee.rs.JaxrsParamClient;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityPrototype;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingExceptionGivenByName;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingWebApplicationException;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithConstructor;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithFromString;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithValueOf;
import ee.jakarta.tck.ws.rs.ee.rs.ParamTest;
import ee.jakarta.tck.ws.rs.ee.rs.RuntimeExceptionMapper;
import ee.jakarta.tck.ws.rs.ee.rs.WebApplicationExceptionMapper;
import ee.jakarta.tck.ws.rs.ee.rs.queryparam.JAXRSClientIT;
import ee.jakarta.tck.ws.rs.ee.rs.queryparam.QueryParamTest;
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
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSSubClientIT
    extends JAXRSClientIT {

  private static final long serialVersionUID = 1L;

  public JAXRSSubClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_rs_queryparam_sub_web/resource/subresource");
  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  @Deployment(testable = false, name = "jaxrs_ee_rs_queryparam_sub_deployment")
  public static WebArchive createDeployment() throws IOException{

    InputStream inStream = JAXRSSubClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/queryparam/sub/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_queryparam_sub_web.war");
    archive.addClasses(TSAppConfig.class, SubResource.class,
      QueryParamTest.class,
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
   * JAXRS:JAVADOC:11; JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2; JAXRS:JAVADOC:22;
   * 
   * @test_Strategy: Client invokes HEAD on root resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamStringTest() throws Fault {
    super.queryParamStringTest();
  }

  /*
   * @testName: queryParamNoQueryTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:SPEC:14;
   * JAXRS:JAVADOC:11; JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2; JAXRS:JAVADOC:22;
   * 
   * @test_Strategy: Client invokes HEAD on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamNoQueryTest() throws Fault {
    super.queryParamNoQueryTest();
  }

  /*
   * @testName: queryParamIntTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:JAVADOC:11;
   * JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * JAXRS:JAVADOC:22;
   * 
   * @test_Strategy: Client invokes HEAD on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamIntTest() throws Fault {
    super.queryParamIntTest();
  }

  /*
   * @testName: queryParamDoubleTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:JAVADOC:11;
   * JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * JAXRS:JAVADOC:22;
   * 
   * @test_Strategy: Client invokes HEAD on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamDoubleTest() throws Fault {
    super.queryParamDoubleTest();
  }

  /*
   * @testName: queryParamFloatTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:JAVADOC:11;
   * JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * JAXRS:JAVADOC:22;
   *
   * @test_Strategy: Client invokes HEAD on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamFloatTest() throws Fault {
    super.queryParamFloatTest();
  }

  /*
   * @testName: queryParamLongTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:JAVADOC:11;
   * JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:JAVADOC:22; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Client invokes GET on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamLongTest() throws Fault {
    super.queryParamLongTest();
  }

  /*
   * @testName: queryParamShortTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:JAVADOC:11;
   * JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * JAXRS:JAVADOC:22;
   * 
   * @test_Strategy: Client invokes GET on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamShortTest() throws Fault {
    super.queryParamShortTest();
  }

  /*
   * @testName: queryParamByteTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:JAVADOC:11;
   * JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * JAXRS:JAVADOC:22;
   * 
   * @test_Strategy: Client invokes GET on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamByteTest() throws Fault {
    super.queryParamByteTest();
  }

  /*
   * @testName: queryParamBooleanTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.1; JAXRS:JAVADOC:11;
   * JAXRS:JAVADOC:3; JAXRS:JAVADOC:21; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * JAXRS:JAVADOC:22;
   * 
   * @test_Strategy: Client invokes GET on a sub resource at /QueryParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void queryParamBooleanTest() throws Fault {
    super.queryParamBooleanTest();
  }

  /*
   * @testName: queryParamEntityWithConstructorTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.2; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamEntityWithConstructorTest() throws Fault {
    super.queryParamEntityWithConstructorTest();
  }

  /*
   * @testName: queryParamEntityWithValueOfTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.3; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamEntityWithValueOfTest() throws Fault {
    super.queryParamEntityWithValueOfTest();
  }

  /*
   * @testName: queryParamEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.3; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamEntityWithFromStringTest() throws Fault {
    super.queryParamEntityWithFromStringTest();
  }

  /*
   * @testName: queryParamSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamSetEntityWithFromStringTest() throws Fault {
    super.queryParamSetEntityWithFromStringTest();
  }

  /*
   * @testName: queryParamSortedSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamSortedSetEntityWithFromStringTest() throws Fault {
    super.queryParamSortedSetEntityWithFromStringTest();
  }

  /*
   * @testName: queryParamListEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamListEntityWithFromStringTest() throws Fault {
    super.queryParamListEntityWithFromStringTest();
  }

  /*
   * @testName: queryFieldParamEntityWithConstructorTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.2; JAXRS:SPEC:6;
   * JAXRS:SPEC:20; JAXRS:SPEC:20.2; JAXRS:SPEC:4;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   * 
   * An implementation is only required to set the annotated field and bean
   * property values of instances created by the implementation runtime. (Check
   * the resource with resource locator is injected field properties)
   */
  @Test
  public void queryFieldParamEntityWithConstructorTest() throws Fault {
    super.queryFieldParamEntityWithConstructorTest();
  }

  /*
   * @testName: queryFieldParamEntityWithValueOfTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.3; JAXRS:SPEC:6;
   * JAXRS:SPEC:20; JAXRS:SPEC:20.2; JAXRS:SPEC:4;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   * 
   * An implementation is only required to set the annotated field and bean
   * property values of instances created by the implementation runtime. (Check
   * the resource with resource locator is injected field properties)
   */
  @Test
  public void queryFieldParamEntityWithValueOfTest() throws Fault {
    super.queryFieldParamEntityWithValueOfTest();
  }

  /*
   * @testName: queryFieldParamEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.3; JAXRS:SPEC:6;
   * JAXRS:SPEC:20; JAXRS:SPEC:20.2; JAXRS:SPEC:4;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   * 
   * An implementation is only required to set the annotated field and bean
   * property values of instances created by the implementation runtime. (Check
   * the resource with resource locator is injected field properties)
   */
  @Test
  public void queryFieldParamEntityWithFromStringTest() throws Fault {
    super.queryFieldParamEntityWithFromStringTest();
  }

  /*
   * @testName: queryFieldParamSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:6;
   * JAXRS:SPEC:20; JAXRS:SPEC:20.2; JAXRS:SPEC:4;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryFieldParamSetEntityWithFromStringTest() throws Fault {
    super.queryFieldParamSetEntityWithFromStringTest();
  }

  /*
   * @testName: queryFieldParamSortedSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:6;
   * JAXRS:SPEC:20; JAXRS:SPEC:20.2; JAXRS:SPEC:4;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   * 
   * An implementation is only required to set the annotated field and bean
   * property values of instances created by the implementation runtime. (Check
   * the resource with resource locator is injected field properties)
   */
  @Test
  public void queryFieldParamSortedSetEntityWithFromStringTest() throws Fault {
    super.queryFieldParamSortedSetEntityWithFromStringTest();
  }

  /*
   * @testName: queryFieldParamListEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:6;
   * JAXRS:SPEC:20; JAXRS:SPEC:20.2; JAXRS:SPEC:4;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   * 
   * An implementation is only required to set the annotated field and bean
   * property values of instances created by the implementation runtime. (Check
   * the resource with resource locator is injected field properties)
   */
  @Test
  public void queryFieldParamListEntityWithFromStringTest() throws Fault {
    super.queryFieldParamListEntityWithFromStringTest();
  }

  /*
   * @testName: queryParamEntityWithEncodedTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:7; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.2; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Verify that named QueryParam @Encoded is handled
   */
  @Test
  public void queryParamEntityWithEncodedTest() throws Fault {
    super.queryParamEntityWithEncodedTest();
  }

  /*
   * @testName: queryFieldParamEntityWithEncodedTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:7; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2; JAXRS:SPEC:4;
   * 
   * @test_Strategy: Verify that named QueryParam @Encoded is handled
   * 
   * An implementation is only required to set the annotated field and bean
   * property values of instances created by the implementation runtime. (Check
   * the resource with resource locator is injected field properties)
   */
  @Test
  public void queryFieldParamEntityWithEncodedTest() throws Fault {
    super.queryFieldParamEntityWithEncodedTest();
  }

  /*
   * @testName: queryParamThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see Section 3.2.
   */
  @Test
  public void queryParamThrowingWebApplicationExceptionTest() throws Fault {
    super.queryParamThrowingWebApplicationExceptionTest();
  }

  /*
   * @testName: queryFieldThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:8; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2; JAXRS:SPEC:4;
   * 
   * @test_Strategy: A WebApplicationException thrown during construction of
   * field or property values using 2 or 3 above is processed directly as
   * described in section 3.3.4.
   * 
   * An implementation is only required to set the annotated field and bean
   * property values of instances created by the implementation runtime. (Check
   * the resource with resource locator is injected field properties)
   */
  @Test
  public void queryFieldThrowingWebApplicationExceptionTest() throws Fault {
    super.queryFieldThrowingWebApplicationExceptionTest();
  }

  /*
   * @testName: queryParamThrowingIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see section 3.2.
   * 
   * An implementation is only required to set the annotated field and bean
   * property values of instances created by the implementation runtime. (Check
   * the resource with resource locator is injected field properties)
   */
  @Test
  public void queryParamThrowingIllegalArgumentExceptionTest() throws Fault {
    super.queryParamThrowingIllegalArgumentExceptionTest();
  }

  /*
   * @testName: queryFieldThrowingIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:9; JAXRS:SPEC:9.1; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2; JAXRS:SPEC:4; JAXRS:SPEC:10;
   * 
   * @test_Strategy: Other exceptions thrown during construction of field or
   * property values using 2 or 3 above are treated as client errors:
   * 
   * if the field or property is annotated with @MatrixParam,
   * 
   * @QueryParam or @PathParam then an implementation MUST generate a
   * WebApplicationException that wraps the thrown exception with a not found
   * response (404 status) and no entity;
   *
   * An implementation is only required to set the annotated field and bean
   * property values of instances created by the implementation runtime. (Check
   * the resource with resource locator is injected field properties)
   */
  @Test
  public void queryFieldThrowingIllegalArgumentExceptionTest() throws Fault {
    super.queryFieldThrowingIllegalArgumentExceptionTest();
  }

}
