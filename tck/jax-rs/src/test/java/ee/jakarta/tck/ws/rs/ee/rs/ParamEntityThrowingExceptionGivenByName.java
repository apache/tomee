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

import java.lang.reflect.Constructor;

import jakarta.ws.rs.WebApplicationException;

public class ParamEntityThrowingExceptionGivenByName
    extends ParamEntityThrowingWebApplicationException {

  public static final String ERROR_MSG = "ParamEntityThrowingExceptionGivenByName created unexpected error";

  public static ParamEntityThrowingExceptionGivenByName fromString(String arg) {
    Object o;
    try {
      Class<?> clazz = Class.forName(parseArg(arg));
      Constructor<?> constructor = clazz.getConstructor();
      o = constructor.newInstance();
    } catch (Exception e) {
      throw new WebApplicationException(new RuntimeException(ERROR_MSG));
    }
    throw (RuntimeException) o;
  }
}
