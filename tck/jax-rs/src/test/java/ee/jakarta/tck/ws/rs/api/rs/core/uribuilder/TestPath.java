/*
 * Copyright (c) 2007, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.api.rs.core.uribuilder;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path(value = "/TestPath")
public class TestPath {

  @GET
  public Response getPlain() {
    return Response.ok().build();
  }

  @Path(value = "/sub")
  public Response headSub() {
    return Response.ok().build();
  }

  @Path(value = "sub1")
  public Response test1() {
    return Response.ok().build();
  }

  @Path(value = "/sub2")
  public Response test1(@QueryParam("testName") String test) {
    return Response.ok(test).build();
  }

}
