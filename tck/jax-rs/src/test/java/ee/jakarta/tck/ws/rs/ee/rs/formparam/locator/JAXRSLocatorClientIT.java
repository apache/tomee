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

package ee.jakarta.tck.ws.rs.ee.rs.formparam.locator;

import ee.jakarta.tck.ws.rs.common.AbstractMessageBodyRW;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityPrototype;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingExceptionGivenByName;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingWebApplicationException;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithConstructor;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithFromString;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithValueOf;
import ee.jakarta.tck.ws.rs.ee.rs.RuntimeExceptionMapper;
import ee.jakarta.tck.ws.rs.ee.rs.WebApplicationExceptionMapper;
import ee.jakarta.tck.ws.rs.ee.rs.formparam.FormParamTest;
import ee.jakarta.tck.ws.rs.ee.rs.formparam.JAXRSClientIT;

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
public class JAXRSLocatorClientIT extends JAXRSClientIT {

  public JAXRSLocatorClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_formparam_locator_web/resource/locator");
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

  @Deployment(testable = false, name = "jaxrs_ee_formparam_locator_deployment")
  public static WebArchive createDeployment() throws IOException{

    InputStream inStream = JAXRSLocatorClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/formparam/locator/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_formparam_locator_web.war");
    archive.addClasses(TSAppConfig.class, MiddleResource.class, LocatorResource.class,
      FormParamTest.class,
      AbstractMessageBodyRW.class,
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
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3;
   * 
   * @test_Strategy: Test sending no content;
   */
  @Test
  public void nonDefaultFormParamNothingSentTest() throws Fault {
    super.nonDefaultFormParamNothingSentTest();
  }

  /*
   * @testName: nonDefaultFormParamValueOfTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3;
   * 
   * @test_Strategy: Test creating a ParamEntityWithValueOf from sending a
   * String;
   */
  @Test
  public void nonDefaultFormParamValueOfTest() throws Fault {
    super.nonDefaultFormParamValueOfTest();
  }

  /*
   * @testName: nonDefaultFormParamFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3;
   * 
   * @test_Strategy: Test creating a ParamEntityWithFromString from sending a
   * String;
   */
  @Test
  public void nonDefaultFormParamFromStringTest() throws Fault {
    _contextRoot += "encoded";
    super.nonDefaultFormParamFromStringTest();
  }

  /*
   * @testName: nonDefaultFormParamFromConstructorTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3;
   * 
   * @test_Strategy: Test creating a ParamEntityWithConstructor from sending a
   * String;
   */
  @Test
  public void nonDefaultFormParamFromConstructorTest() throws Fault {
    super.nonDefaultFormParamFromConstructorTest();
  }

  /*
   * @testName: nonDefaultFormParamFromListConstructorTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3;
   * 
   * @test_Strategy: Test creating a ParamEntityWithConstructor from sending a
   * String;
   */
  @Test
  public void nonDefaultFormParamFromListConstructorTest() throws Fault {
    super.nonDefaultFormParamFromListConstructorTest();
  }

  /*
   * @testName: nonDefaultFormParamFromSetFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3;
   * 
   * @test_Strategy: Test creating a ParamEntityWithListConstructor from sending
   * a String;
   */
  @Test
  public void nonDefaultFormParamFromSetFromStringTest() throws Fault {
    _contextRoot += "encoded";
    super.nonDefaultFormParamFromSetFromStringTest();
  }

  /*
   * @testName: nonDefaultFormParamFromSortedSetFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3;
   * 
   * @test_Strategy: Test creating a ParamEntityWithListConstructor from sending
   * a String;
   */
  @Test
  public void nonDefaultFormParamFromSortedSetFromStringTest() throws Fault {
    _contextRoot += "encoded";
    super.nonDefaultFormParamFromSortedSetFromStringTest();
  }

  /*
   * @testName: nonDefaultFormParamFromListFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3;
   * 
   * @test_Strategy: Test creating a ParamEntityWithListConstructor from sending
   * a String;
   */
  @Test
  public void nonDefaultFormParamFromListFromStringTest() throws Fault {
    _contextRoot += "encoded";
    super.nonDefaultFormParamFromListFromStringTest();
  }

  /*
   * @assertion_ids: JAXRS:SPEC:7; JAXRS:SPEC:12;JAXRS:SPEC:12.2; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.3;
   * 
   * @test_Strategy: Verify that named FormParam @Encoded is handled
   */
  public void formParamEntityWithEncodedTest() throws Fault {
    _contextRoot += "encoded";
    super.paramEntityWithEncodedTest();
  }

  public void defaultFormParamSentTest() throws Fault {
    //do nothing  
  }
  public void defaultFormParamNoArgSentTest() throws Fault {
    //do nothing  
  }
  public void defaultFormParamPutNoArgSentTest() throws Fault {
    //do nothing  
  }
  public void defaultFormParamPutArgSentTest() throws Fault {
    //do nothing  
  }
  public void defaultFormParamValueOfTest() throws Fault {
    //do nothing  
  }
  public void defaultFormParamFromStringTest() throws Fault {
    //do nothing  
  }
  public void defaultFormParamFromConstructorTest() throws Fault {
    //do nothing  
  }
  public void defaultFormParamFromListConstructorTest() throws Fault {
    //do nothing  
  }
  public void defaultFormParamFromSetFromStringTest() throws Fault {
    //do nothing  
  }
  public void defaultFormParamFromSortedSetFromStringTest() throws Fault {
    //do nothing  
  }
  public void defaultFormParamFromListFromStringTest() throws Fault {
    //do nothing  
  }
  public void formParamThrowingWebApplicationExceptionTest() throws Fault {
    //do nothing  
  }
  public void formParamThrowingIllegalArgumentExceptionTest() throws Fault {
    //do nothing  
  }


}
