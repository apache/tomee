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

package ee.jakarta.tck.ws.rs.api.rs.bindingpriority;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.WebTarget;
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

  private static final long serialVersionUID = 1501029701397272718L;

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  /*
   * @testName: checkBindingPriorityHigherRegisteredFirstTest
   * 
   * @assertion_ids: JAXRS:SPEC:92;
   * 
   * @test_Strategy: Priority defined for a filter or interceptor.
   */
  @Test
  public void checkBindingPriorityHigherRegisteredFirstTest() throws Fault {
    AtomicInteger ai = new AtomicInteger(0);
    ContextProvider lowerProiority = new LowerPriorityProvider(ai);
    ContextProvider higherPriority = new HigherPriorityProvider(ai);
    Response response = invokeWithClientRequestFilters(higherPriority,
        lowerProiority);
    assertTrue(response.getStatus() == Status.OK.getStatusCode(),
        "returned status " + response.getStatus());
  }

  /*
   * @testName: checkBindingPriorityLowerRegisteredFirstTest
   * 
   * @assertion_ids: JAXRS:SPEC:92;
   * 
   * @test_Strategy: Priority defined for a filter or interceptor.
   */
  @Test
  public void checkBindingPriorityLowerRegisteredFirstTest() throws Fault {
    AtomicInteger ai = new AtomicInteger(0);
    ContextProvider lowerProiority = new LowerPriorityProvider(ai);
    ContextProvider higherPriority = new HigherPriorityProvider(ai);
    Response response = invokeWithClientRequestFilters(lowerProiority,
        higherPriority);
    assertTrue(response.getStatus() == Status.OK.getStatusCode(),
        "returned status " + response.getStatus());
  }

  //////////////////////////////////////////////////////////////////////

  protected Response invokeWithClientRequestFilters(
      ClientRequestFilter... filters) {
    Client client = ClientBuilder.newClient();
    for (ClientRequestFilter filter : filters)
      client.register(filter);
    WebTarget target = client.target("http://nourl/");
    Response response = target.request().buildGet().invoke();
    return response;
  }

}
