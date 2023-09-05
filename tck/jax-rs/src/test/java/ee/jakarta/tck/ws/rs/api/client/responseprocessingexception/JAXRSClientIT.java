/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.api.client.responseprocessingexception;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.client.ResponseProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = 4199815250447993450L;

  static final Status STATUS = Status.EXPECTATION_FAILED;

  static final String MESSAGE = "Exception thrown by TCK";

  public JAXRSClientIT() {
    setContextRoot("/jaxrs_api_rs_processingexception_web");
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
   * @testName: constructorWithRuntimeExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1027; JAXRS:JAVADOC:1026;
   * 
   * @test_Strategy:Constructs a new JAX-RS runtime response processing
   * exception for a specific response with the specified cause and a detail
   * message of (cause==null ? null : cause.toString())
   * 
   * ResponseProcessingException.getResponse
   */
  @Test
  public void constructorWithRuntimeExceptionTest() throws Fault {
    Response response = buildResponse(STATUS);
    IllegalStateException ile = new IllegalStateException("TCK exception");
    ResponseProcessingException mpe = new ResponseProcessingException(response,
        ile);
    assertCause(mpe, ile);
    assertMessage(mpe, ile.getMessage());
    assertResponse(mpe);
  }

  /*
   * @testName: constructorWithCheckedExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1027; JAXRS:JAVADOC:1026;
   * 
   * @test_Strategy:Constructs a new JAX-RS runtime response processing
   * exception for a specific response with the specified cause and a detail
   * message of (cause==null ? null : cause.toString())
   * 
   * ResponseProcessingException.getResponse
   */
  @Test
  public void constructorWithCheckedExceptionTest() throws Fault {
    Response response = buildResponse(STATUS);
    IOException ioe = new IOException("TCK exception");
    ResponseProcessingException mpe = new ResponseProcessingException(response,
        ioe);
    assertCause(mpe, ioe);
    assertMessage(mpe, ioe.getMessage());
    assertResponse(mpe);
  }

  /*
   * @testName: constructorWithNullThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1027; JAXRS:JAVADOC:1026;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime processing exception with
   * the specified cause and a detail message of (cause==null ? null :
   * cause.toString())
   * 
   * ResponseProcessingException.getResponse
   */
  @Test
  public void constructorWithNullThrowableTest() throws Fault {
    Response response = buildResponse(STATUS);
    ResponseProcessingException mpe = new ResponseProcessingException(response,
        (Throwable) null);
    assertResponse(mpe);
    assertNull(mpe.getCause(),
        "getCause does not work for ResponseProcessingException and null cause");
    assertNull(mpe.getMessage(),
        "getMessage does not work for ResponseProcessingException and null cause");
  }

  /*
   * @testName: constructorWithNullThrowableNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1028; JAXRS:JAVADOC:1026;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime response processing
   * exception with the specified detail message and cause.
   * 
   * ResponseProcessingException.getResponse
   */
  @Test
  public void constructorWithNullThrowableNullMessageTest() throws Fault {
    Response response = buildResponse(STATUS);
    ResponseProcessingException mpe = new ResponseProcessingException(response,
        (String) null, (Throwable) null);
    assertResponse(mpe);
    assertNull(mpe.getCause(),
        "getCause does not work for ResponseProcessingException and null cause");
    assertNull(mpe.getMessage(),
        "getMessage does not work for ResponseProcessingException and null cause");
  }

  /*
   * @testName: constructorWithNullThrowableNotNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1028; JAXRS:JAVADOC:1026;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime response processing
   * exception with the specified detail message and cause.
   * 
   * ResponseProcessingException.getResponse
   */
  @Test
  public void constructorWithNullThrowableNotNullMessageTest() throws Fault {
    Response response = buildResponse(STATUS);
    ResponseProcessingException mpe = new ResponseProcessingException(response,
        MESSAGE, (Throwable) null);
    assertResponse(mpe);
    assertNull(mpe.getCause(),
        "getCause does not work for ResponseProcessingException and null cause and not null message");
    assertMessage(mpe, MESSAGE);
  }

  /*
   * @testName: constructorWithRuntimeExceptionNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1028; JAXRS:JAVADOC:1026;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime response processing
   * exception with the specified detail message and cause.
   * 
   * ResponseProcessingException.getResponse
   */
  @Test
  public void constructorWithRuntimeExceptionNullMessageTest() throws Fault {
    Response response = buildResponse(STATUS);
    IllegalStateException ise = new IllegalStateException(
        "JAXRS TCK exception");
    ResponseProcessingException mpe = new ResponseProcessingException(response,
        (String) null, ise);
    assertResponse(mpe);
    assertCause(mpe, ise);
    assertNull(mpe.getMessage(), "getMessage does not work for",
        "ResponseProcessingException and RuntimeException and null message");
  }

  /*
   * @testName: constructorWithCheckedExceptionNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1028; JAXRS:JAVADOC:1026;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime response processing
   * exception with the specified detail message and cause.
   * 
   * ResponseProcessingException.getResponse
   */
  @Test
  public void constructorWithCheckedExceptionNullMessageTest() throws Fault {
    Response response = buildResponse(STATUS);
    IOException ioe = new IOException("JAXRS TCK exception");
    ResponseProcessingException mpe = new ResponseProcessingException(response,
        (String) null, ioe);
    assertResponse(mpe);
    assertCause(mpe, ioe);
    assertNull(mpe.getMessage(), "getMessage does not work for",
        "ResponseProcessingException and CheckedException and null message");
  }

  /*
   * @testName: constructorWithRuntimeExceptionAndNotNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1028; JAXRS:JAVADOC:1026;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime response processing
   * exception with the specified detail message and cause.
   * 
   * ResponseProcessingException.getResponse
   */
  @Test
  public void constructorWithRuntimeExceptionAndNotNullMessageTest()
      throws Fault {
    Response response = buildResponse(STATUS);
    IllegalStateException ise = new IllegalStateException(
        "JAXRS TCK exception");
    ResponseProcessingException mpe = new ResponseProcessingException(response,
        MESSAGE, ise);
    assertResponse(mpe);
    assertCause(mpe, ise);
    assertMessage(mpe, MESSAGE);
  }

  /*
   * @testName: constructorWithCheckedExceptionAndNotNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1028; JAXRS:JAVADOC:1026;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime response processing
   * exception with the specified detail message and cause.
   * 
   * ResponseProcessingException.getResponse
   */
  @Test
  public void constructorWithCheckedExceptionAndNotNullMessageTest()
      throws Fault {
    Response response = buildResponse(STATUS);
    IOException ioe = new IOException("JAXRS TCK exception");
    ResponseProcessingException mpe = new ResponseProcessingException(response,
        MESSAGE, ioe);
    assertResponse(mpe);
    assertCause(mpe, ioe);
    assertMessage(mpe, MESSAGE);
  }

  /*
   * @testName: constructorWithNotNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1029; JAXRS:JAVADOC:1026;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime response processing
   * exception with the specified detail message. The cause is not initialized
   * 
   * ResponseProcessingException.getResponse
   */
  @Test
  public void constructorWithNotNullMessageTest() throws Fault {
    Response response = buildResponse(STATUS);
    ResponseProcessingException mpe = new ResponseProcessingException(response,
        MESSAGE);
    assertNull(mpe.getCause(),
        "getCause does not work for ResponseProcessingException and not null message");
    assertResponse(mpe);
    assertMessage(mpe, MESSAGE);
  }

  /*
   * @testName: constructorWithNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1029; JAXRS:JAVADOC:1026;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime response processing
   * exception with the specified detail message. The cause is not initialized
   * 
   * ResponseProcessingException.getResponse
   */
  @Test
  public void constructorWithNullMessageTest() throws Fault {
    Response response = buildResponse(STATUS);
    ResponseProcessingException mpe = new ResponseProcessingException(response,
        (String) null);
    assertResponse(mpe);
    assertNull(mpe.getCause(),
        "getCause does not work for ResponseProcessingException and null message");
    assertNull(mpe.getMessage(),
        "getMessage does not work for ResponseProcessingException and null message");
  }

  // /////////////////////////////////////////////////////////////
  protected Response buildResponse(Status status) {
    return Response.status(status).build();
  }

  protected void assertResponse(ResponseProcessingException e) throws Fault {
    assertNotNull(e.getResponse(), "#getResponse is null");
    Response response = e.getResponse();
    assertEqualsInt(response.getStatus(), STATUS.getStatusCode(),
        "response contains unexpected status", response.getStatus());
    logMsg("response contains expected", STATUS, "status");
  }

  protected void assertCause(ResponseProcessingException e, Throwable expected)
      throws Fault {
    assertEquals(e.getCause(), expected, "#getCause does not contain expected",
        expected, "but", e.getCause());
    logMsg("getCause contains expected", expected);
  }

  protected void assertMessage(ResponseProcessingException e, String message)
      throws Fault {
    assertNotNull(e.getMessage(), "#getMessage is null");
    assertContains(e.getMessage(), message,
        "ResponseProcessingException#getMessage()",
        "does not contain expected message", message, "but", e.getMessage());
    logMsg("ResponseProcessingException#getMesaage contains expected message",
        message);
  }
}
