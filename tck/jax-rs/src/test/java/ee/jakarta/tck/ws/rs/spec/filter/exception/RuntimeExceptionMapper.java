/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.spec.filter.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Use RuntimeException as WebApplicationException does not have constructor for
 * message argument and getMessage does not work then
 */
@Provider
public class RuntimeExceptionMapper
    implements ExceptionMapper<RuntimeException> {

  public static final String THROW_AGAIN = "ThrowExceptionOnceAgain";

  @Override
  public Response toResponse(RuntimeException exception) {
    String message = exception.getMessage();
    // Throw again from this mapper
    if (message.equals(THROW_AGAIN))
      throw new RuntimeException("100000");
    // Throw again from postmatching filter
    if (message.equals(PostMatchingThrowingFilter.EXCEPTION_FIRING_HEADER))
      return Response.ok("100000").header(message, "500").build();
    // Throw again from prematching filter
    if (message.equals(PreMatchingThrowingFilter.EXCEPTION_FIRING_HEADER))
      return Response.ok("100000").header(message, "500").build();
    // Once the exception is thrown, this mapper should not be used to handle it
    // returning status 500
    // Do not throw again
    return Response.ok(message).build();
  }

}
