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

package ee.jakarta.tck.ws.rs.spec.filter.namebinding;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("resource")
public class Resource {

  @SingleNameBinding
  @Path("ten")
  @GET
  public String getTen() {
    return "10";
  }

  @Path("one")
  @GET
  // no NameBinding, no interceptor shall be invoked
  public String getOne() {
    return "1";
  }

  @Path("echo")
  @SingleNameBinding
  @POST
  public String echo(String value) {
    return value;
  }

  @Path("complement")
  @GET
  @ComplementNameBinding
  // This has only complement binding, i.e. no interceptor contains only
  // complement binding, i.e. no interceptor shall be called
  public String complement() {
    return "10000";
  }
}
