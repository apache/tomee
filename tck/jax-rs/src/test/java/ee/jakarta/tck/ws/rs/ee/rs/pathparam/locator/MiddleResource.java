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

package ee.jakarta.tck.ws.rs.ee.rs.pathparam.locator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithConstructor;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithFromString;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithValueOf;
import ee.jakarta.tck.ws.rs.ee.rs.pathparam.PathParamTest;

import jakarta.ws.rs.POST;

public class MiddleResource extends PathParamTest {

  private final String returnValue;

  public MiddleResource() {
    returnValue = null;
  }

  protected MiddleResource(String id) {
    returnValue = single(id);
  }

  protected MiddleResource(String id1, String id2) {
    if ("ParamEntityWithConstructor".equals(id1))
      returnValue = paramEntityWithConstructorTest(
          new ParamEntityWithConstructor(id2));
    else if ("ParamEntityWithFromString".equals(id1))
      returnValue = paramEntityWithFromStringTest(
          ParamEntityWithFromString.fromString(id2));
    else if ("ParamEntityWithValueOf".equals(id1))
      returnValue = paramEntityWithValueOfTest(
          ParamEntityWithValueOf.valueOf(id2));
    else if ("SetParamEntityWithFromString".equals(id1)) {
      returnValue = setParamEntityWithFromStringTest(
          Collections.singleton(ParamEntityWithFromString.fromString(id2)));
    } else if ("ListParamEntityWithFromString".equals(id1)) {
      returnValue = listParamEntityWithFromStringTest(
          Collections.singletonList(ParamEntityWithFromString.fromString(id2)));
    } else
      returnValue = two(id1, new PathSegmentImpl(id2));
  }

  protected MiddleResource(String id1, String id2, String id3) {
    returnValue = triple(Integer.parseInt(id1), new PathSegmentImpl(id2),
        Float.parseFloat(id3));
  }

  protected MiddleResource(String id1, String id2, String id3, String id4) {
    returnValue = quard(Double.parseDouble(id1), Boolean.parseBoolean(id2),
        Byte.parseByte(id3), new PathSegmentImpl(id4));
  }

  protected MiddleResource(String id1, String id2, String id3, String id4,
      String id5) {
    returnValue = penta(Long.parseLong(id1), id2, Short.parseShort(id3),
        Boolean.parseBoolean(id4), new PathSegmentImpl(id5));
  }

  protected MiddleResource(String id1, String id2, String id3, String id4,
      String id5, String id6) {
    List<String> list = Arrays.asList(id1, id2, id3, id4, id5, id6);
    returnValue = list(list);
  }

  @POST
  public String returnValue() {
    return returnValue;
  }

}
