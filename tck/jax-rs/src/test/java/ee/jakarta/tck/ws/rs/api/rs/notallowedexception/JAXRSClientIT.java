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

package ee.jakarta.tck.ws.rs.api.rs.notallowedexception;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.NotAllowedException;
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

  private static final long serialVersionUID = 6905238461163637999L;

  private static final Status STATUS = Status.METHOD_NOT_ALLOWED;

  protected static final String MESSAGE = "TCK NotAllowedException description";

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
   * @testName: constructorStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1078; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new method not allowed exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringTest() throws Fault {
    NotAllowedException e = new NotAllowedException(Request.OPTIONS.name(),
        new String[] { Request.HEAD.name() });
    assertResponse(e);
  }

  /*
   * @testName: constructorStringThrowsNPETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1078;
   * 
   * @test_Strategy: Throws: java.lang.NullPointerException - in case the
   * allowed method is null.
   */
  @Test
  public void constructorStringThrowsNPETest() throws Fault {
    try {
      NotAllowedException e = new NotAllowedException((String) null,
          new String[] { Request.HEAD.name() });
      fault(
          "NullPointerException has not been thrown for null method, built exception",
          e);
    } catch (NullPointerException e) {
      logMsg(
          "NullPointerException has been thrown as expected, for null method");
    }
  }

  /*
   * @testName: constructorResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:331; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new method not allowed exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 405.
   * 
   * getResponse
   */
  @Test
  public void constructorResponseTest() throws Fault {
    NotAllowedException e = new NotAllowedException(buildResponse(STATUS));
    assertResponse(e, HOST);
  }

  /*
   * @testName: constructorResponseThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:331;
   * 
   * @test_Strategy: Construct a new method not allowed exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 405.
   */
  @Test
  public void constructorResponseThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          NotAllowedException e = new NotAllowedException(
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
   * @testName: constructorResponseDoesNotThrowWhenNoAllowHeaderTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:331; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Note that this constructor does not validate the presence
   * of HTTP Allow header. I.e. it is possible to use the constructor to create
   * a client-side exception instance even for an invalid HTTP 405 response
   * content returned from a server.
   * 
   * getResponse
   */
  @Test
  public void constructorResponseDoesNotThrowWhenNoAllowHeaderTest()
      throws Fault {
    Response response = Response.status(STATUS).header(HttpHeaders.HOST, HOST)
        .build();
    NotAllowedException e = new NotAllowedException(response);
    assertResponse(e, HOST);
  }

  /*
   * @testName: constructorThrowableStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:332; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new method not allowed exception.
   * 
   * getResponse
   */
  @Test
  public void constructorThrowableStringTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      NotAllowedException e = new NotAllowedException(t, Request.DELETE.name());
      assertResponse(e);
      assertCause(e, t);
    }
  }

  /*
   * @testName: constructorThrowableStringThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:332;
   * 
   * @test_Strategy: Construct a new method not allowed exception. Throws -
   * java.lang.IllegalArgumentException - in case the allowed methods varargs
   * are null.
   */
  @Test
  public void constructorThrowableStringThrowsIAETest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      try {
        NotAllowedException e = new NotAllowedException(t, (String[]) null);
        fault(
            "IllegalArgumentException has NOT been thrown for null methods; exception",
            e);
      } catch (IllegalArgumentException iae) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for null methods");
      }
    }
  }

  /*
   * @testName: constructorResponseThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:333; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new method not allowed exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 405. getResponse
   */
  @Test
  public void constructorResponseThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      NotAllowedException e = new NotAllowedException(buildResponse(STATUS), t);
      assertResponse(e, HOST);
      assertCause(e, t);
    }
  }

  /*
   * @testName: constructorResponseThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:333;
   * 
   * @test_Strategy: Construct a new method not allowed exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 405.
   */
  @Test
  public void constructorResponseThrowableThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          NotAllowedException e = new NotAllowedException(buildResponse(status),
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
   * @testName: constructorResponseThrowableThrowsIAEWhenNoAllowHeaderTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:333;
   * 
   * @test_Strategy: Construct a new method not allowed exception.
   * java.lang.IllegalArgumentException - in case the response does not contain
   * an HTTP Allow header.
   */
  @Test
  public void constructorResponseThrowableThrowsIAEWhenNoAllowHeaderTest()
      throws Fault {
    try {
      Response response = Response.status(STATUS).build();
      NotAllowedException e = new NotAllowedException(response,
          new Throwable());
      fault(
          "IllegalArgumentException has not been thrown when no allow header exception",
          e);
    } catch (IllegalArgumentException e) {
      logMsg(
          "IllegalArgumentException has been thrown as expected for when no allow http header");
    }
  }

  /*
   * @testName: constructorStringStringStringsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1079; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new method not allowed exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringStringStringsTest() throws Fault {
    NotAllowedException e = new NotAllowedException(MESSAGE,
        Request.OPTIONS.name(), new String[] { Request.HEAD.name() });
    assertResponse(e);
    assertMessage(e);
  }

  /*
   * @testName: constructorStringStringStringsThrowsNPETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1079;
   * 
   * @test_Strategy: Throws: java.lang.NullPointerException - in case the
   * allowed method is null.
   */
  @Test
  public void constructorStringStringStringsThrowsNPETest() throws Fault {
    try {
      NotAllowedException e = new NotAllowedException(MESSAGE, (String) null,
          new String[] { Request.HEAD.name() });
      fault(
          "NullPointerException has not been thrown for null method, built exception",
          e);
    } catch (NullPointerException e) {
      logMsg(
          "NullPointerException has been thrown as expected, for null method");
    }
  }

  /*
   * @testName: constructorStringResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1080; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new method not allowed exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringResponseTest() throws Fault {
    NotAllowedException e = new NotAllowedException(MESSAGE,
        buildResponse(STATUS));
    assertResponse(e, HOST);
    assertMessage(e);
  }

  /*
   * @testName: constructorStringResponseThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1080;
   * 
   * @test_Strategy: Construct a new method not allowed exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 405.
   */
  @Test
  public void constructorStringResponseThrowsIAETest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          NotAllowedException e = new NotAllowedException(
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
   * @testName: constructorStringResponseDoesNotThrowWhenNoAllowHeaderTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1080; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Note that this constructor does not validate the presence
   * of HTTP Allow header. I.e. it is possible to use the constructor to create
   * a client-side exception instance even for an invalid HTTP 405 response
   * content returned from a server.
   * 
   * getResponse
   */
  @Test
  public void constructorStringResponseDoesNotThrowWhenNoAllowHeaderTest()
      throws Fault {
    Response response = Response.status(STATUS).header(HttpHeaders.HOST, HOST)
        .build();
    NotAllowedException e = new NotAllowedException(MESSAGE, response);
    assertResponse(e, HOST);
    assertMessage(e);
  }

  /*
   * @testName: constructorStringThrowableStringsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1081; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new method not allowed exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringThrowableStringsTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      NotAllowedException e = new NotAllowedException(MESSAGE, t,
          Request.DELETE.name());
      assertResponse(e);
      assertCause(e, t);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringThrowableStringsThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1081;
   * 
   * @test_Strategy: Construct a new method not allowed exception. Throws -
   * java.lang.IllegalArgumentException - in case the allowed methods varargs
   * are null.
   */
  @Test
  public void constructorStringThrowableStringsThrowsIAETest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      try {
        NotAllowedException e = new NotAllowedException(MESSAGE, t,
            (String[]) null);
        fault(
            "IllegalArgumentException has NOT been thrown for null methods; exception",
            e);
      } catch (IllegalArgumentException iae) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for null methods");
      }
    }
  }

  /*
   * @testName: constructorStringResponseThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1082; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new method not allowed exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 405. getResponse
   */
  @Test
  public void constructorStringResponseThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      NotAllowedException e = new NotAllowedException(MESSAGE,
          buildResponse(STATUS), t);
      assertResponse(e, HOST);
      assertCause(e, t);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringResponseThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1082;
   * 
   * @test_Strategy: Construct a new method not allowed exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 405.
   */
  @Test
  public void constructorStringResponseThrowableThrowsExceptionTest()
      throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          NotAllowedException e = new NotAllowedException(MESSAGE,
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

  /*
   * @testName: constructorStringResponseThrowableThrowsIAEWhenNoAllowHeaderTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1082;
   * 
   * @test_Strategy: Construct a new method not allowed exception.
   * java.lang.IllegalArgumentException - in case the response does not contain
   * an HTTP Allow header.
   */
  @Test
  public void constructorStringResponseThrowableThrowsIAEWhenNoAllowHeaderTest()
      throws Fault {
    try {
      Response response = Response.status(STATUS).build();
      NotAllowedException e = new NotAllowedException(MESSAGE, response,
          new Throwable());
      fault(
          "IllegalArgumentException has not been thrown when no allow header exception",
          e);
    } catch (IllegalArgumentException e) {
      logMsg(
          "IllegalArgumentException has been thrown as expected for when no allow http header");
    }
  }

  // /////////////////////////////////////////////////////////////
  protected Response buildResponse(Status status) {
    return Response.status(status).header("allow", Request.OPTIONS)
        .header("allow", Request.HEAD).header(HttpHeaders.HOST, HOST).build();
  }

  /**
   * Check the given exception contains a prebuilt response containing the http
   * header HOST
   */
  protected void assertResponse(NotAllowedException e, String host)
      throws Fault {
    assertResponse(e);
    String header = e.getResponse().getHeaderString(HttpHeaders.HOST);
    assertNotNull(header, "http header", HttpHeaders.HOST,
        " of response is null");
    assertEquals(host, header, "Found unexpected http", HttpHeaders.HOST,
        "header", header);
    logMsg("Found expected http", HttpHeaders.HOST, "header");
  }

  protected void assertResponse(NotAllowedException e) throws Fault {
    assertNotNull(e.getResponse(), "#getResponse is null");
    Response response = e.getResponse();
    assertEqualsInt(response.getStatus(), STATUS.getStatusCode(),
        "response cobtains unexpected status", response.getStatus());
    logMsg("response contains expected", STATUS, "status");
  }

  protected void assertCause(WebApplicationException e, Throwable expected)
      throws Fault {
    assertEquals(e.getCause(), expected, "#getCause does not contain expected",
        expected, "but", e.getCause());
    logMsg("getCause contains expected", expected);
  }

  protected void assertMessage(NotAllowedException e) throws Fault {
    assertNotNull(e.getMessage(), "getMessage() is null");
    assertContains(e.getMessage(), MESSAGE, "Unexpected getMessage()",
        e.getMessage());
    logMsg("found expected getMessage()=", e.getMessage());
  }
}
