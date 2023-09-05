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

package ee.jakarta.tck.ws.rs.api.rs.notfoundexception;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.NotFoundException;
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

  private static final long serialVersionUID = -7425306771761061203L;

  private static final Status STATUS = Status.NOT_FOUND;

  protected static final String MESSAGE = "TCK NotFoundException description";

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
   * @assertion_ids: JAXRS:JAVADOC:339; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not found" exception.
   * 
   * getResponse
   */
  @Test
  public void constructorTest() throws Fault {
    NotFoundException e = new NotFoundException();
    assertResponse(e);
  }

  /*
   * @testName: constructorResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:340; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not found" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 404.
   * 
   * getResponse
   */
  @Test
  public void constructorResponseTest() throws Fault {
    NotFoundException e = new NotFoundException(buildResponse(STATUS));
    assertResponse(e, HOST);
  }

  /*
   * @testName: constructorResponseThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:340;
   * 
   * @test_Strategy: Construct a new "not found" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 404.
   */
  @Test
  public void constructorResponseThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          NotFoundException e = new NotFoundException(buildResponse(status));
          fault("IllegalArgumentException has not been thrown for status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
      }
  }

  /*
   * @testName: constructorThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:341; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not found" exception.
   * 
   * getResponse
   */
  @Test
  public void constructorThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      NotFoundException e = new NotFoundException(t);
      assertResponse(e);
      assertCause(e, t);
    }
  }

  /*
   * @testName: constructorResponseThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:342; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not found" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 404. getResponse
   */
  @Test
  public void constructorResponseThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      NotFoundException e = new NotFoundException(buildResponse(STATUS), t);
      assertResponse(e, HOST);
      assertCause(e, t);
    }
  }

  /*
   * @testName: constructorResponseThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:342;
   * 
   * @test_Strategy: Construct a new "not found" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 404.
   */
  @Test
  public void constructorResponseThrowableThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          NotFoundException e = new NotFoundException(buildResponse(status),
              new Throwable());
          fault("IllegalArgumentException has not been thrown for status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
      }
  }

  /*
   * @testName: constructorStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1087; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not found" exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringTest() throws Fault {
    NotFoundException e = new NotFoundException(MESSAGE);
    assertResponse(e);
    assertMessage(e);
  }

  /*
   * @testName: constructorStringResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1088; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not found" exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringResponseTest() throws Fault {
    NotFoundException e = new NotFoundException(MESSAGE, buildResponse(STATUS));
    assertResponse(e, HOST);
    assertMessage(e);
  }

  /*
   * @testName: constructorStringResponseThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1088;
   * 
   * @test_Strategy: Construct a new "not found" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 404.
   */
  @Test
  public void constructorStringResponseThrowsIAETest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          NotFoundException e = new NotFoundException(MESSAGE,
              buildResponse(status));
          fault("IllegalArgumentException has not been thrown for status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
      }
  }

  /*
   * @testName: constructorStringThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1089; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not found" exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      NotFoundException e = new NotFoundException(MESSAGE, t);
      assertResponse(e);
      assertCause(e, t);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringResponseThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1090; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not found" exception. getResponse
   */
  @Test
  public void constructorStringResponseThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      NotFoundException e = new NotFoundException(MESSAGE,
          buildResponse(STATUS), t);
      assertResponse(e, HOST);
      assertCause(e, t);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringResponseThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1090;
   * 
   * @test_Strategy: Construct a new "not found" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 404.
   */
  @Test
  public void constructorStringResponseThrowableThrowsExceptionTest()
      throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          NotFoundException e = new NotFoundException(MESSAGE,
              buildResponse(status), new Throwable());
          fault("IllegalArgumentException has not been thrown for status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
      }
  }

  // /////////////////////////////////////////////////////////////
  protected Response buildResponse(Status status) {
    return Response.status(status).header(HttpHeaders.HOST, HOST).build();
  }

  protected void assertResponse(WebApplicationException e) throws Fault {
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
  protected void assertResponse(WebApplicationException e, String host)
      throws Fault {
    assertResponse(e);
    String header = e.getResponse().getHeaderString(HttpHeaders.HOST);
    assertNotNull(header, "http header", HttpHeaders.HOST,
        " of response is null");
    assertEquals(host, header, "Found unexpected http", HttpHeaders.HOST,
        "header", header);
    logMsg("Found expected http", HttpHeaders.HOST, "header");
  }

  protected void assertCause(WebApplicationException e, Throwable expected)
      throws Fault {
    assertEquals(e.getCause(), expected, "#getCause does not contain expected",
        expected, "but", e.getCause());
    logMsg("getCause contains expected", expected);
  }

  protected void assertMessage(WebApplicationException e) throws Fault {
    assertNotNull(e.getMessage(), "getMessage() is null");
    assertContains(e.getMessage(), MESSAGE, "Unexpected getMessage()",
        e.getMessage());
    logMsg("found expected getMessage()=", e.getMessage());
  }

}
