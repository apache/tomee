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

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.Priority;
import jakarta.ws.rs.client.ClientRequestContext;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient.Fault;

@Priority(Integer.MIN_VALUE)
// the lower the number the higher the priority
public class HigherPriorityProvider extends ContextProvider {
  private AtomicInteger counter;

  public HigherPriorityProvider(AtomicInteger counter) {
    super();
    this.counter = counter;
  }

  @Override
  protected void checkFilterContext(ClientRequestContext context) throws Fault {
    assertFault(counter.incrementAndGet() == 1,
        "Lower provider priority has been called as ", counter.get(), "nd");
  }
}
