/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.container.requestcontext.security;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

/**
 * The methods are called here by reflection from the superclass
 */
@Provider
public class RequestFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext)
      throws IOException {
    SecurityContext security = requestContext.getSecurityContext();
    String msg = "security.getUserPrincipal() is null";
    if (security.getUserPrincipal() != null)
      msg = security.getUserPrincipal().getName();
    Response response = Response.ok(msg).build();
    requestContext.abortWith(response);
  }

}
