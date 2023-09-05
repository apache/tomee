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

package ee.jakarta.tck.ws.rs.spec.provider.writer;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("resource")
public class Resource {

  @GET
  @Path("subresponse")
  public OkResponse subresponse() {
    AppXmlObjectWriter.resetSet();
    return new OkResponse("subresponse");
  }

  @GET
  @Produces(MediaType.APPLICATION_XML)
  @Path("supportxml")
  public EntityForWriter supportxml() {
    AppXmlObjectWriter.resetSet();
    return new EntityForWriter("supportxml");
  }

  @GET
  @Path("supportall")
  public EntityForWriter supportAll() {
    AppXmlObjectWriter.resetSet();
    return new EntityForWriter("supportall");
  }

  @POST
  @Path("supportmedia")
  public Response supportMedia(String mediaType) {
    AppXmlObjectWriter.resetSet();
    return Response.ok(new EntityForWriter("supportMedia")).type(mediaType)
        .build();
  }

  @POST
  @Path("writer")
  public Response enable(String which) {
    DefaultEntityWriter.setWritable(false);
    AppXmlObjectWriter.setWritable(false);
    AppJavaEntityWriter.setWritable(false);
    AppAnyEntityWriter.setWritable(false);
    if (which.equalsIgnoreCase(AppAnyEntityWriter.class.getName()))
      AppAnyEntityWriter.setWritable(true);
    if (which.equalsIgnoreCase(AppJavaEntityWriter.class.getName()))
      AppJavaEntityWriter.setWritable(true);
    if (which.equalsIgnoreCase(AppXmlObjectWriter.class.getName()))
      AppXmlObjectWriter.setWritable(true);
    if (which.equalsIgnoreCase(DefaultEntityWriter.class.getName()))
      DefaultEntityWriter.setWritable(true);
    return Response.ok().build();
  }

}
