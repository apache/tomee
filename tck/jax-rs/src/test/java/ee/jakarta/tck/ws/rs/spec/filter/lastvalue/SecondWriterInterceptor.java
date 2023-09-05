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

package ee.jakarta.tck.ws.rs.spec.filter.lastvalue;

import java.io.IOException;
import java.util.ArrayList;

import ee.jakarta.tck.ws.rs.spec.filter.interceptor.Resource;

import jakarta.annotation.Priority;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

@Priority(200)
public class SecondWriterInterceptor implements WriterInterceptor {

  @Override
  public void aroundWriteTo(WriterInterceptorContext context)
      throws IOException, WebApplicationException {
    MultivaluedMap<String, Object> headers = context.getHeaders();
    String header = (String) headers.getFirst(Resource.HEADERNAME);
    if (header != null
        && header.equals(FirstWriterInterceptor.class.getName())) {
      context.setAnnotations(getClass().getAnnotations());
      context.setEntity(toList(getClass().getName()));
      context.setGenericType(String.class);
      context.setMediaType(MediaType.TEXT_PLAIN_TYPE);
      context.setType(ArrayList.class);
    }
    context.proceed();
  }

  private static <T> ArrayList<T> toList(T o) {
    ArrayList<T> list = new ArrayList<T>();
    list.add(o);
    return list;
  }

}
