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

package ee.jakarta.tck.ws.rs.spec.returntype;

import java.util.UUID;

import ee.jakarta.tck.ws.rs.ee.rs.ext.messagebodyreaderwriter.ReadableWritableEntity;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.ext.RuntimeDelegate;

@Path(value = "/ReturnTypeTest")
public class ReturnTypeTest {

  public static final long serialVersionUID = 6121223518891332649L;

  @GET
  @Path("void")
  public String voidTest() {
    return null;
  }

  @GET
  @Path("nullResponse")
  public Response nullResponse() {
    return null;
  }

  @GET
  @Path("nullEntityResponse")
  public Response nullEntityResponse() {
    RuntimeDelegate rd = RuntimeDelegate.getInstance();
    ResponseBuilder rb = rd.createResponseBuilder();
    return rb.entity(null).build();
  }

  @GET
  @Path("default")
  public String defaultTest() {
    return "I am OK";
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("notAcceptable")
  public String notAcceptableTest() {
    return "I am OK";
  }

  @GET
  @Path("nullGenericEntityTest")
  public GenericEntity<?> nullGenericEntityTest() {
    return null;
  }

  @GET
  @Path("genericEntityTest")
  public GenericEntity<UUID> genericEntityTest() {
    UUID uuid = new UUID(serialVersionUID, serialVersionUID >> 1);
    return new GenericEntity<UUID>(uuid,
        uuid.getClass().getGenericSuperclass());
  }

  @GET
  @Path("nullEntityTest")
  public ReadableWritableEntity nullEntity() {
    return null;
  }

  @GET
  @Path("entitybodytest")
  @Produces(MediaType.TEXT_XML)
  public ReadableWritableEntity entityTest() {
    return new ReadableWritableEntity(String.valueOf(serialVersionUID));
  }

  @GET
  @Path("entitybodyresponsetest")
  public Response entityResponseTest() {
    RuntimeDelegate rd = RuntimeDelegate.getInstance();
    ResponseBuilder rb = rd.createResponseBuilder();
    ReadableWritableEntity rwe = entityTest();
    return rb.entity(rwe).build();
  }

}
