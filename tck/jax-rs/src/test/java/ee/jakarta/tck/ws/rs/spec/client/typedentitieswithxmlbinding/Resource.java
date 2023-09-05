/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.spec.client.typedentitieswithxmlbinding;

import ee.jakarta.tck.ws.rs.ee.rs.ext.messagebodyreaderwriter.ReadableWritableEntity;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

@Path("resource")
public class Resource {

  @GET
  @Path("readerprovider")
  public ReadableWritableEntity clientReader() {
    return new ReadableWritableEntity(getClass().getName());
  }

  @POST
  @Path("writerprovider")
  public String clientWriter(ReadableWritableEntity entity) {
    return entity.toXmlString();
  }

  @GET
  @Path("standardreader")
  public String bytearrayreader(@Context HttpHeaders headers) {
    String name = Resource.class.getName();
    MediaType type = headers.getAcceptableMediaTypes().iterator().next();
    if (type != null && type.getSubtype().contains("xml"))
      name = "<resource>" + name + "</resource>";
    return name;
  }

  @POST
  @Path("standardwriter")
  public String bytearraywriter(String value) {
    return value;
  }
}
