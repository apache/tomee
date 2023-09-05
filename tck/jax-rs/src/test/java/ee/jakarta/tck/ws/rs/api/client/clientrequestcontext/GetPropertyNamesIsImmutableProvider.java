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
import jakarta.ws.rs.ext.Provider;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient.Fault;

@Provider
/**
 * Counter should remain the same as any change would suggest that the
 * getPropertyNames collection is not immutable
 */
public class GetPropertyNamesIsImmutableProvider extends ContextProvider {
  private AtomicInteger counter;

  public static final String NEWNAME = "AnyNewNameAddedToPropertyNames";

  public GetPropertyNamesIsImmutableProvider(AtomicInteger counter) {
    super();
    this.counter = counter;
  }

  @Override
  protected void checkFilterContext(ClientRequestContext context) throws Fault {
    Collection<String> properties = context.getPropertyNames();
    try {
      properties.add(NEWNAME);
    } catch (Exception e) {
      // any possible exception here is ok as collection should be
      // immutable
    }
    properties = context.getPropertyNames();
    if (properties.contains(NEWNAME))
      counter.set(counter.get() + 100);
    context.abortWith(Response.ok(counter.get()).build());
  }
}
