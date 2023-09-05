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

package ee.jakarta.tck.ws.rs.api.client.invocation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
/*
 * The misuse of GenericType<Response> is on purpose to check the behavior 
 * corresponding to javadoc  
 */
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = -7647322937577043460L;

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  /*
   * @testName: invokePlainTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:512;
   * 
   * @test_Strategy: Synchronously invoke the request and receive a response
   * back.
   */
  @Test
  public void invokePlainTest() throws Fault {
    Invocation.Builder builder = createInvocationBuilder();
    Invocation invocation = builder.buildGet();
    Response r = invocation.invoke();
    assertContains(r, Request.GET.name());
  }

  /*
   * @testName: invokeThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:512;
   * 
   * @test_Strategy: throws ProcessingException in case the invocation failed.
   * 
   */
  @Test
  public void invokeThrowsExceptionTest() throws Fault {
    Invocation.Builder builder = createInvocationBuilder(null);
    Invocation invocation = builder.buildGet();
    try {
      invocation.invoke();
    } catch (ProcessingException ie) {
      // everything is fine
      logMsg("ProcessingException has been thrown");
      return;
    }
    throw new Fault("ProcessingException has NOT been thrown");
  }

  /*
   * @testName: invokeClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:514;
   * 
   * @test_Strategy: Synchronously invoke the request and receive a response of
   * the specified type back.
   */
  @Test
  public void invokeClassTest() throws Fault {
    Entity<String> entity = createEntity("invokeClassTest");
    Invocation.Builder builder = createInvocationBuilder();
    Invocation invocation = builder.buildPost(entity);
    String r = invocation.invoke(String.class);
    assertContains(r, Request.POST.name());
    assertContains(r, "invokeClassTest");
  }

  /*
   * @testName: invokeClassThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:514;
   * 
   * @test_Strategy: throws ProcessingException in case the invocation failed.
   * 
   */
  @Test
  public void invokeClassThrowsExceptionTest() throws Fault {
    Invocation.Builder builder = createInvocationBuilder(null);
    Invocation invocation = builder.buildGet();
    try {
      invocation.invoke(String.class);
    } catch (ProcessingException ie) {
      // everything is fine
      logMsg("ProcessingException has been thrown");
      return;
    }
    throw new Fault("ProcessingException has NOT been thrown");
  }

  /*
   * @testName: invokeStringThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:514;
   * 
   * @test_Strategy: in case the response status code of the response returned
   * by the server is not SUCCESSFUL
   */
  @Test
  public void invokeStringThrowsWebApplicationExceptionTest() throws Fault {
    Invocation.Builder builder = createInvocationBuilder(
        createBadResponseFilter());
    Invocation invocation = builder.buildGet();
    try {
      invocation.invoke(String.class);
    } catch (WebApplicationException ie) {
      // everything is fine
      logMsg("WebApplicationException has been thrown");
      return;
    }
    throw new Fault("WebApplicationException has NOT been thrown");
  }

  /*
   * @testName: invokeResponseThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:514;
   * 
   * @test_Strategy: in case the response status code of the response returned
   * by the server is not SUCCESSFUL and the response type is not Response
   */
  @Test
  public void invokeResponseThrowsNoWebApplicationExceptionTest() throws Fault {
    Invocation.Builder builder = createInvocationBuilder(
        createBadResponseFilter());
    Invocation invocation = builder.buildGet();
    Response r = invocation.invoke(Response.class);
    assertStatus(r, Status.NOT_ACCEPTABLE);
    logMsg("Returned unexpected response with status code", r.getStatus());
  }

  /*
   * @testName: invokeGenericTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:517;
   * 
   * @test_Strategy: Synchronously invoke the request and receive a response of
   * the specified generic type back.
   */
  @Test
  public void invokeGenericTypeTest() throws Fault {
    GenericType<String> entity = new GenericType<String>() {
    };
    Invocation.Builder builder = createInvocationBuilder();
    Invocation invocation = builder.buildGet();
    String r = invocation.invoke(entity);
    assertContains(r, Request.GET.name());
  }

  /*
   * @testName: invokeGenericTypeThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:517;
   * 
   * @test_Strategy: throws ProcessingException in case the invocation failed.
   * 
   */
  @Test
  public void invokeGenericTypeThrowsExceptionTest() throws Fault {
    Invocation.Builder builder = createInvocationBuilder(null);
    Invocation invocation = builder.buildGet();
    try {
      invocation.invoke(new GenericType<String>() {
      });
    } catch (ProcessingException ie) {
      // everything is fine
      logMsg("ProcessingException has been thrown");
      return;
    }
    throw new Fault("ProcessingException has NOT been thrown");
  }

  /*
   * @testName: invokeGenericTypeStringThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:517;
   * 
   * @test_Strategy: in case the response status code of the response returned
   * by the server is not SUCCESSFUL
   */
  @Test
  public void invokeGenericTypeStringThrowsWebApplicationExceptionTest()
      throws Fault {
    Invocation.Builder builder = createInvocationBuilder(
        createBadResponseFilter());
    Invocation invocation = builder.buildGet();
    try {
      invocation.invoke(new GenericType<String>() {
      });
    } catch (WebApplicationException e) {
      // everything is fine
      logMsg("WebApplicationException has been thrown");
      return;
    }
    throw new Fault("WebApplicationException has NOT been thrown");
  }

  /*
   * @testName:
   * invokeAnnonymousGenericTypeResponseThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:517;
   * 
   * @test_Strategy: in case the response status code of the response returned
   * by the server is not SUCCESSFUL and response type is not Response
   */
  @Test
  public void invokeAnnonymousGenericTypeResponseThrowsNoWebApplicationExceptionTest()
      throws Fault {
    Invocation.Builder builder = createInvocationBuilder(
        createBadResponseFilter());
    Invocation invocation = builder.buildGet();
    Response response = invocation.invoke(new GenericType<Response>() {
    });
    assertStatus(response, Status.NOT_ACCEPTABLE);
    logMsg("Response return code is", response.getStatus(), "as expected");
  }

  /*
   * @testName:
   * invokeExtendedGenericTypeResponseThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:517;
   * 
   * @test_Strategy: in case the response status code of the response returned
   * by the server is not SUCCESSFUL and response type is not Response
   */
  @Test
  public void invokeExtendedGenericTypeResponseThrowsNoWebApplicationExceptionTest()
      throws Fault {
    Invocation.Builder builder = createInvocationBuilder(
        createBadResponseFilter());
    Invocation invocation = builder.buildGet();
    Response response = invocation.invoke(new GenericTypeResponse());
    assertStatus(response, Status.NOT_ACCEPTABLE);
    logMsg("Response return code is", response.getStatus(), "as expected");
  }

  /*
   * @testName: submitPlainTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:520;
   * 
   * @test_Strategy: Submit the request for an asynchronous invocation and
   * receive a future response back.
   */
  @Test
  public void submitPlainTest() throws Fault {
    Invocation.Builder builder = createInvocationBuilder();
    Invocation invocation = builder.buildGet();
    Future<Response> future = invocation.submit();
    Response response;
    try {
      response = future.get();
      assertContains(response, Request.GET.name());
    } catch (Exception e) {
      throw new Fault(e);
    }
  }

  /*
   * @testName: submitPlainThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:520;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * ProcessingException thrown in case of an invocation processing failure
   */
  @Test
  public void submitPlainThrowsProcessingExceptionTest() throws Fault {
    Invocation.Builder builder = createInvocationBuilder(null);
    Invocation invocation = builder.buildGet();
    Future<Response> future = invocation.submit();
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: submitClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:521;
   * 
   * @test_Strategy: Submit the request for an asynchronous invocation and
   * receive a future response of the specified type back.
   */
  @Test
  public void submitClassTest() throws Fault {
    Invocation.Builder builder = createInvocationBuilder();
    Entity<String> entity = createEntity("submitClassTest");
    Invocation invocation = builder.buildPost(entity);
    Future<String> future = invocation.submit(String.class);
    String response;
    try {
      response = future.get();
      assertContains(response, Request.POST.name());
      assertContains(response, "submitClassTest");
    } catch (Exception e) {
      throw new Fault(e);
    }
  }

  /*
   * @testName: submitStringThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:521;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * ProcessingException thrown in case of an invocation processing failure
   */
  @Test
  public void submitStringThrowsProcessingExceptionTest() throws Fault {
    Invocation.Builder builder = createInvocationBuilder(null);
    Invocation invocation = builder.buildGet();
    Future<String> future = invocation.submit(String.class);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: submitStringThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:521;
   * 
   * @test_Strategy:Note that calling the Future.get() method on the returned
   * Future instance may throw a WebApplicationException or one of its
   * subclasses thrown in case the received response status code is not
   * successful and the specified response type is not Response.
   */
  @Test
  public void submitStringThrowsWebApplicationExceptionTest() throws Fault {
    Invocation.Builder builder = createInvocationBuilder(
        createBadResponseFilter());
    Invocation invocation = builder.buildGet();
    Future<String> future = invocation.submit(String.class);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: submitResponseThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:521;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw a WebApplicationException or one of its
   * subclasses thrown in case the received response status code is not
   * successful and the specified response type is not Response.
   */
  @Test
  public void submitResponseThrowsNoWebApplicationExceptionTest() throws Fault {
    Invocation.Builder builder = createInvocationBuilder(
        createBadResponseFilter());
    Invocation invocation = builder.buildGet();
    Future<Response> future = invocation.submit(Response.class);
    assertStatus(future, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: submitGenericTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:522;
   * 
   * @test_Strategy: Submit the request for an asynchronous invocation and
   * receive a future response of the specified generic type back.
   */
  @Test
  public void submitGenericTypeTest() throws Fault {
    Invocation.Builder builder = createInvocationBuilder();
    Entity<String> entity = createEntity("submitGenericTypeTest");
    Invocation invocation = builder.buildPost(entity);
    Future<String> future = invocation.submit(new GenericType<String>() {
    });
    String response;
    try {
      response = future.get();
      assertContains(response, Request.POST.name());
      assertContains(response, "submitGenericTypeTest");
    } catch (Exception e) {
      throw new Fault(e);
    }
  }

  /*
   * @testName: submitGenericTypeThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:522;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * ProcessingException thrown in case of an invocation processing failure
   */
  @Test
  public void submitGenericTypeThrowsProcessingExceptionTest() throws Fault {
    Invocation.Builder builder = createInvocationBuilder(null);
    Entity<String> entity = createEntity("submitGenericTypeTest");
    Invocation invocation = builder.buildPost(entity);
    Future<String> future = invocation.submit(new GenericType<String>() {
    });
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: submitGenericTypeStringThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:522;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw a WebApplicationException or one of its
   * subclasses thrown in case the received response status code is not
   * successful and the specified response type is not Response.
   */
  @Test
  public void submitGenericTypeStringThrowsWebApplicationExceptionTest()
      throws Fault {
    Invocation.Builder builder = createInvocationBuilder(
        createBadResponseFilter());
    Entity<String> entity = createEntity("submitGenericTypeTest");
    Invocation invocation = builder.buildPost(entity);
    Future<String> future = invocation.submit(new GenericType<String>() {
    });
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName:
   * submitAnnonymousGenericTypeResponseThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:522;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw a WebApplicationException or one of its
   * subclasses thrown in case the received response status code is not
   * successful and the specified response type is not Response.
   */
  @Test
  public void submitAnnonymousGenericTypeResponseThrowsNoWebApplicationExceptionTest()
      throws Fault {
    Invocation.Builder builder = createInvocationBuilder(
        createBadResponseFilter());
    Entity<String> entity = createEntity("submitGenericTypeTest");
    Invocation invocation = builder.buildPost(entity);
    Future<Response> future = invocation.submit(new GenericType<Response>() {
    });
    assertStatus(future, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName:
   * submitExtendedGenericTypeResponseThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:522;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw a WebApplicationException or one of its
   * subclasses thrown in case the received response status code is not
   * successful and the specified response type is not Response.
   */
  @Test
  public void submitExtendedGenericTypeResponseThrowsNoWebApplicationExceptionTest()
      throws Fault {
    Invocation.Builder builder = createInvocationBuilder(
        createBadResponseFilter());
    Entity<String> entity = createEntity("submitGenericTypeTest");
    Invocation invocation = builder.buildPost(entity);
    Future<Response> future = invocation.submit(new GenericTypeResponse());
    assertStatus(future, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: submitInvocationCallbackTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:523;
   * 
   * @test_Strategy: Submit the request for an asynchronous invocation and
   * register an InvocationCallback to process the future result of the
   * invocation.
   */
  @Test
  public void submitInvocationCallbackTest() throws Fault {
    InvocationCallback<String> callback = createCallback(String.class);
    Invocation.Builder builder = createInvocationBuilder();
    Invocation invocation = builder.buildGet();
    Future<String> future = invocation.submit(callback);
    String response;
    try {
      response = future.get();
      assertContains(response, Request.GET.name());
    } catch (Exception e) {
      throw new Fault(e);
    }
  }

  /*
   * @testName: submitInvocationCallbackThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:523;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw an ExecutionException that wraps a
   * ProcessingException thrown in case of an invocation processing failure
   */
  @Test
  public void submitInvocationCallbackThrowsProcessingExceptionTest()
      throws Fault {
    InvocationCallback<String> callback = createCallback(String.class);
    Invocation.Builder builder = createInvocationBuilder(null);
    Invocation invocation = builder.buildGet();
    Future<String> future = invocation.submit(callback);
    assertExceptionWithProcessingExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: submitInvocationCallbackStringThrowsWebAppExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:523;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw a WebApplicationException or one of its
   * subclasses thrown in case the received response status code is not
   * successful and the specified response type is not Response.
   */
  @Test
  public void submitInvocationCallbackStringThrowsWebAppExceptionTest()
      throws Fault {
    InvocationCallback<String> callback = createCallback(String.class);
    Invocation.Builder builder = createInvocationBuilder(
        createBadResponseFilter());
    Invocation invocation = builder.buildGet();
    Future<String> future = invocation.submit(callback);
    assertExceptionWithWebApplicationExceptionIsThrownAndLog(future);
  }

  /*
   * @testName: submitInvocationCallbackResponseThrowsNoWebAppExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:523;
   * 
   * @test_Strategy: Note that calling the Future.get() method on the returned
   * Future instance may throw a WebApplicationException or one of its
   * subclasses thrown in case the received response status code is not
   * successful and the specified response type is not Response.
   */
  @Test
  public void submitInvocationCallbackResponseThrowsNoWebAppExceptionTest()
      throws Fault {
    InvocationCallback<Response> callback = createCallback(Response.class);
    Invocation.Builder builder = createInvocationBuilder(
        createBadResponseFilter());
    Invocation invocation = builder.buildGet();
    Future<Response> future = invocation.submit(callback);
    Response r = null;
    try {
      r = future.get();
    } catch (Exception e) {
      throw new Fault("Unexpected exception caught", e);
    }
    assertStatus(r, Status.NOT_ACCEPTABLE);
    logMsg("Found expected status code", r.getStatus());
  }

  // ///////////////////////////////////////////////////////////////////////

  /**
   * Simulates server side
   * 
   * @return Response containing request method and entity
   */
  protected ClientRequestFilter createRequestFilter() {
    ClientRequestFilter filter = new ClientRequestFilter() {
      @Override
      public void filter(ClientRequestContext ctx) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(ctx.getMethod()).append(";");
        if (ctx.hasEntity())
          sb.append(ctx.getEntity()).append(";");
        Response r = Response.ok(sb.toString()).build();
        ctx.abortWith(r);
      }
    };
    return filter;
  }

  protected Invocation.Builder createInvocationBuilder(
      ClientRequestFilter filter) {
    Client client = ClientBuilder.newClient();
    if (filter != null)
      client.register(filter);
    WebTarget target = client.target("http://cts.tck:888");
    Invocation.Builder builder = target.request();
    return builder;
  }

  protected Invocation.Builder createInvocationBuilder() {
    return createInvocationBuilder(createRequestFilter());
  }

  protected ClientRequestFilter createBadResponseFilter() {
    ClientRequestFilter filter = new ClientRequestFilter() {
      @Override
      public void filter(ClientRequestContext arg0) throws IOException {
        Response r = Response.status(Status.NOT_ACCEPTABLE).build();
        arg0.abortWith(r);
      }
    };
    return filter;
  }

  @SuppressWarnings("unchecked")
  protected <T> InvocationCallback<T> createCallback(Class<T> clazz) {
    InvocationCallback<T> callback = null;
    if (clazz == String.class)
      callback = (InvocationCallback<T>) new InvocationCallback<String>() {
        @Override
        public void completed(String arg0) {
        }

        @Override
        public void failed(Throwable throwable) {
        }
      };
    else if (clazz == Response.class)
      callback = (InvocationCallback<T>) new InvocationCallback<Response>() {
        @Override
        public void completed(Response arg0) {
        }

        @Override
        public void failed(Throwable throwable) {
        }
      };
    return callback;
  }

  /*
   * readEntity vs. getEntity (from response) is set here
   */
  protected String getEntity(Response r) {
    String entity = r.readEntity(String.class);
    return entity.toLowerCase();
  }

  /**
   * Create entity instance
   */
  protected <T> Entity<T> createEntity(T t) {
    return Entity.entity(t, MediaType.WILDCARD_TYPE);
  }

  protected void assertStatus(Response response, Status status) throws Fault {
    assertTrue(response.getStatus() == status.getStatusCode(),
        "Returned unexpected " + response.getStatus() + " status code");
  }

  protected void assertContains(Response response, String what) throws Fault {
    assertStatus(response, Status.OK);
    String entity = getEntity(response);
    assertContains(entity, what);
  }

  protected void assertContains(String responseEntity, String what)
      throws Fault {
    assertTrue(responseEntity.toLowerCase().contains(what.toLowerCase()),
        responseEntity + " does not contain expected " + what);
    logMsg("Found expected", what);
  }

  // for submit ------------------------------------------------------------

  protected void assertStatus(Future<Response> future, Status status)
      throws Fault {
    Response response;
    try {
      response = future.get();
    } catch (Exception e) {
      throw new Fault(e);
    }
    assertStatus(response, status);
    logMsg("Response cotains expected status", status);
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
    assertTrue(hasWrapped(e, ProcessingException.class),
        "ExecutionException wrapped " + e.getCause() +
        " rather then ProcessingException");
    logMsg("ExecutionException.getCause is ProcessingException as expected");
  }

  protected void //
      assertWebApplicationExceptionIsCauseAndLog(ExecutionException e)
          throws Fault {
    logMsg("ExecutionException has been thrown as expected", e);
    assertTrue(hasWrapped(e, WebApplicationException.class),
        "ExecutionException wrapped " + e.getCause() +
        " rather then WebApplicationException");
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
}
