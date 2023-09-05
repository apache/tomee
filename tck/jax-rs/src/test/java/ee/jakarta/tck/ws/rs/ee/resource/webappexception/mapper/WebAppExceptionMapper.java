/*
 * Copyright (c) 2012, 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WebAppExceptionMapper
    implements ExceptionMapper<WebApplicationException> {

  @Override
  public Response toResponse(WebApplicationException exception) {
    // When not found, i.e. url is wrong, one get also
    // WebApplicationException
    if (exception.getClass() != WebApplicationException.class) {
      // When response has entity, the ExceptionMapper is not used
      // Let's mark these WebApplicationException with message
      // DirectResponseUsageResource.ENTITY
      if (exception.getMessage().equals(DirectResponseUsageResource.ENTITY))
        return Response.status(400).entity(
            "WebAppExceptionMapper should not be used when WebApplicationException has an entity")
            .build();
      // Lets mark the WebApplicationException without entity by a message
      // in this case, the WebApplicationException should have been used
      else if (exception.getMessage()
          .equals(ResponseWithNoEntityUsesMapperResource.MESSAGE))
        return Response.status(Status.FOUND).build();
      // default, not a source by TCK, but vi, possible config issue?
      else
        return exception.getResponse();
    }
    return Response.status(Status.ACCEPTED).build();
  }

}