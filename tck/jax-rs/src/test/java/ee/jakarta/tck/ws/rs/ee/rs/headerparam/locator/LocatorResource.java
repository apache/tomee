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

package ee.jakarta.tck.ws.rs.ee.rs.headerparam.locator;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingExceptionGivenByName;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingWebApplicationException;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithConstructor;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithFromString;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithValueOf;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;

@Path("resource")
public class LocatorResource extends MiddleResource {

  @Path("locator")
  public MiddleResource locatorHasArguments(
      @HeaderParam("X-CTSTEST-HEADERTEST-STRINGTEST1") @DefaultValue("default") String stringheader1,
      @HeaderParam("X-CTSTEST-HEADERTEST-stringtest2") @DefaultValue("default") String stringheader2,
      @HeaderParam("X-CTSTEST-HEADERTEST-inttest1") int intheader1,
      @HeaderParam("X-CTSTEST-HEADERTEST-inttest2") int intheader2,
      @HeaderParam("X-CTSTEST-HEADERTEST-bytetest1") byte byteheader1,
      @HeaderParam("X-CTSTEST-HEADERTEST-bytetest2") byte byteheader2,
      @HeaderParam("X-CTSTEST-HEADERTEST-doubletest1") double doubleheader1,
      @HeaderParam("X-CTSTEST-HEADERTEST-doubletest2") double doubleheader2,
      @HeaderParam("X-CTSTEST-HEADERTEST-floattest1") float floatheader1,
      @HeaderParam("X-CTSTEST-HEADERTEST-floattest2") float floatheader2,
      @HeaderParam("X-CTSTEST-HEADERTEST-shorttest1") short shortheader1,
      @HeaderParam("X-CTSTEST-HEADERTEST-shorttest2") short shortheader2,
      @HeaderParam("X-CTSTEST-HEADERTEST-longtest1") long longheader1,
      @HeaderParam("X-CTSTEST-HEADERTEST-longtest2") long longheader2,
      @HeaderParam("X-CTSTEST-HEADERTEST-booleantest1") boolean booleanheader1,
      @HeaderParam("X-CTSTEST-HEADERTEST-booleantest2") boolean booleanheader2,
      @DefaultValue("HeaderParamTest") @HeaderParam("ParamEntityWithConstructor") ParamEntityWithConstructor paramEntityWithConstructor,
      @DefaultValue("HeaderParamTest") @HeaderParam("ParamEntityWithFromString") ParamEntityWithFromString paramEntityWithFromString,
      @DefaultValue("HeaderParamTest") @HeaderParam("ParamEntityWithValueOf") ParamEntityWithValueOf paramEntityWithValueOf,
      @DefaultValue("HeaderParamTest") @HeaderParam("SetParamEntityWithFromString") Set<ParamEntityWithFromString> setParamEntityWithFromString,
      @DefaultValue("HeaderParamTest") @HeaderParam("SortedSetParamEntityWithFromString") SortedSet<ParamEntityWithFromString> sortedSetParamEntityWithFromString,
      @DefaultValue("HeaderParamTest") @HeaderParam("ListParamEntityWithFromString") List<ParamEntityWithFromString> listParamEntityWithFromString,
      @HeaderParam("ParamEntityThrowingWebApplicationException") ParamEntityThrowingWebApplicationException paramEntityThrowingWebApplicationException,
      @HeaderParam("ParamEntityThrowingExceptionGivenByName") ParamEntityThrowingExceptionGivenByName paramEntityThrowingExceptionGivenByName) {
    return new MiddleResource(stringheader1, stringheader2, intheader1,
        intheader2, byteheader1, byteheader2, doubleheader1, doubleheader2,
        floatheader1, floatheader2, shortheader1, shortheader2, longheader1,
        longheader2, booleanheader1, booleanheader2, paramEntityWithConstructor,
        paramEntityWithFromString, paramEntityWithValueOf,
        setParamEntityWithFromString, sortedSetParamEntityWithFromString,
        listParamEntityWithFromString,
        paramEntityThrowingWebApplicationException,
        paramEntityThrowingExceptionGivenByName);
  }

}
