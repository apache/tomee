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

package ee.jakarta.tck.ws.rs.ee.rs.ext.paramconverter;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

@Path("locator")
public class Locator {

  @Path("sbquery")
  public Resource stringBeanQuery() {
    return new Resource();
  }

  @Path("sbpath")
  public Resource stringBeanPath() {
    return new Resource();
  }

  @Path("sbmatrix")
  public Resource stringBeanMatrix() {
    return new Resource();
  }

  @Path("sbform")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Resource stringBeanForm() {
    return new Resource();
  }

  @Path("sbcookie")
  public Resource stringBeanCookie() {
    return new Resource();
  }

  @Path("sbheader")
  public Resource stringBeanHeader() {
    return new Resource();
  }

}
