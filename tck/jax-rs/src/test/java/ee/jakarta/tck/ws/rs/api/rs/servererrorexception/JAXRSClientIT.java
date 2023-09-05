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

package ee.jakarta.tck.ws.rs.api.rs.servererrorexception;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.ServerErrorException;
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

  private static final long serialVersionUID = -143670490120851422L;

  public static final Status.Family FAMILY = Status.Family.SERVER_ERROR;

  protected static final String MESSAGE = "TCK ServerErrorException description";

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
   * @testName: constructorStatusTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:351; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.SERVER_ERROR status code family.
   * 
   * getResponse
   */
  @Test
  public void constructorStatusTest() throws Fault {
    for (Status status : getStatusesFromFamily()) {
      ServerErrorException e = new ServerErrorException(status);
      assertResponse(e, status);
    }
  }

  /*
   * @testName: constructorStatusThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:351;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.SERVER_ERROR status code family.
   */
  @Test
  public void constructorStatusThrowsExceptionTest() throws Fault {
    for (Status status : getStatusesOutsideFamily()) {
      try {
        ServerErrorException e = new ServerErrorException(status);
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
    }
  }

  /*
   * @testName: constructorStatusNullThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:351;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.SERVER_ERROR status code family.
   */
  @Test
  public void constructorStatusNullThrowsExceptionTest() throws Fault {
    try {
      ServerErrorException e = new ServerErrorException((Status) null);
      fault("IllegalArgumentException has not been thrown for null status",
          "; exception", e);
    } catch (IllegalArgumentException e) {
      logMsg(
          "IllegalArgumentException has been thrown as expected for null status");
    }
  }

  /*
   * @testName: constructorIntTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:352; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.SERVER_ERROR status
   * code family.
   * 
   * getResponse
   */
  @Test
  public void constructorIntTest() throws Fault {
    for (Status status : getStatusesFromFamily()) {
      ServerErrorException e = new ServerErrorException(status.getStatusCode());
      assertResponse(e, status);
    }
  }

  /*
   * @testName: constructorIntThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:352;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.SERVER_ERROR status
   * code family.
   */
  @Test
  public void constructorIntThrowsExceptionTest() throws Fault {
    for (Status status : getStatusesOutsideFamily()) {
      try {
        ServerErrorException e = new ServerErrorException(
            status.getStatusCode());
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
    }
  }

  /*
   * @testName: constructorIntNotValidStatusThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:352;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.SERVER_ERROR status
   * code family.
   */
  @Test
  public void constructorIntNotValidStatusThrowsExceptionTest() throws Fault {
    for (int status : new int[] { -1, Integer.MAX_VALUE, Integer.MIN_VALUE }) {
      try {
        ServerErrorException e = new ServerErrorException(status);
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
    }
  }

  /*
   * @testName: constructorResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:353; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.SERVER_ERROR status code family.
   * 
   * getResponse
   */
  @Test
  public void constructorResponseTest() throws Fault {
    for (Status status : getStatusesFromFamily()) {
      ServerErrorException e = new ServerErrorException(buildResponse(status));
      assertResponse(e, status, HOST);
    }
  }

  /*
   * @testName: constructorResponseThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:353;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.SERVER_ERROR status code family.
   */
  @Test
  public void constructorResponseThrowsExceptionTest() throws Fault {
    for (Status status : getStatusesOutsideFamily())
      try {
        ServerErrorException e = new ServerErrorException(
            buildResponse(status));
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
  }

  /*
   * @testName: constructorStatusThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:354; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.SERVER_ERROR status code family.
   * 
   * getResponse
   */
  @Test
  public void constructorStatusThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Status status : getStatusesFromFamily())
      for (Throwable throwable : throwables) {
        ServerErrorException e = new ServerErrorException(status, throwable);
        assertResponse(e, status);
        assertCause(e, throwable);
      }
  }

  /*
   * @testName: constructorStatusThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:354;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.SERVER_ERROR status code family.
   */
  @Test
  public void constructorStatusThrowableThrowsExceptionTest() throws Fault {
    Throwable throwable = new Throwable();
    for (Status status : getStatusesOutsideFamily()) {
      try {
        ServerErrorException e = new ServerErrorException(status, throwable);
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
    }
  }

  /*
   * @testName: constructorStatusNullThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:354;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.SERVER_ERROR status code family.
   */
  @Test
  public void constructorStatusNullThrowableThrowsExceptionTest() throws Fault {
    Throwable throwable = new Throwable();
    try {
      ServerErrorException e = new ServerErrorException((Status) null,
          throwable);
      fault("IllegalArgumentException has not been thrown for null status",
          "; exception", e);
    } catch (IllegalArgumentException e) {
      logMsg(
          "IllegalArgumentException has been thrown as expected for null status");
    }
  }

  /*
   * @testName: constructorIntThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:355; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.SERVER_ERROR status
   * code family.
   * 
   * getResponse
   */
  @Test
  public void constructorIntThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Status status : getStatusesFromFamily())
      for (Throwable throwable : throwables) {
        ServerErrorException e = new ServerErrorException(
            status.getStatusCode(), throwable);
        assertResponse(e, status);
        assertCause(e, throwable);
      }
  }

  /*
   * @testName: constructorIntThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:355;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.SERVER_ERROR status
   * code family.
   */
  @Test
  public void constructorIntThrowableThrowsExceptionTest() throws Fault {
    Throwable throwable = new Throwable();
    for (Status status : getStatusesOutsideFamily())
      try {
        ServerErrorException e = new ServerErrorException(
            status.getStatusCode(), throwable);
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
  }

  /*
   * @testName: constructorIntNotValidStatusThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:355;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.SERVER_ERROR status
   * code family.
   */
  @Test
  public void constructorIntNotValidStatusThrowableThrowsExceptionTest()
      throws Fault {
    Throwable throwable = new Throwable();
    for (int status : new int[] { -1, Integer.MAX_VALUE, Integer.MIN_VALUE }) {
      try {
        ServerErrorException e = new ServerErrorException(status, throwable);
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
    }
  }

  /*
   * @testName: constructorResponseThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:356; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.SERVER_ERROR status code family.
   * 
   * getResponse
   */
  @Test
  public void constructorResponseThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Status status : getStatusesFromFamily())
      for (Throwable throwable : throwables) {
        ServerErrorException e = new ServerErrorException(buildResponse(status),
            throwable);
        assertResponse(e, status, HOST);
        assertCause(e, throwable);
      }
  }

  /*
   * @testName: constructorResponseThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:356;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.SERVER_ERROR status code family.
   */
  @Test
  public void constructorResponseThrowableThrowsExceptionTest() throws Fault {
    Throwable throwable = new Throwable();
    for (Status status : getStatusesOutsideFamily())
      try {
        ServerErrorException e = new ServerErrorException(buildResponse(status),
            throwable);
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
  }

  /*
   * @testName: constructorStringStatusTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1098; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new server error exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringStatusTest() throws Fault {
    for (Status status : getStatusesFromFamily()) {
      ServerErrorException e = new ServerErrorException(MESSAGE, status);
      assertResponse(e, status);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringStatusThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1098;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.SERVER_ERROR status code family.
   */
  @Test
  public void constructorStringStatusThrowsIAETest() throws Fault {
    for (Status status : getStatusesOutsideFamily()) {
      try {
        ServerErrorException e = new ServerErrorException(MESSAGE, status);
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
    }
  }

  /*
   * @testName: constructorStringStatusNullThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1098;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.SERVER_ERROR status code family.
   */
  @Test
  public void constructorStringStatusNullThrowsExceptionTest() throws Fault {
    try {
      ServerErrorException e = new ServerErrorException(MESSAGE, (Status) null);
      fault("IllegalArgumentException has not been thrown for null status",
          "; exception", e);
    } catch (IllegalArgumentException e) {
      logMsg(
          "IllegalArgumentException has been thrown as expected for null status");
    }
  }

  /*
   * @testName: constructorStringIntTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1099; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new server error exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringIntTest() throws Fault {
    for (Status status : getStatusesFromFamily()) {
      ServerErrorException e = new ServerErrorException(MESSAGE,
          status.getStatusCode());
      assertResponse(e, status);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringIntThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1099;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.SERVER_ERROR status
   * code family.
   */
  @Test
  public void constructorStringIntThrowsIAETest() throws Fault {
    for (Status status : getStatusesOutsideFamily()) {
      try {
        ServerErrorException e = new ServerErrorException(MESSAGE,
            status.getStatusCode());
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
    }
  }

  /*
   * @testName: constructorStringIntNotValidStatusThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1099;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.SERVER_ERROR status
   * code family.
   */
  @Test
  public void constructorStringIntNotValidStatusThrowsIAETest() throws Fault {
    for (int status : new int[] { -1, Integer.MAX_VALUE, Integer.MIN_VALUE }) {
      try {
        ServerErrorException e = new ServerErrorException(MESSAGE, status);
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
    }
  }

  /*
   * @testName: constructorStringResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1100; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new server error exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringResponseTest() throws Fault {
    for (Status status : getStatusesFromFamily()) {
      ServerErrorException e = new ServerErrorException(MESSAGE,
          buildResponse(status));
      assertResponse(e, status, HOST);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringResponseThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1100;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.SERVER_ERROR status code family.
   */
  @Test
  public void constructorStringResponseThrowsIAETest() throws Fault {
    for (Status status : getStatusesOutsideFamily())
      try {
        ServerErrorException e = new ServerErrorException(MESSAGE,
            buildResponse(status));
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
  }

  /*
   * @testName: constructorStringStatusThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1101; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new server error exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringStatusThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Status status : getStatusesFromFamily())
      for (Throwable throwable : throwables) {
        ServerErrorException e = new ServerErrorException(MESSAGE, status,
            throwable);
        assertResponse(e, status);
        assertCause(e, throwable);
        assertMessage(e);
      }
  }

  /*
   * @testName: constructorStringStatusThrowableThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1101;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.SERVER_ERROR status code family.
   */
  @Test
  public void constructorStringStatusThrowableThrowsIAETest() throws Fault {
    Throwable throwable = new Throwable();
    for (Status status : getStatusesOutsideFamily()) {
      try {
        ServerErrorException e = new ServerErrorException(MESSAGE, status,
            throwable);
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
    }
  }

  /*
   * @testName: constructorStringStatusNullThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1101;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.SERVER_ERROR status code family.
   */
  @Test
  public void constructorStringStatusNullThrowableThrowsExceptionTest()
      throws Fault {
    Throwable throwable = new Throwable();
    try {
      ServerErrorException e = new ServerErrorException(MESSAGE, (Status) null,
          throwable);
      fault("IllegalArgumentException has not been thrown for null status",
          "; exception", e);
    } catch (IllegalArgumentException e) {
      logMsg(
          "IllegalArgumentException has been thrown as expected for null status");
    }
  }

  /*
   * @testName: constructorStringIntThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1102; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new server error exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringIntThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Status status : getStatusesFromFamily())
      for (Throwable throwable : throwables) {
        ServerErrorException e = new ServerErrorException(MESSAGE,
            status.getStatusCode(), throwable);
        assertResponse(e, status);
        assertCause(e, throwable);
        assertMessage(e);
      }
  }

  /*
   * @testName: constructorStringIntThrowableThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1102;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.SERVER_ERROR status
   * code family.
   */
  @Test
  public void constructorStringIntThrowableThrowsIAETest() throws Fault {
    Throwable throwable = new Throwable();
    for (Status status : getStatusesOutsideFamily())
      try {
        ServerErrorException e = new ServerErrorException(MESSAGE,
            status.getStatusCode(), throwable);
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
  }

  /*
   * @testName: constructorStringIntNotValidStatusThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1102;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.SERVER_ERROR status
   * code family.
   */
  @Test
  public void constructorStringIntNotValidStatusThrowableThrowsExceptionTest()
      throws Fault {
    Throwable throwable = new Throwable();
    for (int status : new int[] { -1, Integer.MAX_VALUE, Integer.MIN_VALUE }) {
      try {
        ServerErrorException e = new ServerErrorException(MESSAGE, status,
            throwable);
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
    }
  }

  /*
   * @testName: constructorStringResponseThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1103; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new server error exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringResponseThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Status status : getStatusesFromFamily())
      for (Throwable throwable : throwables) {
        ServerErrorException e = new ServerErrorException(MESSAGE,
            buildResponse(status), throwable);
        assertResponse(e, status, HOST);
        assertCause(e, throwable);
        assertMessage(e);
      }
  }

  /*
   * @testName: constructorStringResponseThrowableThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1103;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.SERVER_ERROR status code family.
   */
  @Test
  public void constructorStringResponseThrowableThrowsIAETest() throws Fault {
    Throwable throwable = new Throwable();
    for (Status status : getStatusesOutsideFamily())
      try {
        ServerErrorException e = new ServerErrorException(MESSAGE,
            buildResponse(status), throwable);
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
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
        "response contains unexpected status", response.getStatus());
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

  protected void assertCause(WebApplicationException e, Throwable expected)
      throws Fault {
    assertEquals(e.getCause(), expected, "#getCause does not contain expected",
        expected, "but", e.getCause());
    logMsg("getCause contains expected", expected);
  }

  protected static List<Status> getStatusesFromFamily() {
    List<Status> list = new LinkedList<Status>();
    for (Status status : Status.values())
      if (Status.Family.familyOf(status.getStatusCode()).equals(FAMILY))
        list.add(status);
    return list;
  }

  protected static List<Status> getStatusesOutsideFamily() {
    List<Status> list = new LinkedList<Status>();
    for (Status status : Status.values())
      if (!Status.Family.familyOf(status.getStatusCode()).equals(FAMILY))
        list.add(status);
    return list;
  }

  protected void assertMessage(WebApplicationException e) throws Fault {
    assertNotNull(e.getMessage(), "getMessage() is null");
    assertContains(e.getMessage(), MESSAGE, "Unexpected getMessage()",
        e.getMessage());
    logMsg("found expected getMessage()=", e.getMessage());
  }
}
