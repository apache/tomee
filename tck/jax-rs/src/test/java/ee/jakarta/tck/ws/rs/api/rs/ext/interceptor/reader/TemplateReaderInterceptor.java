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

package ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.InterceptorCallbackMethods;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.TemplateInterceptorBody;

/**
 * This class is a superclass for any interceptor @Provider. Any such provider
 * is then given a body, inherited from TemplateInterceptorBody. The body
 * actually contains methods with name equalIgnoreCase to ContextOperation items
 * name, the name of the method executed is passed by http header OPERATION
 * 
 * @see TemplateInterceptorBody
 * 
 *      The injection of the body solves the issue with inheritance from two
 *      super-classes.
 */
public abstract class TemplateReaderInterceptor
    implements ReaderInterceptor, InterceptorCallbackMethods {

  protected ReaderInterceptorContext readerCtx;

  protected TemplateInterceptorBody<ReaderInterceptorContext> interceptorBody;

  public TemplateReaderInterceptor(
      TemplateInterceptorBody<ReaderInterceptorContext> interceptorBody) {
    super();
    this.interceptorBody = interceptorBody;
  }

  @Override
  public Object aroundReadFrom(ReaderInterceptorContext ctx)
      throws IOException, WebApplicationException {
    this.readerCtx = ctx;
    return interceptorBody.executeMethod(readerCtx, this);
  }

  @Override
  public void writeEntity(String entity) {
    readerCtx.setInputStream(new ByteArrayInputStream(entity.getBytes()));
  }

  @Override
  public Object proceed() throws IOException {
    return readerCtx.proceed();
  }

  @Override
  public String getHeaderString() {
    MultivaluedMap<String, String> headers = readerCtx.getHeaders();
    return headers.getFirst(TemplateInterceptorBody.OPERATION);
  }

}
