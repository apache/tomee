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

package ee.jakarta.tck.ws.rs.ee.rs.container.requestcontext.illegalstate;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("resource")
public class Resource {

  @GET
  @Path("setmethod")
  public Response setMethod() {
    return createResponseWithHeader();
  }

  @GET
  @Path("setrequesturi1")
  public Response setRequestUri1() {
    return createResponseWithHeader();
  }

  @GET
  @Path("setrequesturi2")
  public Response setRequestUri2() {
    return createResponseWithHeader();
  }

  @GET
  @Path("abortwith")
  public Response abortWith() {
    return createResponseWithHeader();
  }

  @GET
  @Path("setentitystream")
  public Response setEntityStream() {
    return createResponseWithHeader();
  }

  @GET
  @Path("setsecuritycontext")
  public Response setSecurityContext() {
    return createResponseWithHeader();
  }

  // //////////////////////////////////////////////////////////////////

  @Context
  UriInfo info;

  private Response createResponseWithHeader() {
    // get value of @Path(value)
    List<PathSegment> segments = info.getPathSegments();
    PathSegment last = segments.get(segments.size() - 1);
    // convert the value to ContextOperation
    ContextOperation op = ContextOperation
        .valueOf(last.getPath().toUpperCase());
    Response.ResponseBuilder builder = Response.ok();
    // set a header with ContextOperation so that the filter knows what to
    // do
    builder = builder.header(TemplateFilter.OPERATION, op.name());
    return builder.build();
  }

}
