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

package ee.jakarta.tck.ws.rs.spec.provider.standardhaspriority;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class TckUniversalProvider extends AbstractProvider
    implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
  private static ProviderWalker visitor = new ProviderWalker();

  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return visitor.isWritable(type);
  }

  @Override
  public long getSize(Object t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    Method m = getMethod("getSize", type);
    return (Long) invoke(m, type, genericType, annotations, mediaType);
  }

  @Override
  public void writeTo(Object t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    Method m = getMethod("writeTo", type);
    invoke(m, type, genericType, annotations, mediaType, httpHeaders,
        entityStream);
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return isWriteable(type, genericType, annotations, mediaType);
  }

  @Override
  public Object readFrom(Class<Object> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    Method m = getMethod("readFrom", type);
    return invoke(m, type.cast(null), type, genericType, annotations, mediaType,
        httpHeaders, entityStream);
  }

  private static Method getMethod(String methodName, Class<?> type) {
    Method[] methods = visitor.getClass().getMethods();
    for (Method m : methods)
      if (m.getName().equals(methodName))
        if (m.getParameterTypes()[0] == type || m.getReturnType() == type)
          return m;
    return null;
  }

  private static Object invoke(Method m, Object... args) {
    Object ret = null;
    try {
      ret = m.invoke(visitor, args);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    return ret;
  }

}
