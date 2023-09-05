/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.api.rs.runtimetype;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.RuntimeType;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {
  private static final long serialVersionUID = -2994744934835260890L;

  final static String[] names = { "CLIENT", "SERVER" };

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  /*
   * @testName: valueOfTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:974;
   * 
   * @test_Strategy: Test valueOf method
   */
  @Test
  public void valueOfTest() throws Fault {
    assertEqualsInt(RuntimeType.values().length, 2,
        "Unexpected number of values of RuntimeType enum");
    RuntimeType type = RuntimeType.valueOf(names[0]);
    assertEquals(RuntimeType.CLIENT, type, "Unexpected RuntimeType", type);
    logMsg("#valueOf(", names[0], ") equals", type, "as expected");

    type = RuntimeType.valueOf(names[1]);
    assertEquals(RuntimeType.SERVER, type, "Unexpected RuntimeType", type);
    logMsg("#valueOf(", names[1], ") equals", type, "as expected");
  }

  /*
   * @testName: valuesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:975;
   * 
   * @test_Strategy: Test values method
   */
  @Test
  public void valuesTest() throws Fault {
    RuntimeType[] types = RuntimeType.values();
    assertEqualsInt(types.length, 2,
        "Unexpected number of values of RuntimeType enum");

    String[] dynamicNames = { types[0].name(), types[1].name() };
    String singleDynamicName = dynamicNames[0] + ", " + dynamicNames[1];

    for (String name : names) {
      assertContains(singleDynamicName, name, name,
          "has unexpectedly not found in list", singleDynamicName);
      logMsg("Found", name, "in valus()", singleDynamicName);
    }

  }

}
