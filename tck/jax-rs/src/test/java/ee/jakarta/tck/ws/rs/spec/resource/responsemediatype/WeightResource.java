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

package ee.jakarta.tck.ws.rs.spec.resource.responsemediatype;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;

@Path("weight")
public class WeightResource {

  @POST
  @Produces("text/plain;qs=0.9")
  public String plain() {
    return MediaType.TEXT_PLAIN;
  }

  @POST
  @Produces("text/html;qs=0.8")
  public String html(@Context Request req) {
    return MediaType.TEXT_HTML;
  }

  @POST
  @Produces("text/xml;qs=0.5")
  public String xml() {
    return MediaType.TEXT_XML;
  }

  @POST
  @Produces("application/*;qs=0.5")
  public String app() {
    return MediaType.WILDCARD;
  }

  @POST
  @Produces("application/xml;qs=0.5")
  public String appxml() {
    return MediaType.APPLICATION_XML;
  }

  @POST
  @Produces("image/png;qs=0.6")
  public String png() {
    return "image/png";
  }

  @POST
  @Produces("image/*;qs=0.7")
  public String image() {
    return "image/any";
  }

  @POST
  @Produces("*/*;qs=0.1")
  public String any() {
    return MediaType.WILDCARD;
  }

}
