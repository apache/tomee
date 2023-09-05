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

package ee.jakarta.tck.ws.rs.spec.template;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path(value = "/TemplateTest")
public class TemplateTest {

  @GET
  @Path("{id}")
  public String limited(@PathParam("id") String id) {
    StringBuffer sb = new StringBuffer().append("id=" + id);
    return sb.toString();
  }

  @GET
  @Path("xyz/{id}")
  public String onelimited(@PathParam("id") String id) {
    StringBuffer sb = new StringBuffer().append("id2=" + id);
    return sb.toString();
  }

  @GET
  @Path("{id}/{name}.html")
  public String filetype(@PathParam("id") String id,
      @PathParam("name") String name) {
    StringBuffer sb = new StringBuffer().append("id4=" + id);
    sb.append("name=" + name);
    return sb.toString();
  }

  @GET
  @Path("{id: .+}")
  public String nolimites(@PathParam("id") String id) {
    StringBuffer sb = new StringBuffer().append("id1=" + id);
    return sb.toString();
  }

  @GET
  @Path(value = "xyz/{id: .+}")
  public String onenolimite(@PathParam("id") String id) {
    StringBuffer sb = new StringBuffer().append("id3=" + id);
    return sb.toString();
  }

  @GET
  @Path(value = "{id: .+}/{name}.xml")
  public String filetype1(@PathParam("id") String id,
      @PathParam("name") String name) {
    StringBuffer sb = new StringBuffer().append("id5=" + id);
    sb.append("name=" + name);
    return sb.toString();
  }
}
