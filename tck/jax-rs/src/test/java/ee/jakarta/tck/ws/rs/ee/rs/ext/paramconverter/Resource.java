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

package ee.jakarta.tck.ws.rs.ee.rs.ext.paramconverter;

import java.util.concurrent.atomic.AtomicInteger;

import ee.jakarta.tck.ws.rs.common.provider.StringBean;

import jakarta.activation.DataSource;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("resource")
public class Resource {
  public static final String NULL = "NULL";

  public static final String DEFAULT = "DEFAULT";

  @Path("sbquery")
  @GET
  public String stringBeanQuery(
      @DefaultValue(DEFAULT) @QueryParam("param") StringBean param) {
    return param.get();
  }

  @Path("dsquery")
  @GET
  public String dataSourceQuery(@QueryParam("param") DataSource param) {
    return param.getName();
  }

  @Path("aiquery")
  @GET
  public String atomicIntegerQuery(
      @DefaultValue(DEFAULT) @QueryParam("param") AtomicInteger param) {
    return String.valueOf(param.get());
  }

  @Path("sbpath/{param}")
  @GET
  public String stringBeanPath(@PathParam("param") StringBean param) {
    return param.get();
  }

  @Path("sbpath/default")
  @GET
  public String stringBeanPathNeverHere(
      @DefaultValue(DEFAULT) @PathParam("param") StringBean param) {
    return param.get();
  }

  @Path("sbmatrix")
  @GET
  public String stringBeanMatrix(
      @DefaultValue(DEFAULT) @MatrixParam("param") StringBean param) {
    return param.get();
  }

  @Path("sbform")
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String stringBeanForm(
      @DefaultValue(DEFAULT) @FormParam("param") StringBean param) {
    return param.get();
  }

  @Path("sbcookie")
  @GET
  public String stringBeanCookie(
      @DefaultValue(DEFAULT) @CookieParam("param") StringBean param) {
    return param.get();
  }

  @Path("sbheader")
  @GET
  public String stringBeanHeader(
      @DefaultValue(DEFAULT) @HeaderParam("param") StringBean param) {
    return param.get();
  }
}
