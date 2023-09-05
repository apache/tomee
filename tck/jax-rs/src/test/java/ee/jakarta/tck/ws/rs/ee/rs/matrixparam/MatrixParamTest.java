/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.matrixparam;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingExceptionGivenByName;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingWebApplicationException;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithConstructor;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithFromString;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithValueOf;
import ee.jakarta.tck.ws.rs.ee.rs.ParamTest;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;

@Path(value = "/MatrixParamTest")
public class MatrixParamTest extends ParamTest {

  @DefaultValue("MatrixParamTest")
  @MatrixParam("FieldParamEntityWithConstructor")
  ParamEntityWithConstructor fieldParamEntityWithConstructor;

  @Encoded
  @DefaultValue("MatrixParamTest")
  @MatrixParam("FieldParamEntityWithFromString")
  ParamEntityWithFromString fieldParamEntityWithFromString;

  @DefaultValue("MatrixParamTest")
  @MatrixParam("FieldParamEntityWithValueOf")
  ParamEntityWithValueOf fieldParamEntityWithValueOf;

  @DefaultValue("MatrixParamTest")
  @MatrixParam("FieldSetParamEntityWithFromString")
  Set<ParamEntityWithFromString> fieldSetParamEntityWithFromString;

  @DefaultValue("MatrixParamTest")
  @MatrixParam("FieldSortedSetParamEntityWithFromString")
  SortedSet<ParamEntityWithFromString> fieldSortedSetParamEntityWithFromString;

  @DefaultValue("MatrixParamTest")
  @MatrixParam("FieldListParamEntityWithFromString")
  List<ParamEntityWithFromString> fieldListParamEntityWithFromString;

  @MatrixParam("FieldParamEntityThrowingWebApplicationException")
  public ParamEntityThrowingWebApplicationException fieldEntityThrowingWebApplicationException;

  @MatrixParam("FieldParamEntityThrowingExceptionGivenByName")
  public ParamEntityThrowingExceptionGivenByName fieldEntityThrowingExceptionGivenByName;

  @GET
  public String stringParamHandling(
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

    sb = new StringBuilder();

    sb.append("stringtest=").append(stringheader);
    sb.append("stringtest1=").append(stringheader1);

    sb.append("inttest=").append(intheader);
    sb.append("inttest1=").append(intheader1);
    sb.append("inttest2=").append(intheader2);

    sb.append("doubletest=").append(doubleheader);
    sb.append("doubletest1=").append(doubleheader1);
    sb.append("doubletest2=").append(doubleheader2);

    sb.append("floattest=").append(floatheader);
    sb.append("floattest1=").append(floatheader1);
    sb.append("floattest2=").append(floatheader2);

    sb.append("longtest=").append(longheader);
    sb.append("longtest1=").append(longheader1);
    sb.append("longtest2=").append(longheader2);

    sb.append("shorttest=").append(shortheader);
    sb.append("shorttest1=").append(shortheader1);
    sb.append("shorttest2=").append(shortheader2);

    sb.append("bytetest=").append(byteheader);
    sb.append("bytetest1=").append(byteheader1);
    sb.append("bytetest2=").append(byteheader2);

    sb.append("booleantest=").append(booleanheader);
    sb.append("booleantest1=").append(booleanheader1);
    sb.append("booleantest2=").append(booleanheader2);

    setReturnValues(paramEntityWithConstructor, paramEntityWithFromString,
        paramEntityWithValueOf, setParamEntityWithFromString,
        sortedSetParamEntityWithFromString, listParamEntityWithFromString, "");

    setReturnValues(fieldParamEntityWithConstructor,
        fieldParamEntityWithFromString, fieldParamEntityWithValueOf,
        fieldSetParamEntityWithFromString,
        fieldSortedSetParamEntityWithFromString,
        fieldListParamEntityWithFromString, FIELD);

    return sb.toString();
  }
}
