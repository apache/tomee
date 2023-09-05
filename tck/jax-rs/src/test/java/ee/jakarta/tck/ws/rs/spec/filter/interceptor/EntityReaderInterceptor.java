/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.spec.filter.interceptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;

@Provider
public class EntityReaderInterceptor implements ReaderInterceptor {

  @Override
  public Object aroundReadFrom(ReaderInterceptorContext context)
      throws IOException, WebApplicationException {
    MultivaluedMap<String, String> headers = context.getHeaders();
    String header = headers.getFirst(Resource.HEADERNAME);
    if (header != null && header.startsWith(getClass().getName())) {
      StringBuilder sb = new StringBuilder().append("<interceptor>")
          .append(getClass().getName());
      if (header.contains(Resource.DIRECTION))
        sb.append(Resource.DIRECTION);
      sb.append("</interceptor>");
      String content = sb.toString();
      ByteArrayInputStream stream = new ByteArrayInputStream(
          content.getBytes());
      context.setInputStream(stream);
    }
    return context.proceed();
  }
}
