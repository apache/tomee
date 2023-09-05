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

package ee.jakarta.tck.ws.rs.spec.provider.overridestandard;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import ee.jakarta.tck.ws.rs.common.impl.StringStreamingOutput;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TckStreamingOutputProvider extends AbstractProvider implements
    MessageBodyReader<StreamingOutput>, MessageBodyWriter<StreamingOutput> {

  @Override
  public long getSize(StreamingOutput t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return getLength();
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return StreamingOutput.class.isAssignableFrom(type);
  }

  @Override
  public void writeTo(StreamingOutput t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    t.write(entityStream);
    entityStream.write(getWriterName().getBytes());
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return isWriteable(type, genericType, annotations, mediaType);
  }

  @Override
  public StreamingOutput readFrom(Class<StreamingOutput> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    return new StringStreamingOutput(getReaderName());
  }

}
