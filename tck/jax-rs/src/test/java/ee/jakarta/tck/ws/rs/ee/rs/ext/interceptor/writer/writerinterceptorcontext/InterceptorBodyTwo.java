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

package ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.writer.writerinterceptorcontext;

import java.io.IOException;

import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.TemplateInterceptorBody;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.WriterInterceptorContext;

public class InterceptorBodyTwo
    extends TemplateInterceptorBody<WriterInterceptorContext> {
  @Override
  protected Object operationMethodNotFound(String operation)
      throws IOException {
    return proceed();
  }

  public void getHeadersIsMutable() {
    MultivaluedMap<String, Object> headers = context.getHeaders();
    Object o = headers.getFirst(PROPERTY);
    assertTrue(o != null, PROPERTY, "header NOT found");
    setEntity(o);
  }

  public void proceedThrowsIOException() throws IOException {
    throw new IOException("Interceptor test IoException");
  }

  public void proceedThrowsWebAppException() throws IOException {
    context.proceed();
  }

  public void setEntity() {
    Object entity = context.getEntity();
    setEntity(entity);
  }
}
