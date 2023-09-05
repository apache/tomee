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

package ee.jakarta.tck.ws.rs.api.rs.core.configurable;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Configurable;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient.Fault;

public abstract class SingleCheckAssertable extends Assertable {

  protected abstract void check(Configurable<?> configurable) throws Fault;

  @Override
  public void check1OnClient(Client client) throws Fault {
    check(client);
  }

  @Override
  public void check2OnTarget(WebTarget target) throws Fault {
    check(target);
  }

}
