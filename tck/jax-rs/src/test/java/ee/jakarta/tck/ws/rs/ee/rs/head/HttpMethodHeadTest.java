/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.head;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path(value = "/HeadTest")
public class HttpMethodHeadTest {

  @HEAD
  @Produces(value = "text/plain")
  public Response headPlain() {
    return Response.ok().header("CTS-HEAD", "text-plain").build();
  }

  @HEAD
  @Produces(value = "text/html")
  public Response headHtml() {
    return Response.ok().header("CTS-HEAD", "text-html").build();
  }

  @HEAD
  @Path(value = "/sub")
  @Produces(value = "text/html")
  public Response headSub() {
    return Response.ok().header("CTS-HEAD", "sub-text-html").build();
  }

  @GET
  @Path(value = "/get")
  public Response get() {
    return Response.ok("HEAD-GET").header("CTS-HEAD", "get").build();
  }
}
