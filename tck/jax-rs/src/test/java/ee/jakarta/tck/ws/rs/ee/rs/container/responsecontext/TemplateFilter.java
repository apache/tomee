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

package ee.jakarta.tck.ws.rs.ee.rs.container.responsecontext;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response.Status;

public abstract class TemplateFilter implements ContainerResponseFilter {

  public static final String OPERATION = "OPERATION";

  public static final String PROPERTYNAME = "getSetProperty";

  public static final String HEADER = "HEADER";

  protected ContainerRequestContext requestContext;

  protected ContainerResponseContext responseContext;

  @Override
  public void filter(ContainerRequestContext requestContext,
      ContainerResponseContext responseContext) throws IOException {
    this.requestContext = requestContext;
    this.responseContext = responseContext;
    String operation = getHeaderString();
    Method[] methods = getClass().getMethods();
    for (Method method : methods)
      if (operation.equalsIgnoreCase(method.getName())) {
        try {
          method.invoke(this);
          return;
        } catch (Exception e) {
          e.printStackTrace();
          responseContext.setStatus(Status.SERVICE_UNAVAILABLE.getStatusCode());
          setEntity(e.getMessage());
          return;
        }
      }
    operationMethodNotFound(operation);
  }

  protected void operationMethodNotFound(String operation) {
    responseContext.setStatus(Status.SERVICE_UNAVAILABLE.getStatusCode());
    setEntity("Operation " + operation + " not implemented");
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

  protected boolean assertTrue(boolean conditionTrue, Object... msg) {
    if (conditionTrue)
      return false;
    StringBuilder sb = new StringBuilder();
    if (msg != null)
      for (Object str : msg)
        sb.append(str).append(" ");
    setEntity(sb.toString());
    responseContext.setStatus(Status.NOT_ACCEPTABLE.getStatusCode());
    return true;
  }

  // might be replaced with ctx.getStringHeader()
  protected String getHeaderString() {
    MultivaluedMap<String, Object> headers = responseContext.getHeaders();
    return (String) headers.getFirst(OPERATION);
  }

  protected void setEntity(String entity) {
    responseContext.setEntity(entity, null, MediaType.TEXT_PLAIN_TYPE);
  }

}
