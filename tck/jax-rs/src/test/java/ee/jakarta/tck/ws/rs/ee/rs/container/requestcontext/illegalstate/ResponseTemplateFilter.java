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

package ee.jakarta.tck.ws.rs.ee.rs.container.requestcontext.illegalstate;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

public class ResponseTemplateFilter extends TemplateFilter
    implements ContainerResponseFilter {

  protected ContainerResponseContext responseContext;

  @Override
  public void filter(ContainerRequestContext requestContext,
      ContainerResponseContext responseContext) throws IOException {
    this.responseContext = responseContext;
    this.requestContext = requestContext;
    String operation = requestContext.getHeaderString(OPERATION);
    Method[] methods = getClass().getMethods();
    for (Method method : methods)
      if (operation.equalsIgnoreCase(method.getName())) {
        try {
          method.invoke(this);
          return;
        } catch (Exception e) {
          e.printStackTrace();
          setEntity(e.getMessage());
          responseContext.setStatus(Status.SERVICE_UNAVAILABLE.getStatusCode());
        }
      }
    // When method not found, it is request context operation
  }

  @Override
  protected void setEntity(String entity) {
    responseContext.setEntity(entity, (Annotation[]) null,
        MediaType.WILDCARD_TYPE);
  }

}
