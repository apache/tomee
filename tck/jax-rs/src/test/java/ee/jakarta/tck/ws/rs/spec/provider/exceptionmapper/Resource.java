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

package ee.jakarta.tck.ws.rs.spec.provider.exceptionmapper;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;

@Path("resource")
public class Resource {

  @Path("throwable")
  @GET
  public String throwable() throws Throwable {
    throw new Throwable(new RuntimeException(new ClientErrorException(499)));
  }

  @Path("exception")
  @GET
  public String exception() throws Exception {
    throw new Exception(new RuntimeException(new ClientErrorException(499)));
  }

  @Path("runtime")
  @GET
  public String runtime() {
    throw new RuntimeException(new ClientErrorException(499));
  }

  @Path("webapp")
  @GET
  public String webApp() {
    throw new WebApplicationException(new RuntimeException());
  }

  @Path("clienterror")
  @GET
  public String clienterror() {
    throw new ClientErrorException(499, new RuntimeException());
  }

  @Path("mapped")
  @GET
  public String mappedException() {
    throw new ExceptionFromMappedException();
  }

  @Path("chain")
  @GET
  public String chain() {
    throw new FilterChainTestException();
  }
}
