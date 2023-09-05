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

@Path("resource/noentity")
public class ResponseWithNoEntityUsesMapperResource {

  public static final String MESSAGE = "Use mapper";

  private static Response buildResponse(int status) {
    ResponseBuilder rb = Response.status(status);
    return rb.build();
  }

  @Path("{id}")
  public Response getException(@PathParam("id") int id) {
    WebApplicationException wae = null;
    switch (id) {
    case 4000:
      wae = new ClientErrorException(MESSAGE, buildResponse(400));
      break;
    case 400:
      wae = new BadRequestException(MESSAGE, buildResponse(id));
      break;
    case 403:
      wae = new ForbiddenException(MESSAGE, buildResponse(id));
      break;
    case 406:
      wae = new NotAcceptableException(MESSAGE, buildResponse(id));
      break;
    case 405:
      wae = new NotAllowedException(MESSAGE, buildResponse(id));
      break;
    case 401:
      wae = new NotAuthorizedException(MESSAGE, buildResponse(id));
      break;
    case 404:
      wae = new NotFoundException(MESSAGE, buildResponse(id));
      break;
    case 415:
      wae = new NotSupportedException(MESSAGE, buildResponse(id));
      break;
    case 3000:
      wae = new RedirectionException(MESSAGE, buildResponse(300));
      break;
    case 5000:
      wae = new ServerErrorException(MESSAGE, buildResponse(500));
      break;
    case 500:
      wae = new InternalServerErrorException(MESSAGE, buildResponse(id));
      break;
    case 503:
      wae = new ServiceUnavailableException(MESSAGE, buildResponse(id));
      break;
    }
    throw wae;
  }

}