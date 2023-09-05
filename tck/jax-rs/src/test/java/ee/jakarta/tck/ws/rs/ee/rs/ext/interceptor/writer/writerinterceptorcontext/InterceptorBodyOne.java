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
import java.io.OutputStream;

import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.TemplateInterceptorBody;
import ee.jakarta.tck.ws.rs.common.impl.ReplacingOutputStream;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.WriterInterceptorContext;

public class InterceptorBodyOne
    extends TemplateInterceptorBody<WriterInterceptorContext> {

  public void getEntity() {
    Object entity = context.getEntity();
    setEntity(entity);
  }

  public void getHeaders() {
    MultivaluedMap<String, Object> headers = context.getHeaders();
    String keys = JaxrsUtil.iterableToString(";", headers.keySet());
    setEntity(keys);
  }

  public void getHeadersIsMutable() {
    MultivaluedMap<String, Object> headers = context.getHeaders();
    Object o = headers.getFirst(PROPERTY);
    assertTrue(o == null, PROPERTY, "header allready exist");
    headers.add(PROPERTY, PROPERTY);
  }

  public void getOutputStream() throws IOException {
    setEntityToOutputStream(NULL);
  }

  public void proceedThrowsIOException() {
    try {
      context.proceed(); // Should throw IOException which is wrapped in
      setEntity(NULL); // TemplateWriterInterceptor to RuntimeExecption
    } catch (IOException ioe) {
      setEntity(IOE); // let the client know
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  public String proceedThrowsWebAppException() throws IOException {
    try {
      context.proceed(); // Should throw WebApplicationException in
      throw new ProceedException(NULL); // TemplateWriterInterceptor
    } catch (WebApplicationException e) {
      throw new ProceedException(WAE);
    } catch (IOException e) {
      throw new ProceedException(NULL); // TemplateWriterInterceptor to
      // RuntimeExecption
    }
  }

  public void fromProceedThrowsWebAppException() throws IOException {
    // intentionally blank, no need to do enything
    // this is called on an interceptor on a response given by
    // ProceedException.getResponse()
  }

  public void setEntity() {
    context.setEntity(OPERATION);
  }

  public void setOutputStream() throws IOException {
    OutputStream originalStream = context.getOutputStream();
    OutputStream replace = new ReplacingOutputStream(originalStream, 't', 'x');
    context.setOutputStream(replace);
  }

  private void setEntityToOutputStream(String entity) throws IOException {
    OutputStream stream = context.getOutputStream();
    stream.write(entity.getBytes());
  }
}
