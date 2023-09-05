/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.core.uriinfo;

import java.net.URI;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

@Path(value = "resource")
public class URIInfoTest {

  @GET
  @Path("/query")
  public String queryTest(@Context UriInfo info) {
    StringBuilder buf = new StringBuilder();
    for (String param : info.getQueryParameters().keySet()) {
      buf.append(param + "=" + info.getQueryParameters().getFirst(param));
    }
    return buf.toString();
  }

  @GET
  @Path("/query1")
  public String queryTest1(@Context UriInfo info) {
    StringBuilder buf = new StringBuilder();
    for (String param : info.getQueryParameters(true).keySet()) {
      buf.append(param + "=" + info.getQueryParameters(true).getFirst(param));
    }
    return buf.toString();
  }

  @GET
  @Path("/query2")
  public String queryTest2(@Context UriInfo info) {
    StringBuilder buf = new StringBuilder();
    for (String param : info.getQueryParameters(false).keySet()) {
      buf.append(param + "=" + info.getQueryParameters(false).getFirst(param));
    }
    return buf.toString();
  }

  @GET
  @Path("/apath")
  public String apathTest(@Context UriInfo info) {
    StringBuilder sb = new StringBuilder();
    URI uri = info.getAbsolutePath();
    UriBuilder urib = info.getAbsolutePathBuilder();

    sb.append(uri.toString());
    if (!uri.toString().equals(urib.build().toString())) {
      sb.append("Got unexpected = " + urib.build().toString());
      sb.append("FAILED");
    }
    return sb.toString();
  }

  @GET
  @Path("/path")
  public String pathTest(@Context UriInfo info) {
    return info.getPath();
  }

  @GET
  @Path("/path1%20/%2010")
  public String pathTest1(@Context UriInfo info) {
    return info.getPath(true);
  }

  @GET
  @Path("/path2%20/%2010")
  public String pathTest2(@Context UriInfo info) {
    return info.getPath(false);
  }

  @GET
  @Path("/baseuri")
  public String baseUriTest(@Context UriInfo info) {
    StringBuilder sb = new StringBuilder();
    URI uri = info.getBaseUri();
    UriBuilder urib = info.getBaseUriBuilder();

    sb.append(uri.toString());
    if (!uri.toString().equals(urib.build().toString())) {
      sb.append("Got unexpected = " + urib.build().toString());
      sb.append("FAILED");
    }
    return sb.toString();
  }

  @GET
  @Path("/pathparam/{a}/{b}")
  public String pathparamTest(@Context UriInfo info) {
    StringBuilder buf = new StringBuilder();
    for (String param : info.getPathParameters().keySet()) {
      buf.append(param + "=" + info.getPathParameters().getFirst(param));
    }
    return buf.toString();
  }

  @GET
  @Path("/pathparam1/{a}/{b}")
  public String pathparamTest1(@Context UriInfo info) {
    StringBuilder buf = new StringBuilder();
    for (String param : info.getPathParameters(true).keySet()) {
      buf.append(param + "=" + info.getPathParameters(true).getFirst(param));
    }
    return buf.toString();
  }

  @GET
  @Path("/pathparam2/{a}/{b}")
  public String pathparamTest2(@Context UriInfo info) {
    StringBuilder buf = new StringBuilder();
    for (String param : info.getPathParameters(false).keySet()) {
      buf.append(param + "=" + info.getPathParameters(false).getFirst(param));
    }
    return buf.toString();
  }

  @GET
  @Path("/pathseg")
  public String pathsegTest(@Context UriInfo info) {
    StringBuilder buf = new StringBuilder();
    for (PathSegment param : info.getPathSegments()) {
      buf.append(param.getPath() + "/");
    }
    return buf.toString();
  }

  @GET
  @Path("/pathseg1%20/%2010")
  public String pathsegTest1(@Context UriInfo info) {
    StringBuilder buf = new StringBuilder();
    for (PathSegment param : info.getPathSegments(true)) {
      buf.append(param.getPath() + "/");
    }
    return buf.toString();
  }

  @GET
  @Path("/pathseg2%20/%2010")
  public String pathsegTest2(@Context UriInfo info) {
    StringBuilder buf = new StringBuilder();
    for (PathSegment param : info.getPathSegments(false)) {
      buf.append(param.getPath() + "/");
    }
    return buf.toString();
  }

  @GET
  @Path("/request")
  public String requestTest(@Context UriInfo info) {
    StringBuilder sb = new StringBuilder();
    URI uri = info.getRequestUri();
    UriBuilder urib = info.getRequestUriBuilder();

    sb.append(uri.toString());
    if (!uri.toString().equals(urib.build().toString())) {
      sb.append("Got unexpected = " + urib.build().toString());
      sb.append("FAILED");
    }
    return sb.toString();
  }

  @GET
  @Path("/resource")
  public String resourcesTest(@Context UriInfo info) {
    StringBuilder buf = new StringBuilder();
    for (Object resource : info.getMatchedResources()) {
      buf.append(resource.toString() + "=");
    }
    buf.append("number=" + info.getMatchedResources().size());
    return buf.toString();
  }

  @GET
  @Path("/uri")
  public String urisTest(@Context UriInfo info) {
    StringBuilder buf = new StringBuilder();
    for (Object resource : info.getMatchedURIs()) {
      buf.append(resource.toString() + "=");
    }
    buf.append("number=" + info.getMatchedURIs().size());
    return buf.toString();
  }

  @GET
  @Path("/uri1")
  public String urisTest1(@Context UriInfo info) {
    StringBuilder buf = new StringBuilder();
    for (String resource : info.getMatchedURIs(true)) {
      buf.append(resource + "=");
    }
    buf.append("number=" + info.getMatchedURIs(true).size());
    return buf.toString();
  }

  @GET
  @Path("/uri2")
  public String urisTest2(@Context UriInfo info) {
    StringBuilder buf = new StringBuilder();
    for (String resource : info.getMatchedURIs(false)) {
      buf.append(resource + "=");
    }
    buf.append("number=" + info.getMatchedURIs(false).size());
    return buf.toString();
  }

  public static final String ENCODED = "%50%51%52%30%39%70%71%72/%7e%2d%2E%5f";

  public static final String DECODED = "PQR09pqr/~-._";

  @GET
  @Path("{id1}/{id2}")
  public String normalizedUri(@Context UriInfo info) {
    int ret = 1;
    if (!info.getAbsolutePath().toString().contains(ENCODED))
      ret += 10;
    if (!info.getPath().toString().contains(ENCODED))
      ret += 100;
    if (!info.getRequestUri().toString().contains(ENCODED))
      ret += 1000;
    return String.valueOf(ret);
  }
}
