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

package ee.jakarta.tck.ws.rs.api.rs.ext.runtimedelegate;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.ws.rs.ext.RuntimeDelegate;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSDelegateClient extends JAXRSCommonClient {
  private static final long serialVersionUID = 1L;

  /**
   * Check what is the RuntimeDelegate
   * 
   * @param wantTckRuntimeDelegate
   * @throws Fault
   *           when wantTckRuntimeDelegate && RuntimeDelegate.getInstance !=
   *           TckRuntimeDelegate when !wantTckRuntimeDelegate &&
   *           RuntimeDelegate.getInstance == TckRuntimeDelegate
   */
  protected void assertRuntimeDelegate(boolean wantTckRuntimeDelegate)
      throws Fault {
    RuntimeDelegate delegate = RuntimeDelegate.getInstance();
    Class<? extends RuntimeDelegate> clazz = delegate.getClass();
    boolean check = clazz == TckRuntimeDelegate.class;
    check = (wantTckRuntimeDelegate ? check : !check);
    assertTrue(check, "TckRuntimeDelegate was " +
        (wantTckRuntimeDelegate ? "" : " not ") + "set, got " + clazz.getName());
    logMsg("Found", clazz.getName());
  }

  protected void assertRuntimeDelegate() throws Fault {
    assertRuntimeDelegate(true);
  }

}
