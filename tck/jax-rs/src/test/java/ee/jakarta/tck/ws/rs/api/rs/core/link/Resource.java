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

package ee.jakarta.tck.ws.rs.api.rs.core.link;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import ee.jakarta.tck.ws.rs.common.impl.TRACE;

@Path("resource")
public class Resource {

  @GET
  @Path("get")
  public String get() {
    return "GET";
  }

  @DELETE
  @Path("delete")
  public String delete() {
    return "DELETE";
  }

  @TRACE
  @Path("trace")
  public String trace() {
    return "TRACE";
  }

  @GET
  @Produces(MediaType.APPLICATION_SVG_XML)
  @Path("producessvgxml")
  public String producesSvgXml() {
    return MediaType.APPLICATION_SVG_XML;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("consumesappjson")
  public String consumesAppJson() {
    return MediaType.APPLICATION_JSON;
  }

  @POST
  @Produces({ MediaType.APPLICATION_XHTML_XML, MediaType.APPLICATION_ATOM_XML,
      MediaType.APPLICATION_SVG_XML })
  @Path("producesxml")
  public String producesXml() {
    return MediaType.APPLICATION_XHTML_XML;
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("consumesform")
  public String consumesForm() {
    return MediaType.APPLICATION_FORM_URLENCODED;
  }

}
