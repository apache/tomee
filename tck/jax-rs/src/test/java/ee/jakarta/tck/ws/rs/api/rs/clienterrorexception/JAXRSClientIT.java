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

package ee.jakarta.tck.ws.rs.api.rs.clienterrorexception;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.ClientErrorException;
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

  private static final long serialVersionUID = 6441920735149224053L;

  protected static final String MESSAGE = "TCK ClientErrorException description";

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
   * @assertion_ids: JAXRS:JAVADOC:310; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or if
   * it is not from the Response.Status.Family.CLIENT_ERROR status code family.
   * 
   * getResponse
   */
  @Test
  public void constructorStatusTest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) == Status.Family.CLIENT_ERROR) {
        ClientErrorException e = new ClientErrorException(status);
        assertResponse(e, status);
      }
  }

  /*
   * @testName: constructorStatusThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:310;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or if
   * it is not from the Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorStatusThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) != Status.Family.CLIENT_ERROR)
        try {
          ClientErrorException e = new ClientErrorException(status);
          fault("IllegalArgumentException has not been thrown for status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
  }

  /*
   * @testName: constructorStatusNullThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:310;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or if
   * it is not from the Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorStatusNullThrowsExceptionTest() throws Fault {
    try {
      ClientErrorException e = new ClientErrorException((Status) null);
      fault(
          "IllegalArgumentException has not been thrown for null status; exception",
          e);
    } catch (IllegalArgumentException e) {
      logMsg(
          "IllegalArgumentException has been thrown as expected for null status");
    }
  }

  /*
   * @testName: constructorIntTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:311; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or if it is not from the
   * Response.Status.Family.CLIENT_ERROR status code family.
   * 
   * getResponse
   */
  @Test
  public void constructorIntTest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) == Status.Family.CLIENT_ERROR) {
        ClientErrorException e = new ClientErrorException(
            status.getStatusCode());
        assertResponse(e, status);
      }
  }

  /*
   * @testName: constructorIntThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:311;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or if it is not from the
   * Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorIntThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) != Status.Family.CLIENT_ERROR)
        try {
          ClientErrorException e = new ClientErrorException(
              status.getStatusCode());
          fault("IllegalArgumentException has not been thrown for status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
  }

  /*
   * @testName: constructorIntNotValidStatusThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:311;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or if it is not from the
   * Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorIntNotValidStatusThrowsExceptionTest() throws Fault {
    for (int status : new int[] { -1, 999, Integer.MIN_VALUE,
        Integer.MAX_VALUE })
      try {
        ClientErrorException e = new ClientErrorException(status);
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
  }

  /*
   * @testName: constructorResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:312; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.CLIENT_ERROR status code family.
   * 
   * getResponse
   */
  @Test
  public void constructorResponseTest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) == Status.Family.CLIENT_ERROR) {
        Response response = buildResponse(status);
        ClientErrorException e = new ClientErrorException(response);
        assertResponse(e, status, HOST);
      }
  }

  /*
   * @testName: constructorResponseThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:312;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorResponseThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) != Status.Family.CLIENT_ERROR)
        try {
          Response response = Response.status(status).build();
          ClientErrorException e = new ClientErrorException(response);
          fault("IllegalArgumentException has not been thrown for status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
  }

  /*
   * @testName: constructorStatusThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:313; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or if
   * it is not from the Response.Status.Family.CLIENT_ERROR status code family.
   * 
   * getResponse
   */
  @Test
  public void constructorStatusThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) == Status.Family.CLIENT_ERROR) {
        for (Throwable throwable : throwables) {
          ClientErrorException e = new ClientErrorException(status, throwable);
          assertResponse(e, status);
          assertCause(e, throwable);
        }
      }
  }

  /*
   * @testName: constructorStatusThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:313;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or if
   * it is not from the Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorStatusThrowableThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) != Status.Family.CLIENT_ERROR)
        try {
          ClientErrorException e = new ClientErrorException(status,
              new Throwable());
          fault("IllegalArgumentException has not been thrown for status",
              status, "exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
  }

  /*
   * @testName: constructorStatusNullThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:313;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or if
   * it is not from the Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorStatusNullThrowableThrowsExceptionTest() throws Fault {
    try {
      ClientErrorException e = new ClientErrorException((Status) null,
          new Throwable());
      fault(
          "IllegalArgumentException has not been thrown for null status; exception",
          e);
    } catch (IllegalArgumentException e) {
      logMsg(
          "IllegalArgumentException has been thrown as expected for null status");
    }
  }

  /*
   * @testName: constructorIntThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:314; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or if it is not from the
   * Response.Status.Family.CLIENT_ERROR status code family.
   * 
   * getResponse
   */
  @Test
  public void constructorIntThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) == Status.Family.CLIENT_ERROR) {
        for (Throwable throwable : throwables) {
          ClientErrorException e = new ClientErrorException(
              status.getStatusCode(), throwable);
          assertResponse(e, status);
          assertCause(e, throwable);
        }
      }
  }

  /*
   * @testName: constructorIntThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:314;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or if it is not from the
   * Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorIntThrowableThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) != Status.Family.CLIENT_ERROR)
        try {
          ClientErrorException e = new ClientErrorException(
              status.getStatusCode(), new Throwable());
          fault("IllegalArgumentException has not been thrown for status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
  }

  /*
   * @testName: constructorIntNotValidStatusThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:314;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or if it is not from the
   * Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorIntNotValidStatusThrowableThrowsExceptionTest()
      throws Fault {
    for (int status : new int[] { -1, 999, Integer.MIN_VALUE,
        Integer.MAX_VALUE })
      try {
        ClientErrorException e = new ClientErrorException(status,
            new Throwable());
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
  }

  /*
   * @testName: constructorResponseThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:315; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.CLIENT_ERROR status code family.
   * 
   * getResponse
   */
  @Test
  public void constructorResponseThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) == Status.Family.CLIENT_ERROR) {
        for (Throwable throwable : throwables) {
          Response response = buildResponse(status);
          ClientErrorException e = new ClientErrorException(response,
              throwable);
          assertResponse(e, status, HOST);
          assertCause(e, throwable);
        }
      }
  }

  /*
   * @testName: constructorResponseThrowableThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:315;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorResponseThrowableThrowsExceptionTest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) != Status.Family.CLIENT_ERROR)
        try {
          Response response = Response.status(status).build();
          ClientErrorException e = new ClientErrorException(response,
              new Throwable());
          fault("IllegalArgumentException has not been thrown for status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
  }

  /*
   * @testName: constructorStringStatusTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1060; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new client error exception. getResponse
   */
  @Test
  public void constructorStringStatusTest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) == Status.Family.CLIENT_ERROR) {
        ClientErrorException e = new ClientErrorException(MESSAGE, status);
        assertResponse(e, status);
        assertMessage(e);
      }
  }

  /*
   * @testName: constructorStringStatusThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1060;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or if
   * it is not from the Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorStringStatusThrowsIAETest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) != Status.Family.CLIENT_ERROR)
        try {
          ClientErrorException e = new ClientErrorException(MESSAGE, status);
          fault("IllegalArgumentException has not been thrown for status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
  }

  /*
   * @testName: constructorStringStatusNullThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1060;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or if
   * it is not from the Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorStringStatusNullThrowsIAETest() throws Fault {
    try {
      ClientErrorException e = new ClientErrorException(MESSAGE, (Status) null);
      fault(
          "IllegalArgumentException has not been thrown for null status; exception",
          e);
    } catch (IllegalArgumentException e) {
      logMsg(
          "IllegalArgumentException has been thrown as expected for null status");
    }
  }

  /*
   * @testName: constructorStringIntTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1061; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or if it is not from the
   * Response.Status.Family.CLIENT_ERROR status code family.
   * 
   * getResponse
   */
  @Test
  public void constructorStringIntTest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) == Status.Family.CLIENT_ERROR) {
        ClientErrorException e = new ClientErrorException(MESSAGE,
            status.getStatusCode());
        assertResponse(e, status);
        assertMessage(e);
      }
  }

  /*
   * @testName: constructorStringIntThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1061;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or if it is not from the
   * Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorStringIntThrowsIAETest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) != Status.Family.CLIENT_ERROR)
        try {
          ClientErrorException e = new ClientErrorException(MESSAGE,
              status.getStatusCode());
          fault("IllegalArgumentException has not been thrown for status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
  }

  /*
   * @testName: constructorStringIntNotValidStatusThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1061;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or if it is not from the
   * Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorStringIntNotValidStatusThrowsExceptionTest()
      throws Fault {
    for (int status : new int[] { -1, 999, Integer.MIN_VALUE,
        Integer.MAX_VALUE })
      try {
        ClientErrorException e = new ClientErrorException(MESSAGE, status);
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
  }

  /*
   * @testName: constructorStringResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1062; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new client error exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringResponseTest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) == Status.Family.CLIENT_ERROR) {
        Response response = buildResponse(status);
        ClientErrorException e = new ClientErrorException(MESSAGE, response);
        assertResponse(e, status, HOST);
        assertMessage(e);
      }
  }

  /*
   * @testName: constructorStringResponseThrowsIEATest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1062;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorStringResponseThrowsIEATest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) != Status.Family.CLIENT_ERROR)
        try {
          Response response = Response.status(status).build();
          ClientErrorException e = new ClientErrorException(MESSAGE, response);
          fault("IllegalArgumentException has not been thrown for status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
  }

  /*
   * @testName: constructorStringStatusThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1063; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new client error exception. getResponse
   */
  @Test
  public void constructorStringStatusThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) == Status.Family.CLIENT_ERROR) {
        for (Throwable throwable : throwables) {
          ClientErrorException e = new ClientErrorException(MESSAGE, status,
              throwable);
          assertResponse(e, status);
          assertCause(e, throwable);
          assertMessage(e);
        }
      }
  }

  /*
   * @testName: constructorStringStatusThrowableThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1063;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or if
   * it is not from the Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorStringStatusThrowableThrowsIAETest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) != Status.Family.CLIENT_ERROR)
        try {
          ClientErrorException e = new ClientErrorException(MESSAGE, status,
              new Throwable());
          fault("IllegalArgumentException has not been thrown for status",
              status, "exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
  }

  /*
   * @testName: constructorStringStatusNullThrowableThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1063;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is null or if
   * it is not from the Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorStringStatusNullThrowableThrowsIAETest() throws Fault {
    try {
      ClientErrorException e = new ClientErrorException(MESSAGE, (Status) null,
          new Throwable());
      fault(
          "IllegalArgumentException has not been thrown for null status; exception",
          e);
    } catch (IllegalArgumentException e) {
      logMsg(
          "IllegalArgumentException has been thrown as expected for null status");
    }
  }

  /*
   * @testName: constructorStringIntThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1064; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new client error exception.
   * 
   * getResponse
   */
  @Test
  public void constructorStringIntThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) == Status.Family.CLIENT_ERROR) {
        for (Throwable throwable : throwables) {
          ClientErrorException e = new ClientErrorException(MESSAGE,
              status.getStatusCode(), throwable);
          assertResponse(e, status);
          assertCause(e, throwable);
          assertMessage(e);
        }
      }
  }

  /*
   * @testName: constructorStringIntThrowableThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1064;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or if it is not from the
   * Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorStringIntThrowableThrowsIAETest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) != Status.Family.CLIENT_ERROR)
        try {
          ClientErrorException e = new ClientErrorException(MESSAGE,
              status.getStatusCode(), new Throwable());
          fault("IllegalArgumentException has not been thrown for status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
  }

  /*
   * @testName: constructorStringIntNotValidStatusThrowableThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1064;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the status code is not a valid
   * HTTP status code or if it is not from the
   * Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorStringIntNotValidStatusThrowableThrowsIAETest()
      throws Fault {
    for (int status : new int[] { -1, 999, Integer.MIN_VALUE,
        Integer.MAX_VALUE })
      try {
        ClientErrorException e = new ClientErrorException(MESSAGE, status,
            new Throwable());
        fault("IllegalArgumentException has not been thrown for status", status,
            "; exception", e);
      } catch (IllegalArgumentException e) {
        logMsg(
            "IllegalArgumentException has been thrown as expected for status",
            status);
      }
  }

  /*
   * @testName: constructorStringResponseThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1065; JAXRS:JAVADOC:12;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.CLIENT_ERROR status code family.
   * 
   * getResponse
   */
  @Test
  public void constructorStringResponseThrowableTest() throws Fault {
    Throwable[] throwables = new Throwable[] { new RuntimeException(),
        new IOException(), new Error(), new Throwable() };
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) == Status.Family.CLIENT_ERROR) {
        for (Throwable throwable : throwables) {
          Response response = buildResponse(status);
          ClientErrorException e = new ClientErrorException(MESSAGE, response,
              throwable);
          assertResponse(e, status, HOST);
          assertCause(e, throwable);
          assertMessage(e);
        }
      }
  }

  /*
   * @testName: constructorStringResponseThrowableThrowsIAETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1065;
   * 
   * @test_Strategy: Construct a new client error exception.
   * java.lang.IllegalArgumentException - in case the response status code is
   * not from the Response.Status.Family.CLIENT_ERROR status code family.
   */
  @Test
  public void constructorStringResponseThrowableThrowsIAETest() throws Fault {
    for (Status status : Status.values())
      if (Status.Family
          .familyOf(status.getStatusCode()) != Status.Family.CLIENT_ERROR)
        try {
          Response response = Response.status(status).build();
          ClientErrorException e = new ClientErrorException(MESSAGE, response,
              new Throwable());
          fault("IllegalArgumentException has not been thrown for status",
              status, "; exception", e);
        } catch (IllegalArgumentException e) {
          logMsg(
              "IllegalArgumentException has been thrown as expected for status",
              status);
        }
  }

  // ////////////////////////////////////////////////////////////////////////
  protected Response buildResponse(Status status) {
    Response r = Response.status(status).header(HttpHeaders.HOST, HOST).build();
    return r;
  }

  protected void assertResponse(ClientErrorException e, Status status)
      throws Fault {
    assertNotNull(e.getResponse(), "getResponse is null");
    int got = e.getResponse().getStatus();
    assertEqualsInt(got, status.getStatusCode(), "Status set in Response", got,
        "differes from expected", status);
    logMsg("Response of the exception contains expected status", status);
  }

  /**
   * Check the given exception contains a prebuilt response containing the http
   * header HOST
   */
  protected void assertResponse(ClientErrorException e, Status status,
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

  protected void assertMessage(ClientErrorException e) throws Fault {
    assertNotNull(e.getMessage(), "getMessage() is null");
    assertContains(e.getMessage(), MESSAGE, "Unexpected getMessage()",
        e.getMessage());
    logMsg("found expected getMessage()=", e.getMessage());
  }

}
