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
 * This exception mapper shall never be used. If used, than the Spec. is not
 * followed for various set of reasons such as 1)RuntimeExceptionMapper should
 * be used first 2) RuntimeExceptionMapper has once been used and after
 * consequent exception no Exception mapper should be used, as defined in
 * Section 6.7 of the Spec. 3) Checked Exception (e.g. IOException) has been
 * unexpectedly thrown
 */
@Provider
public class NeverUsedExceptionMapper implements ExceptionMapper<Exception> {

  @Override
  public Response toResponse(Exception exception) {
    return Response.ok(Integer.MIN_VALUE).build();
  }

}
