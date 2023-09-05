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

package ee.jakarta.tck.ws.rs.ee.rs.matrixparam.locator;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingExceptionGivenByName;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingWebApplicationException;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithConstructor;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithFromString;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithValueOf;
import ee.jakarta.tck.ws.rs.ee.rs.matrixparam.MatrixParamTest;

import jakarta.ws.rs.POST;

public class MiddleResource extends MatrixParamTest {

  private final String returnValue;

  public MiddleResource() {
    returnValue = null;
  }

  protected MiddleResource(String stringheader, String stringheader1,
      int intheader, int intheader1, int intheader2, byte byteheader,
      byte byteheader1, byte byteheader2, double doubleheader,
      double doubleheader1, double doubleheader2, float floatheader,
      float floatheader1, float floatheader2, short shortheader,
      short shortheader1, short shortheader2, long longheader, long longheader1,
      long longheader2, boolean booleanheader, boolean booleanheader1,
      boolean booleanheader2,
      ParamEntityWithConstructor paramEntityWithConstructor,
      ParamEntityWithFromString paramEntityWithFromString,
      ParamEntityWithValueOf paramEntityWithValueOf,
      Set<ParamEntityWithFromString> setParamEntityWithFromString,
      SortedSet<ParamEntityWithFromString> sortedSetParamEntityWithFromString,
      List<ParamEntityWithFromString> listParamEntityWithFromString,
      ParamEntityThrowingWebApplicationException paramEntityThrowingWebApplicationException,
      ParamEntityThrowingExceptionGivenByName paramEntityThrowingExceptionGivenByName) {
    returnValue = stringParamHandling(stringheader, stringheader1, intheader,
        intheader1, intheader2, byteheader, byteheader1, byteheader2,
        doubleheader, doubleheader1, doubleheader2, floatheader, floatheader1,
        floatheader2, shortheader, shortheader1, shortheader2, longheader,
        longheader1, longheader2, booleanheader, booleanheader1, booleanheader2,
        paramEntityWithConstructor, paramEntityWithFromString,
        paramEntityWithValueOf, setParamEntityWithFromString,
        sortedSetParamEntityWithFromString, listParamEntityWithFromString,
        paramEntityThrowingWebApplicationException,
        paramEntityThrowingExceptionGivenByName);
  }

  @POST
  public String returnValue() {
    return returnValue;
  }

}
