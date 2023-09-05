/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.spec.filter.lastvalue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;

import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ArrayListEntityProvider implements
    MessageBodyReader<ArrayList<String>>, MessageBodyWriter<ArrayList<String>> {

  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return type == ArrayList.class;
  }

  @Override
  public long getSize(ArrayList<String> t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    int annlen = annotations.length > 0
        ? annotations[0].annotationType().getName().length()
        : 0;
    return t.iterator().next().length() + annlen
        + mediaType.toString().length();
  }

  @Override
  public void writeTo(ArrayList<String> t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    String ann = "";
    if (annotations.length > 0)
      ann = annotations[0].annotationType().getName();
    entityStream
        .write((t.iterator().next() + ann + mediaType.toString()).getBytes());
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return type == ArrayList.class;
  }

  @Override
  public ArrayList<String> readFrom(Class<ArrayList<String>> type,
      Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    String text = JaxrsUtil.readFromStream(entityStream);
    entityStream.close();
    String ann = "";
    if (annotations.length > 0)
      ann = annotations[0].annotationType().getName();
    ArrayList<String> list = new ArrayList<String>();
    list.add(text + ann + mediaType.toString());
    return list;
  }
}
