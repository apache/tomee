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

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.Response;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient.Fault;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;

public class GetPropertyNamesProvider extends ContextProvider {
  private AtomicInteger counter;

  public GetPropertyNamesProvider(AtomicInteger counter) {
    super();
    this.counter = counter;
  }

  @Override
  protected void checkFilterContext(ClientRequestContext context) throws Fault {
    if (counter.incrementAndGet() == 2) {
      Collection<String> properties = context.getPropertyNames();
      String entity = properties == null ? "NULL"
          : JaxrsUtil.iterableToString(";", properties);
      Response r = Response.ok(entity).build();
      context.abortWith(r);
    } else {
      context.setProperty("PROPERTY1", "value1");
      context.setProperty("PROPERTY2", "value2");
    }
  }

  protected static <T> String collectionToString(Collection<T> collection) {
    StringBuilder sb = new StringBuilder();
    for (T t : collection)
      sb.append(t).append(";");
    return sb.toString();
  }
}
