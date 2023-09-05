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

package ee.jakarta.tck.ws.rs.ee.rs.formparam.sub;

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
public class JAXRSSubClientIT extends JAXRSClientIT {

  public JAXRSSubClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_formparam_sub_web/resource/sub");
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

  @Deployment(testable = false, name = "jaxrs_ee_formparam_sub_deployment")
  public static WebArchive createDeployment() throws IOException{

    InputStream inStream = JAXRSSubClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/formparam/sub/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_formparam_sub_web.war");
    archive.addClasses(TSAppConfig.class, SubResource.class,
      FormParamTest.class,
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
   * JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test sending no content;
   */
  @Test
  public void nonDefaultFormParamNothingSentTest() throws Fault {
    super.nonDefaultFormParamNothingSentTest();
  }

  /*
   * @testName: defaultFormParamSentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test sending override of default argument content;
   */
  @Test
  public void defaultFormParamSentTest() throws Fault {
    super.defaultFormParamSentTest();
  }

  /*
   * @testName: defaultFormParamNoArgSentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test sending no argument content, receiving default;
   */
  @Test
  public void defaultFormParamNoArgSentTest() throws Fault {
    super.defaultFormParamNoArgSentTest();
  }

  /*
   * @testName: defaultFormParamPutNoArgSentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test sending no argument content, PUT, receiving default;
   */
  @Test
  public void defaultFormParamPutNoArgSentTest() throws Fault {
    super.defaultFormParamPutNoArgSentTest();
  }

  /*
   * @testName: defaultFormParamPutArgSentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test sending argument content, PUT;
   */
  @Test
  public void defaultFormParamPutArgSentTest() throws Fault {
    super.defaultFormParamPutArgSentTest();
  }

  /*
   * @testName: defaultFormParamValueOfTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test creating a ParamEntityWithValueOf from default;
   */
  @Test
  public void defaultFormParamValueOfTest() throws Fault {
    super.defaultFormParamValueOfTest();
  }

  /*
   * @testName: nonDefaultFormParamValueOfTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test creating a ParamEntityWithValueOf from sending a
   * String;
   */
  @Test
  public void nonDefaultFormParamValueOfTest() throws Fault {
    super.nonDefaultFormParamValueOfTest();
  }

  /*
   * @testName: defaultFormParamFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test creating a ParamEntityWithFromString from default;
   */
  @Test
  public void defaultFormParamFromStringTest() throws Fault {
    super.defaultFormParamFromStringTest();
  }

  /*
   * @testName: nonDefaultFormParamFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test creating a ParamEntityWithFromString from sending a
   * String;
   */
  @Test
  public void nonDefaultFormParamFromStringTest() throws Fault {
    super.nonDefaultFormParamFromStringTest();
  }

  /*
   * @testName: defaultFormParamFromConstructorTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test creating a ParamEntityWithFromString from default;
   */
  @Test
  public void defaultFormParamFromConstructorTest() throws Fault {
    super.defaultFormParamFromConstructorTest();
  }

  /*
   * @testName: nonDefaultFormParamFromConstructorTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test creating a ParamEntityWithConstructor from sending a
   * String;
   */
  @Test
  public void nonDefaultFormParamFromConstructorTest() throws Fault {
    super.nonDefaultFormParamFromConstructorTest();
  }

  /*
   * @testName: defaultFormParamFromListConstructorTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test creating a ParamEntityWithConstructor from default;
   */
  @Test
  public void defaultFormParamFromListConstructorTest() throws Fault {
    super.defaultFormParamFromListConstructorTest();
  }

  /*
   * @testName: nonDefaultFormParamFromListConstructorTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test creating a ParamEntityWithConstructor from sending a
   * String;
   */
  @Test
  public void nonDefaultFormParamFromListConstructorTest() throws Fault {
    super.nonDefaultFormParamFromListConstructorTest();
  }

  /*
   * @testName: defaultFormParamFromSetFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test creating a ParamEntityWithFromString from default;
   */
  @Test
  public void defaultFormParamFromSetFromStringTest() throws Fault {
    super.defaultFormParamFromSetFromStringTest();
  }

  /*
   * @testName: nonDefaultFormParamFromSetFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test creating a ParamEntityWithListConstructor from sending
   * a String;
   */
  @Test
  public void nonDefaultFormParamFromSetFromStringTest() throws Fault {
    super.nonDefaultFormParamFromSetFromStringTest();
  }

  /*
   * @testName: defaultFormParamFromSortedSetFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test creating a ParamEntityWithFromString from default;
   */
  @Test
  public void defaultFormParamFromSortedSetFromStringTest() throws Fault {
    super.defaultFormParamFromSortedSetFromStringTest();
  }

  /*
   * @testName: nonDefaultFormParamFromSortedSetFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test creating a ParamEntityWithListConstructor from sending
   * a String;
   */
  @Test
  public void nonDefaultFormParamFromSortedSetFromStringTest() throws Fault {
    super.nonDefaultFormParamFromSortedSetFromStringTest();
  }

  /*
   * @testName: defaultFormParamFromListFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:12.1;
   * JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test creating a ParamEntityWithFromString from default;
   * 
   */
  @Test
  public void defaultFormParamFromListFromStringTest() throws Fault {
    super.defaultFormParamFromListFromStringTest();
  }

  /*
   * @testName: nonDefaultFormParamFromListFromStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:4; JAXRS:SPEC:12; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Test creating a ParamEntityWithListConstructor from sending
   * a String;
   */
  @Test
  public void nonDefaultFormParamFromListFromStringTest() throws Fault {
    super.nonDefaultFormParamFromListFromStringTest();
  }

  /*
   * @assertion_ids: JAXRS:SPEC:7; JAXRS:SPEC:12;JAXRS:SPEC:12.2; JAXRS:SPEC:20;
   * JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Verify that named FormParam @Encoded is handled
   */
  public void formParamEntityWithEncodedTest() throws Fault {
    super.paramEntityWithEncodedTest();
  }

  /*
   * @testName: formParamThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see Section 3.2.
   */
  @Test
  public void formParamThrowingWebApplicationExceptionTest() throws Fault {
    super.formParamThrowingIllegalArgumentExceptionTest();
  }

  /*
   * @testName: formParamThrowingIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3; JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see section 3.2. Exceptions thrown during
   * construction of @FormParam annotated parameter values are treated the same
   * as if the parameter were annotated with @HeaderParam.
   */
  @Test
  public void formParamThrowingIllegalArgumentExceptionTest() throws Fault {
    super.formParamThrowingIllegalArgumentExceptionTest();
  }
}
