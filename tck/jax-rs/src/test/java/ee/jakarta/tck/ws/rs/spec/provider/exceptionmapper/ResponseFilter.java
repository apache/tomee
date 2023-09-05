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

import java.io.IOException;
import java.lang.annotation.Annotation;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ResponseFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext,
      ContainerResponseContext responseContext) throws IOException {
    Object objectEntity = responseContext.getEntity();
    if (objectEntity != null) {
      String entity = objectEntity.toString();
      // Affect only one single test of JAXRS:SPEC:82;
      if (entity.equals(FilterChainTestExceptionMapper.class.getName()))
        responseContext.setEntity(getClass().getName(), (Annotation[]) null,
            responseContext.getMediaType());
    }
  }

}
