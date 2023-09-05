/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id$
 */

package ee.jakarta.tck.ws.rs.signaturetest;

/**
 * <p>
 * Factory to obtain SignatureTestDriver implementations.
 * </p>
 */
public class SignatureTestDriverFactory {

  /**
   * <p>
   * Identifier for the driver that uses API Check to perform signature
   * validation.
   * </p>
   */
  public static final String API_CHECK = "apicheck";

  /**
   * <p>
   * Identifier for the driver that uses the Signature Test framwork for
   * signature validation.
   * </p>
   */
  public static final String SIG_TEST = "sigtest";

  // ------------------------------------------------------------ Constructors

  // Access via factory method
  private SignatureTestDriverFactory() {
  } // END SignatureTestDriverFactory

  // ---------------------------------------------------------- Public Methods

  /**
   * <p>
   * Obtain a {@link SignatureTestDriver} instance based on the
   * <code>type</code> argument.
   *
   * @param type
   *          the driver type to create
   * @return a {@link SignatureTestDriver} implementation
   */
  public static SignatureTestDriver getInstance(String type) {

    if (type == null || type.length() == 0) {
      throw new IllegalArgumentException("Type was null or empty");
    }

    if (API_CHECK.equals(type)) {
      return new ApiCheckDriver();
    } else if (SIG_TEST.equals(type)) {
      return new SigTestDriver();
    } else {
      throw new IllegalArgumentException("Unknown Type: '" + type + '\'');
    }

  } // END getInstance

} // END SignatureTestDriverFactory
