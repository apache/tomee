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

package ee.jakarta.tck.ws.rs.ee.rs.client.syncinvoker;

import ee.jakarta.tck.ws.rs.common.impl.TRACE;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("resource")
public class Resource {

  @GET
  @Path("get")
  public String get() {
    return "get";
  }

  @GET
  @Path("getnotok")
  public Response getNotOk() {
    return Response.status(Status.NOT_ACCEPTABLE).build();
  }

  @HEAD
  @Path("head")
  public String head() {
    return "head";
  }

  @HEAD
  @Path("headnotok")
  public Response headNotOk() {
    return Response.status(Status.NOT_ACCEPTABLE).build();
  }

  @PUT
  @Path("put")
  public String put(String value) {
    return value;
  }

  @PUT
  @Path("putnotok")
  public Response putNotOk(String value) {
    return Response.status(Status.NOT_ACCEPTABLE).build();
  }

  @POST
  @Path("post")
  public String post(String value) {
    return value;
  }

  @POST
  @Path("postnotok")
  public Response postNotOk(String value) {
    return Response.status(Status.NOT_ACCEPTABLE).build();
  }

  @DELETE
  @Path("delete")
  public String delete() {
    return "delete";
  }

  @DELETE
  @Path("deletenotok")
  public Response deleteNotOk() {
    return Response.status(Status.NOT_ACCEPTABLE).build();
  }

  @OPTIONS
  @Path("options")
  public String options() {
    return "options";
  }

  @OPTIONS
  @Path("optionsnotok")
  public Response optionsNotOk() {
    return Response.status(Status.NOT_ACCEPTABLE).build();
  }

  @TRACE
  @Path("trace")
  public String trace() {
    return "trace";
  }

  @TRACE
  @Path("tracenotok")
  public Response traceNotOk() {
    return Response.status(Status.NOT_ACCEPTABLE).build();
  }

}
