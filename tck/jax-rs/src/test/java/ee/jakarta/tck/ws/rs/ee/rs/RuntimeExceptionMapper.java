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

package ee.jakarta.tck.ws.rs.ee.rs;

import java.util.IllegalFormatException;
import java.util.MissingFormatArgumentException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RuntimeExceptionMapper
    implements ExceptionMapper<RuntimeException> {

  @Override
  public Response toResponse(RuntimeException exception) {
    Status status = Status.GONE;
    if (exception instanceof MissingFormatArgumentException)
      status = Status.MOVED_PERMANENTLY; // 301
    else if (exception instanceof IllegalFormatException)
      status = Status.SEE_OTHER; // 303
    else if (exception instanceof IllegalArgumentException)
      status = Status.NOT_MODIFIED;// 304
    if (status == Status.GONE)
      exception.printStackTrace();
    return Response.status(status).entity(exception.getMessage()).build();
  }

}
