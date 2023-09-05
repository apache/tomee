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

package ee.jakarta.tck.ws.rs.api.rs.ext.runtimedelegate.setinstance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.ext.RuntimeDelegate;
import ee.jakarta.tck.ws.rs.api.rs.ext.runtimedelegate.JAXRSDelegateClient;
import ee.jakarta.tck.ws.rs.api.rs.ext.runtimedelegate.TckRuntimeDelegate;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSDelegateClient {

  private static final long serialVersionUID = -5586431064207012301L;

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  /*
   * @testName: askForTckRuntimeDelegateGivenBySetInstanceTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:291;JAXRS:JAVADOC:292;
   * 
   * @test_Strategy: Set new RuntimeDelegate and check it is TckRuntimeDelegate
   * 
   */
  @Test
  public void askForTckRuntimeDelegateGivenBySetInstanceTest() throws Fault {
    RuntimeDelegate original = RuntimeDelegate.getInstance();
    RuntimeDelegate.setInstance(new TckRuntimeDelegate());
    try {
      assertRuntimeDelegate();
    } finally {
      RuntimeDelegate.setInstance(original);
      assertRuntimeDelegate(false);
    }
  }

  /*
   * @testName: checkTckRuntimeDelegateIsNotDefaultTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:292;
   * 
   * @test_Strategy: Check by default, it is not our RuntimeDelegate
   */
  @Test
  public void checkTckRuntimeDelegateIsNotDefaultTest() throws Fault {
    assertRuntimeDelegate(false);
  }

}
