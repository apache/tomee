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

package ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.reader.readerinterceptorcontext;

import java.io.IOException;
import java.io.InputStream;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.TemplateInterceptorBody;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;

public class InterceptorTwoBody
    extends TemplateInterceptorBody<ReaderInterceptorContext> {
  @Override
  protected Object operationMethodNotFound(String operation)
      throws IOException {
    return proceed();
  }

  public void getHeadersIsMutable() {
    MultivaluedMap<String, String> headers = context.getHeaders();
    String first = headers.getFirst(PROPERTY);
    setEntity(first == null ? NULL : PROPERTY);
  }

  public void proceedThrowsIOException() throws IOException {
    throw new IOException("Interceptor#proceed IOException tck test");
  }

  public Object proceedThrowsWebAppException()
      throws WebApplicationException, IOException {
    return context.proceed();
  }

  public void setInputStream() throws IOException {
    InputStream stream = context.getInputStream();
    String entity = JaxrsUtil.readFromStream(stream);
    setEntity(NULL.equals(entity) ? ENTITY2 : NULL);
  }
}
