/*
 * Copyright (c) 2012, 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.resource.webappexception.mapper;

import java.io.IOException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path(value = "resource")
public class Resource {

  @GET
  @Path("noresponse")
  public String noresponse() {
    throw new WebApplicationException();
  }

  @GET
  @Path("responseok")
  public String responseOk() {
    Response r = Response.ok().build();
    throw new WebApplicationException(r);
  }

  @GET
  @Path("responsestatusok")
  public String responseStatusOk() {
    throw new WebApplicationException(Status.OK);
  }

  @GET
  @Path("responseentity")
  public String responseEntity() {
    Response r = Response.ok(getClass().getSimpleName()).build();
    throw new WebApplicationException(r);
  }

  @GET
  @Path("responsestatusintok")
  public String responseStatusIntOk() {
    throw new WebApplicationException(Status.OK.getStatusCode());
  }

  @GET
  @Path("responsethrowable")
  public String responseThrowable() {
    throw new WebApplicationException(getIOException());
  }

  @GET
  @Path("responsethrowableok")
  public String responseThrowableOk() {
    Response r = Response.ok().build();
    throw new WebApplicationException(getIOException(), r);
  }

  @GET
  @Path("responsestatusthrowableok")
  public String responseThrowableStatusOk() {
    throw new WebApplicationException(getIOException(), Status.OK);
  }

  @GET
  @Path("responsestatusthrowableintok")
  public String responseThrowableStatusIntOk() {
    throw new WebApplicationException(getIOException(),
        Status.OK.getStatusCode());
  }

  @GET
  @Path("uncheckedexception")
  public String throwIOExecption() {
    throw new ClassCastException("ERROR");
  }

  IOException getIOException() {
    IOException ioe = new IOException("You should NOT see this message");
    return ioe;
  }

}