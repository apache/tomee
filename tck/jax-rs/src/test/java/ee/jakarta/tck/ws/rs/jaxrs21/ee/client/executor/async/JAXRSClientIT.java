/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.jaxrs21.ee.client.executor.async;

import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.impl.TRACE;
import ee.jakarta.tck.ws.rs.ee.rs.client.asyncinvoker.Resource;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import ee.jakarta.tck.ws.rs.common.client.JdkLoggingFilter;
import ee.jakarta.tck.ws.rs.jaxrs21.ee.client.executor.ExecutorServiceChecker;

import jakarta.ws.rs.client.AsyncInvoker;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
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
public class JAXRSClientIT
    extends ee.jakarta.tck.ws.rs.ee.rs.client.asyncinvoker.JAXRSClientIT
    implements ExecutorServiceChecker {

  private static final long serialVersionUID = 21L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_jaxrs21_ee_client_executor_async_web/resource");
  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  @Deployment(testable = false, name = "jaxrs21_ee_client_executor_async_deployment")
  public static WebArchive createDeployment() throws IOException{

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_jaxrs21_ee_client_executor_async_web.war");
    archive.addClasses(TSAppConfig.class, TRACE.class,
      Resource.class);
    return archive;

  }


  /* Run test */
  // --------------------------------------------------------------------
  // ---------------------- DELETE --------------------------------------
  // --------------------------------------------------------------------
  /*
   * @testName: deleteTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void deleteTest() throws Fault {
    super.deleteTest();
  }

  @Override
  public void deleteWhileServerWaitTest() throws Fault {
    //do nothing
  }

  @Override
  public void deleteThrowsExceptionTest() throws Fault {
    //do nothing
  }

  /*
   * @testName: deleteWithStringClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void deleteWithStringClassWhileServerWaitTest()
      throws Fault {
    super.deleteWithStringClassWhileServerWaitTest();
  }

  /*
   * @testName: deleteWithResponseClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void deleteWithResponseClassWhileServerWaitTest()
      throws Fault {
    super.deleteWithResponseClassWhileServerWaitTest();
  }

  @Override
  public void deleteWithClassThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void deleteWithClassThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void deleteWithClassThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }

  /*
   * @testName: deleteWithGenericTypeStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void deleteWithGenericTypeStringWhileServerWaitTest()
      throws Fault {
    super.deleteWithGenericTypeStringWhileServerWaitTest();
  }

  /*
   * @testName: deleteWithGenericTypeResponseWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void deleteWithGenericTypeResponseWhileServerWaitTest()
      throws Fault {
    super.deleteWithGenericTypeResponseWhileServerWaitTest();
  }

  @Override
  public void deleteWithGenericTypeThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void deleteWithGenericTypeThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void deleteWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }

  /*
   * @testName: deleteWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void deleteWithCallbackWhileServerWaitTest() throws Fault {
    super.deleteWithCallbackWhileServerWaitTest();
  }

  /*
   * @testName: deleteWithCallbackStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void deleteWithCallbackStringWhileServerWaitTest()
      throws Fault {
    super.deleteWithCallbackStringWhileServerWaitTest();
  }

  @Override
  public void deleteWithCallbackStringThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }
  
  @Override
  public void deleteWithCallbackStringThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void deleteWithCallbackThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }

  // ------------------------------------------------------------------
  // ---------------------------GET------------------------------------
  // ------------------------------------------------------------------
  /*
   * @testName: getTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void getTest() throws Fault {
    super.getTest();
  }
  
  @Override
  public void getWhileServerWaitTest() throws Fault {
    //do nothing
  }

  @Override
  public void getThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  /*
   * @testName: getWithStringClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void getWithStringClassWhileServerWaitTest() throws Fault {
    super.getWithStringClassWhileServerWaitTest();
  }

  /*
   * @testName: getWithResponseClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void getWithResponseClassWhileServerWaitTest()
      throws Fault {
    super.deleteWithResponseClassWhileServerWaitTest();
  }

  @Override
  public void getWithClassThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void getWithClassThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void getWithClassThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }
  

  /*
   * @testName: getWithGenericTypeStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void getWithGenericTypeStringWhileServerWaitTest()
      throws Fault {
    super.getWithGenericTypeStringWhileServerWaitTest();
  }

  /*
   * @testName: getWithGenericTypeResponseWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void getWithGenericTypeResponseWhileServerWaitTest()
      throws Fault {
    super.getWithGenericTypeResponseWhileServerWaitTest();
  }

  @Override
  public void getWithGenericTypeThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void getWithGenericTypeThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void getWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }

  /*
   * @testName: getWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void getWithCallbackWhileServerWaitTest() throws Fault {
    super.getWithCallbackWhileServerWaitTest();
  }

  /*
   * @testName: getWithCallbackStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void getWithCallbackStringWhileServerWaitTest()
      throws Fault {
    super.getWithCallbackStringWhileServerWaitTest();
  }

  @Override
  public void getWithCallbackStringThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void getWithCallbackStringThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void getWithCallbackThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }
  

  // ------------------------------------------------------------------
  // ---------------------------HEAD-----------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: headTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP HEAD method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void headTest() throws Fault {
    super.headTest();
  }
  
  @Override
  public void headWhileServerWaitTest() throws Fault {
    //do nothing
  }

  @Override
  public void headThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  /*
   * @testName: headWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP HEAD method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void headWithCallbackWhileServerWaitTest() throws Fault {
    super.headWithCallbackWhileServerWaitTest();
  }

  @Override
  public void headWithCallbackStringThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  // ------------------------------------------------------------------
  // ---------------------------METHOD-----------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: methodTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void methodTest() throws Fault {
    super.methodTest();
  }

  @Override
  public void methodWhileServerWaitTest() throws Fault {
    //do nothing
  }

  @Override
  public void methodThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  /*
   * @testName: methodWithStringClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void methodWithStringClassWhileServerWaitTest()
      throws Fault {
    super.methodWithStringClassWhileServerWaitTest();
  }

  /*
   * @testName: methodWithResponseClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void methodWithResponseClassWhileServerWaitTest()
      throws Fault {
    super.methodWithResponseClassWhileServerWaitTest();
  }
  
  @Override
  public void methodWithClassThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void methodWithClassThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }
  
  @Override
  public void methodWithClassThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }

  /*
   * @testName: methodWithGenericTypeStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void methodWithGenericTypeStringWhileServerWaitTest()
      throws Fault {
    super.methodWithGenericTypeStringWhileServerWaitTest();
  }

  /*
   * @testName: methodWithGenericTypeResponseWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void methodWithGenericTypeResponseWhileServerWaitTest()
      throws Fault {
    super.methodWithGenericTypeResponseWhileServerWaitTest();
  }

  @Override
  public void methodWithGenericTypeThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void methodWithGenericTypeThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void methodWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }
  

  /*
   * @testName: methodWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void methodWithCallbackWhileServerWaitTest() throws Fault {
    super.methodWithCallbackWhileServerWaitTest();
  }

  /*
   * @testName: methodWithCallbackStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void methodWithCallbackStringWhileServerWaitTest()
      throws Fault {
    super.methodWithCallbackStringWhileServerWaitTest();
  }

  @Override
  public void methodWithCallbackThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void methodWithCallbackThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void methodWithCallbackThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }
  

  /*
   * @testName: methodWithEntityWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void methodWithEntityWhileServerWaitTest() throws Fault {
    super.methodWithEntityWhileServerWaitTest();
  }

  @Override
  public void methodWithEntityThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }
  

  /*
   * @testName: methodWithStringClassWithEntityWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void methodWithStringClassWithEntityWhileServerWaitTest()
      throws Fault {
    super.methodWithStringClassWithEntityWhileServerWaitTest();
  }

  /*
   * @testName: methodWithResponseClassWithEntityWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void methodWithResponseClassWithEntityWhileServerWaitTest()
      throws Fault {
    super.methodWithResponseClassWithEntityWhileServerWaitTest();
  }

  /*
   * @testName: methodWithGenericTypeStringWithEntityWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void methodWithGenericTypeStringWithEntityWhileServerWaitTest()
      throws Fault {
    super.methodWithGenericTypeStringWithEntityWhileServerWaitTest();
  }

  @Override
  public void methodWithClassWithEntityThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void methodWithClassWithEntityThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void methodWithClassWithEntityThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }
  
  /*
   * @testName: methodWithGenericTypeResponseWithEntityWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void methodWithGenericTypeResponseWithEntityWhileServerWaitTest()
      throws Fault {
    super.methodWithGenericTypeResponseWithEntityWhileServerWaitTest();
  }

  @Override
  public void methodWithGenericTypeWithEntityThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void methodWithGenericTypeWithEntityThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }
  
  @Override
  public void methodWithGenericTypeWithEntityThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }

  /*
   * @testName: methodWithCallbackWithEntityWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void methodWithCallbackWithEntityWhileServerWaitTest()
      throws Fault {
    super.methodWithCallbackWithEntityWhileServerWaitTest();
  }

  /*
   * @testName: methodWithCallbackStringWithEntityWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void methodWithCallbackStringWithEntityWhileServerWaitTest()
      throws Fault {
    super.methodWithCallbackStringWithEntityWhileServerWaitTest();
  }

  @Override
  public void methodWithCallbackWithEntityThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void methodWithCallbackWithEntityThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void methodWithCallbackWithEntityThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }
  

  // ------------------------------------------------------------------
  // ---------------------------OPTIONS--------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: optionsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void optionsTest() throws Fault {
    super.optionsTest();
  }

  @Override
  public void optionsWhileServerWaitTest() throws Fault {
    //do nothing
  }

  @Override
  public void optionsThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }
  

  /*
   * @testName: optionsWithStringClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void optionsWithStringClassWhileServerWaitTest()
      throws Fault {
    super.optionsWithStringClassWhileServerWaitTest();
  }

  /*
   * @testName: optionsWithResponseClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void optionsWithResponseClassWhileServerWaitTest()
      throws Fault {
    super.optionsWithResponseClassWhileServerWaitTest();
  }

  @Override
  public void optionsWithClassThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void optionsWithClassThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void optionsWithClassThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }
  
  /*
   * @testName: optionsWithGenericTypeStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void optionsWithGenericTypeStringWhileServerWaitTest()
      throws Fault {
    super.optionsWithGenericTypeStringWhileServerWaitTest();
  }

  /*
   * @testName: optionsWithGenericTypeResponseWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void optionsWithGenericTypeResponseWhileServerWaitTest()
      throws Fault {
    super.optionsWithGenericTypeResponseWhileServerWaitTest();
  }

  @Override
  public void optionsWithGenericTypeThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }
  
  @Override
  public void optionsWithGenericTypeThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }
  
  @Override
  public void optionsWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }

  /*
   * @testName: optionsWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void optionsWithCallbackWhileServerWaitTest()
      throws Fault {
    super.optionsWithCallbackWhileServerWaitTest();
  }

  /*
   * @testName: optionsWithStringCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void optionsWithStringCallbackWhileServerWaitTest()
      throws Fault {
    super.optionsWithStringCallbackWhileServerWaitTest();
  }

  @Override
  public void optionsWithCallbackThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void optionsWithCallbackThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void optionsWithCallbackThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }

  // ------------------------------------------------------------------
  // ---------------------------POST-----------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: postTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void postTest() throws Fault {
    super.postTest();
  }

  @Override
  public void postWhileServerWaitTest() throws Fault {
    //do nothing
  }

  @Override
  public void postThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  /*
   * @testName: postWithStringClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void postWithStringClassWhileServerWaitTest() throws Fault {
    super.postWithStringClassWhileServerWaitTest();
  }

  /*
   * @testName: postWithResponseClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void postWithResponseClassWhileServerWaitTest()
      throws Fault {
    super.postWithResponseClassWhileServerWaitTest();
  }

  @Override
  public void postWithClassThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }
  
  @Override
  public void postWithClassThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void postWithClassThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }

  /*
   * @testName: postWithGenericTypeStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void postWithGenericTypeStringWhileServerWaitTest()
      throws Fault {
    super.postWithGenericTypeStringWhileServerWaitTest();
  }

  /*
   * @testName: postWithGenericTypeResponseWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void postWithGenericTypeResponseWhileServerWaitTest()
      throws Fault {
    super.postWithGenericTypeResponseWhileServerWaitTest();
  }
  
  @Override
  public void postWithGenericTypeThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void postWithGenericTypeThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void postWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }

  /*
   * @testName: postWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void postWithCallbackWhileServerWaitTest() throws Fault {
    super.postWithCallbackWhileServerWaitTest();
  }

  @Override
  public void postWithCallbackThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void postWithCallbackThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void postWithCallbackThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }
  

  // ------------------------------------------------------------------
  // ---------------------------PUT -----------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: putTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP PUT method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void putTest() throws Fault {
    super.putTest();
  }

  @Override
  public void putWhileServerWaitTest() throws Fault {
    //do nothing
  }

  @Override
  public void putThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void putWithClassThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void putWithClassThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void putWithClassThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }

  /*
   * @testName: putWithStringClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void putWithStringClassWhileServerWaitTest() throws Fault {
    super.putWithStringClassWhileServerWaitTest();
  }

  @Override
  public void putWithGenericTypeThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void putWithGenericTypeThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void putWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }
  

  /*
   * @testName: putWithResponseClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void putWithResponseClassWhileServerWaitTest()
      throws Fault {
    super.putWithResponseClassWhileServerWaitTest();
  }

  /*
   * @testName: putWithGenericTypeStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void putWithGenericTypeStringWhileServerWaitTest()
      throws Fault {
    super.putWithGenericTypeStringWhileServerWaitTest();
  }

  /*
   * @testName: putWithGenericTypeResponseWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void putWithGenericTypeResponseWhileServerWaitTest()
      throws Fault {
    super.putWithGenericTypeResponseWhileServerWaitTest();
  }

  /*
   * @testName: putWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void putWithCallbackWhileServerWaitTest() throws Fault {
    super.putWithCallbackWhileServerWaitTest();
  }

  /*
   * @testName: putWithStringCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void putWithStringCallbackWhileServerWaitTest()
      throws Fault {
    super.putWithStringCallbackWhileServerWaitTest();
  }

  @Override
  public void putWithCallbackThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void putWithCallbackThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }
  
  @Override
  public void putWithCallbackThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }

  // ------------------------------------------------------------------
  // ---------------------------TRACE -----------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: traceTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void traceTest() throws Fault {
    super.traceTest();
  }

  @Override
  public void traceWhileServerWaitTest() throws Fault {
    //do nothing
  }

  @Override
  public void traceThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }
  
  
  /*
   * @testName: traceWithStringClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void traceWithStringClassWhileServerWaitTest() throws Fault {
    super.traceWithStringClassWhileServerWaitTest();
  }

  /*
   * @testName: traceWithResponseClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void traceWithResponseClassWhileServerWaitTest()
      throws Fault {
    super.traceWithResponseClassWhileServerWaitTest();
  }

  @Override
  public void traceWithClassThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void traceWithClassThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void traceWithClassThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }
  

  /*
   * @testName: traceWithGenericTypeStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void traceWithGenericTypeStringWhileServerWaitTest()
      throws Fault {
    super.traceWithGenericTypeStringWhileServerWaitTest();
  }

  /*
   * @testName: traceWithGenericTypeResponseWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void traceWithGenericTypeResponseWhileServerWaitTest()
      throws Fault {
    super.traceWithGenericTypeResponseWhileServerWaitTest();
  }

  @Override
  public void traceWithGenericTypeThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void traceWithGenericTypeThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void traceWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }
  

  /*
   * @testName: traceWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void traceWithCallbackWhileServerWaitTest() throws Fault {
    super.traceWithCallbackWhileServerWaitTest();
  }

  @Override
  public void traceWithCallbackThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void traceWithCallbackThrowsWebApplicationExceptionTest() throws Fault {
    //do nothing
  }

  @Override
  public void traceWithCallbackThrowsNoWebApplicationExceptionForResponseTest() throws Fault {
    //do nothing
  }
  
  
  
  /*
   * @testName: traceWithStringCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * asynchronously.
   */
  @Test
  @Disabled
  public void traceWithStringCallbackWhileServerWaitTest()
      throws Fault {
    super.traceWithStringCallbackWhileServerWaitTest();
  }

  // ///////////////////////////////////////////////////////////////////////
  // utility methods

  /**
   * Create AsyncInvoker for given resource method and start time
   */
  protected AsyncInvoker startAsyncInvokerForMethod(String methodName) {
    Client client = createClient();
    client.register(new JdkLoggingFilter(false));
    WebTarget target = client.target(getAbsoluteUrl(methodName));
    AsyncInvoker async = target.request().async();
    setStartTime();
    return async;
  }
}
