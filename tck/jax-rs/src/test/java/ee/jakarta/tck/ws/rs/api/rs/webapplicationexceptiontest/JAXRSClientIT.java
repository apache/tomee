/*
 * Copyright (c) 2011, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.api.rs.webapplicationexceptiontest;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * WebApplicationException tests are also at ee/resource/webappexception/nomapper
 */
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = 8214271241869777148L;

  private static final Status STATUS = Status.INTERNAL_SERVER_ERROR;

  protected static final String MESSAGE = "TCK WebApplicationException description";

  protected static final String HOST = "www.jcp.org";

  public JAXRSClientIT() {
    setContextRoot("/jaxrs_api_rs_webapplicationexceptiontest_web");
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
   * @class.setup_props: webServerHost; webServerPort; ts_home;
   */

  /*
   * @testName: statusNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:16;
   * 
   * @test_Strategy: Verify that WebApplicationException(Status Null) works.
   */
  @Test
  public void statusNullTest() throws Fault {
    Response.Status st = null;
    try {
      WebApplicationException e = new WebApplicationException(st);
      throw new Fault("No exception thrown.  Test FAILED", e);
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace("Expected exception caught.  Test PASSED");
    } catch (Throwable th) {
      throw new Fault("Wrong exception caught.  Test FAILED", th.getCause());
    }
  }

  /*
   * @testName: throwableStatusTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:20;
   * 
   * @test_Strategy: Verify that WebApplicationException(Throwable, Status Null)
   * works.
   */
  @Test
  public void throwableStatusTest1() throws Fault {
    Response.Status st = null;
    try {
      WebApplicationException e = new WebApplicationException(new Throwable(
          "CTS-WebApplicationExceptionTest-throwableStatusTest1-FAIL"), st);
      throw new Fault("No exception thrown.  Test FAILED", e);
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace("Expected exception thrown.  Test PASS");
    } catch (Throwable th) {
      throw new Fault("Wrong-Exception", th.getCause());
    }
  }

  /*
   * @testName: constructorStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1111; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new instance with a blank message and default
   * HTTP status code of 500.
   * 
   * getResponse
   */
  @Test
  public void constructorStringTest() throws Fault {
    WebApplicationException e = new WebApplicationException(MESSAGE);
    assertResponse(e, STATUS);
    assertMessage(e);
  }

  /*
   * @testName: constructorStringResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1112; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new instance using the supplied response.
   * 
   * getResponse
   */
  @Test
  public void constructorStringResponseTest() throws Fault {
    for (Status status : Status.values()) {
      Response response = buildResponse(status);
      WebApplicationException e = new WebApplicationException(MESSAGE,
          response);
      assertResponse(e, status, HOST);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringNullResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1112; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new instance using the supplied response. a
   * value of null will be replaced with an internal server error response
   * (status code 500).
   * 
   * getResponse
   */
  @Test
  public void constructorStringNullResponseTest() throws Fault {
    WebApplicationException e = new WebApplicationException(MESSAGE,
        (Response) null);
    assertResponse(e, STATUS);
    assertMessage(e);
  }

  /*
   * @testName: constructorStringIntTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1113; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new instance with a message and specified HTTP
   * status code.
   * 
   * getResponse
   */
  @Test
  public void constructorStringIntTest() throws Fault {
    for (Status status : Status.values()) {
      WebApplicationException e = new WebApplicationException(MESSAGE,
          status.getStatusCode());
      assertResponse(e, status);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringStatusTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1114; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new instance with a message and specified HTTP
   * status code.
   * 
   * getResponse
   */
  @Test
  public void constructorStringStatusTest() throws Fault {
    for (Status status : Status.values()) {
      WebApplicationException e = new WebApplicationException(MESSAGE, status);
      assertResponse(e, status);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringNullStatusThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1114;
   * 
   * @test_Strategy: Throws java.lang.IllegalArgumentException - if status is
   * null.
   */
  @Test
  public void constructorStringNullStatusThrowsIAETest() throws Fault {
    try {
      WebApplicationException e = new WebApplicationException(MESSAGE,
          (Status) null);
      fault("No IllegalArgumentException has been thrown, built expcetion is",
          e);
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected when",
          "null Status");
    }
  }

  /*
   * @testName: constructorStringThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1115; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new instance with a message and default HTTP
   * status code of 500.
   * 
   * getResponse
   */
  @Test
  public void constructorStringThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      WebApplicationException e = new WebApplicationException(MESSAGE, t);
      assertResponse(e, STATUS);
      assertMessage(e);
      assertCause(e, t);
    }
  }

  /*
   * @testName: constructorStringThrowableResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1116; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new instance using the supplied response.
   * 
   * getResponse
   */
  @Test
  public void constructorStringThrowableResponseTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Status status : Status.values()) {
      Response response = buildResponse(status);
      for (Throwable t : throwables) {
        WebApplicationException e = new WebApplicationException(MESSAGE, t,
            response);
        assertResponse(e, status, HOST);
        assertMessage(e);
        assertCause(e, t);
      }
    }
  }

  /*
   * @testName: constructorStringThrowableNullResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1116; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new instance using the supplied response. a
   * value of null will be replaced with an internal server error response
   * (status code 500).
   * 
   * getResponse
   */
  @Test
  public void constructorStringThrowableNullResponseTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      WebApplicationException e = new WebApplicationException(MESSAGE, t,
          (Response) null);
      assertResponse(e, STATUS);
      assertMessage(e);
      assertCause(e, t);
    }
  }

  /*
   * @testName: constructorStringThrowableIntTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1117; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new instance with a message and specified HTTP
   * status code.
   * 
   * getResponse
   */
  @Test
  public void constructorStringThrowableIntTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Status status : Status.values()) {
      for (Throwable t : throwables) {
        WebApplicationException e = new WebApplicationException(MESSAGE, t,
            status.getStatusCode());
        assertResponse(e, status);
        assertMessage(e);
        assertCause(e, t);
      }
    }
  }

  /*
   * @testName: constructorStringThrowableStatusTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1118; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new instance with a message and specified HTTP
   * status code.
   * 
   * getResponse
   */
  @Test
  public void constructorStringThrowableStatusTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Status status : Status.values()) {
      for (Throwable t : throwables) {
        WebApplicationException e = new WebApplicationException(MESSAGE, t,
            status);
        assertResponse(e, status);
        assertMessage(e);
        assertCause(e, t);
      }
    }
  }

  /*
   * @testName: constructorStringThrowableNullStatusThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1118;
   * 
   * @test_Strategy: Throws java.lang.IllegalArgumentException - if status is
   * null.
   */
  @Test
  public void constructorStringThrowableNullStatusThrowsIAETest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      try {
        WebApplicationException e = new WebApplicationException(MESSAGE, t,
            (Status) null);
        fault("IllegalArgumentException has not been thrown, exception:", e);
      } catch (IllegalArgumentException e) {
        logMsg("IllegalArgumentException has been thrown when",
            "null Status as expected");
      }
    }
  }

  // /////////////////////////////////////////////////////////////
  protected Response buildResponse(Status status) {
    return Response.status(status).header(HttpHeaders.HOST, HOST).build();
  }

  protected static void assertResponse(WebApplicationException e, Status status)
      throws Fault {
    assertNotNull(e.getResponse(), "#getResponse is null");
    Response response = e.getResponse();
    assertEqualsInt(response.getStatus(), status.getStatusCode(),
        "response contains unexpected status", response.getStatus(),
        "response:", response);
    logMsg("response contains expected", status, "status");
  }

  /**
   * Check the given exception contains a prebuilt response containing the http
   * header HOST
   */
  protected void assertResponse(WebApplicationException e, Status status,
      String host) throws Fault {
    assertResponse(e, status);
    String header = e.getResponse().getHeaderString(HttpHeaders.HOST);
    assertNotNull(header, "http header", HttpHeaders.HOST,
        " of response is null");
    assertEquals(host, header, "Found unexpected http", HttpHeaders.HOST,
        "header", header);
    logMsg("Found expected http", HttpHeaders.HOST, "header");
  }

  protected static void assertCause(WebApplicationException e,
      Throwable expected) throws Fault {
    assertEquals(e.getCause(), expected, "#getCause does not contain expected",
        expected, "but", e.getCause());
    logMsg("getCause contains expected", expected);
  }

  protected static void assertMessage(WebApplicationException e) throws Fault {
    assertNotNull(e.getMessage(), "getMessage() is null");
    assertContains(e.getMessage(), MESSAGE, "Unexpected getMessage()",
        e.getMessage());
    logMsg("found expected getMessage()=", e.getMessage());
  }
}
