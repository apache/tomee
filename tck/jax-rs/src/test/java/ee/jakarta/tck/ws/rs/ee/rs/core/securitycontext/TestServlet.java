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

package ee.jakarta.tck.ws.rs.ee.rs.core.securitycontext;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/Servlet")
public class TestServlet {
  public static enum Security {
    SECURED, UNSECURED
  };

  public static enum Scheme {
    BASIC, DIGEST, NOSCHEME
  }

  public static enum Role {
    DIRECTOR, OTHERROLE, NOROLE
  }

  private static void addSecuredInfo(SecurityContext context,
      StringBuilder sb) {
    Security security;
    security = context.isSecure() ? Security.SECURED : Security.UNSECURED;
    sb.append(security).append("|");
  }

  private static void addSchemaInfo(SecurityContext context, StringBuilder sb) {
    Scheme scheme;
    String authScheme = context.getAuthenticationScheme();
    if (authScheme == null)
      scheme = Scheme.NOSCHEME;
    else if (authScheme.equalsIgnoreCase(Scheme.BASIC.name()))
      scheme = Scheme.BASIC;
    else
      scheme = Scheme.DIGEST;
    sb.append(scheme).append("|");
  }

  private static void addRoleInfo(SecurityContext context, StringBuilder sb) {
    java.security.Principal userPrincipal = context.getUserPrincipal();
    String principal = userPrincipal == null ? "" : userPrincipal.getName();
    sb.append(principal).append("|");
  }

  private static void addPrincipalInfo(SecurityContext context,
      StringBuilder sb) {
    Role role;
    if (context.isUserInRole(Role.DIRECTOR.name()))
      role = Role.DIRECTOR;
    else if (context.isUserInRole(Role.OTHERROLE.name()))
      role = Role.OTHERROLE;
    else
      role = Role.NOROLE;
    sb.append(role).append("|");
  }

  @GET
  @Path("/Context")
  public Response test(@Context SecurityContext context) {
    StringBuilder sb = new StringBuilder();
    addSecuredInfo(context, sb);
    addPrincipalInfo(context, sb);
    addRoleInfo(context, sb);
    addSchemaInfo(context, sb);
    return Response.ok(sb.toString()).build();
  }

}
