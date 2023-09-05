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

package ee.jakarta.tck.ws.rs.ee.rs.ext.messagebodyreaderwriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import ee.jakarta.tck.ws.rs.common.AbstractMessageBodyRW;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

/**
 * This class is used by
 * ee.jakarta.tck.ws.rs.ee.rs.ext.providers.ProvidersServlet
 */
@Provider
public class EntityMessageReader extends AbstractMessageBodyRW
    implements MessageBodyReader<ReadableWritableEntity> {

  @Override
  public boolean isReadable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    String path = getSpecifiedAnnotationValue(annotations,
        EntityAnnotation.class);
    if (path == null)
      return false;
    path = path.toLowerCase();
    boolean readable = path.contains("body");
    readable |= path.contains("head");
    readable |= path.contains("ioexception");
    readable |= path.contains("webexception");
    readable &= MediaType.TEXT_XML_TYPE.isCompatible(mediaType);
    readable &= ReadableWritableEntity.class.isAssignableFrom(type);
    return readable;
  }

  @Override
  public ReadableWritableEntity readFrom(Class<ReadableWritableEntity> type,
      Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    String ea = getSpecifiedAnnotationValue(annotations, EntityAnnotation.class)
        .toLowerCase();
    String entity = "null";
    if (ea.contains("body"))
      entity = readInputStream(entityStream);
    else if (ea.contains("head"))
      entity = httpHeaders.getFirst(ReadableWritableEntity.NAME);
    else if (ea.contains("ioexception"))
      throw new IOException("CTS test IOException");
    else if (ea.contains("webexception")) {
      entity = httpHeaders.getFirst(ReadableWritableEntity.NAME);
      throw new WebApplicationException(Status.valueOf(entity));
    }
    return ReadableWritableEntity.fromString(entity);
  }

  String readInputStream(InputStream is) throws IOException {
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);
    return br.readLine();
  }
}
