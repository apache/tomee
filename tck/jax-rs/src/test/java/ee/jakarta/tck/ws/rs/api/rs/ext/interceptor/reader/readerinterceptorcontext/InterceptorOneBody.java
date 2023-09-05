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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.TemplateInterceptorBody;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;

public class InterceptorOneBody
    extends TemplateInterceptorBody<ReaderInterceptorContext> {

  public void getHeaders() {
    MultivaluedMap<String, String> headers = context.getHeaders();
    setEntity(JaxrsUtil.iterableToString(";", headers.keySet()));
  }

  public void getHeadersIsMutable() {
    MultivaluedMap<String, String> headers = context.getHeaders();
    headers.add(PROPERTY, PROPERTY);
  }

  public void getInputStream() throws IOException {
    InputStream stream = context.getInputStream();
    String entity = JaxrsUtil.readFromStream(stream);
    stream.close();
    setEntity(entity);
  }

  public void proceedThrowsIOException() {
    try {
      context.proceed();
      setEntity(NULL);
    } catch (IOException ioe) {
      setEntity(IOE);
    }
  }

  public Object proceedThrowsWebAppException()
      throws WebApplicationException, IOException {
    Object proceedObject = new ExceptionThrowingStringBean(NULL, false);
    try {
      proceedObject = context.proceed();
    } catch (WebApplicationException e) {
      // Exception has been thrown, message body reader has read nothing
      // hence we need to set expected value;
      ((StringBean) proceedObject).set(WAE);
    }
    return proceedObject;
  }

  public void setInputStream() {
    ByteArrayInputStream stream = new ByteArrayInputStream(NULL.getBytes());
    context.setInputStream(stream);
  }

}
