/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public class ParamTest {
  protected boolean noparam = true;

  protected StringBuilder sb;

  public static final String FIELD = "Field";

  // public static final String BEAN = "Bean";
  public static final String PARAM = "Param";

  protected void appendNonNullSetNoParam(String name, Number value) {
    if (value.doubleValue() != 0d) {
      noparam = false;
      sb.append(name).append("=").append(value);
    }
  }

  protected void appendTrueSetNoParam(String name, boolean value) {
    if (value) {
      noparam = false;
      sb.append(name).append("=").append(value);
    }
  }

  protected void append(Class<? extends ParamEntityPrototype> clazz,
      ParamEntityPrototype entity, String... prefix) {
    // add prefix
    for (String p : prefix)
      sb.append(p);
    sb.append(clazz.getSimpleName()).append("=");
    sb.append(entity != null ? entity.getValue() : "null");
  }

  protected void append(Class<? extends ParamEntityPrototype> clazz,
      Collection<? extends ParamEntityPrototype> collection, String... prefix) {
    // add prefix
    for (String p : prefix)
      sb.append(p);
    sb.append(clazz.getSimpleName()).append("=");
    if (collection != null && collection.iterator().hasNext())
      sb.append(collection.iterator().next().getValue());
    else
      sb.append("null");
  }

  protected void setReturnValues(
      ParamEntityWithConstructor paramEntityWithConstructor,
      ParamEntityWithFromString paramEntityWithFromString,
      ParamEntityWithValueOf paramEntityWithValueOf,
      Set<ParamEntityWithFromString> setParamEntityWithFromString,
      SortedSet<ParamEntityWithFromString> sortedSetParamEntityWithFromString,
      List<ParamEntityWithFromString> listParamEntityWithFromString,
      String prefix) {
    append(ParamEntityWithConstructor.class, paramEntityWithConstructor,
        prefix);
    append(ParamEntityWithFromString.class, paramEntityWithFromString, prefix);
    append(ParamEntityWithValueOf.class, paramEntityWithValueOf, prefix);

    append(ParamEntityWithFromString.class, setParamEntityWithFromString,
        prefix, JaxrsParamClient.CollectionName.SET.value());
    append(ParamEntityWithFromString.class, sortedSetParamEntityWithFromString,
        prefix, JaxrsParamClient.CollectionName.SORTED_SET.value());
    append(ParamEntityWithFromString.class, listParamEntityWithFromString,
        prefix, JaxrsParamClient.CollectionName.LIST.value());
  }
}
