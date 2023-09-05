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

package ee.jakarta.tck.ws.rs.api.rs.serviceunavailableexception;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.ServiceUnavailableException;
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

  private static final long serialVersionUID = 4296616216116337115L;

  private static final Status STATUS = Status.SERVICE_UNAVAILABLE;

  protected static final String MESSAGE = "TCK ServiceUnavailableException description";

  protected static final String HOST = "www.jcp.org";

  protected Date date = Calendar.getInstance().getTime();

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
   * @assertion_ids: JAXRS:JAVADOC:359; JAXRS:JAVADOC:357; JAXRS:JAVADOC:358;
   * JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception without any
   * "Retry-After" information specified for the failed request.
   * 
   * Get the retry time for the failed request.
   * 
   * Check if the underlying response contains the information on when is it
   * possible to HttpHeaders#RETRY_AFTER retry the request.
   * 
   * getResponse
   */
  @Test
  public void constructorTest() throws Fault {
    ServiceUnavailableException e = new ServiceUnavailableException();
    assertResponse(e);
    assertRetryTimeIsNull(e, true);
    assertHasRetryAfter(e, false);
  }

  /*
   * @testName: constructorLongTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:360; JAXRS:JAVADOC:357; JAXRS:JAVADOC:358;
   * JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception with an
   * interval specifying the "Retry-After" information for the failed request.
   * 
   * Get the retry time for the failed request.
   * 
   * Check if the underlying response contains the information on when is it
   * possible to HttpHeaders#RETRY_AFTER retry the request.
   * 
   * getResponse
   */
  public void constructorLongTest() throws Fault {
    ServiceUnavailableException e = new ServiceUnavailableException(5L);
    assertResponse(e);
    assertRetryTimeMin(e, 5);
    assertHasRetryAfter(e, true);
  }

  /*
   * @testName: constructorDateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:361; JAXRS:JAVADOC:357; JAXRS:JAVADOC:358;
   * JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception with an
   * interval specifying the "Retry-After" information for the failed request.
   * 
   * Get the retry time for the failed request.
   * 
   * Check if the underlying response contains the information on when is it
   * possible to HttpHeaders#RETRY_AFTER retry the request.
   * 
   * getResponse
   */
  public void constructorDateTest() throws Fault {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, 50);
    ServiceUnavailableException e = new ServiceUnavailableException(
        calendar.getTime());
    assertResponse(e);
    assertRetryTimeMin(e, 50);
    assertHasRetryAfter(e, true);
  }

  /*
   * @testName: constructorResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:362; JAXRS:JAVADOC:357; JAXRS:JAVADOC:358;
   * JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 503.
   * 
   * Get the retry time for the failed request.
   * 
   * Check if the underlying response contains the information on when is it
   * possible to HttpHeaders#RETRY_AFTER retry the request.
   * 
   * getResponse
   */
  public void constructorResponseTest() throws Fault {
    ServiceUnavailableException e = new ServiceUnavailableException(
        buildResponse(STATUS));
    assertResponse(e, HOST);
    assertRetryTimeIsNull(e, true);
    assertHasRetryAfter(e, false);
  }

  /*
   * @testName: constructorResponseThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:362;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 503.
   */
  public void constructorResponseThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          ServiceUnavailableException e = new ServiceUnavailableException(
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
   * @testName: constructorDateThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:363; JAXRS:JAVADOC:357; JAXRS:JAVADOC:358;
   * JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception with a date
   * specifying the "Retry-After" information for the failed request and an
   * underlying request failure cause.
   *
   * Get the retry time for the failed request.
   * 
   * Check if the underlying response contains the information on when is it
   * possible to HttpHeaders#RETRY_AFTER retry the request.
   * 
   * getResponse
   */
  public void constructorDateThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, 40);

    for (Throwable t : throwables) {
      ServiceUnavailableException e = new ServiceUnavailableException(
          calendar.getTime(), t);
      assertResponse(e);
      assertCause(e, t);
      assertRetryTimeMin(e, 40);
      assertHasRetryAfter(e, true);
    }
  }

  /*
   * @testName: constructorLongThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:364; JAXRS:JAVADOC:357; JAXRS:JAVADOC:358;
   * JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 503.
   *
   * Get the retry time for the failed request.
   * 
   * Check if the underlying response contains the information on when is it
   * possible to HttpHeaders#RETRY_AFTER retry the request.
   * 
   * getResponse
   */
  public void constructorLongThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      ServiceUnavailableException e = new ServiceUnavailableException(30L, t);
      assertResponse(e);
      assertCause(e, t);
      assertRetryTimeMin(e, 30);
      assertHasRetryAfter(e, true);
    }
  }

  /*
   * @testName: constructorResponseThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:365; JAXRS:JAVADOC:357; JAXRS:JAVADOC:358;
   * JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 503.
   *
   * Get the retry time for the failed request.
   * 
   * Check if the underlying response contains the information on when is it
   * possible to HttpHeaders#RETRY_AFTER retry the request.
   * 
   * getResponse
   */
  public void constructorResponseThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      ServiceUnavailableException e = new ServiceUnavailableException(
          buildResponse(STATUS), t);
      assertResponse(e, HOST);
      assertCause(e, t);
      assertRetryTimeIsNull(e, true);
      assertHasRetryAfter(e, false);
    }
  }

  /*
   * @testName: constructorResponseThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:365;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 503.
   */
  public void constructorResponseThrowableThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          ServiceUnavailableException e = new ServiceUnavailableException(
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
   * @testName: constructorStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1104; JAXRS:JAVADOC:357; JAXRS:JAVADOC:358;
   * JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception without any
   * "Retry-After" information specified for the failed request.
   * 
   * Get the retry time for the failed request.
   * 
   * Check if the underlying response contains the information on when is it
   * possible to HttpHeaders#RETRY_AFTER retry the request.
   * 
   * getResponse
   */
  public void constructorStringTest() throws Fault {
    ServiceUnavailableException e = new ServiceUnavailableException(MESSAGE);
    assertResponse(e);
    assertRetryTimeIsNull(e, true);
    assertHasRetryAfter(e, false);
    assertMessage(e);
  }

  /*
   * @testName: constructorStringLongTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1105; JAXRS:JAVADOC:357; JAXRS:JAVADOC:358;
   * JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception with an
   * interval specifying the "Retry-After" information for the failed request.
   * 
   * Get the retry time for the failed request.
   * 
   * Check if the underlying response contains the information on when is it
   * possible to HttpHeaders#RETRY_AFTER retry the request.
   * 
   * getResponse
   */
  public void constructorStringLongTest() throws Fault {
    ServiceUnavailableException e = new ServiceUnavailableException(MESSAGE,
        5L);
    assertResponse(e);
    assertRetryTimeMin(e, 5);
    assertHasRetryAfter(e, true);
    assertMessage(e);
  }

  /*
   * @testName: constructorStringDateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1106; JAXRS:JAVADOC:357; JAXRS:JAVADOC:358;
   * JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception with an
   * interval specifying the "Retry-After" information for the failed request.
   * 
   * Get the retry time for the failed request.
   * 
   * Check if the underlying response contains the information on when is it
   * possible to HttpHeaders#RETRY_AFTER retry the request.
   * 
   * getResponse
   */
  public void constructorStringDateTest() throws Fault {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, 50);
    ServiceUnavailableException e = new ServiceUnavailableException(MESSAGE,
        calendar.getTime());
    assertResponse(e);
    assertRetryTimeMin(e, 50);
    assertHasRetryAfter(e, true);
    assertMessage(e);
  }

  /*
   * @testName: constructorStringResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1107; JAXRS:JAVADOC:357; JAXRS:JAVADOC:358;
   * JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 503.
   * 
   * Get the retry time for the failed request.
   * 
   * Check if the underlying response contains the information on when is it
   * possible to HttpHeaders#RETRY_AFTER retry the request.
   * 
   * getResponse
   */
  public void constructorStringResponseTest() throws Fault {
    ServiceUnavailableException e = new ServiceUnavailableException(MESSAGE,
        buildResponse(STATUS));
    assertResponse(e, HOST);
    assertRetryTimeIsNull(e, true);
    assertHasRetryAfter(e, false);
    assertMessage(e);
  }

  /*
   * @testName: constructorStringResponseThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1107;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 503.
   */
  public void constructorStringResponseThrowsIAETest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          ServiceUnavailableException e = new ServiceUnavailableException(
              MESSAGE, buildResponse(status));
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
   * @testName: constructorStringDateThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1108; JAXRS:JAVADOC:357; JAXRS:JAVADOC:358;
   * JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception with a date
   * specifying the "Retry-After" information for the failed request and an
   * underlying request failure cause.
   *
   * Get the retry time for the failed request.
   * 
   * Check if the underlying response contains the information on when is it
   * possible to HttpHeaders#RETRY_AFTER retry the request.
   * 
   * getResponse
   */
  public void constructorStringDateThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, 40);

    for (Throwable t : throwables) {
      ServiceUnavailableException e = new ServiceUnavailableException(MESSAGE,
          calendar.getTime(), t);
      assertResponse(e);
      assertCause(e, t);
      assertRetryTimeMin(e, 40);
      assertHasRetryAfter(e, true);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringLongThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1109; JAXRS:JAVADOC:357; JAXRS:JAVADOC:358;
   * JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 503.
   *
   * Get the retry time for the failed request.
   * 
   * Check if the underlying response contains the information on when is it
   * possible to HttpHeaders#RETRY_AFTER retry the request.
   * 
   * getResponse
   */
  public void constructorStringLongThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      ServiceUnavailableException e = new ServiceUnavailableException(MESSAGE,
          30L, t);
      assertResponse(e);
      assertCause(e, t);
      assertRetryTimeMin(e, 30);
      assertHasRetryAfter(e, true);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringResponseThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1110; JAXRS:JAVADOC:357; JAXRS:JAVADOC:358;
   * JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception.
   * 
   * Get the retry time for the failed request.
   * 
   * Check if the underlying response contains the information on when is it
   * possible to HttpHeaders#RETRY_AFTER retry the request.
   * 
   * getResponse
   */
  public void constructorStringResponseThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Throwable t : throwables) {
      ServiceUnavailableException e = new ServiceUnavailableException(MESSAGE,
          buildResponse(STATUS), t);
      assertResponse(e, HOST);
      assertCause(e, t);
      assertRetryTimeIsNull(e, true);
      assertHasRetryAfter(e, false);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringResponseThrowableThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1110;
   * 
   * @test_Strategy: Construct a new "service unavailable" exception.
   * java.lang.IllegalArgumentException - in case the status code set in the
   * response is not HTTP 503.
   */
  public void constructorStringResponseThrowableThrowsIAETest() throws Fault {
    for (Status status : Status.values())
      if (status != STATUS) {
        try {
          ServiceUnavailableException e = new ServiceUnavailableException(
              MESSAGE, buildResponse(status), new Throwable());
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

  protected static void assertResponse(WebApplicationException e) throws Fault {
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

  protected static void assertCause(WebApplicationException e,
      Throwable expected) throws Fault {
    assertEquals(e.getCause(), expected, "#getCause does not contain expected",
        expected, "but", e.getCause());
    logMsg("getCause contains expected", expected);
  }

  protected void assertRetryTimeIsNull(ServiceUnavailableException e,
      boolean isNull) throws Fault {
    Date retryTime = e.getRetryTime(date);
    assertTrue((isNull && retryTime == null) || (!isNull && retryTime != null),
        "RetryTime was unexpectedly " + retryTime);
    logMsg("Found expected retry time", retryTime);
  }

  protected void assertRetryTimeMin(ServiceUnavailableException e, int seconds)
      throws Fault {
    Date retryTime = e.getRetryTime(date);
    assertNotNull(date, "#getRetryTime is null");

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.SECOND, seconds);
    Date minTime = calendar.getTime();

    assertTrue(retryTime.compareTo(minTime) >= -1000, "RetryTime " +
        retryTime.getTime() + " was unexpectedly lower then expected " +
        minTime.getTime());
    logMsg("Found expected retry time", retryTime);
  }

  protected static void assertHasRetryAfter(ServiceUnavailableException e,
      boolean hasIt) throws Fault {
    boolean retry = e.hasRetryAfter();
    assertEqualsBool(hasIt, retry, "#hasRetryAfter is unexpectedly", retry);
    logMsg("#hasRetryAfter returned expected", retry, "value");
  }

  protected static void assertMessage(WebApplicationException e) throws Fault {
    assertNotNull(e.getMessage(), "getMessage() is null");
    assertContains(e.getMessage(), MESSAGE, "Unexpected getMessage()",
        e.getMessage());
    logMsg("found expected getMessage()=", e.getMessage());
  }

}
