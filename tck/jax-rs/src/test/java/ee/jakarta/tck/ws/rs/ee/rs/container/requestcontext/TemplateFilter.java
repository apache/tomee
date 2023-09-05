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

package ee.jakarta.tck.ws.rs.ee.rs.container.requestcontext;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public abstract class TemplateFilter implements ContainerRequestFilter {

  public static final String OPERATION = "OPERATION";

  public static final String PROPERTYNAME = "getSetProperty";

  protected ContainerRequestContext requestContext;

  @Override
  public void filter(ContainerRequestContext requestContext)
      throws IOException {
    this.requestContext = requestContext;
    String operation = getHeaderString();
    Method[] methods = getClass().getMethods();
    for (Method method : methods)
      if (operation.equalsIgnoreCase(method.getName())) {
        try {
          method.invoke(this);
          return;
        } catch (Exception e) {
          e.printStackTrace();
          Response response = Response.status(Status.SERVICE_UNAVAILABLE)
              .entity(e.getMessage()).build();
          requestContext.abortWith(response);
        }
      }
    Response response = Response.status(Status.SERVICE_UNAVAILABLE)
        .entity("Operation " + operation + " not implemented").build();
    requestContext.abortWith(response);
  }

  // ////////////////////////////////////////////////////////////////////
  protected static <T> String collectionToString(Collection<T> collection) {
    StringBuilder sb = new StringBuilder();
    for (T item : collection) {
      String replace = item.toString().toLowerCase().replace("_", "-")
          .replace(" ", "");
      sb.append(replace).append(" ");
    }
    return sb.toString();
  }

  protected void abortWithEntity(String entity) {
    StringBuilder sb = new StringBuilder();
    if (entity != null)
      sb.append(entity).append(";");
    sb.append(getHeaderString());
    Response response = Response.ok(sb.toString()).build();
    requestContext.abortWith(response);
  }

  protected boolean assertTrue(boolean conditionTrue, Object... msg) {
    if (conditionTrue)
      return false;
    StringBuilder sb = new StringBuilder();
    if (msg != null)
      for (Object str : msg)
        sb.append(str).append(" ");
    Response response = Response.status(Status.NOT_ACCEPTABLE)
        .entity(sb.toString()).build();
    requestContext.abortWith(response);
    return true;
  }

  // might be replaced with ctx.getStringHeader()
  protected String getHeaderString() {
    MultivaluedMap<String, String> headers = requestContext.getHeaders();
    return headers.getFirst(OPERATION);
  }
}
