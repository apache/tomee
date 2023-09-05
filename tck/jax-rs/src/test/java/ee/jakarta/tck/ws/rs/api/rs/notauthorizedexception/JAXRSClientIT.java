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

package ee.jakarta.tck.ws.rs.api.rs.notauthorizedexception;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = -377888431788668222L;

  private static final Status STATUS = Status.UNAUTHORIZED;

  private static final String[] CHALLENGE = { "challenge1", "challenge2",
      "challenge3" };

  protected static final String MESSAGE = "TCK NotAuthorizedException description";

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
   * @testName: constructorObjectsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:335; JAXRS:JAVADOC:334; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not authorized" exception.
   * 
   * Get the list of authorization challenges associated with the exception and
   * applicable to the resource requested by the client.
   * 
   * getResponse
   */
  @Test
  public void constructorObjectsTest() throws Fault {
    NotAuthorizedException e = new NotAuthorizedException((Object) CHALLENGE[0],
        CHALLENGE[1], CHALLENGE[2]);
    assertResponse(e);
    assertChallenges(e);
  }

  /*
   * @testName: constructorObjectsThrowsNPEWhenNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:335;
   * 
   * @test_Strategy: Throws: java.lang.NullPointerException - in case the
   * challenge parameter is null.
   */
  @Test
  public void constructorObjectsThrowsNPEWhenNullTest() throws Fault {
    try {
      NotAuthorizedException e = new NotAuthorizedException((Object) null,
          CHALLENGE[1], CHALLENGE[2]);
      fault(
          "NullPointerException has NOT been thrown as expected for null challenge; exception",
          e);
    } catch (NullPointerException npe) {
      logMsg(
          "NullPointerException has been thrown as expected for null challenge");
    }
  }

  /*
   * @testName: constructorResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:336; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not authorized" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 401.
   * 
   * getResponse
   */
  @Test
  public void constructorResponseTest() throws Fault {
    NotAuthorizedException e = new NotAuthorizedException(
        buildResponse(STATUS));
    assertResponse(e, HOST);
  }

  /*
   * @testName: constructorResponseThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:336;
   * 
   * @test_Strategy: Construct a new "not authorized" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 401.
   */
  @Test
  public void constructorResponseThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          NotAuthorizedException e = new NotAuthorizedException(
              buildResponse(status));
          fault("IllegalArgumentException has not been thrown for status",
              status, "and exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
      }
  }

  /*
   * @testName: constructorThrowableObjectTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:337; JAXRS:JAVADOC:334; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not authorized" exception.
   * 
   * Get the list of authorization challenges associated with the exception and
   * applicable to the resource requested by the client.
   * 
   * getResponse
   */
  @Test
  public void constructorThrowableObjectTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      NotAuthorizedException e = new NotAuthorizedException(t, CHALLENGE[0],
          CHALLENGE[1], CHALLENGE[2]);
      assertResponse(e);
      assertChallenges(e);
      assertCause(e, t);
    }
  }

  /*
   * @testName: constructorResponseThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:338; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not authorized" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 401. getResponse
   */
  @Test
  public void constructorResponseThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      NotAuthorizedException e = new NotAuthorizedException(
          buildResponse(STATUS), t);
      assertResponse(e, HOST);
      assertCause(e, t);
    }
  }

  /*
   * @testName: constructorResponseThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:338;
   * 
   * @test_Strategy: Construct a new "not authorized" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 401.
   */
  @Test
  public void constructorResponseThrowableThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          NotAuthorizedException e = new NotAuthorizedException(
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
   * @testName: constructorStringObjectsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1083; JAXRS:JAVADOC:334; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not authorized" exception.
   * 
   * Get the list of authorization challenges associated with the exception and
   * applicable to the resource requested by the client.
   * 
   * getResponse
   */
  @Test
  public void constructorStringObjectsTest() throws Fault {
    NotAuthorizedException e = new NotAuthorizedException(MESSAGE,
        (Object) CHALLENGE[0], CHALLENGE[1], CHALLENGE[2]);
    assertResponse(e);
    assertChallenges(e);
    assertMessage(e);
  }

  /*
   * @testName: constructorStringObjectsThrowsNPEWhenNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1083;
   * 
   * @test_Strategy: Throws: java.lang.NullPointerException - in case the
   * challenge parameter is null.
   */
  @Test
  public void constructorStringObjectsThrowsNPEWhenNullTest() throws Fault {
    try {
      NotAuthorizedException e = new NotAuthorizedException(MESSAGE,
          (Object) null, CHALLENGE[1], CHALLENGE[2]);
      fault(
          "NullPointerException has NOT been thrown as expected for null challenge; exception",
          e);
    } catch (NullPointerException npe) {
      logMsg(
          "NullPointerException has been thrown as expected for null challenge");
    }
  }

  /*
   * @testName: constructorStringResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1084; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not authorized" exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringResponseTest() throws Fault {
    NotAuthorizedException e = new NotAuthorizedException(MESSAGE,
        buildResponse(STATUS));
    assertResponse(e, HOST);
    assertMessage(e);
  }

  /*
   * @testName: constructorStringResponseThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1084;
   * 
   * @test_Strategy: Construct a new "not authorized" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 401.
   */
  @Test
  public void constructorStringResponseThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          NotAuthorizedException e = new NotAuthorizedException(MESSAGE,
              buildResponse(status));
          fault("IllegalArgumentException has not been thrown for status",
              status, "and exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
      }
  }

  /*
   * @testName: constructorStringThrowableObjectsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1085; JAXRS:JAVADOC:334; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not authorized" exception.
   * 
   * Get the list of authorization challenges associated with the exception and
   * applicable to the resource requested by the client.
   * 
   * getResponse
   */
  @Test
  public void constructorStringThrowableObjectsTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      NotAuthorizedException e = new NotAuthorizedException(MESSAGE, t,
          CHALLENGE[0], CHALLENGE[1], CHALLENGE[2]);
      assertResponse(e);
      assertChallenges(e);
      assertCause(e, t);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringResponseThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1086; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "not authorized" exception. getResponse
   */
  @Test
  public void constructorStringResponseThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      NotAuthorizedException e = new NotAuthorizedException(MESSAGE,
          buildResponse(STATUS), t);
      assertResponse(e, HOST);
      assertCause(e, t);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringResponseThrowableThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1086;
   * 
   * @test_Strategy: Construct a new "not authorized" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 401.
   */
  @Test
  public void constructorStringResponseThrowableThrowsIAETest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          NotAuthorizedException e = new NotAuthorizedException(MESSAGE,
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
        "response cobtains unexpected status", response.getStatus());
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

  protected void assertChallenges(NotAuthorizedException e) throws Fault {
    List<Object> challenges = e.getChallenges();
    String list = JaxrsUtil.iterableToString(";", challenges);
    assertContains(list, CHALLENGE[0], "Challenge", CHALLENGE[0],
        "not found in", list);
    assertContains(list, CHALLENGE[1], "Challenge", CHALLENGE[1],
        "not found in", list);
    assertContains(list, CHALLENGE[2], "Challenge", CHALLENGE[2],
        "not found in", list);
  }

  protected void assertMessage(NotAuthorizedException e) throws Fault {
    assertNotNull(e.getMessage(), "getMessage() is null");
    assertContains(e.getMessage(), MESSAGE, "Unexpected getMessage()",
        e.getMessage());
    logMsg("found expected getMessage()=", e.getMessage());
  }
}
