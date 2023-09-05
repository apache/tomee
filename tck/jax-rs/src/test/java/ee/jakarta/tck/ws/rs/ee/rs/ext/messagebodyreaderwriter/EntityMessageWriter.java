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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import ee.jakarta.tck.ws.rs.common.AbstractMessageBodyRW;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

/**
 * This class is used by
 * ee.jakarta.tck.ws.rs.ee.rs.ext.providers.ProvidersServlet
 */
@Provider
public class EntityMessageWriter extends AbstractMessageBodyRW
    implements MessageBodyWriter<ReadableWritableEntity> {

  @Override
  public long getSize(ReadableWritableEntity t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return t.toXmlString().length();
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    String path = getPathValue(annotations);
    if (path == null)
      return false;
    path = path.toLowerCase();
    boolean writable = path.contains("body");
    writable |= path.contains("head");
    writable |= path.contains("ioexception");
    writable &= MediaType.TEXT_XML_TYPE.isCompatible(mediaType);
    writable &= ReadableWritableEntity.class.isAssignableFrom(type);
    return writable;
  }

  @Override
  public void writeTo(ReadableWritableEntity t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    String path = getPathValue(annotations).toLowerCase();
    if (path.contains("body"))
      entityStream.write(t.toXmlString().getBytes());
    else if (path.contains("head"))
      httpHeaders.add(ReadableWritableEntity.NAME, t.toXmlString());
    else if (path.contains("ioexception"))
      throw new IOException("CTS test IOException");
  }

}
