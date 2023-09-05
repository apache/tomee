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

package ee.jakarta.tck.ws.rs.spec.filter.exception;

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.ReaderInterceptorContext;

@Provider
@Priority(500)
public class AddTenGlobalInterceptor extends AbstractAddInterceptor {

  public static final String EXCEPTION_FIRING_HEADER = "AddTenInterceptorThrowsException";

  public AddTenGlobalInterceptor() {
    super(10);
  }

  @Override
  public Object aroundReadFrom(ReaderInterceptorContext context)
      throws IOException, WebApplicationException {
    String header = context.getHeaders().getFirst(EXCEPTION_FIRING_HEADER);
    if (header != null)
      throw new RuntimeException(header);
    return super.aroundReadFrom(context);
  }
}
