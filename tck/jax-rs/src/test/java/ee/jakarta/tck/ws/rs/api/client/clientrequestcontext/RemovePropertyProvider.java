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

package ee.jakarta.tck.ws.rs.api.client.clientrequestcontext;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.Response;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient.Fault;

public class RemovePropertyProvider extends ContextProvider {
  private AtomicInteger counter;

  public RemovePropertyProvider(AtomicInteger counter) {
    super();
    this.counter = counter;
  }

  @Override
  protected void checkFilterContext(ClientRequestContext context) throws Fault {
    String propName = "PROPERTY";
    switch (counter.incrementAndGet()) {
    case 1:
      Object property = context.getProperty(propName);
      assertTrue(property == null, "property already exist");
      context.setProperty(propName, propName);
      break;
    case 2:
      property = context.getProperty(propName);
      assertTrue(property != null, "property not exist");
      context.removeProperty(propName);
      break;
    case 3:
      property = context.getProperty(propName);
      assertTrue(property == null, "property already exist");
      Response response = Response.ok("NULL").build();
      context.abortWith(response);
      break;
    }
  }

}
