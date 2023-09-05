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

package ee.jakarta.tck.ws.rs.api.rs.core.configurable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;

@Provider
public class CallableProvider
    implements MessageBodyWriter<Callable<?>>, MessageBodyReader<Callable<?>> {

  @Override
  public long getSize(Callable<?> arg0, Class<?> arg1, Type arg2,
      Annotation[] arg3, MediaType arg4) {
    return arg0.toString().length();
  }

  @Override
  public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2,
      MediaType arg3) {
    return Callable.class.isAssignableFrom(arg0);
  }

  @Override
  public void writeTo(Callable<?> arg0, Class<?> arg1, Type arg2,
      Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5,
      OutputStream arg6) throws IOException, WebApplicationException {
    arg6.write(arg0.toString().getBytes());
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return isWriteable(type, genericType, annotations, mediaType);
  }

  @Override
  public Callable<?> readFrom(Class<Callable<?>> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      final InputStream entityStream)
      throws IOException, WebApplicationException {
    String content = null;
    try {
      content = JaxrsUtil.readFromStream(entityStream);
      entityStream.close();
    } catch (Exception e) {
      content = "Error while reading Callable from InputStream";
    }
    return createCallable(content);
  }

  public static Callable<String> createCallable(final String content) {
    Callable<String> callable = new Callable<String>() {
      @Override
      public String call() throws Exception {
        return toString();
      }

      @Override
      public String toString() {
        return content;
      }
    };
    return callable;
  }

}
