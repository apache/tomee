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

package ee.jakarta.tck.ws.rs.ee.rs.ext.exceptionmapper;

import java.io.IOException;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * This class is used by
 * ee.jakarta.tck.ws.rs.ee.rs.ext.providers.ProvidersServlet
 */
@Provider
public class AnyExceptionExceptionMapper implements ExceptionMapper<Exception> {

  @Override
  public Response toResponse(Exception arg0) {
    Status status = Status.NO_CONTENT;
    if (arg0 instanceof WebApplicationException)
      return ((WebApplicationException) arg0).getResponse();
    else if (arg0 instanceof RuntimeException)
      throw new RuntimeException("CTS Test RuntimeException", arg0);
    else if (arg0 instanceof IOException)
      status = Status.SERVICE_UNAVAILABLE;
    else if (arg0 != null)
      status = Status.NOT_ACCEPTABLE;
    return Response.status(status).build();
  }

}
