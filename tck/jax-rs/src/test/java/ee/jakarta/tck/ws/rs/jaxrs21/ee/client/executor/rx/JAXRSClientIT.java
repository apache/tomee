/*
 * Copyright (c) 2017, 2022 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.jaxrs21.ee.client.executor.rx;

import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.impl.TRACE;
import ee.jakarta.tck.ws.rs.jaxrs21.ee.client.rxinvoker.Resource;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import ee.jakarta.tck.ws.rs.common.client.JdkLoggingFilter;
import ee.jakarta.tck.ws.rs.jaxrs21.ee.client.executor.ExecutorServiceChecker;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
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
/**
 * @since 2.1
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT
    extends ee.jakarta.tck.ws.rs.jaxrs21.ee.client.rxinvoker.JAXRSClientIT
    implements ExecutorServiceChecker {

  private static final long serialVersionUID = 21L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_jaxrs21_ee_client_executor_rx_web/resource");
  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  @Deployment(testable = false, name = "jaxrs21_ee_client_executor_rx_deployment")
  public static WebArchive createDeployment() throws IOException{

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_jaxrs21_ee_client_executor_rx_web.war");
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
   * @test_Strategy: Invoke HTTP DELETE method for the current request.
   */
  @Test
  public void deleteTest() throws Fault {
    super.deleteTest();
  }

  /*
   * @testName: deleteWithStringClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * 
   */
  @Test
  @Disabled
  public void deleteWithStringClassTest() throws Fault {
    super.deleteWithStringClassTest();
  }

  /*
   * @testName: deleteWithResponseClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * 
   */
  @Test
  @Disabled
  public void deleteWithResponseClassTest() throws Fault {
    super.deleteWithResponseClassTest();
  }

  /*
   * @testName: deleteWithGenericTypeStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   */
  @Test
  @Disabled
  public void deleteWithGenericTypeStringTest() throws Fault {
    super.deleteWithGenericTypeStringTest();
  }

  /*
   * @testName: deleteWithGenericTypeResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   */
  @Test
  @Disabled
  public void deleteWithGenericTypeResponseTest() throws Fault {
    super.deleteWithGenericTypeResponseTest();
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
   */
  @Test
  @Disabled
  public void getTest() throws Fault {
    super.getTest();
  }

  /*
   * @testName: getWithStringClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   */
  @Test
  @Disabled
  public void getWithStringClassTest() throws Fault {
    super.getWithStringClassTest();
  }

  /*
   * @testName: getWithResponseClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   */
  @Test
  @Disabled
  public void getWithResponseClassTest() throws Fault {
    super.getWithResponseClassTest();
  }

  /*
   * @testName: getWithGenericTypeStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   */
  @Test
  @Disabled
  public void getWithGenericTypeStringTest() throws Fault {
    super.getWithGenericTypeStringTest();
  }

  /*
   * @testName: getWithGenericTypeResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   */
  @Test
  @Disabled
  public void getWithGenericTypeResponseTest() throws Fault {
    super.getWithGenericTypeResponseTest();
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
   */
  @Test
  @Disabled
  public void headTest() throws Fault {
    super.headTest();
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
   */
  @Test
  public void optionsTest() throws Fault {
    super.optionsTest();
  }

  /*
   * @testName: optionsWithStringClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   */
  @Test
  @Disabled
  public void optionsWithStringClassTest() throws Fault {
    super.optionsWithStringClassTest();
  }

  /*
   * @testName: optionsWithResponseClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   */
  @Test
  @Disabled
  public void optionsWithResponseClassTest() throws Fault {
    super.optionsWithResponseClassTest();
  }

  /*
   * @testName: optionsWithGenericTypeStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   */
  @Test
  @Disabled
  public void optionsWithGenericTypeStringTest() throws Fault {
    super.optionsWithGenericTypeStringTest();
  }

  /*
   * @testName: optionsWithGenericTypeResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   */
  @Test
  @Disabled
  public void optionsWithGenericTypeResponseTest() throws Fault {
    super.optionsWithGenericTypeResponseTest();
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
   */
  @Test
  @Disabled
  public void postTest() throws Fault {
    super.postTest();
  }

  /*
   * @testName: postWithStringClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   */
  @Test
  @Disabled
  public void postWithStringClassTest() throws Fault {
    super.postWithStringClassTest();
  }

  /*
   * @testName: postWithResponseClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   */
  @Test
  @Disabled
  public void postWithResponseClassTest() throws Fault {
    super.postWithResponseClassTest();
  }

  /*
   * @testName: postWithGenericTypeStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   */
  @Test
  @Disabled
  public void postWithGenericTypeStringTest() throws Fault {
    super.postWithGenericTypeStringTest();
  }

  /*
   * @testName: postWithGenericTypeResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   */
  @Test
  @Disabled
  public void postWithGenericTypeResponseTest() throws Fault {
    super.postWithGenericTypeResponseTest();
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
   */
  @Test
  @Disabled
  public void putTest() throws Fault {
    super.putTest();
  }

  /*
   * @testName: putWithStringClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   */
  @Test
  @Disabled
  public void putWithStringClassTest() throws Fault {
    super.putWithStringClassTest();
  }

  /*
   * @testName: putWithResponseClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   */
  @Test
  @Disabled
  public void putWithResponseClassTest() throws Fault {
    super.putWithResponseClassTest();
  }

  /*
   * @testName: putWithGenericTypeStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   */
  @Test
  @Disabled
  public void putWithGenericTypeStringTest() throws Fault {
    super.putWithGenericTypeStringTest();
  }

  /*
   * @testName: putWithGenericTypeResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   */
  @Test
  @Disabled
  public void putWithGenericTypeResponseTest() throws Fault {
    super.putWithGenericTypeResponseTest();
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
   */
  @Test
  @Disabled
  public void traceTest() throws Fault {
    super.traceTest();
  }

  /*
   * @testName: traceWithStringClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   */
  @Test
  @Disabled
  public void traceWithStringClassTest() throws Fault {
    super.traceWithStringClassTest();
  }

  /*
   * @testName: traceWithResponseClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   */
  @Test
  @Disabled
  public void traceWithResponseClassTest() throws Fault {
    super.traceWithResponseClassTest();
  }

  /*
   * @testName: traceWithGenericTypeStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   */
  @Test
  @Disabled
  public void traceWithGenericTypeStringTest() throws Fault {
    super.traceWithGenericTypeStringTest();
  }

  /*
   * @testName: traceWithGenericTypeResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1131;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   */
  @Test
  @Disabled
  public void traceWithGenericTypeResponseTest() throws Fault {
    super.traceWithGenericTypeResponseTest();
  }

  @Override
  public void deleteThrowsExceptionTest() throws Fault {
    //do nothing
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
  @Override
  public void getThrowsProcessingExceptionTest() throws Fault {
    //do nothing
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
  @Override
  public void headThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }
  @Override
  public void methodTest() throws Fault {
    //do nothing
  }
  @Override
  public void methodThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }
  @Override
  public void methodWithStringClassTest() throws Fault {
    //do nothing
  }
  @Override
  public void methodWithResponseClassTest() throws Fault {
    //do nothing
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
  @Override
  public void methodWithGenericTypeStringTest() throws Fault {
    //do nothing
  }
  @Override
  public void methodWithGenericTypeResponseTest() throws Fault {
    //do nothing
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
  @Override
  public void methodWithEntityTest() throws Fault {
    //do nothing
  }
  @Override
  public void methodWithEntityThrowsProcessingExceptionTest() throws Fault {
    //do nothing
  }
  @Override
  public void methodWithStringClassWithEntityTest() throws Fault {
    //do nothing
  }
  @Override
  public void methodWithResponseClassWithEntityTest() throws Fault {
    //do nothing
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
  @Override
  public void methodWithGenericTypeStringWithEntityTest() throws Fault {
    //do nothing
  }
  @Override
  public void methodWithGenericTypeResponseWithEntityTest() throws Fault {
    //do nothing
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
  @Override
  public void optionsThrowsProcessingExceptionTest() throws Fault {
    //do nothing
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
  @Override
  public void postThrowsProcessingExceptionTest() throws Fault {
    //do nothing
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
  @Override
  public void traceThrowsProcessingExceptionTest() throws Fault {
    //do nothing
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
  

  // ///////////////////////////////////////////////////////////////////////
  // utility methods

  protected Invocation.Builder startBuilderForMethod(String methodName) {
    Client client = createClient();
    client.register(new JdkLoggingFilter(false));
    WebTarget target = client.target(getAbsoluteUrl(methodName));
    Invocation.Builder ib = target.request();
    return ib;
  }
}
