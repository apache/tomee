/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.core.securitycontext;

import org.apache.commons.httpclient.Header;

import ee.jakarta.tck.ws.rs.common.webclient.http.HttpResponse;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.ws.rs.core.Response;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 */
public abstract class JAXRSClient extends JAXRSCommonClient {
  private static final long serialVersionUID = 1L;

  protected static final String URL = "Context";

  protected HttpResponse response;

  protected String wwwAuthenticate;

  protected String user;

  protected String password;

  protected String authuser;

  protected String authpassword;

  public void setup() {
    user = System.getProperty("user");
    password = System.getProperty("password");
    authuser = System.getProperty("authuser");
    authpassword = System.getProperty("authpassword");
    assertTrue(!isNullOrEmpty(user), "user was not set");
    assertTrue(!isNullOrEmpty(password),
        "password was not set");
    assertTrue(!isNullOrEmpty(authuser),
        "authuser was not set");
    assertTrue(!isNullOrEmpty(authpassword),
        "authpassword was not set");
    super.setup();
  }

  public void noAuthorizationTest() throws Fault {
    setProperty(STATUS_CODE, getStatusCode(Response.Status.UNAUTHORIZED));
    invokeRequest();
    assertTrue(wwwAuthenticate != null,
        "Expected authentication request missing!");
  }

  protected void invokeRequest() throws Fault {
    setProperty(REQUEST, buildRequest("GET", URL));
    invoke();
    response = _testCase.getResponse();
    Header header = response.getResponseHeader("WWW-Authenticate");
    wwwAuthenticate = header == null ? null : header.getValue();
  }
}
