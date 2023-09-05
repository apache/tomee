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

package ee.jakarta.tck.ws.rs.spec.provider.visibility;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;

@Path("resource")
public class Resource {

  @Path("contextresolver")
  @GET
  public Response contextresolver(@Context Providers providers) {
    ContextResolver<HolderClass> holder = providers
        .getContextResolver(HolderClass.class, MediaType.WILDCARD_TYPE);
    return holder.getContext(HolderClass.class).toResponse();
  }

  @Path("exceptionmapper")
  @GET
  public String exceptionMapper() {
    throw new VisibilityException();
  }

  @Path("bodywriter")
  @GET
  public DummyClass bodyWriter() {
    return new DummyClass();
  }

  @Path("bodyreader")
  @POST
  public String bodyWriter(String text) {
    return text;
  }

}
