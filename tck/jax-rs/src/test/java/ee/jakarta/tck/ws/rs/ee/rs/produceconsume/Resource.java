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

package ee.jakarta.tck.ws.rs.ee.rs.produceconsume;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

@Path("resource")
public class Resource {

  public static final String WIDGETS_XML = "application/widgets+xml";

  public static final String UNKNOWN = "unknown/unknown";

  @GET
  @Path("plain")
  @Produces(MediaType.TEXT_PLAIN)
  public String pPlain() {
    return MediaType.TEXT_PLAIN;
  }

  @GET
  @Path("html")
  @Produces(MediaType.TEXT_HTML)
  public String pHtml() {
    return MediaType.TEXT_HTML;
  }

  @GET
  @Path("widgetsxml")
  @Produces(WIDGETS_XML)
  public String pWidgetxml() {
    return WIDGETS_XML;
  }

  @GET
  @Path("unknown")
  @Produces(UNKNOWN)
  public String pUnknown() {
    return UNKNOWN;
  }

  @GET
  @Path("any")
  @Produces(MediaType.WILDCARD)
  public String pAny() {
    return MediaType.WILDCARD;
  }

  @POST
  @Path("plain")
  @Consumes(MediaType.TEXT_PLAIN)
  public String cPlain() {
    return MediaType.TEXT_PLAIN;
  }

  @POST
  @Path("html")
  @Consumes(MediaType.TEXT_HTML)
  public String cHtml() {
    return MediaType.TEXT_HTML;
  }

  @POST
  @Path("widgetsxml")
  @Consumes(WIDGETS_XML)
  public String cWidgetxml() {
    return WIDGETS_XML;
  }

  @POST
  @Path("unknown")
  @Consumes(UNKNOWN)
  public String cUnknown() {
    return UNKNOWN;
  }

  @POST
  @Path("any")
  @Consumes(MediaType.WILDCARD)
  public String cAny() {
    return MediaType.WILDCARD;
  }

  @POST
  @Path("plus")
  @Produces(MediaType.TEXT_PLAIN + "," + MediaType.TEXT_XML)
  @Consumes(MediaType.TEXT_PLAIN + "," + MediaType.TEXT_XML)
  public String plus(@Context HttpHeaders headers) {
    return headers.getMediaType() == null ? "null"
        : headers.getMediaType().toString();
  }
}
