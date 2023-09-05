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

package ee.jakarta.tck.ws.rs.spec.resource.annotationprecedence.subclass;

import ee.jakarta.tck.ws.rs.spec.resource.annotationprecedence.ResourceInterface;
import ee.jakarta.tck.ws.rs.spec.resource.annotationprecedence.SuperClass;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("resource")
public class Resource extends SuperClass implements ResourceInterface {

  @PUT
  @Path("put")
  @Consumes(MediaType.TEXT_HTML)
  @Produces(MediaType.TEXT_HTML)
  public String get(
      @DefaultValue("subclass") @MatrixParam("ijk") String param) {
    return param;
  }
}
