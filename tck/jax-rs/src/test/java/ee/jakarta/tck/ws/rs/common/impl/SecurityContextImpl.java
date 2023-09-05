/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.common.impl;

import java.security.Principal;

import jakarta.ws.rs.core.SecurityContext;

/**
 * This is simple implementation of a SecurityContext interface There are two
 * constructors, the default representing dummy SecurityContext with no
 * principal, scheme and role, and a constructor with principal scheme and
 * information about whether the connection is secure and whether user is in
 * role
 */
public class SecurityContextImpl implements SecurityContext {

  /**
   * Representation of principal, role, scheme, and secure connection
   */
  public SecurityContextImpl(String principal, boolean isUserInRole,
      boolean isSecure, String scheme) {
    super();
    this.principal = principal;
    this.isUserInRole = isUserInRole;
    this.isSecure = isSecure;
    this.scheme = scheme;
  }

  private String principal = null;

  private boolean isUserInRole = false;

  private boolean isSecure = false;

  private String scheme = null;

  /**
   * Dummy security context
   */
  public SecurityContextImpl() {
  }

  @Override
  public Principal getUserPrincipal() {
    if (principal == null)
      return null;
    else {
      Principal p = new Principal() {
        @Override
        public String getName() {
          return principal;
        }
      };
      return p;
    }
  }

  @Override
  public boolean isUserInRole(String role) {
    return isUserInRole;
  }

  @Override
  public boolean isSecure() {
    return isSecure;
  }

  @Override
  public String getAuthenticationScheme() {
    return scheme;
  }

}
