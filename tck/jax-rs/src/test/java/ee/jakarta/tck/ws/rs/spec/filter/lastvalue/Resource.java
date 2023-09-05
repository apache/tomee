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

package ee.jakarta.tck.ws.rs.spec.filter.lastvalue;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

@Path("resource")
public class Resource {

  public static final String HEADERNAME = "FILTER_HEADER";

  public static final String getName() {
    // make this long enough to let entity provider getSize()
    // be enough to let our interceptor name fit in
    return "<resource>" + Resource.class.getName() + "</resource>";
  }

  @POST
  @Path("postlist")
  public String postList(List<String> list) {
    return list.iterator().next();
  }

  @GET
  @Path("getlist")
  public Response getList() {
    ArrayList<String> list = new ArrayList<String>();
    list.add(getName());
    GenericEntity<ArrayList<String>> entity = new GenericEntity<ArrayList<String>>(
        list) {
    };
    return buildResponse(entity);
  }

  @POST
  @Path("poststring")
  public Response postString(String string) {
    return buildResponse(string);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Send header that would have the power to enable filter / interceptor
  // The header is passed from client request
  @Context
  private HttpHeaders headers;

  private Response buildResponse(Object content) {
    return buildResponse(content, MediaType.WILDCARD_TYPE);
  }

  private Response buildResponse(Object content, MediaType type) {
    List<String> list = headers.getRequestHeader(HEADERNAME);
    String name = null;
    if (list != null && list.size() != 0)
      name = list.iterator().next();
    ResponseBuilder builder = Response.ok(content, type).type(type);
    if (name != null)
      builder.header(HEADERNAME, name);
    return builder.build();
  }

}
