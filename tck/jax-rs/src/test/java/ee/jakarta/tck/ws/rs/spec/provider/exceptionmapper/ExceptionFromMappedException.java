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

package ee.jakarta.tck.ws.rs.spec.provider.exceptionmapper;

/**
 * This Exception is meant to be thrown from a Resource, caught by designated
 * mapper which then throws another exception which the spec command not to be
 * caught
 */
public class ExceptionFromMappedException extends RuntimeException {

  public ExceptionFromMappedException(String string) {
    super(string);
  }

  public ExceptionFromMappedException() {
    super("This is intentional exception");
  }

  private static final long serialVersionUID = -2119170102914335228L;
}
