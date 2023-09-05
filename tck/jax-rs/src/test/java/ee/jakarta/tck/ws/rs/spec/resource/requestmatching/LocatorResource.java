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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("resource/locator")
public class LocatorResource {

  @Path("locator")
  public AnotherResourceLocator locator() {
    return new AnotherResourceLocator();
  }

  @GET
  @Path("locator")
  public String subresourcePrecedence() {
    return getClass().getSimpleName();
  }

  @Path("sub")
  public AnotherResourceLocator foundAnotherResourceLocatorByPath() {
    return new AnotherResourceLocator();
  }

}
