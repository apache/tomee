/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.client.asyncinvoker;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.impl.TRACE;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.common.client.JdkLoggingFilter;

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

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.AsyncInvoker;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  private static final long serialVersionUID = -696868584437674095L;

  protected long millis;

  protected int callbackResult = 0;

  protected Throwable callbackException = null;

  private final static String NONEXISTING_SITE = "somenonexisting.domain-site";

  public JAXRSClientIT() {
    setup();
    setContextRoot("jaxrs_ee_rs_client_asyncinvoker_web/resource");
  }

  static final String[] METHODS = { "DELETE", "GET", "OPTIONS" };

  static final String[] ENTITY_METHODS = { "PUT", "POST" };

  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/client/asyncinvoker/web.xml.template");
    // Replace the servlet_adaptor in web.xml.template with the System variable set as servlet adaptor
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_client_asyncinvoker_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class,
      StringBean.class,
      TRACE.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  /* Run test */
  // --------------------------------------------------------------------
  // ---------------------- DELETE --------------------------------------
  // --------------------------------------------------------------------
  /*
   * @testName: deleteTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:375;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   */
  @Test
  public void deleteTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("delete");
    Future<Response> future = async.delete();
    checkFutureOkResponseNoTime(future);
    //return future;
  }

  /*
   * @testName: deleteWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:375;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   */
  @Test
  public void deleteWhileServerWaitTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("deleteandwait");
    Future<Response> future = async.delete();
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: deleteThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:375;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void deleteThrowsExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("delete");
    Future<Response> future = async.delete();
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: deleteWithStringClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:376;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   */
  @Test
  public void deleteWithStringClassWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("deleteandwait");
    Future<String> future = async.delete(String.class);
    checkFutureString(future, "delete");
    //return future;
  }

  /*
   * @testName: deleteWithResponseClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:376;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   */
  @Test
  public void deleteWithResponseClassWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("deleteandwait");
    Future<Response> future = async.delete(Response.class);
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: deleteWithClassThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:376;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void deleteWithClassThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("delete");
    Future<String> future = async.delete(String.class);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: deleteWithClassThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:376;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void deleteWithClassThrowsWebApplicationExceptionTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("deletenotok");
    Future<String> future = async.delete(String.class);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: deleteWithClassThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:376;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void deleteWithClassThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("deletenotok");
    Future<Response> future = async.delete(Response.class);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: deleteWithGenericTypeStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:377;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   */
  @Test
  public void deleteWithGenericTypeStringWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("deleteandwait");
    GenericType<String> generic = createGeneric(String.class);
    Future<String> future = async.delete(generic);
    checkFutureString(future, "delete");
    //return future;
  }

  /*
   * @testName: deleteWithGenericTypeResponseWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:377;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   */
  @Test
  public void deleteWithGenericTypeResponseWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("deleteandwait");
    GenericType<Response> generic = createGeneric(Response.class);
    Future<Response> future = async.delete(generic);
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: deleteWithGenericTypeThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:377;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void deleteWithGenericTypeThrowsProcessingExceptionTest()
      throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("delete");
    GenericType<String> generic = createGeneric(String.class);
    Future<String> future = async.delete(generic);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: deleteWithGenericTypeThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:377;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void deleteWithGenericTypeThrowsWebApplicationExceptionTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("deletenotok");
    GenericType<String> generic = createGeneric(String.class);
    Future<String> future = async.delete(generic);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName:
   * deleteWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:377;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void deleteWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("deletenotok");
    GenericType<Response> generic = createGeneric(Response.class);
    Future<Response> future = async.delete(generic);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: deleteWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:378;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   */
  @Test
  public void deleteWithCallbackWhileServerWaitTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("deleteandwait");
    InvocationCallback<Response> callback = createCallback(true);
    Future<Response> future = async.delete(callback);
    checkFutureOkResponse(future);
    assertCallbackCall();
    //return future;
  }

  /*
   * @testName: deleteWithCallbackStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:378;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   */
  @Test
  public void deleteWithCallbackStringWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("deleteandwait");
    InvocationCallback<String> callback = createStringCallback(true);
    Future<String> future = async.delete(callback);
    checkFutureString(future, "delete");
    assertCallbackCall();
    //return future;
  }

  /*
   * @testName: deleteWithCallbackStringThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:378;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void deleteWithCallbackStringThrowsProcessingExceptionTest()
      throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("deleteandwait");
    InvocationCallback<String> callback = createStringCallback(false);
    Future<String> future = async.delete(callback);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: deleteWithCallbackStringThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:378;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void deleteWithCallbackStringThrowsWebApplicationExceptionTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("deletenotok");
    InvocationCallback<String> callback = createStringCallback(false);
    Future<String> future = async.delete(callback);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: deleteWithCallbackThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:378;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void deleteWithCallbackThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("deletenotok");
    InvocationCallback<Response> callback = createCallback(false);
    Future<Response> future = async.delete(callback);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  // ------------------------------------------------------------------
  // ---------------------------GET------------------------------------
  // ------------------------------------------------------------------
  /*
   * @testName: getTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:379;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * asynchronously.
   */
  @Test
  public void getTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("get");
    Future<Response> future = async.get();
    checkFutureOkResponseNoTime(future);
    //return future;
  }

  /*
   * @testName: getWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:379;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * asynchronously.
   */
  @Test
  public void getWhileServerWaitTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("getandwait");
    Future<Response> future = async.get();
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: getThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:379;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void getThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("get");
    Future<Response> future = async.get();
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: getWithStringClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:380;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * asynchronously.
   */
  @Test
  public void getWithStringClassWhileServerWaitTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("getandwait");
    Future<String> future = async.get(String.class);
    checkFutureString(future, "get");
    //return future;
  }

  /*
   * @testName: getWithResponseClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:380;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * asynchronously.
   */
  @Test
  public void getWithResponseClassWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("getandwait");
    Future<Response> future = async.get(Response.class);
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: getWithClassThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:380;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void getWithClassThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("get");
    Future<String> future = async.get(String.class);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: getWithClassThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:380;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void getWithClassThrowsWebApplicationExceptionTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("getnotok");
    Future<String> future = async.get(String.class);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: getWithClassThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:380;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void getWithClassThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("getnotok");
    Future<Response> future = async.get(Response.class);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: getWithGenericTypeStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:381;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * asynchronously.
   */
  @Test
  public void getWithGenericTypeStringWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("getandwait");
    GenericType<String> generic = createGeneric(String.class);
    Future<String> future = async.get(generic);
    checkFutureString(future, "get");
    //return future;
  }

  /*
   * @testName: getWithGenericTypeResponseWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:381;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * asynchronously.
   */
  @Test
  public void getWithGenericTypeResponseWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("getandwait");
    GenericType<Response> generic = createGeneric(Response.class);
    Future<Response> future = async.get(generic);
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: getWithGenericTypeThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:381;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void getWithGenericTypeThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("get");
    GenericType<String> generic = createGeneric(String.class);
    Future<String> future = async.get(generic);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: getWithGenericTypeThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:381;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void getWithGenericTypeThrowsWebApplicationExceptionTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("getnotok");
    GenericType<String> generic = createGeneric(String.class);
    Future<String> future = async.get(generic);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: getWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:381;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void getWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("getnotok");
    GenericType<Response> generic = createGeneric(Response.class);
    Future<Response> future = async.get(generic);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: getWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:382;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * asynchronously.
   */
  @Test
  public void getWithCallbackWhileServerWaitTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("getandwait");
    InvocationCallback<Response> callback = createCallback(true);
    Future<Response> future = async.get(callback);
    checkFutureOkResponse(future);
    assertCallbackCall();
    //return future;
  }

  /*
   * @testName: getWithCallbackStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:382;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * asynchronously.
   */
  @Test
  public void getWithCallbackStringWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("getandwait");
    InvocationCallback<String> callback = createStringCallback(true);
    Future<String> future = async.get(callback);
    checkFutureString(future, "get");
    assertCallbackCall();
    //return future;
  }

  /*
   * @testName: getWithCallbackStringThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:382;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void getWithCallbackStringThrowsProcessingExceptionTest()
      throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("get");
    InvocationCallback<String> callback = createStringCallback(false);
    Future<String> future = async.get(callback);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: getWithCallbackStringThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:382;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void getWithCallbackStringThrowsWebApplicationExceptionTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("getnotok");
    InvocationCallback<String> callback = createStringCallback(false);
    Future<String> future = async.get(callback);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: getWithCallbackThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:382;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void getWithCallbackThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("getnotok");
    InvocationCallback<Response> callback = createCallback(false);
    Future<Response> future = async.get(callback);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  // ------------------------------------------------------------------
  // ---------------------------HEAD-----------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: headTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:383;
   * 
   * @test_Strategy: Invoke HTTP HEAD method for the current request
   * asynchronously.
   */
  @Test
  public void headTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("head");
    Future<Response> future = async.head();
    checkFutureOkResponseNoTime(future);
    //return future;
  }

  /*
   * @testName: headWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:383;
   * 
   * @test_Strategy: Invoke HTTP HEAD method for the current request
   * asynchronously.
   */
  @Test
  public void headWhileServerWaitTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("headandwait");
    Future<Response> future = async.head();
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: headThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:383;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void headThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("head");
    Future<Response> future = async.head();
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: headWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:384;
   * 
   * @test_Strategy: Invoke HTTP HEAD method for the current request
   * asynchronously.
   */
  @Test
  public void headWithCallbackWhileServerWaitTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("headandwait");
    InvocationCallback<Response> callback = createCallback(true);
    Future<Response> future = async.head(callback);
    checkFutureOkResponse(future);
    assertCallbackCall();
    //return future;
  }

  /*
   * @testName: headWithCallbackStringThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:384;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void headWithCallbackStringThrowsProcessingExceptionTest()
      throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("head");
    InvocationCallback<Response> callback = createCallback(false);
    Future<Response> future = async.head(callback);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  // ------------------------------------------------------------------
  // ---------------------------METHOD-----------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: methodTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:385;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  public void methodTest() throws Fault {
    Future<Response> future = null;
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(method.toLowerCase());
      future = async.method(method);
      checkFutureOkResponseNoTime(future);
    }
    //return future;
  }

  /*
   * @testName: methodWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:385;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  public void methodWhileServerWaitTest() throws Fault {
    Future<Response> future = null;
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "andwait");
      future = async.method(method);
      checkFutureOkResponse(future);
    }
    //return future;
  }

  /*
   * @testName: methodThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:385;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void methodThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    Future<Response> future = null;
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(method.toLowerCase());
      future = async.method(method);
      assertExceptionWithProcessingExceptionIsThrownAndLog(future);
    }
  }

  /*
   * @testName: methodWithStringClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:386;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  public void methodWithStringClassWhileServerWaitTest()
      throws Fault {
    Future<String> future = null;
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "andwait");
      future = async.method(method, String.class);
      checkFutureString(future, method);
    }
    //return future;
  }

  /*
   * @testName: methodWithResponseClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:386;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  public void methodWithResponseClassWhileServerWaitTest()
      throws Fault {
    Future<Response> future = null;
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "andwait");
      future = async.method(method, Response.class);
      checkFutureOkResponse(future);
    }
    //return future;
  }

  /*
   * @testName: methodWithClassThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:386;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void methodWithClassThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    Future<String> future = null;
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(method.toLowerCase());
      future = async.method(method, String.class);
      assertExceptionWithProcessingExceptionIsThrownAndLog(future);
    }
  }

  /*
   * @testName: methodWithClassThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:386;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void methodWithClassThrowsWebApplicationExceptionTest() throws Fault {
    Future<String> future = null;
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "notok");
      future = async.method(method, String.class);
      assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
    }
  }

  /*
   * @testName: methodWithClassThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:386;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void methodWithClassThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    Future<Response> future = null;
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "notok");
      future = async.method(method, Response.class);
      checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
    }
  }

  /*
   * @testName: methodWithGenericTypeStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:387;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  public void methodWithGenericTypeStringWhileServerWaitTest()
      throws Fault {
    GenericType<String> generic = createGeneric(String.class);
    Future<String> future = null;
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "andwait");
      future = async.method(method, generic);
      checkFutureString(future, method);
    }
    //return future;
  }

  /*
   * @testName: methodWithGenericTypeResponseWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:387;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  public void methodWithGenericTypeResponseWhileServerWaitTest()
      throws Fault {
    GenericType<Response> generic = createGeneric(Response.class);
    Future<Response> future = null;
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "andwait");
      future = async.method(method, generic);
      checkFutureOkResponse(future);
    }
    //return future;
  }

  /*
   * @testName: methodWithGenericTypeThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:387;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void methodWithGenericTypeThrowsProcessingExceptionTest()
      throws Fault {
    _hostname = NONEXISTING_SITE;
    Future<Response> future = null;
    GenericType<Response> generic = createGeneric(Response.class);
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(method.toLowerCase());
      future = async.method(method, generic);
      assertExceptionWithProcessingExceptionIsThrownAndLog(future);
    }
  }

  /*
   * @testName: methodWithGenericTypeThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:387;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void methodWithGenericTypeThrowsWebApplicationExceptionTest()
      throws Fault {
    Future<String> future = null;
    GenericType<String> generic = createGeneric(String.class);
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "notok");
      future = async.method(method, generic);
      assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
    }
  }

  /*
   * @testName:
   * methodWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:387;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void methodWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    Future<Response> future = null;
    GenericType<Response> generic = createGeneric(Response.class);
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "notok");
      future = async.method(method, generic);
      checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
    }
  }

  /*
   * @testName: methodWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:388;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  public void methodWithCallbackWhileServerWaitTest() throws Fault {
    InvocationCallback<Response> callback = createCallback(true);
    Future<Response> future = null;
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "andwait");
      future = async.method(method, callback);
      checkFutureOkResponse(future);
      assertCallbackCall();
    }
    //return future;
  }

  /*
   * @testName: methodWithCallbackStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:388;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  public void methodWithCallbackStringWhileServerWaitTest()
      throws Fault {
    InvocationCallback<String> callback = createStringCallback(true);
    Future<String> future = null;
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "andwait");
      future = async.method(method, callback);
      checkFutureString(future, method);
      assertCallbackCall();
    }
    //return future;
  }

  /*
   * @testName: methodWithCallbackThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:388;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void methodWithCallbackThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    Future<String> future = null;
    InvocationCallback<String> callback = createStringCallback(false);
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(method.toLowerCase());
      future = async.method(method, callback);
      assertExceptionWithProcessingExceptionIsThrownAndLog(future);
    }
  }

  /*
   * @testName: methodWithCallbackThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:388;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void methodWithCallbackThrowsWebApplicationExceptionTest()
      throws Fault {
    Future<String> future = null;
    InvocationCallback<String> callback = createStringCallback(false);
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "notok");
      future = async.method(method, callback);
      assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
    }
  }

  /*
   * @testName: methodWithCallbackThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:388;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void methodWithCallbackThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    Future<Response> future = null;
    InvocationCallback<Response> callback = createCallback(false);
    for (String method : METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "notok");
      future = async.method(method, callback);
      checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
    }
  }

  /*
   * @testName: methodWithEntityWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:389;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  public void methodWithEntityWhileServerWaitTest() throws Fault {
    Future<Response> future = null;
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "andwait");
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity);
      checkFutureOkResponse(future);
    }
    //return future;
  }

  /*
   * @testName: methodWithEntityThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:389;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void methodWithEntityThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    Future<Response> future = null;
    for (String method : ENTITY_METHODS) {
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      AsyncInvoker async = startAsyncInvokerForMethod(method.toLowerCase());
      future = async.method(method, entity);
      assertExceptionWithProcessingExceptionIsThrownAndLog(future);
    }
  }

  /*
   * @testName: methodWithStringClassWithEntityWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:390;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  public void methodWithStringClassWithEntityWhileServerWaitTest()
      throws Fault {
    Future<String> future = null;
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "andwait");
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity, String.class);
      checkFutureString(future, method);
    }
    //return future;
  }

  /*
   * @testName: methodWithResponseClassWithEntityWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:390;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  public void methodWithResponseClassWithEntityWhileServerWaitTest()
      throws Fault {
    Future<Response> future = null;
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "andwait");
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity, Response.class);
      checkFutureOkResponse(future);
    }
    //return future;
  }

  /*
   * @testName: methodWithClassWithEntityThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:390;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void methodWithClassWithEntityThrowsProcessingExceptionTest()
      throws Fault {
    _hostname = NONEXISTING_SITE;
    Future<String> future = null;
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(method.toLowerCase());
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity, String.class);
      assertExceptionWithProcessingExceptionIsThrownAndLog(future);
    }
  }

  /*
   * @testName: methodWithClassWithEntityThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:390;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void methodWithClassWithEntityThrowsWebApplicationExceptionTest()
      throws Fault {
    Future<String> future = null;
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "notok");
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity, String.class);
      assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
    }
  }

  /*
   * @testName:
   * methodWithClassWithEntityThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:390;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void methodWithClassWithEntityThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    Future<Response> future = null;
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "notok");
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity, Response.class);
      checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
    }
  }

  /*
   * @testName: methodWithGenericTypeStringWithEntityWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:391;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  public void methodWithGenericTypeStringWithEntityWhileServerWaitTest()
      throws Fault {
    Future<String> future = null;
    GenericType<String> generic = createGeneric(String.class);
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "andwait");
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity, generic);
      checkFutureString(future, method);
    }
    //return future;
  }

  /*
   * @testName: methodWithGenericTypeResponseWithEntityWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:391;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  public void methodWithGenericTypeResponseWithEntityWhileServerWaitTest()
      throws Fault {
    Future<Response> future = null;
    GenericType<Response> generic = createGeneric(Response.class);
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "andwait");
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity, generic);
      checkFutureOkResponse(future);
    }
    //return future;
  }

  /*
   * @testName: methodWithGenericTypeWithEntityThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:391;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void methodWithGenericTypeWithEntityThrowsProcessingExceptionTest()
      throws Fault {
    _hostname = NONEXISTING_SITE;
    GenericType<String> generic = createGeneric(String.class);
    Future<String> future = null;
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(method.toLowerCase());
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity, generic);
      assertExceptionWithProcessingExceptionIsThrownAndLog(future);
    }
  }

  /*
   * @testName: methodWithGenericTypeWithEntityThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:391;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void methodWithGenericTypeWithEntityThrowsWebApplicationExceptionTest()
      throws Fault {
    Future<String> future = null;
    GenericType<String> generic = createGeneric(String.class);
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "notok");
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity, generic);
      assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
    }
  }

  /*
   * @testName:
   * methodWithGenericTypeWithEntityThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:391;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void methodWithGenericTypeWithEntityThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    Future<Response> future = null;
    GenericType<Response> generic = createGeneric(Response.class);
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "notok");
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity, generic);
      checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
    }
  }

  /*
   * @testName: methodWithCallbackWithEntityWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:392;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  public void methodWithCallbackWithEntityWhileServerWaitTest()
      throws Fault {
    Future<Response> future = null;
    InvocationCallback<Response> callback = createCallback(true);
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "andwait");
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity, callback);
      checkFutureOkResponse(future);
      assertCallbackCall();
    }
    //return future;
  }

  /*
   * @testName: methodWithCallbackStringWithEntityWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:392;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * asynchronously.
   */
  @Test
  public void methodWithCallbackStringWithEntityWhileServerWaitTest()
      throws Fault {
    Future<String> future = null;
    InvocationCallback<String> callback = createStringCallback(true);
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "andwait");
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity, callback);
      checkFutureString(future, method);
      assertCallbackCall();
    }
    //return future;
  }

  /*
   * @testName: methodWithCallbackWithEntityThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:392;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void methodWithCallbackWithEntityThrowsProcessingExceptionTest()
      throws Fault {
    _hostname = NONEXISTING_SITE;
    InvocationCallback<String> callback = createStringCallback(false);
    Future<String> future = null;
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(method.toLowerCase());
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity, callback);
      assertExceptionWithProcessingExceptionIsThrownAndLog(future);
    }
  }

  /*
   * @testName: methodWithCallbackWithEntityThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:392;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void methodWithCallbackWithEntityThrowsWebApplicationExceptionTest()
      throws Fault {
    Future<String> future = null;
    InvocationCallback<String> callback = createStringCallback(false);
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "notok");
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity, callback);
      assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
    }
  }

  /*
   * @testName:
   * methodWithCallbackWithEntityThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:392;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void methodWithCallbackWithEntityThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    Future<Response> future = null;
    InvocationCallback<Response> callback = createCallback(false);
    for (String method : ENTITY_METHODS) {
      AsyncInvoker async = startAsyncInvokerForMethod(
          method.toLowerCase() + "notok");
      Entity<String> entity = Entity.entity(method, MediaType.WILDCARD_TYPE);
      future = async.method(method, entity, callback);
      checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
    }
  }

  // ------------------------------------------------------------------
  // ---------------------------OPTIONS--------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: optionsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:393;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * asynchronously.
   */
  @Test
  public void optionsTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("options");
    Future<Response> future = async.options();
    checkFutureOkResponseNoTime(future);
    //return future;
  }

  /*
   * @testName: optionsWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:393;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * asynchronously.
   */
  @Test
  public void optionsWhileServerWaitTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("optionsandwait");
    Future<Response> future = async.options();
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: optionsThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:393;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void optionsThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("options");
    Future<Response> future = async.options();
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: optionsWithStringClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:394;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * asynchronously.
   */
  @Test
  public void optionsWithStringClassWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("optionsandwait");
    Future<String> future = async.options(String.class);
    checkFutureString(future, "options");
    //return future;
  }

  /*
   * @testName: optionsWithResponseClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:394;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * asynchronously.
   */
  @Test
  public void optionsWithResponseClassWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("optionsandwait");
    Future<Response> future = async.options(Response.class);
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: optionsWithClassThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:394;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void optionsWithClassThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("options");
    Future<String> future = async.options(String.class);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: optionsWithClassThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:394;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void optionsWithClassThrowsWebApplicationExceptionTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("optionsnotok");
    Future<String> future = async.options(String.class);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: optionsWithClassThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:394;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void optionsWithClassThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("optionsnotok");
    Future<Response> future = async.options(Response.class);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: optionsWithGenericTypeStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:395;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * asynchronously.
   */
  @Test
  public void optionsWithGenericTypeStringWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("optionsandwait");
    GenericType<String> generic = createGeneric(String.class);
    Future<String> future = async.options(generic);
    checkFutureString(future, "options");
    //return future;
  }

  /*
   * @testName: optionsWithGenericTypeResponseWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:395;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * asynchronously.
   */
  @Test
  public void optionsWithGenericTypeResponseWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("optionsandwait");
    GenericType<Response> generic = createGeneric(Response.class);
    Future<Response> future = async.options(generic);
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: optionsWithGenericTypeThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:395;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void optionsWithGenericTypeThrowsProcessingExceptionTest()
      throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("options");
    GenericType<String> generic = createGeneric(String.class);
    Future<String> future = async.options(generic);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: optionsWithGenericTypeThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:395;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void optionsWithGenericTypeThrowsWebApplicationExceptionTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("optionsnotok");
    GenericType<String> generic = createGeneric(String.class);
    Future<String> future = async.options(generic);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName:
   * optionsWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:395;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void optionsWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("optionsnotok");
    GenericType<Response> generic = createGeneric(Response.class);
    Future<Response> future = async.options(generic);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: optionsWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:396;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * asynchronously.
   */
  @Test
  public void optionsWithCallbackWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("optionsandwait");
    InvocationCallback<Response> callback = createCallback(true);
    Future<Response> future = async.options(callback);
    checkFutureOkResponse(future);
    assertCallbackCall();
    //return future;
  }

  /*
   * @testName: optionsWithStringCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:396;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * asynchronously.
   */
  @Test
  public void optionsWithStringCallbackWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("optionsandwait");
    InvocationCallback<String> callback = createStringCallback(true);
    Future<String> future = async.options(callback);
    checkFutureString(future, "options");
    assertCallbackCall();
    //return future;
  }

  /*
   * @testName: optionsWithCallbackThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:396;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void optionsWithCallbackThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("options");
    InvocationCallback<String> callback = createStringCallback(false);
    Future<String> future = async.options(callback);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: optionsWithCallbackThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:396;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void optionsWithCallbackThrowsWebApplicationExceptionTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("optionsnotok");
    InvocationCallback<String> callback = createStringCallback(false);
    Future<String> future = async.options(callback);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName:
   * optionsWithCallbackThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:396;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void optionsWithCallbackThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("optionsnotok");
    InvocationCallback<Response> callback = createCallback(false);
    Future<Response> future = async.options(callback);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  // ------------------------------------------------------------------
  // ---------------------------POST-----------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: postTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:397;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * asynchronously.
   */
  @Test
  public void postTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("post");
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    Future<Response> future = async.post(entity);
    checkFutureOkResponseNoTime(future);
    //return future;
  }

  /*
   * @testName: postWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:397;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * asynchronously.
   */
  @Test
  public void postWhileServerWaitTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("postandwait");
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    Future<Response> future = async.post(entity);
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: postThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:397;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void postThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("post");
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    Future<Response> future = async.post(entity);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: postWithStringClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:398;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * asynchronously.
   */
  @Test
  public void postWithStringClassWhileServerWaitTest() throws Fault {
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("postandwait");
    Future<String> future = async.post(entity, String.class);
    checkFutureString(future, "post");
    //return future;
  }

  /*
   * @testName: postWithResponseClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:398;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * asynchronously.
   */
  @Test
  public void postWithResponseClassWhileServerWaitTest()
      throws Fault {
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("postandwait");
    Future<Response> future = async.post(entity, Response.class);
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: postWithClassThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:398;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void postWithClassThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("post");
    Future<String> future = async.post(entity, String.class);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: postWithClassThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:398;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void postWithClassThrowsWebApplicationExceptionTest() throws Fault {
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("postnotok");
    Future<String> future = async.post(entity, String.class);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: postWithClassThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:398;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void postWithClassThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("postnotok");
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    Future<Response> future = async.post(entity, Response.class);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: postWithGenericTypeStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:399;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * asynchronously.
   */
  @Test
  public void postWithGenericTypeStringWhileServerWaitTest()
      throws Fault {
    GenericType<String> generic = createGeneric(String.class);
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("postandwait");
    Future<String> future = async.post(entity, generic);
    checkFutureString(future, "post");
    //return future;
  }

  /*
   * @testName: postWithGenericTypeResponseWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:399;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * asynchronously.
   */
  @Test
  public void postWithGenericTypeResponseWhileServerWaitTest()
      throws Fault {
    GenericType<Response> generic = createGeneric(Response.class);
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("postandwait");
    Future<Response> future = async.post(entity, generic);
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: postWithGenericTypeThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:399;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void postWithGenericTypeThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    GenericType<String> generic = createGeneric(String.class);
    AsyncInvoker async = startAsyncInvokerForMethod("post");
    Future<String> future = async.post(entity, generic);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: postWithGenericTypeThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:399;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void postWithGenericTypeThrowsWebApplicationExceptionTest()
      throws Fault {
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    GenericType<String> generic = createGeneric(String.class);
    AsyncInvoker async = startAsyncInvokerForMethod("postnotok");
    Future<String> future = async.post(entity, generic);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName:
   * postWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:399;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void postWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("postnotok");
    GenericType<Response> generic = createGeneric(Response.class);
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    Future<Response> future = async.post(entity, generic);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: postWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:400;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * asynchronously.
   */
  @Test
  public void postWithCallbackWhileServerWaitTest() throws Fault {
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    InvocationCallback<Response> callback = createCallback(true);
    AsyncInvoker async = startAsyncInvokerForMethod("postandwait");
    Future<Response> future = async.post(entity, callback);
    checkFutureOkResponse(future);
    assertCallbackCall();
    //return future;
  }

  /*
   * @testName: postWithCallbackThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:400;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void postWithCallbackThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    InvocationCallback<String> callback = createStringCallback(false);
    AsyncInvoker async = startAsyncInvokerForMethod("post");
    Future<String> future = async.post(entity, callback);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: postWithCallbackThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:400;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void postWithCallbackThrowsWebApplicationExceptionTest() throws Fault {
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    InvocationCallback<String> callback = createStringCallback(false);
    AsyncInvoker async = startAsyncInvokerForMethod("postnotok");
    Future<String> future = async.post(entity, callback);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: postWithCallbackThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:400;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void postWithCallbackThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("postnotok");
    InvocationCallback<Response> callback = createCallback(false);
    Entity<String> entity = Entity.entity("post", MediaType.WILDCARD_TYPE);
    Future<Response> future = async.post(entity, callback);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  // ------------------------------------------------------------------
  // ---------------------------PUT -----------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: putTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:401;
   * 
   * @test_Strategy: Invoke HTTP PUT method for the current request
   * asynchronously.
   */
  @Test
  public void putTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("put");
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    Future<Response> future = async.put(entity);
    checkFutureOkResponseNoTime(future);
    //return future;
  }

  /*
   * @testName: putWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:401;
   * 
   * @test_Strategy: Invoke HTTP PUT method for the current request
   * asynchronously.
   */
  @Test
  public void putWhileServerWaitTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("putandwait");
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    Future<Response> future = async.put(entity);
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: putThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:401;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void putThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("put");
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    Future<Response> future = async.put(entity);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: putWithStringClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:402;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * asynchronously.
   */
  @Test
  public void putWithStringClassWhileServerWaitTest() throws Fault {
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("putandwait");
    Future<String> future = async.put(entity, String.class);
    checkFutureString(future, "put");
    //return future;
  }

  /*
   * @testName: putWithResponseClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:402;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * asynchronously.
   */
  @Test
  public void putWithResponseClassWhileServerWaitTest()
      throws Fault {
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("putandwait");
    Future<Response> future = async.put(entity, Response.class);
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: putWithClassThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:402;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void putWithClassThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("put");
    Future<String> future = async.put(entity, String.class);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: putWithClassThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:402;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void putWithClassThrowsWebApplicationExceptionTest() throws Fault {
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("putnotok");
    Future<String> future = async.put(entity, String.class);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: putWithClassThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:402;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void putWithClassThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("putnotok");
    Future<Response> future = async.put(entity, Response.class);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: putWithGenericTypeStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:403;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * asynchronously.
   */
  @Test
  public void putWithGenericTypeStringWhileServerWaitTest()
      throws Fault {
    GenericType<String> generic = createGeneric(String.class);
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("putandwait");
    Future<String> future = async.put(entity, generic);
    checkFutureString(future, "put");
    //return future;
  }

  /*
   * @testName: putWithGenericTypeResponseWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:403;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * asynchronously.
   */
  @Test
  public void putWithGenericTypeResponseWhileServerWaitTest()
      throws Fault {
    GenericType<Response> generic = createGeneric(Response.class);
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("putandwait");
    Future<Response> future = async.put(entity, generic);
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: putWithGenericTypeThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:403;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void putWithGenericTypeThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    GenericType<String> generic = createGeneric(String.class);
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("put");
    Future<String> future = async.put(entity, generic);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: putWithGenericTypeThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:403;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void putWithGenericTypeThrowsWebApplicationExceptionTest()
      throws Fault {
    GenericType<String> generic = createGeneric(String.class);
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("putnotok");
    Future<String> future = async.put(entity, generic);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: putWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:403;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void putWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("putnotok");
    GenericType<Response> generic = createGeneric(Response.class);
    Future<Response> future = async.put(entity, generic);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: putWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:404;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * asynchronously.
   */
  @Test
  public void putWithCallbackWhileServerWaitTest() throws Fault {
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    InvocationCallback<Response> callback = createCallback(true);
    AsyncInvoker async = startAsyncInvokerForMethod("putandwait");
    Future<Response> future = async.put(entity, callback);
    checkFutureOkResponse(future);
    assertCallbackCall();
    //return future;
  }

  /*
   * @testName: putWithStringCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:404;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * asynchronously.
   */
  @Test
  public void putWithStringCallbackWhileServerWaitTest()
      throws Fault {
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    InvocationCallback<String> callback = createStringCallback(true);
    AsyncInvoker async = startAsyncInvokerForMethod("putandwait");
    Future<String> future = async.put(entity, callback);
    checkFutureString(future, "put");
    assertCallbackCall();
    //return future;
  }

  /*
   * @testName: putWithCallbackThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:404;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void putWithCallbackThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    InvocationCallback<String> callback = createStringCallback(false);
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("put");
    Future<String> future = async.put(entity, callback);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: putWithCallbackThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:404;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void putWithCallbackThrowsWebApplicationExceptionTest() throws Fault {
    InvocationCallback<String> callback = createStringCallback(false);
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("putnotok");
    Future<String> future = async.put(entity, callback);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: putWithCallbackThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:404;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void putWithCallbackThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    Entity<String> entity = Entity.entity("put", MediaType.WILDCARD_TYPE);
    AsyncInvoker async = startAsyncInvokerForMethod("putnotok");
    InvocationCallback<Response> callback = createCallback(false);
    Future<Response> future = async.put(entity, callback);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  // ------------------------------------------------------------------
  // ---------------------------TRACE -----------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: traceTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:405;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * asynchronously.
   */
  @Test
  public void traceTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("trace");
    Future<Response> future = async.trace();
    checkFutureOkResponseNoTime(future);
    //return future;
  }

  /*
   * @testName: traceWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:405;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * asynchronously.
   */
  @Test
  public void traceWhileServerWaitTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("traceandwait");
    Future<Response> future = async.trace();
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: traceThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:405;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void traceThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("trace");
    Future<Response> future = async.trace();
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: traceWithStringClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:406;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * asynchronously.
   */
  @Test
  public void traceWithStringClassWhileServerWaitTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("traceandwait");
    Future<String> future = async.trace(String.class);
    checkFutureString(future, "trace");
    //return future;
  }

  /*
   * @testName: traceWithResponseClassWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:406;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * asynchronously.
   */
  @Test
  public void traceWithResponseClassWhileServerWaitTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("traceandwait");
    Future<Response> future = async.trace(Response.class);
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: traceWithClassThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:406;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void traceWithClassThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    AsyncInvoker async = startAsyncInvokerForMethod("trace");
    Future<String> future = async.trace(String.class);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: traceWithClassThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:406;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void traceWithClassThrowsWebApplicationExceptionTest() throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("tracenotok");
    Future<String> future = async.trace(String.class);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: traceWithClassThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:406;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void traceWithClassThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("tracenotok");
    Future<Response> future = async.trace(Response.class);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: traceWithGenericTypeStringWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:407;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * asynchronously.
   */
  @Test
  public void traceWithGenericTypeStringWhileServerWaitTest()
      throws Fault {
    GenericType<String> generic = createGeneric(String.class);
    AsyncInvoker async = startAsyncInvokerForMethod("traceandwait");
    Future<String> future = async.trace(generic);
    checkFutureString(future, "trace");
    //return future;
  }

  /*
   * @testName: traceWithGenericTypeResponseWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:407;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * asynchronously.
   */
  @Test
  public void traceWithGenericTypeResponseWhileServerWaitTest()
      throws Fault {
    GenericType<Response> generic = createGeneric(Response.class);
    AsyncInvoker async = startAsyncInvokerForMethod("traceandwait");
    Future<Response> future = async.trace(generic);
    checkFutureOkResponse(future);
    //return future;
  }

  /*
   * @testName: traceWithGenericTypeThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:407;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void traceWithGenericTypeThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    GenericType<String> generic = createGeneric(String.class);
    AsyncInvoker async = startAsyncInvokerForMethod("trace");
    Future<String> future = async.trace(generic);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: traceWithGenericTypeThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:407;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void traceWithGenericTypeThrowsWebApplicationExceptionTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("tracenotok");
    GenericType<String> generic = createGeneric(String.class);
    Future<String> future = async.trace(generic);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName:
   * traceWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:407;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void traceWithGenericTypeThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("tracenotok");
    GenericType<Response> generic = createGeneric(Response.class);
    Future<Response> future = async.trace(generic);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: traceWithCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:408;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * asynchronously.
   */
  @Test
  public void traceWithCallbackWhileServerWaitTest() throws Fault {
    InvocationCallback<Response> callback = createCallback(true);
    AsyncInvoker async = startAsyncInvokerForMethod("traceandwait");
    Future<Response> future = async.trace(callback);
    checkFutureOkResponse(future);
    assertCallbackCall();
    //return future;
  }

  /*
   * @testName: traceWithStringCallbackWhileServerWaitTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:408;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * asynchronously.
   */
  @Test
  public void traceWithStringCallbackWhileServerWaitTest()
      throws Fault {
    InvocationCallback<String> callback = createStringCallback(true);
    AsyncInvoker async = startAsyncInvokerForMethod("traceandwait");
    Future<String> future = async.trace(callback);
    checkFutureString(future, "trace");
    assertCallbackCall();
    //return future;
  }

  /*
   * @testName: traceWithCallbackThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:408;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps an
   * jakarta.ws.rs.ProcessingException thrown in case of an invocation processing
   * failure.
   */
  @Test
  public void traceWithCallbackThrowsProcessingExceptionTest() throws Fault {
    _hostname = NONEXISTING_SITE;
    InvocationCallback<String> callback = createStringCallback(false);
    AsyncInvoker async = startAsyncInvokerForMethod("trace");
    Future<String> future = async.trace(callback);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: traceWithCallbackThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:408;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void traceWithCallbackThrowsWebApplicationExceptionTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("tracenotok");
    InvocationCallback<String> callback = createStringCallback(false);
    Future<String> future = async.trace(callback);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: traceWithCallbackThrowsNoWebApplicationExceptionForResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:408;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * WebApplicationException or one of its subclasses thrown in case the
   * received response status code is not successful and the specified response
   * type is not Response.
   */
  @Test
  public void traceWithCallbackThrowsNoWebApplicationExceptionForResponseTest()
      throws Fault {
    AsyncInvoker async = startAsyncInvokerForMethod("tracenotok");
    InvocationCallback<Response> callback = createCallback(false);
    Future<Response> future = async.trace(callback);
    checkFutureStatusResponseNoTime(future, Status.NOT_ACCEPTABLE);
  }

  // ///////////////////////////////////////////////////////////////////////
  // utility methods

  protected String getUrl(String method) {
    StringBuilder url = new StringBuilder();
    url.append("http://").append(_hostname).append(":").append(_port);
    url.append("/").append(getContextRoot()).append("/").append(method);
    return url.toString();
  }

  /**
   * Create AsyncInvoker for given resource method and start time
   */
  protected AsyncInvoker startAsyncInvokerForMethod(String methodName) {
    Client client = ClientBuilder.newClient();
    client.register(new JdkLoggingFilter(false));
    WebTarget target = client.target(getUrl(methodName));
    AsyncInvoker async = target.request().async();
    setStartTime();
    return async;
  }

  protected void assertOkAndLog(Response response, Status status) throws Fault {
    assertTrue(response.getStatus() == status.getStatusCode(),
        "Returned unexpected status" +response.getStatus());
    String msg = new StringBuilder().append("Returned status ")
        .append(status.getStatusCode()).append(" (").append(status.name())
        .append(")").toString();
    TestUtil.logMsg(msg);
  }

  protected void checkFutureOkResponseNoTime(Future<Response> future)
      throws Fault {
    checkFutureStatusResponseNoTime(future, Status.OK);
  }

  protected void checkFutureStatusResponseNoTime(Future<Response> future,
      Status status) throws Fault {
    Response response = null;
    try {
      response = future.get();
    } catch (Exception e) {
      throw new Fault(e);
    }
    assertOkAndLog(response, status);
  }

  protected void checkFutureOkResponse(Future<Response> future) throws Fault {
    checkMaxEndTime();
    assertTrue(!future.isDone(), "Future cannot be done, yet!");
    checkFutureOkResponseNoTime(future);
  }

  protected void checkFutureString(Future<String> future, String expectedValue)
      throws Fault {
    checkMaxEndTime();
    assertTrue(!future.isDone(), "Future cannot be done, yet!");
    String value = null;
    try {
      value = future.get();
    } catch (Exception e) {
      throw new Fault(e);
    }
    assertTrue(expectedValue.equalsIgnoreCase(value), "expected value"+
        expectedValue+ "differes from acquired value"+ value);
  }

  protected void //
      assertExceptionWithWebApplicationExceptionIsThrownAndLog(Future<?> future)
          throws Fault {
    try {
      future.get();
      throw new Fault("ExecutionException has not been thrown");
    } catch (ExecutionException e) {
      assertWebApplicationExceptionIsCauseAndLog(e);
    } catch (InterruptedException e) {
      throw new Fault("Unexpected exception thrown", e);
    }
  }

  protected void assertExceptionWithProcessingExceptionIsThrownAndLog(
      Future<?> future) throws Fault {
    try {
      future.get();
      throw new Fault("ExecutionException has not been thrown");
    } catch (ExecutionException e) {
      assertProcessingExceptionIsCauseAndLog(e);
    } catch (InterruptedException e) {
      throw new Fault("Unexpected exception thrown", e);
    }
  }

  protected void //
      assertProcessingExceptionIsCauseAndLog(ExecutionException e)
          throws Fault {
    logMsg("ExecutionException has been thrown as expected", e);
    assertTrue(hasWrapped(e, jakarta.ws.rs.ProcessingException.class),
        "ExecutionException wrapped"+ e.getCause()+
        "rather then ProcessingException");
    logMsg("ExecutionException.getCause is ProcessingException as expected");
  }

  protected void //
      assertWebApplicationExceptionIsCauseAndLog(ExecutionException e)
          throws Fault {
    logMsg("ExecutionException has been thrown as expected", e);
    assertTrue(hasWrapped(e, WebApplicationException.class),
        "ExecutionException wrapped"+ e.getCause()+
        "rather then WebApplicationException");
    logMsg(
        "ExecutionException.getCause is WebApplicationException as expected");
  }

  static boolean //
      hasWrapped(Throwable parent, Class<? extends Throwable> wrapped) {
    while (parent.getCause() != null) {
      if (wrapped.isInstance(parent.getCause()))
        return true;
      parent = parent.getCause();
    }
    return false;
  }

  protected void sleep(int millis) throws Fault {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      throw new Fault(e);
    }
  }

  protected void setStartTime() {
    millis = System.currentTimeMillis();
    logMsg("Start time:", millis);
  }

  protected void checkMaxEndTime() throws Fault {
    long endMillis = System.currentTimeMillis();
    long diff = endMillis - millis;
    logMsg("Client was returned control in", diff, "milliseconds from request");
    assertTrue(diff <= Resource.SLEEP_TIME,
        "AsyncInvoker was blocked waiting for a response");
  }

  protected void checkMinEndTime() throws Fault {
    long endMillis = System.currentTimeMillis();
    long diff = endMillis - millis;
    logMsg("Callback#completed() called in", diff, "milliseconds from request");
    assertTrue(diff >= Resource.SLEEP_TIME,
        "AsyncInvoker.completed() was called unexpectedly soon, after"+ diff+
        "milliseconds");
  }

  protected void assertCallbackCall() throws Fault {
    while (callbackResult == 0) {
      try {
        Thread.sleep(100L);
      } catch (InterruptedException e) {
        throw new Fault(e);
      }
    }
    switch (callbackResult) {
    case 1:
      logMsg("Callback completed() call ok");
      break;
    case 2:
    case 3:
      logMsg("Callback completed() call failed with error");
      throw new Fault("Callback call failed with error", callbackException);
    }
    callbackResult = 0;
  }

  protected <T> GenericType<T> createGeneric(Class<T> clazz) {
    return new GenericType<T>(clazz);
  }

  /**
   * @param check
   *          defines whether the test actually cares about methods
   *          {@link InvocationCallback#completed(Object)} and
   *          {@link InvocationCallback#failed(Throwable)} being called
   * @return
   */
  protected InvocationCallback<Response> createCallback(boolean check) {
    InvocationCallback<Response> callback = new Callback<Response>(check) {
      @Override
      public void completed(Response response) {
        checkEndTime();
      }
    };
    return callback;
  }

  protected InvocationCallback<String> createStringCallback(boolean check) {
    InvocationCallback<String> callback = new Callback<String>(check) {
      @Override
      public void completed(String response) {
        checkEndTime();
      }
    };
    return callback;
  }

  abstract class Callback<RESPONSE> implements InvocationCallback<RESPONSE> {
    protected boolean check;

    public Callback(boolean check) {
      this.check = check;
    }

    protected void checkEndTime() {
      if (check)
        try {
          JAXRSClientIT.this.checkMinEndTime();
          callbackResult = 1;
        } catch (Fault e) {
          callbackResult = 2;
          callbackException = e;
          throw new RuntimeException(e);
        }
    }

    @Override
    public void failed(Throwable throwable) {
      if (check) {
        callbackResult = 3;
        callbackException = throwable;
        throw new RuntimeException(throwable);
      }
    }
  }
}
