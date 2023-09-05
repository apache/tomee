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

package ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.writer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.InterceptorCallbackMethods;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.TemplateInterceptorBody;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

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
public abstract class TemplateWriterInterceptor
    implements WriterInterceptor, InterceptorCallbackMethods {

  protected WriterInterceptorContext writerCtx;

  protected TemplateInterceptorBody<WriterInterceptorContext> interceptorBody;

  public TemplateWriterInterceptor(
      TemplateInterceptorBody<WriterInterceptorContext> interceptorBody) {
    super();
    this.interceptorBody = interceptorBody;
  }

  @Override
  public void aroundWriteTo(WriterInterceptorContext ctx) throws IOException {
    this.writerCtx = ctx;
    interceptorBody.executeMethod(writerCtx, this);
  }

  @Override
  public void writeEntity(String entity) {
    Type type = writerCtx.getGenericType();
    if (type instanceof Class) {
      Class<?> clazz = ((Class<?>) type);
      if (clazz == InputStreamReader.class) {
        ByteArrayInputStream bis = new ByteArrayInputStream(entity.getBytes());
        InputStreamReader reader = new InputStreamReader(bis);
        writerCtx.setEntity(reader);
      } else {
        writerCtx.setEntity(entity);
      }
    }
  }

  @Override
  public Object proceed() throws IOException {
    writerCtx.proceed();
    return null;
  }

  @Override
  public String getHeaderString() {
    MultivaluedMap<String, Object> headers = writerCtx.getHeaders();
    return (String) headers.getFirst(TemplateInterceptorBody.OPERATION);
  }

}
