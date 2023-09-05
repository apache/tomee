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

package ee.jakarta.tck.ws.rs.ee.rs.cookieparam.locator;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingExceptionGivenByName;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingWebApplicationException;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithConstructor;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithFromString;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithValueOf;
import ee.jakarta.tck.ws.rs.ee.rs.cookieparam.CookieParamTest;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.Response;

public class MiddleResource extends CookieParamTest {

  private final Response returnValue;

  public MiddleResource() {
    returnValue = null;
  }

  protected MiddleResource(String todo, String value,
      ParamEntityWithConstructor paramEntityWithConstructor,
      ParamEntityWithFromString paramEntityWithFromString,
      ParamEntityWithValueOf paramEntityWithValueOf,
      Set<ParamEntityWithFromString> setParamEntityWithFromString,
      SortedSet<ParamEntityWithFromString> sortedSetParamEntityWithFromString,
      List<ParamEntityWithFromString> listParamEntityWithFromString,
      ParamEntityThrowingWebApplicationException paramEntityThrowingWebApplicationException,
      ParamEntityThrowingExceptionGivenByName paramEntityThrowingExceptionGivenByName) {
    returnValue = cookieParamHandling(todo, value, paramEntityWithConstructor,
        paramEntityWithFromString, paramEntityWithValueOf,
        setParamEntityWithFromString, sortedSetParamEntityWithFromString,
        listParamEntityWithFromString,
        paramEntityThrowingWebApplicationException,
        paramEntityThrowingExceptionGivenByName);
  }

  @POST
  public Response returnValue() {
    return returnValue;
  }

}
