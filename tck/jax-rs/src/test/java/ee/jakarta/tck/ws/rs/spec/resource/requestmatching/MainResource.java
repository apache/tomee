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

package ee.jakarta.tck.ws.rs.spec.resource.requestmatching;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("resource")
public class MainResource {
  public static final String ID = "resource";

  @GET
  public String main() {
    return ID;
  }

  @Path(MainSubResource.ID)
  @GET
  public String sub() {
    return MainResource.class.getSimpleName();
  }

  @GET
  @Path("locator/locator/locator")
  public String locator() {
    return MainResourceLocator.class.getSimpleName();
  }

  @GET
  @Path("{id}")
  public String id() {
    return ID;
  }

  @POST
  @Path("consumes")
  @Consumes(MediaType.TEXT_PLAIN)
  public String consumes() {
    return getClass().getSimpleName();
  }

  @Path("consumeslocator")
  public MainResourceLocator consumeslocator() {
    return new MainResourceLocator();
  }

  @POST
  @Path("produces")
  @Produces(MediaType.TEXT_PLAIN)
  public String produces() {
    return getClass().getSimpleName();
  }

  @Path("produceslocator")
  public MainResourceLocator produceslocator() {
    return new MainResourceLocator();
  }

  @Path("l2locator")
  public Object l2locator() {
    return new MainResourceLocator();
  }

}
