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

package ee.jakarta.tck.ws.rs.spec.provider.standardwithjaxrsclient;

import java.math.BigDecimal;

import javax.xml.transform.Source;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

@Path("resource")
public class Resource {

  @Path("source")
  @POST
  public Response source(Source source) {
    return Response.ok(source).build();
  }

  @Path("map")
  @POST
  @Produces(MediaType.APPLICATION_FORM_URLENCODED)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response map(MultivaluedMap<String, String> map) {
    return Response.ok(map).build();
  }

  @Path("char")
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public char chars(char symbol) {
    return symbol;
  }

  @Path("character")
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public Character character(Character character) {
    return character;
  }

  @Path("bool")
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public boolean bool(boolean bool) {
    return bool;
  }

  @Path("boolean")
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public Boolean bigbool(Boolean bool) {
    return bool;
  }

  @Path("bigdecimal")
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public BigDecimal number(BigDecimal number) {
    return number;
  }

  @Path("integer")
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public Integer number(Integer number) {
    return number;
  }

  @Path("biglong")
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public Long number(Long number) {
    return number;
  }

  @Path("bigdouble")
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public Double number(Double number) {
    return number;
  }

  @Path("int")
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public int integer(int i) {
    return i;
  }

  @Path("long")
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public long l(long l) {
    return l;
  }

}
