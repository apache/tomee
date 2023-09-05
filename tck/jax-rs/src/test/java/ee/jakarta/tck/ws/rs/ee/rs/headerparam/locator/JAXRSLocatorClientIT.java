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

package ee.jakarta.tck.ws.rs.ee.rs.headerparam.locator;

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
import ee.jakarta.tck.ws.rs.ee.rs.headerparam.HeaderParamTest;
import ee.jakarta.tck.ws.rs.ee.rs.headerparam.JAXRSClientIT;
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
public class JAXRSLocatorClientIT
    extends JAXRSClientIT {
  private static final long serialVersionUID = 1L;

  public JAXRSLocatorClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_rs_headerparam_locator_web/resource/locator");
  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  @Deployment(testable = false, name = "jaxrs_ee_rs_headerparam_locator_deployment")
  public static WebArchive createDeployment() throws IOException{

    InputStream inStream = JAXRSLocatorClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/headerparam/locator/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_headerparam_locator_web.war");
    archive.addClasses(TSAppConfig.class, LocatorResource.class, MiddleResource.class,
      HeaderParamTest.class,
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
   * @testName: headerParamStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.1; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3; JAXRS:JAVADOC:3; JAXRS:JAVADOC:5;
   * 
   * @test_Strategy: Client invokes HEAD on root resource at /HeaderParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void headerParamStringTest() throws Fault {
    super.headerParamStringTest();
  }

  /*
   * @testName: headerParamNoQueryTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.1; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3; JAXRS:JAVADOC:5;
   * 
   * @test_Strategy: Client invokes GET on a resource at /HeaderParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void headerParamNoQueryTest() throws Fault {
    super.headerParamNoQueryTest();
  }

  /*
   * @testName: headerParamIntTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.1; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3; JAXRS:JAVADOC:5;
   * 
   * @test_Strategy: Client invokes GET on a resource at /HeaderParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void headerParamIntTest() throws Fault {
    super.headerParamIntTest();
  }

  /*
   * @testName: headerParamDoubleTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.1; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3; JAXRS:JAVADOC:5;
   * 
   * @test_Strategy: Client invokes GET on a resource at /HeaderParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void headerParamDoubleTest() throws Fault {
    super.headerParamDoubleTest();
  }

  /*
   * @testName: headerParamFloatTest
   *
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.1; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3; JAXRS:JAVADOC:5;
   *
   * @test_Strategy: Client invokes GET on a resource at /HeaderParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void headerParamFloatTest() throws Fault {
    super.headerParamFloatTest();
  }

  /*
   * @testName: headerParamLongTest
   *
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.1; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3; JAXRS:JAVADOC:5;
   *
   * @test_Strategy: Client invokes GET on a sub resource at /HeaderParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void headerParamLongTest() throws Fault {
    super.headerParamLongTest();
  }

  /*
   * @testName: headerParamShortTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.1; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3; JAXRS:JAVADOC:5;
   * 
   * @test_Strategy: Client invokes GET on a sub resource at /HeaderParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void headerParamShortTest() throws Fault {
    super.headerParamShortTest();
  }

  /*
   * @testName: headerParamByteTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.1; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3; JAXRS:JAVADOC:5;
   * 
   * @test_Strategy: Client invokes GET on a sub resource at /HeaderParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void headerParamByteTest() throws Fault {
    super.headerParamByteTest();
  }

  /*
   * @testName: headerParamBooleanTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.1; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3; JAXRS:JAVADOC:5;
   * 
   * @test_Strategy: Client invokes GET on a sub resource at /HeaderParamTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void headerParamBooleanTest() throws Fault {
    super.headerParamBooleanTest();
  }

  /*
   * @testName: headerParamEntityWithConstructorTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.2; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3; JAXRS:JAVADOC:6; JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void headerParamEntityWithConstructorTest() throws Fault {
    super.headerParamEntityWithConstructorTest();
  }

  /*
   * @testName: headerParamEntityWithValueOfTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.3; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3; JAXRS:JAVADOC:6; JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void headerParamEntityWithValueOfTest() throws Fault {
    super.headerParamEntityWithValueOfTest();
  }

  /*
   * @testName: headerParamEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.3; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3; JAXRS:JAVADOC:6; JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void headerParamEntityWithFromStringTest() throws Fault {
    super.headerParamEntityWithFromStringTest();
  }

  /*
   * @testName: headerParamSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.4; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3; JAXRS:JAVADOC:6; JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void headerParamSetEntityWithFromStringTest() throws Fault {
    super.headerParamSetEntityWithFromStringTest();
  }

  /*
   * @testName: headerParamSortedSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.4; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3; JAXRS:JAVADOC:6; JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void headerParamSortedSetEntityWithFromStringTest() throws Fault {
    super.headerParamSortedSetEntityWithFromStringTest();
  }

  /*
   * @testName: headerParamListEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.4; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3; JAXRS:JAVADOC:6; JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void headerParamListEntityWithFromStringTest() throws Fault {
    super.headerParamListEntityWithFromStringTest();
  }

  /*
   * @testName: headerParamThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3; JAXRS:SPEC:20; JAXRS:SPEC:20.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see Section 3.2.
   */
  @Test
  public void headerParamThrowingWebApplicationExceptionTest() throws Fault {
    super.headerParamThrowingWebApplicationExceptionTest();
  }

  /*
   * @testName: headerParamThrowingIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3; JAXRS:SPEC:20; JAXRS:SPEC:20.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see section 3.2.
   */
  @Test
  public void headerParamThrowingIllegalArgumentExceptionTest() throws Fault {
    super.headerParamThrowingIllegalArgumentExceptionTest();
  }

  public void headerFieldParamEntityWithConstructorTest() throws Fault {
    //do nothing  
  }
  public void headerFieldParamEntityWithValueOfTest() throws Fault {
    //do nothing  
  }
  public void headerFieldParamEntityWithFromStringTest() throws Fault {
    //do nothing  
  }
  public void headerFieldParamSetEntityWithFromStringTest() throws Fault {
    //do nothing  
  }
  public void headerFieldParamSortedSetEntityWithFromStringTest() throws Fault {
    //do nothing  
  }
  public void headerFieldParamListEntityWithFromStringTest() throws Fault {
    //do nothing  
  }
  public void headerFieldThrowingWebApplicationExceptionTest() throws Fault {
    //do nothing  
  }
  public void headerFieldThrowingIllegalArgumentExceptionTest() throws Fault {
    //do nothing  
  }
  
  /**
   * change the GET request to POST request so that the new resources will
   * handle it
   */
  @Override
  protected String buildRequest(Request type, String... path) {
    return super.buildRequest(Request.POST, path);
  }

}
