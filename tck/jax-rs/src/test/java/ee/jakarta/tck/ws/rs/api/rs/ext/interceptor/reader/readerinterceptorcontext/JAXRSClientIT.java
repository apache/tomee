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

package ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.reader.readerinterceptorcontext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.TemplateInterceptorBody;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.reader.ReaderClient;
import ee.jakarta.tck.ws.rs.common.client.TextCaser;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends ReaderClient<ContextOperation> {

  private static final long serialVersionUID = -6962070973647934636L;

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
   * @testName: getHeadersOperationSetTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:923; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Get mutable map of HTTP headers.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void getHeadersOperationSetTest() throws Fault {
    ResponseBuilder builder = createResponse(ContextOperation.GETHEADERS);
    Response fake = builder.build();

    addProviders(fake);
    setProperty(Property.SEARCH_STRING_IGNORE_CASE,
        TemplateInterceptorBody.OPERATION);
    invoke();
  }

  /*
   * @testName: getHeadersHeadersSetTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:923; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Get mutable map of HTTP headers.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void getHeadersHeadersSetTest() throws Fault {
    ResponseBuilder builder = createResponse(ContextOperation.GETHEADERS);
    for (int i = 0; i != 5; i++) {
      setProperty(Property.UNORDERED_SEARCH_STRING,
          TemplateInterceptorBody.PROPERTY + i);
      builder = builder.header(TemplateInterceptorBody.PROPERTY + i, "any");
    }
    Response fake = builder.build();

    addProviders(fake);
    setTextCaser(TextCaser.LOWER);
    setProperty(Property.UNORDERED_SEARCH_STRING,
        TemplateInterceptorBody.OPERATION);
    invoke();
  }

  /* Run test */
  /*
   * @testName: getHeadersIsMutableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:923; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Get mutable map of HTTP headers.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void getHeadersIsMutableTest() throws Fault {
    ResponseBuilder builder = createResponse(
        ContextOperation.GETHEADERSISMUTABLE);
    Response fake = builder.build();

    addProviders(fake);
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.PROPERTY);
    invoke();
  }

  /*
   * @testName: getInputStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:924; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Get the input stream of the object to be read.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void getInputStreamTest() throws Fault {
    String entity = "getInputStreamEntity";
    ResponseBuilder builder = createResponse(ContextOperation.GETINPUTSTREAM);
    Response fake = builder.entity(entity).build();

    addProviders(fake);
    setProperty(Property.SEARCH_STRING, entity);
    invoke();
  }

  /*
   * @testName: proceedThrowsIOExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:925; JAXRS:JAVADOC:926; JAXRS:JAVADOC:920;
   * JAXRS:SPEC:85;
   * 
   * @test_Strategy: Throws: IOException - if an IO error arises
   * 
   * Proceed is tested in any of the interceptor tests.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void proceedThrowsIOExceptionTest() throws Fault {
    ResponseBuilder builder = createResponse(
        ContextOperation.PROCEEDTHROWSIOEXCEPTION);
    Response fake = builder.build();

    addProviders(fake);
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.IOE);
    invoke();
  }

  /*
   * @testName: proceedThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:925; JAXRS:JAVADOC:1008; JAXRS:JAVADOC:920;
   * JAXRS:SPEC:85;
   * 
   * @test_Strategy: Throws: WebApplicationException - thrown by the wrapped
   * {@code MessageBodyReader.readFrom} method.
   * 
   * Proceed is tested in any of the intercepter tests.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void proceedThrowsWebApplicationExceptionTest() throws Fault {
    ResponseBuilder builder = createResponse(
        ContextOperation.PROCEEDTHROWSWEBAPPEXCEPTION);
    Response fake = builder.build();
    addProviders(fake);
    addProvider(ExceptionThrowingStringBeanEntityProvider.class);
    invoke();
    // Exception thrown, caught in InterceptorBodyOne
    StringBean bean = getResponseBody(ExceptionThrowingStringBean.class);
    //
    assertContains(bean.get(), TemplateInterceptorBody.WAE,
        "WebApplicationException has not been thrown and the message is",
        bean.get());
    logMsg("WebApplicationException has been thrown as expected", bean.get());
  }

  /*
   * @testName: setInputStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:927; JAXRS:JAVADOC:920; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Update the input stream of the object to be read.
   * 
   * ReaderInterceptor.aroundReadFrom If abortWith is invoked, execution is
   * aborted
   */
  @Test
  public void setInputStreamTest() throws Fault {
    ResponseBuilder builder = createResponse(ContextOperation.SETINPUTSTREAM);
    Response fake = builder.build();

    addProviders(fake);
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.ENTITY2);
    invoke();
  }

  // ///////////////////////////////////////////////////////////////////////
  /**
   * Do not forget to register providers
   */
  @Override
  protected void addProviders(Response response) throws Fault {
    super.addProviders(response);
    addProvider(ReaderInterceptorTwo.class);
    addProvider(ReaderInterceptorOne.class);
  }
}
