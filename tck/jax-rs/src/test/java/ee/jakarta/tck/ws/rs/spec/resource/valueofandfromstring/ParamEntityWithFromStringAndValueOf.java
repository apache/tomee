/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.spec.resource.valueofandfromstring;

import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityPrototype;

/**
 * If both methods are present then valueOf MUST be used unless the type is an
 * enum in which case fromString MUST be used.
 */
public class ParamEntityWithFromStringAndValueOf extends ParamEntityPrototype {

  public static ParamEntityWithFromStringAndValueOf valueOf(String arg) {
    ParamEntityWithFromStringAndValueOf newEntity = new ParamEntityWithFromStringAndValueOf();
    newEntity.value = EnumWithFromStringAndValueOf.VALUEOF.name();
    return newEntity;
  }

  public static ParamEntityWithFromStringAndValueOf fromString(String arg) {
    ParamEntityWithFromStringAndValueOf newEntity = new ParamEntityWithFromStringAndValueOf();
    newEntity.value = EnumWithFromStringAndValueOf.FROMSTRING.name();
    return newEntity;
  }
}
