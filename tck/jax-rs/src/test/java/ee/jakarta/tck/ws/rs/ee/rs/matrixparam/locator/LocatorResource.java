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

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;

@Path("resource")
public class LocatorResource extends MiddleResource {

  @Path("locator")
  public MiddleResource locatorHasArguments(
      @MatrixParam("stringtest") String stringheader,
      @MatrixParam("stringtest1") String stringheader1,
      @MatrixParam("inttest") int intheader,
      @MatrixParam("inttest1") int intheader1,
      @MatrixParam("inttest2") int intheader2,
      @MatrixParam("bytetest") byte byteheader,
      @MatrixParam("bytetest1") byte byteheader1,
      @MatrixParam("bytetest2") byte byteheader2,
      @MatrixParam("doubletest") double doubleheader,
      @MatrixParam("doubletest1") double doubleheader1,
      @MatrixParam("doubletest2") double doubleheader2,
      @MatrixParam("floattest") float floatheader,
      @MatrixParam("floattest1") float floatheader1,
      @MatrixParam("floattest2") float floatheader2,
      @MatrixParam("shorttest") short shortheader,
      @MatrixParam("shorttest1") short shortheader1,
      @MatrixParam("shorttest2") short shortheader2,
      @MatrixParam("longtest") long longheader,
      @MatrixParam("longtest1") long longheader1,
      @MatrixParam("longtest2") long longheader2,
      @MatrixParam("booleantest") boolean booleanheader,
      @MatrixParam("booleantest1") boolean booleanheader1,
      @MatrixParam("booleantest2") boolean booleanheader2,
      @DefaultValue("MatrixParamTest") @MatrixParam("ParamEntityWithConstructor") ParamEntityWithConstructor paramEntityWithConstructor,
      @Encoded @DefaultValue("MatrixParamTest") @MatrixParam("ParamEntityWithFromString") ParamEntityWithFromString paramEntityWithFromString,
      @DefaultValue("MatrixParamTest") @MatrixParam("ParamEntityWithValueOf") ParamEntityWithValueOf paramEntityWithValueOf,
      @DefaultValue("MatrixParamTest") @MatrixParam("SetParamEntityWithFromString") Set<ParamEntityWithFromString> setParamEntityWithFromString,
      @DefaultValue("MatrixParamTest") @MatrixParam("SortedSetParamEntityWithFromString") SortedSet<ParamEntityWithFromString> sortedSetParamEntityWithFromString,
      @DefaultValue("MatrixParamTest") @MatrixParam("ListParamEntityWithFromString") List<ParamEntityWithFromString> listParamEntityWithFromString,
      @MatrixParam("ParamEntityThrowingWebApplicationException") ParamEntityThrowingWebApplicationException paramEntityThrowingWebApplicationException,
      @MatrixParam("ParamEntityThrowingExceptionGivenByName") ParamEntityThrowingExceptionGivenByName paramEntityThrowingExceptionGivenByName) {
    return new MiddleResource(stringheader, stringheader1, intheader,
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

}
