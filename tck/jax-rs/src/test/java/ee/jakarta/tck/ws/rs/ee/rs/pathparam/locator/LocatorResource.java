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

package ee.jakarta.tck.ws.rs.ee.rs.pathparam.locator;

import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingExceptionGivenByName;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingWebApplicationException;

import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("resource")
public class LocatorResource extends MiddleResource {

  @Path("locator/{id1}")
  public MiddleResource locatorHasArguments(@PathParam("id1") String id1) {
    return new MiddleResource(id1);
  }

  @Path("locator/{id1}/{id2}")
  public MiddleResource locatorHasArguments(@PathParam("id1") String id1,
      @PathParam("id2") String id2) {
    return new MiddleResource(id1, id2);
  }

  @Path("locatorencoded/{id1}/{id2}")
  public MiddleResource locatorHasEncodedArguments(@PathParam("id1") String id1,
      @Encoded @PathParam("id2") String id2) {
    return new MiddleResource(id1, id2);
  }

  @Path("locator/{id1}/{id2}/{id3}")
  public MiddleResource locatorHasArguments(@PathParam("id1") String id1,
      @PathParam("id2") String id2, @PathParam("id3") String id3) {
    return new MiddleResource(id1, id2, id3);
  }

  @Path("locator/{id1}/{id2}/{id3}/{id4}")
  public MiddleResource locatorHasArguments(@PathParam("id1") String id1,
      @PathParam("id2") String id2, @PathParam("id3") String id3,
      @PathParam("id4") String id4) {
    return new MiddleResource(id1, id2, id3, id4);
  }

  @Path("locator/{id1}/{id2}/{id3}/{id4}/{id5}")
  public MiddleResource locatorHasArguments(@PathParam("id1") String id1,
      @PathParam("id2") String id2, @PathParam("id3") String id3,
      @PathParam("id4") String id4, @PathParam("id5") String id5) {
    return new MiddleResource(id1, id2, id3, id4, id5);
  }

  @Path("locator/{id1}/{id2}/{id3}/{id4}/{id5}/{id6}")
  public MiddleResource locatorHasArguments(@PathParam("id1") String id1,
      @PathParam("id2") String id2, @PathParam("id3") String id3,
      @PathParam("id4") String id4, @PathParam("id5") String id5,
      @PathParam("id6") String id6) {
    return new MiddleResource(id1, id2, id3, id4, id5, id6);
  }

  @Path("/locator/ParamEntityThrowingWebApplicationException/{id}")
  public MiddleResource locatorHasArguments(
      @PathParam("id") ParamEntityThrowingWebApplicationException paramEntityThrowingWebApplicationException) {
    return null;
  }

  @Path("/locator/ParamEntityThrowingExceptionGivenByName/{id}")
  public MiddleResource locatorHasArguments(
      @PathParam("id") ParamEntityThrowingExceptionGivenByName paramEntityThrowingExceptionGivenByName) {
    return null;
  }

}
