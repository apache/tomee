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

package ee.jakarta.tck.ws.rs.ee.rs.client.invocationbuilder;

import java.util.List;
import java.util.Map.Entry;

import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;

import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;

@Path("resource")
public class Resource {

  @GET
  @Path("languages")
  public String languages(@Context HttpHeaders headers) {
    return JaxrsUtil.iterableToString(" ", headers.getAcceptableLanguages());
  }

  @GET
  @Path("headerstostring")
  public String headersToString(@Context HttpHeaders headers) {
    MultivaluedMap<String, String> map = headers.getRequestHeaders();
    StringBuilder sb = new StringBuilder();
    for (Entry<String, List<String>> header : map.entrySet())
      sb.append(header.getKey()).append(":")
          .append(JaxrsUtil.iterableToString(" ", header.getValue()))
          .append(";");
    return sb.toString();
  }

  @GET
  @Path("cookie")
  public String cookie(@CookieParam("tck") String cookie) {
    return cookie;
  }

  @GET
  @Path("allow")
  public String allow() {
    return "allow";
  }

  @DELETE
  @Path("forbid")
  public String forbid() {
    return "forbid";
  }

  @GET
  @Path("get")
  public String get() {
    return "get";
  }

  @HEAD
  @Path("head")
  public String head() {
    return "head";
  }

  @PUT
  @Path("put")
  public String put(String value) {
    return value;
  }

  @POST
  @Path("post")
  public String post(String value) {
    return value;
  }

  @DELETE
  @Path("delete")
  public String delete() {
    return "delete";
  }

  @OPTIONS
  @Path("options")
  public String options() {
    return "options";
  }

}
