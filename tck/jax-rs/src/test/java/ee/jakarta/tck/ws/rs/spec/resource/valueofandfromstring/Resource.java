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

package ee.jakarta.tck.ws.rs.spec.resource.valueofandfromstring;

import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

@Path("resource")
public class Resource {

  @GET
  @Path("enummatrix")
  public String enummatrix(
      @MatrixParam("param") EnumWithFromStringAndValueOf param) {
    return param.name();
  }

  @GET
  @Path("enumquery")
  public String enumquery(
      @QueryParam("param") EnumWithFromStringAndValueOf param) {
    return param.name();
  }

  @GET
  @Path("enumpath/{param}")
  public String enumpath(
      @PathParam("param") EnumWithFromStringAndValueOf param) {
    return param.name();
  }

  @GET
  @Path("enumcookie")
  public String enumcookie(
      @CookieParam("param") EnumWithFromStringAndValueOf param) {
    return param.name();
  }

  @GET
  @Path("enumheader")
  public String enumheader(
      @HeaderParam("param") EnumWithFromStringAndValueOf param) {
    return param.name();
  }

  @GET
  @Path("entitymatrix")
  public String entitymatrix(
      @MatrixParam("param") ParamEntityWithFromStringAndValueOf param) {
    return param.getValue();
  }

  @GET
  @Path("entityquery")
  public String entityquery(
      @QueryParam("param") ParamEntityWithFromStringAndValueOf param) {
    return param.getValue();
  }

  @GET
  @Path("entitypath/{param}")
  public String entitypath(
      @PathParam("param") ParamEntityWithFromStringAndValueOf param) {
    return param.getValue();
  }

  @GET
  @Path("entitycookie")
  public String entitycookie(
      @CookieParam("param") ParamEntityWithFromStringAndValueOf param) {
    return param.getValue();
  }

  @GET
  @Path("entityheader")
  public String entityheader(
      @HeaderParam("param") ParamEntityWithFromStringAndValueOf param) {
    return param.getValue();
  }
}
