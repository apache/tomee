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

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ExceptionMatcher
    implements ExceptionMapper<WebApplicationException> {

  @Override
  public Response toResponse(WebApplicationException exception) {
    Response response = exception.getResponse();
    int status = response == null ? Status.INTERNAL_SERVER_ERROR.getStatusCode()
        : response.getStatus();
    if (response != null && response.getEntity() != null)
      return response;
    switch (status) {
    case 404:
    case 405:
    case 406:
    case 415:
      return Response.ok(String.valueOf(status)).build();
    }
    return response;
  }

}
