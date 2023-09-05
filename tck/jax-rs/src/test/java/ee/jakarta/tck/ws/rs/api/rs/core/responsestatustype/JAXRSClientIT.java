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

package ee.jakarta.tck.ws.rs.api.rs.core.responsestatustype;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.Status.Family;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import jakarta.ws.rs.core.Response.StatusType;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = -4533695636737308500L;


  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  // name it to ensure sorting
  static final int[] status_codes = { 200, 201, 202, 204, 205, 206, 301, 302,
      303, 304, 305, 307, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410,
      411, 412, 413, 414, 415, 416, 417, 500, 501, 502, 503, 504, 505 };

  // name it to ensure sorting
  static final Response.Status.Family[] status_family = {
      Response.Status.Family.SUCCESSFUL, Response.Status.Family.SUCCESSFUL,
      Response.Status.Family.SUCCESSFUL, Response.Status.Family.SUCCESSFUL,
      Response.Status.Family.SUCCESSFUL, Response.Status.Family.SUCCESSFUL,
      Response.Status.Family.REDIRECTION, Response.Status.Family.REDIRECTION,
      Response.Status.Family.REDIRECTION, Response.Status.Family.REDIRECTION,
      Response.Status.Family.REDIRECTION, Response.Status.Family.REDIRECTION,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.SERVER_ERROR, Response.Status.Family.SERVER_ERROR,
      Response.Status.Family.SERVER_ERROR, Response.Status.Family.SERVER_ERROR,
      Response.Status.Family.SERVER_ERROR,
      Response.Status.Family.SERVER_ERROR };

  final String[] status = { "OK", "Created", "Accepted", "No Content",
      "Reset Content", "Partial Content", "Moved Permanently", "Found",
      "See Other", "Not Modified", "Use Proxy", "Temporary Redirect",
      "Bad Request", "Unauthorized", "Payment Required", "Forbidden",
      "Not Found", "Method Not Allowed", "Not Acceptable",
      "Proxy Authentication Required", "Request Timeout", "Conflict", "Gone",
      "Length Required", "Precondition Failed", "Request Entity Too Large",
      "Request-URI Too Long", "Unsupported Media Type",
      "Requested Range Not Satisfiable", "Expectation Failed",
      "Internal Server Error", "Not Implemented", "Bad Gateway",
      "Service Unavailable", "Gateway Timeout", "HTTP Version Not Supported" };

  /* Run test */

  /*
   * @testName: getFamilyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:302;
   * 
   * @test_Strategy: Get the class of status code
   */
  @Test
  public void getFamilyTest() throws Fault {
    Response response;
    for (int i = 0; i != status_codes.length; i++) {
      response = Response.status(status_codes[i]).build();
      StatusType type = response.getStatusInfo();
      Family family = type.getFamily();
      assertTrue(family == status_family[i], "unexpected family " + family +
          " differs from " + status_family[i]);
      logMsg("Found expected family", family, "for status", status_codes[i]);
    }
  }

  /*
   * @testName: getReasonPhraseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:303;
   * 
   * @test_Strategy: Get the reason phrase
   */
  @Test
  public void getReasonPhraseTest() throws Fault {
    Response response;
    for (int i = 0; i != status_codes.length; i++) {
      response = Response.status(status_codes[i]).build();
      StatusType type = response.getStatusInfo();
      String phrase = type.getReasonPhrase();
      assertTrue(phrase.equals(status[i]), "unexpected phrase " + phrase +
          " differs from " + status[i]);
      logMsg("Found expected phrase", phrase, "for status", status_codes[i]);

    }
  }

  /*
   * @testName: getStatusCodeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:304;
   * 
   * @test_Strategy: Get the associated status code.
   */
  @Test
  public void getStatusCodeTest() throws Fault {
    Response response;
    for (int i = 0; i != status_codes.length; i++) {
      response = Response.status(status_codes[i]).build();
      StatusType type = response.getStatusInfo();
      int code = type.getStatusCode();
      assertTrue(code == status_codes[i], "unexpected status code " + code +
          " differs from " + status_codes[i]);
      logMsg("Found expected status code", code, "for status", status_codes[i]);
    }
  }

  /*
   * @testName: familyOfTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:885;
   * 
   * @test_Strategy: Get the response status family for the status code.
   */
  @Test
  public void familyOfTest() throws Fault {
    for (int i = 0; i != status_codes.length; i++) {
      Family family = Status.Family.familyOf(status_codes[i]);
      assertTrue(family == status_family[i], family + " differs from expected: " +
          status_family[i]);
    }
    logMsg("#familyOf() returned expected Family for given statuses");
  }
}