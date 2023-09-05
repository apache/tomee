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

package ee.jakarta.tck.ws.rs.api.rs.ext.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;

@Provider
public class InputStreamReaderProvider implements
    MessageBodyReader<InputStreamReader>, MessageBodyWriter<InputStreamReader> {

  @Override
  public long getSize(InputStreamReader arg0, Class<?> arg1, Type arg2,
      Annotation[] arg3, MediaType arg4) {
    return InputStreamReader.class.getName().length();
  }

  @Override
  public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2,
      MediaType arg3) {
    return arg0 == InputStreamReader.class;
  }

  @Override
  public void writeTo(InputStreamReader arg0, Class<?> arg1, Type arg2,
      Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5,
      OutputStream arg6) throws IOException, WebApplicationException {
    String entity = JaxrsUtil.readFromReader(arg0);
    arg0.close();
    arg6.write(entity.getBytes());
  }

  @Override
  public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2,
      MediaType arg3) {
    return isWriteable(arg0, arg1, arg2, arg3);
  }

  @Override
  public InputStreamReader readFrom(Class<InputStreamReader> arg0, Type arg1,
      Annotation[] arg2, MediaType arg3, MultivaluedMap<String, String> arg4,
      InputStream arg5) throws IOException, WebApplicationException {
    return new InputStreamReader(arg5);
  }

}
