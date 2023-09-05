/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.formparam;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingExceptionGivenByName;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingWebApplicationException;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithConstructor;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithFromString;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithValueOf;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path(value = "/FormParamTest/")
public class FormParamTest {

  public static final String response(String argument) {
    return new StringBuilder().append("CTS_FORMPARAM:").append(argument)
        .toString();
  }

  @Path(value = "/PostNonDefParam")
  @POST
  @Consumes("application/x-www-form-urlencoded")
  public Response nonDefault(
      @FormParam("non_default_argument") String nonDefaultArgument) {
    return Response.ok(response(nonDefaultArgument)).build();
  }

  @Path(value = "/PostDefParam")
  @POST
  @Consumes("application/x-www-form-urlencoded")
  public Response defaultValue(
      @Encoded @DefaultValue("default") @FormParam("default_argument") String defaultArgument) {
    return Response.ok(response(defaultArgument)).build();
  }

  @Path(value = "/DefParam")
  @PUT
  @Consumes("application/x-www-form-urlencoded")
  public Response defaultValuePut(
      @DefaultValue("DefParam") @FormParam("default_argument") String defaultArgument) {
    return Response.ok(response(defaultArgument)).build();
  }

  @Path(value = "/ParamEntityWithValueOf")
  @POST
  @Consumes("application/x-www-form-urlencoded")
  public Response defaultValueOf(
      @DefaultValue("ValueOf") @FormParam("default_argument") ParamEntityWithValueOf defaultArgument) {
    return Response.ok(response(defaultArgument.getValue())).build();
  }

  @Path(value = "/Constructor")
  @POST
  @Consumes("application/x-www-form-urlencoded")
  public Response defaultConstructor(
      @DefaultValue("Constructor") @FormParam("default_argument") ParamEntityWithConstructor defaultArgument) {
    return Response.ok(response(defaultArgument.getValue())).build();
  }

  @Path(value = "/ParamEntityWithFromString")
  @POST
  @Consumes("application/x-www-form-urlencoded")
  public Response fromString(
      @Encoded @DefaultValue("FromString") @FormParam("default_argument") ParamEntityWithFromString defaultArgument) {
    return Response.ok(response(defaultArgument.getValue())).build();
  }

  @Path(value = "/ListConstructor")
  @POST
  @Consumes("application/x-www-form-urlencoded")
  public Response defaultListConstructor(
      @DefaultValue("ListConstructor") @FormParam("default_argument") List<ParamEntityWithConstructor> defaultArgument) {
    return Response
        .ok(response(defaultArgument.listIterator().next().getValue())).build();
  }

  @Path(value = "/SetFromString")
  @POST
  @Consumes("application/x-www-form-urlencoded")
  public Response setFromString(
      @Encoded @DefaultValue("SetFromString") @FormParam("default_argument") Set<ParamEntityWithFromString> defaultArgument) {
    return Response.ok(response(defaultArgument.iterator().next().getValue()))
        .build();
  }

  @Path(value = "/SortedSetFromString")
  @POST
  @Consumes("application/x-www-form-urlencoded")
  public Response sortedSetFromString(
      @Encoded @DefaultValue("SortedSetFromString") @FormParam("default_argument") SortedSet<ParamEntityWithFromString> defaultArgument) {
    return Response.ok(response(defaultArgument.first().getValue())).build();
  }

  @Path(value = "/ListFromString")
  @POST
  @Consumes("application/x-www-form-urlencoded")
  public Response listFromString(
      @Encoded @DefaultValue("ListFromString") @FormParam("default_argument") List<ParamEntityWithFromString> defaultArgument) {
    return Response.ok(response(defaultArgument.iterator().next().getValue()))
        .build();
  }

  @Path(value = "/ParamEntityThrowingWebApplicationException")
  @POST
  @Consumes("application/x-www-form-urlencoded")
  public Response throwWebApplicationException(
      @FormParam("default_argument") ParamEntityThrowingWebApplicationException defaultArgument) {
    return Response.ok().build();
  }

  @Path(value = "/ParamEntityThrowingExceptionGivenByName")
  @POST
  @Consumes("application/x-www-form-urlencoded")
  public Response throwWebApplicationException(
      @FormParam("default_argument") ParamEntityThrowingExceptionGivenByName defaultArgument) {
    return Response.ok().build();
  }
}
