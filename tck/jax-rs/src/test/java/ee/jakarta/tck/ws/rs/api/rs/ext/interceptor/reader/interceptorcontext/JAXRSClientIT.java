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

package ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.reader.interceptorcontext;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.ContextOperation;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.InputStreamReaderProvider;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.TemplateInterceptorBody;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.reader.ReaderClient;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends ReaderClient<ContextOperation> {

  private static final long serialVersionUID = -8828149277776372718L;

  public JAXRSClientIT() {
    setup();
  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }
  
  /*
   * @testName: getAnnotationsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:903; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Get an array of the annotations formally declared on the
   * artifact that initiated the intercepted entity provider invocation. The
   * annotations are not on client reader.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void getAnnotationsTest() throws Fault {
    Annotation[] annotations = ContextOperation.class.getAnnotations();
    ResponseBuilder builder = createResponse(ContextOperation.GETANNOTATIONS);
    Response fake = builder.entity(TemplateInterceptorBody.ENTITY, annotations)
        .build();
    addProviders(fake);
    for (Annotation a : annotations)
      setProperty(Property.UNEXPECTED_RESPONSE_MATCH,
          a.annotationType().getName());
    invoke();
  }

  /*
   * @testName: getGenericTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:904; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Get an array of the annotations formally declared on the
   * artifact that initiated the intercepted entity provider invocation.
   * 
   * If abortWith is invoked, execution is aborted
   */
  @Test
  public void getGenericTypeTest() throws Fault {
    ResponseBuilder builder = createResponse(ContextOperation.GETGENERICTYPE);
    Response fake = builder.build();

    addProviders(fake);
    setProperty(Property.SEARCH_STRING, String.class.getName());
    invoke();
  }

  /*
   * @testName: getMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:905; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Get media type of HTTP entity.
   *
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void getMediaTypeTest() throws Fault {
    ResponseBuilder builder = createResponse(ContextOperation.GETMEDIATYPE);
    Response fake = builder.type(MediaType.APPLICATION_JSON_TYPE).build();

    addProviders(fake);
    setProperty(Property.SEARCH_STRING, MediaType.APPLICATION_JSON);
    invoke();
  }

  /*
   * @testName: getPropertyIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:906; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Returns null if there is no property by that name.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void getPropertyIsNullTest() throws Fault {
    ResponseBuilder builder = createResponse(ContextOperation.GETPROPERTY);
    Response fake = builder.build();

    addProviders(fake);
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.NULL);
    invoke();
  }

  /*
   * @testName: getPropertyNamesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1007; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Returns immutable java.util.Collection collection
   * containing the property names available within the context of the current
   * request/response exchange context.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void getPropertyNamesTest() throws Fault {
    ResponseBuilder builder = createResponse(ContextOperation.GETPROPERTYNAMES);
    Response fake = builder.build();

    addProviders(fake);
    for (int i = 0; i != 5; i++)
      setProperty(Property.UNORDERED_SEARCH_STRING,
          TemplateInterceptorBody.PROPERTY + i);
    invoke();
  }

  /*
   * @testName: getPropertyNamesIsReadOnlyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1007; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Returns immutable java.util.Collection containing the
   * property names available within the context of the current request/response
   * exchange context.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void getPropertyNamesIsReadOnlyTest() throws Fault {
    ResponseBuilder builder = createResponse(
        ContextOperation.GETPROPERTYNAMESISREADONLY);
    Response fake = builder.build();

    addProviders(fake);
    setProperty(Property.UNORDERED_SEARCH_STRING, TemplateInterceptorBody.NULL);
    invoke();
  }

  /*
   * @testName: getTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:908; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Get Java type supported by corresponding message body
   * provider.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void getTypeTest() throws Fault {
    ResponseBuilder builder = createResponse(ContextOperation.GETTYPE);
    Response fake = builder.build();

    addProviders(fake);
    setProperty(Property.SEARCH_STRING, String.class.getName());
    invoke();
  }

  /*
   * @testName: removePropertyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:909; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Removes a property with the given name from the current
   * request/response exchange context. After removal, subsequent calls to
   * getProperty(java.lang.String) to retrieve the property value will return
   * null.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void removePropertyTest() throws Fault {
    ResponseBuilder builder = createResponse(ContextOperation.REMOVEPROPERTY);
    Response fake = builder.build();

    addProviders(fake);
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.NULL);
    invoke();
  }

  /*
   * @testName: setAnnotationsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:910; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Update annotations on the formal declaration of the
   * artifact that initiated the intercepted entity provider invocation.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void setAnnotationsTest() throws Fault {
    Annotation[] annotations = ReaderInterceptorOne.class.getAnnotations();
    ResponseBuilder builder = createResponse(ContextOperation.SETANNOTATIONS);
    Response fake = builder.build();
    addProviders(fake);
    for (Annotation a : annotations)
      setProperty(Property.UNORDERED_SEARCH_STRING,
          a.annotationType().getName());
    invoke();
  }

  /*
   * @testName: setAnnotationsNullThrowsNPETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:910; JAXRS:JAVADOC:920;
   * 
   * @test_Strategy: Throws NullPointerException - in case the input parameter
   * is null.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void setAnnotationsNullThrowsNPETest() throws Fault {
    ResponseBuilder builder = createResponse(
        ContextOperation.SETANNOTATIONSNULL);
    Response fake = builder.entity(TemplateInterceptorBody.ENTITY).build();
    addProviders(fake);
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.NPE);
    invoke();
  }

  /*
   * @testName: setGenericTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:911; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Update type of the object to be produced or written.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void setGenericTypeTest() throws Fault {
    ResponseBuilder builder = createResponse(ContextOperation.SETGENERICTYPE);
    Response fake = builder.build();

    addProviders(fake);
    setProperty(Property.SEARCH_STRING, "[B");
    invoke();
  }

  /*
   * @testName: setMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:912; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Update media type of HTTP entity.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void setMediaTypeTest() throws Fault {
    ResponseBuilder builder = createResponse(ContextOperation.SETMEDIATYPE);
    Response fake = builder.build();

    addProviders(fake);
    setProperty(Property.SEARCH_STRING, MediaType.APPLICATION_FORM_URLENCODED);
    invoke();
  }

  /*
   * @testName: setPropertyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:913; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Binds an object to a given property name in the current
   * request/response exchange context. If the name specified is already used
   * for a property, this method will replace the value of the property with the
   * new value.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void setPropertyTest() throws Fault {
    ResponseBuilder builder = createResponse(ContextOperation.SETPROPERTY);
    Response fake = builder.build();

    addProviders(fake);
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.ENTITY2);
    invoke();
  }

  /*
   * @testName: setPropertyNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:913; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: If a null value is passed, the effect is the same as
   * calling the removeProperty(String) method.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void setPropertyNullTest() throws Fault {
    ResponseBuilder builder = createResponse(ContextOperation.SETPROPERTYNULL);
    Response fake = builder.build();

    addProviders(fake);
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.NULL);
    invoke();
  }

  /*
   * @testName: setTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:914; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Update Java type before calling message body provider.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void setTypeTest() throws Fault {
    ResponseBuilder builder = createResponse(ContextOperation.SETTYPE);
    ByteArrayInputStream bais = new ByteArrayInputStream(
        TemplateInterceptorBody.ENTITY.getBytes());
    Reader reader = new InputStreamReader(bais);
    Response fake = builder.entity(reader).build();

    addProviders(fake);
    addProvider(InputStreamReaderProvider.class);
    invoke();
    InputStreamReader isr = getResponseBody(InputStreamReader.class);
    try {
      String entity = JaxrsUtil.readFromReader(isr);
      assertTrue(entity.contains(InputStreamReader.class.getName()),
          "Expected " + InputStreamReader.class.getName() + " not found");
      logMsg("#setType set correct type", entity);
    } catch (IOException e) {
      throw new Fault(e);
    }
  }

  /*
   * @testName: ioExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:921; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Throws IOException - if an IO error arises.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void ioExceptionTest() throws Fault {
    ResponseBuilder builder = createResponse(ContextOperation.IOEXCEPTION);
    Response fake = builder.build();
    addProviders(fake);
    invoke();
    try {
      getResponseBody();
    } catch (Exception e) {
      assertNotNull(e.getMessage(), "Returned unexpected exception", e);
      IOException io = assertCause(e, IOException.class,
          "Unexpected exception has been found:", e);
      assertContains(io.getMessage(), TemplateInterceptorBody.IOE,
          "Found unexpected message from IOException", e.getMessage());
      logMsg("found expected IOException", io);
      return;
    }
    fault("Expected IOException not found");
  }

  /*
   * @testName: webApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:922; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: throws WebApplicationException thrown by wrapped method.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void webApplicationExceptionTest() throws Fault {
    ResponseBuilder builder = createResponse(
        ContextOperation.WEBAPPLICATIONEXCEPTION);
    Response fake = builder.build();
    addProviders(fake);
    invoke();
    try {
      getResponseBody();
    } catch (Exception e) {
      WebApplicationException we = assertCause(e, WebApplicationException.class,
          "Found unexpected exception", e);
      assertNotNull(we.getResponse(),
          "WebApplicationException.getResponse is null");
      Response response = we.getResponse();
      String entity = response.getEntity().toString();
      assertEqualsInt(Status.CONFLICT.getStatusCode(), response.getStatus(),
          "Unexcpected status returned from WebApplicationException.getResponse",
          response.getStatus());
      assertEquals(TemplateInterceptorBody.ENTITY2, entity,
          "Found unexpected body content from WebApplicationException.getResponse",
          entity);
      logMsg("found expected WebApplicationException", we);
      return;
    }
    fault("Expected WebApplicationException not found");
  }

  // /////////////////////////////////////////////////////////////////////
  @Override
  protected void addProviders(Response response) throws Fault {
    super.addProviders(response);
    addProvider(ReaderInterceptorTwo.class);
    addProvider(new ReaderInterceptorOne());
  }
}
