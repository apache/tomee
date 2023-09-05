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

package ee.jakarta.tck.ws.rs.api.rs.badrequestexception;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
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

  private static final long serialVersionUID = -7606579523483319730L;

  private static final Status STATUS = Status.BAD_REQUEST;

  protected static final String MESSAGE = "TCK BadRequestException description";

  protected static final String HOST = "www.jcp.org";

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  /*
   * @testName: constructorTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:305; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new bad client request exception.
   * 
   * getResponse
   */
  @Test
  public void constructorTest() throws Fault {
    BadRequestException e = new BadRequestException();
    assertResponse(e);

  }

  /*
   * @testName: constructorResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:306; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new bad client request exception. throws -
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 400.
   * 
   * getResponse
   */
  @Test
  public void constructorResponseTest() throws Fault {
    Response response = buildResponse();
    BadRequestException e = new BadRequestException(response);
    assertResponse(e, HOST);
  }

  /*
   * @testName: constructorResponseThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:306;
   * 
   * @test_Strategy: Construct a new bad client request exception. throws -
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 400.
   */
  @Test
  public void constructorResponseThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS)
        try {
          Response response = Response.status(status).build();
          BadRequestException e = new BadRequestException(response);
          fault("Exception has been not been thrown for response with status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg("IllegalArgumentException has been thrown for status", status,
              "as expected");
        }
  }

  /*
   * @testName: constructorThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:307; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new bad client request exception. cause - the
   * underlying cause of the exception.
   * 
   * getResponse
   */
  @Test
  public void constructorThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable throwable : throwables) {
      BadRequestException e = new BadRequestException(throwable);
      assertResponse(e);
      assertCause(e, throwable);
    }
  }

  /*
   * @testName: constructorResponseThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:308; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new bad client request exception. throws -
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 400.
   * 
   * getResponse
   */
  @Test
  public void constructorResponseThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    Response response = buildResponse();
    for (Throwable throwable : throwables) {
      BadRequestException e = new BadRequestException(response, throwable);
      assertResponse(e, HOST);
      assertCause(e, throwable);
    }
  }

  /*
   * @testName: constructorResponseThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:308;
   * 
   * @test_Strategy: Construct a new bad client request exception. throws -
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 400.
   */
  @Test
  public void constructorResponseThrowableThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS)
        try {
          Response response = Response.status(status).build();
          BadRequestException e = new BadRequestException(response,
              new Throwable());
          fault("Exception has been not been thrown for response with status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg("IllegalArgumentException has been thrown for status", status,
              "as expected");
        }
  }

  /*
   * @testName: constructorStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1056;
   * 
   * @test_Strategy: message - the detail message (which is saved for later
   * retrieval by the Throwable.getMessage() method).
   */
  @Test
  public void constructorStringTest() throws Fault {
    BadRequestException e = new BadRequestException(MESSAGE);
    assertMessage(e);
    assertResponse(e);
  }

  /*
   * @testName: constructorStringResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1057;
   * 
   * @test_Strategy: message - the detail message (which is saved for later
   * retrieval by the Throwable.getMessage() method).
   */
  @Test
  public void constructorStringResponseTest() throws Fault {
    Response response = buildResponse();
    BadRequestException e = new BadRequestException(MESSAGE, response);
    assertResponse(e, HOST);
    assertMessage(e);
  }

  /*
   * @testName: constructorStringResponseThrowsIEATest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1057;
   * 
   * @test_Strategy: Construct a new bad client request exception. throws -
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 400.
   */
  @Test
  public void constructorStringResponseThrowsIEATest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS)
        try {
          Response response = Response.status(status).build();
          BadRequestException e = new BadRequestException(MESSAGE, response);
          fault("Exception has been not been thrown for response with status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg("IllegalArgumentException has been thrown for status", status,
              "as expected");
        }
  }

  /*
   * @testName: constructorStringThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1058;
   * 
   * @test_Strategy: Construct a new bad client request exception.
   */
  @Test
  public void constructorStringThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable throwable : throwables) {
      BadRequestException e = new BadRequestException(MESSAGE, throwable);
      assertMessage(e);
      assertCause(e, throwable);
      assertResponse(e);
    }
  }

  /*
   * @testName: constructorStringRequestThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1059;
   * 
   * @test_Strategy: Construct a new bad client request exception.
   */
  @Test
  public void constructorStringRequestThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    Response response = buildResponse();
    for (Throwable throwable : throwables) {
      BadRequestException e = new BadRequestException(MESSAGE, response,
          throwable);
      assertMessage(e);
      assertCause(e, throwable);
      assertResponse(e, HOST);
    }
  }

  /*
   * @testName: constructorStringRequestThrowableThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1059;
   * 
   * @test_Strategy: Construct a new bad client request exception. throws -
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 400.
   */
  @Test
  public void constructorStringRequestThrowableThrowsIAETest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS)
        try {
          Response response = Response.status(status).build();
          BadRequestException e = new BadRequestException(MESSAGE, response,
              new Throwable());
          fault("Exception has been not been thrown for response with status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg("IllegalArgumentException has been thrown for status", status,
              "as expected");
        }
  }

  // /////////////////////////////////////////////////////////////
  protected Response buildResponse() {
    Response r = Response.status(STATUS).header(HttpHeaders.HOST, HOST).build();
    return r;
  }

  protected void assertResponse(BadRequestException e) throws Fault {
    assertNotNull(e.getResponse(), "#getResponse is null");
    Response response = e.getResponse();
    assertEqualsInt(response.getStatus(), STATUS.getStatusCode(),
        "response contains unexpected status", response.getStatus());
    logMsg("response contains expected", STATUS, "status");
  }

  /**
   * Check the given exception contains a prebuilt response containing the http
   * header HOST
   */
  protected void assertResponse(BadRequestException e, String host)
      throws Fault {
    assertResponse(e);
    String header = e.getResponse().getHeaderString(HttpHeaders.HOST);
    assertNotNull(header, "http header", HttpHeaders.HOST,
        " of response is null");
    assertEquals(host, header, "Found unexpected http", HttpHeaders.HOST,
        "header", header);
    logMsg("Found expected http", HttpHeaders.HOST, "header");
  }

  protected void assertMessage(BadRequestException e) throws Fault {
    assertNotNull(e.getMessage(), "getMessage() is null");
    assertContains(e.getMessage(), MESSAGE, "Unexpected getMessage()",
        e.getMessage());
    logMsg("found expected getMessage()=", e.getMessage());
  }

  protected void assertCause(WebApplicationException e, Throwable expected)
      throws Fault {
    assertEquals(e.getCause(), expected, "#getCause does not contain expected",
        expected, "but", e.getCause());
    logMsg("getCause contains expected", expected);
  }
}
