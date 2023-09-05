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

package ee.jakarta.tck.ws.rs.ee.rs.get;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path(value = "/GetTest")
public class HttpMethodGetTest {

  static String html_content = "<html>"
      + "<head><title>CTS-get text/html</title></head>"
      + "<body>CTS-get text/html</body></html>";

  @GET
  public Response getPlain() {
    return Response.ok("CTS-get text/plain").header("CTS-HEAD", "text-plain")
        .build();
  }

  @GET
  @Produces(value = "text/html")
  public Response getHtml() {
    return Response.ok(html_content).header("CTS-HEAD", "text-html").build();
  }

  @GET
  @Path(value = "/sub")
  public Response getSub() {
    return Response.ok("CTS-get text/plain")
        .header("CTS-HEAD", "sub-text-plain").build();
  }

  @GET
  @Path(value = "/sub")
  @Produces(value = "text/html")
  public Response headSub() {
    return Response.ok(html_content).header("CTS-HEAD", "sub-text-html")
        .build();
  }

  @Path("{id}")
  public SubResource getAbstractResource(@PathParam("id") int id) {
    return new SubResource();
  }

  @Path("recursive")
  public RecursiveLocator recursion() {
    return new RecursiveLocator();
  }
}
