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

package ee.jakarta.tck.ws.rs.spec.resourceconstructor;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

@Path("resource")
public class Resource {
  private HttpHeaders headers;

  private UriInfo info;

  private Application application;

  private Request request;

  private Providers provider;

  public Resource() {
  }

  public Resource(@Context HttpHeaders headers) {
    this.headers = headers;
  }

  public Resource(@Context HttpHeaders headers, @Context UriInfo info) {
    this.headers = headers;
    this.info = info;
  }

  public Resource(@Context HttpHeaders headers, @Context UriInfo info,
      @Context Application application) {
    this.application = application;
    this.headers = headers;
    this.info = info;
  }

  public Resource(@Context HttpHeaders headers, @Context UriInfo info,
      @Context Application application, @Context Request request) {
    this.application = application;
    this.headers = headers;
    this.info = info;
    this.request = request;
  }

  protected Resource(@Context HttpHeaders headers, @Context UriInfo info,
      @Context Application application, @Context Request request,
      @Context Providers provider) {
    this.application = application;
    this.headers = headers;
    this.info = info;
    this.request = request;
    this.provider = provider;
  }

  @GET
  @Path("mostAttributes")
  public Response isUsedConstructorWithMostAttributes() {
    boolean ok = application != null;
    ok &= headers != null;
    ok &= info != null;
    ok &= request != null;
    ok &= provider == null;
    Status status = ok ? Status.OK : Status.NOT_ACCEPTABLE;
    return Response.status(status).build();
  }

  @GET
  @Path("packageVisibility")
  Response isAvailablePackageVisibility() {
    return Response.ok().build();
  }

  @GET
  @Path("protectedVisibility")
  protected Response isAvailabeProtectedVisibility() {
    return Response.ok().build();
  }

  @SuppressWarnings({ "static-method" })
  @GET
  @Path("privateVisibility")
  private Response isAvailabePrivateVisibility() {
    return Response.ok().build();
  }

}
