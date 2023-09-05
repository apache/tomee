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

package ee.jakarta.tck.ws.rs.api.client.clientbuilder;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Configuration;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = 7395392827433641768L;

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  /*
   * @testName: newClientNoParamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1019;
   * 
   * @test_Strategy: Create new client instance using the default client builder
   * factory provided by the JAX-RS implementation provider.
   */
  @Test
  public void newClientNoParamTest() throws Fault {
    Client client = ClientBuilder.newClient();
    assertTrue(client != null, "could not create Client instance");
  }

  /*
   * @testName: newClientWithConfigurationTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1020;
   * 
   * @test_Strategy: Create new configured client instance using the default
   * client builder factory provided by the JAX-RS implementation provider.
   */
  @Test
  public void newClientWithConfigurationTest() throws Fault {
    String property = "JAXRSTCK";
    Client client = ClientBuilder.newClient();
    client.property(property, property);
    Configuration config = client.getConfiguration();
    client = ClientBuilder.newClient(config);
    assertNotNull(client, "could not create Client instance");
    assertEquals(property, client.getConfiguration().getProperty(property),
        "client does not contain given config");
  }

}
