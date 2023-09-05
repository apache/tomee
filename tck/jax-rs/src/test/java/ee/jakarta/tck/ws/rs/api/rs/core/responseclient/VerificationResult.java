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

package ee.jakarta.tck.ws.rs.api.rs.core.responseclient;

/**
 * Verification holder structure
 */
public class VerificationResult {
  public boolean pass;

  public StringBuilder message;

  public VerificationResult() {
    pass = true;
    message = new StringBuilder();
  }

  public VerificationResult(VerificationResult result) {
    this.pass = result.pass;
    this.message = new StringBuilder().append(message);
  }

  public VerificationResult append(VerificationResult result) {
    this.pass &= result.pass;
    this.message.append(result.message).append("\n");
    return this;
  }

  @Override
  public String toString() {
    return message.toString();
  }
}
