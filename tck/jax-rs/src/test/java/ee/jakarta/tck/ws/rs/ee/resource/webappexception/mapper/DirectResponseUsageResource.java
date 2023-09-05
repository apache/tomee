/*
 * Copyright (c) 2014, 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.resource.webappexception.mapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.RedirectionException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

@Path("resource/direct")
public class DirectResponseUsageResource {

  public static final String ENTITY = "DirectEntity";

  private static Response buildResponse(int status, String entity) {
    ResponseBuilder rb = Response.status(status);
    if (entity != null)
      rb.entity(entity);
    return rb.build();
  }

  public static String getReasonPhrase(int status) {
    return Response.status(status).build().getStatusInfo().getReasonPhrase();
  }

  private static Response buildResponse(int status, boolean entity) {
    ResponseBuilder rb = Response.status(status);
    if (entity)
      rb.entity(getReasonPhrase(status));
    return rb.build();
  }

  @Path("{id}")
  public Response getException(@PathParam("id") int id) {
    WebApplicationException wae = null;
    switch (id) {
    case 2000:
      wae = new WebApplicationException(ENTITY, buildResponse(200, ENTITY));
      break;
    case 4000:
      wae = new ClientErrorException(ENTITY, buildResponse(400, ENTITY));
      break;
    case 400:
      wae = new BadRequestException(ENTITY, buildResponse(id, true));
      break;
    case 403:
      wae = new ForbiddenException(ENTITY, buildResponse(id, true));
      break;
    case 406:
      wae = new NotAcceptableException(ENTITY, buildResponse(id, true));
      break;
    case 405:
      wae = new NotAllowedException(ENTITY, buildResponse(id, true));
      break;
    case 401:
      wae = new NotAuthorizedException(ENTITY, buildResponse(id, true));
      break;
    case 404:
      wae = new NotFoundException(ENTITY, buildResponse(id, true));
      break;
    case 415:
      wae = new NotSupportedException(ENTITY, buildResponse(id, true));
      break;
    case 3000:
      wae = new RedirectionException(ENTITY, buildResponse(300, ENTITY));
      break;
    case 5000:
      wae = new ServerErrorException(ENTITY, buildResponse(500, ENTITY));
      break;
    case 500:
      wae = new InternalServerErrorException(ENTITY, buildResponse(id, true));
      break;
    case 503:
      wae = new ServiceUnavailableException(ENTITY, buildResponse(id, true));
      break;
    }
    throw wae;
  }

}