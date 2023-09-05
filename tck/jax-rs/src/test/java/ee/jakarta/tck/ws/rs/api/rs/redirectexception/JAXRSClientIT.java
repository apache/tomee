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

package ee.jakarta.tck.ws.rs.api.rs.redirectexception;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.RedirectionException;
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

  private static final long serialVersionUID = 1351456637402581583L;

  public static final Status.Family FAMILY = Status.Family.REDIRECTION;

  protected static final String MESSAGE = "TCK RedirectionException description";

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
   * @testName: constructorStatusUriTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:348; JAXRS:JAVADOC:347; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new redirection exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.REDIRECTION status code family.
   * 
   * Get the redirection response location.
   * 
   * getResponse
   */
  @Test
  public void constructorStatusUriTest() throws Fault {
    for (Status status : getStatusesFromFamily()) {
      RedirectionException e = new RedirectionException(status, LOCATION);
      assertResponse(e, status);
      assertLocation(e, LOCATION);
    }
  }

  /*
   * @testName: constructorStatusUriThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:348;
   * 
   * @test_Strategy: Construct a new redirection exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.REDIRECTION status code family.
   */
  @Test
  public void constructorStatusUriThrowsExceptionTest() throws Fault {
    for (Status status : getStatusesOutsideFamily()) {
      try {
        RedirectionException e = new RedirectionException(status, LOCATION);
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
   * @testName: constructorStatusNullUriThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:348;
   * 
   * @test_Strategy: Construct a new redirection exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.REDIRECTION status code family.
   */
  @Test
  public void constructorStatusNullUriThrowsExceptionTest() throws Fault {
    try {
      RedirectionException e = new RedirectionException((Status) null,
          LOCATION);
      fault("IllegalArgumentException has not been thrown for null status",
          "; exception", e);
    } catch (IllegalArgumentException e) {
      logMsg(
          "IllegalArgumentException has been thrown as expected for null status");
    }
  }

  /*
   * @testName: constructorIntUriTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:349; JAXRS:JAVADOC:347; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new redirection exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.REDIRECTION status
   * code family.
   * 
   * Get the redirection response location.
   * 
   * getResponse
   */
  @Test
  public void constructorIntUriTest() throws Fault {
    for (Status status : getStatusesFromFamily()) {
      RedirectionException e = new RedirectionException(status.getStatusCode(),
          LOCATION);
      assertResponse(e, status);
      assertLocation(e, LOCATION);
    }
  }

  /*
   * @testName: constructorIntUriThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:349;
   * 
   * @test_Strategy: Construct a new redirection exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.REDIRECTION status
   * code family.
   */
  @Test
  public void constructorIntUriThrowsExceptionTest() throws Fault {
    for (Status status : getStatusesOutsideFamily()) {
      try {
        RedirectionException e = new RedirectionException(
            status.getStatusCode(), LOCATION);
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
   * @assertion_ids: JAXRS:JAVADOC:349;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.REDIRECTION status
   * code family.
   */
  @Test
  public void constructorIntNotValidStatusThrowsExceptionTest() throws Fault {
    for (int status : new int[] { -1, Integer.MAX_VALUE, Integer.MIN_VALUE }) {
      try {
        RedirectionException e = new RedirectionException(status, LOCATION);
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
   * @assertion_ids: JAXRS:JAVADOC:350; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new redirection exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.REDIRECTION status code family.
   * 
   * getResponse
   */
  @Test
  public void constructorResponseTest() throws Fault {
    for (Status status : getStatusesFromFamily()) {
      RedirectionException e = new RedirectionException(buildResponse(status));
      assertResponse(e, status, HOST);
    }
  }

  /*
   * @testName: constructorResponseThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:350;
   * 
   * @test_Strategy: Construct a new redirection exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.REDIRECTION status code family.
   */
  @Test
  public void constructorResponseThrowsExceptionTest() throws Fault {
    for (Status status : getStatusesOutsideFamily())
      try {
        RedirectionException e = new RedirectionException(
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
   * @testName: constructorStringStatusUriTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1095; JAXRS:JAVADOC:347; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new redirection exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.REDIRECTION status code family.
   * 
   * Get the redirection response location.
   * 
   * getResponse
   */
  @Test
  public void constructorStringStatusUriTest() throws Fault {
    for (Status status : getStatusesFromFamily()) {
      RedirectionException e = new RedirectionException(MESSAGE, status,
          LOCATION);
      assertResponse(e, status);
      assertLocation(e, LOCATION);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringStatusUriThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1095;
   * 
   * @test_Strategy: Construct a new redirection exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.REDIRECTION status code family.
   */
  @Test
  public void constructorStringStatusUriThrowsIAETest() throws Fault {
    for (Status status : getStatusesOutsideFamily()) {
      try {
        RedirectionException e = new RedirectionException(MESSAGE, status,
            LOCATION);
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
   * @testName: constructorStringStatusNullUriThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1095;
   * 
   * @test_Strategy: Construct a new redirection exception.
   * java.lang.IllegalArgumentException - in case the status code is null or is
   * not from Response.Status.Family.REDIRECTION status code family.
   */
  @Test
  public void constructorStringStatusNullUriThrowsIAETest() throws Fault {
    try {
      RedirectionException e = new RedirectionException(MESSAGE, (Status) null,
          LOCATION);
      fault("IllegalArgumentException has not been thrown for null status",
          "; exception", e);
    } catch (IllegalArgumentException e) {
      logMsg(
          "IllegalArgumentException has been thrown as expected for null status");
    }
  }

  /*
   * @testName: constructorStringIntUriTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1096; JAXRS:JAVADOC:347; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new redirection exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.REDIRECTION status
   * code family.
   * 
   * Get the redirection response location.
   * 
   * getResponse
   */
  @Test
  public void constructorStringIntUriTest() throws Fault {
    for (Status status : getStatusesFromFamily()) {
      RedirectionException e = new RedirectionException(MESSAGE,
          status.getStatusCode(), LOCATION);
      assertResponse(e, status);
      assertLocation(e, LOCATION);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringIntUriThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1096;
   * 
   * @test_Strategy: Construct a new redirection exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.REDIRECTION status
   * code family.
   */
  @Test
  public void constructorStringIntUriThrowsIAETest() throws Fault {
    for (Status status : getStatusesOutsideFamily()) {
      try {
        RedirectionException e = new RedirectionException(MESSAGE,
            status.getStatusCode(), LOCATION);
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
   * @assertion_ids: JAXRS:JAVADOC:1096;
   * 
   * @test_Strategy: Construct a new server error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or is not from Response.Status.Family.REDIRECTION status
   * code family.
   */
  @Test
  public void constructorStringIntNotValidStatusThrowsIAETest() throws Fault {
    for (int status : new int[] { -1, Integer.MAX_VALUE, Integer.MIN_VALUE }) {
      try {
        RedirectionException e = new RedirectionException(MESSAGE, status,
            LOCATION);
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
   * @assertion_ids: JAXRS:JAVADOC:1097; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new redirection exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.REDIRECTION status code family.
   * 
   * getResponse
   */
  @Test
  public void constructorStringResponseTest() throws Fault {
    for (Status status : getStatusesFromFamily()) {
      RedirectionException e = new RedirectionException(MESSAGE,
          buildResponse(status));
      assertResponse(e, status, HOST);
      assertMessage(e);
    }
  }

  /*
   * @testName: constructorStringResponseThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1097;
   * 
   * @test_Strategy: Construct a new redirection exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.REDIRECTION status code family.
   */
  @Test
  public void constructorStringResponseThrowsIAETest() throws Fault {
    for (Status status : getStatusesOutsideFamily())
      try {
        RedirectionException e = new RedirectionException(MESSAGE,
            buildResponse(status));
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

  protected static void assertLocation(RedirectionException e, URI expected)
      throws Fault {
    URI location = e.getLocation();
    assertEquals(location, expected, "#getLocation()={", location,
        "} differs from expected", expected);
    logMsg("Found expected location", location);
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

  protected static final URI LOCATION = URI
      .create("http://oracle.com:888/" + FAMILY + "test");

}
