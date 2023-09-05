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

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

public class ParamEntityThrowingWebApplicationException
    extends ParamEntityWithFromString {

  public static ParamEntityThrowingWebApplicationException fromString(
      String arg) {
    throw new WebApplicationException(Status.valueOf(parseArg(arg)));
  }

  protected static String parseArg(String arg) {
    arg = arg.replace("%3d", "=").replace("%3D", "=");
    arg = arg.contains("=") ? arg.replaceAll(".*=", "") : arg;
    return arg;
  }
}
