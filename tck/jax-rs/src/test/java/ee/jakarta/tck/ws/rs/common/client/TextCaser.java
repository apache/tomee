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

package ee.jakarta.tck.ws.rs.common.client;

/**
 * Standard WebTestCase can search strings in an order case sensitive, case
 * insensitive or not in order case sensitive. When there is a need to match not
 * in order and case insensitive, this class is used.
 */
public enum TextCaser {
  UPPER, NONE, LOWER;

  /**
   * Get the text upper cased, lower cased, or unchanged, depending on current
   * TextCaser value
   */
  public final String getCasedText(String text) {
    String ret = null;
    switch (this) {
    case UPPER:
      ret = text.toUpperCase();
      break;
    case LOWER:
      ret = text.toLowerCase();
      break;
    case NONE:
      ret = text;
      break;
    }
    return ret;
  }
}
