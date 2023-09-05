/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

/**
 * This is class ParamEntity With "fromString()" method Implements Comparable
 * and overrides equals and hashCode for cases when it is in a sorted set
 */
public class ParamEntityWithFromString extends ParamEntityPrototype
    implements java.lang.Comparable<ParamEntityWithFromString> {

  public static ParamEntityWithFromString fromString(String arg) {
    ParamEntityWithFromString newEntity = new ParamEntityWithFromString();
    newEntity.value = arg;
    return newEntity;
  }

  @Override
  public int compareTo(ParamEntityWithFromString o) {
    return this.value.compareTo(o.value);
  }

  @Override
  public boolean equals(Object obj) {
    return this.value.equals(obj);
  }

  @Override
  public int hashCode() {
    return this.value.hashCode();
  }
}
