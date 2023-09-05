/*
 * Copyright (c) 2012, 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.resource.java2entity;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import ee.jakarta.tck.ws.rs.common.AbstractMessageBodyRW;

/**
 * If isWritable arguments are passed according to the spec, writes OK,
 * otherwise IncorrectCollectionWriter writes ERROR
 */
@Provider
public class CollectionWriter extends AbstractMessageBodyRW
    implements MessageBodyWriter<Collection<?>> {
  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    String path = getPathValue(annotations);
    // Return type : Other
    if (path.equalsIgnoreCase(type.getSimpleName()))
      return checkOther(type, genericType);
    else if (path.equalsIgnoreCase("response/linkedlist"))
      return checkResponseNongeneric(type, genericType);
    else if (path.equalsIgnoreCase("response/genericentity/linkedlist"))
      return checkGeneric(type, genericType);
    else if (path.equalsIgnoreCase("genericentity/linkedlist"))
      return checkGeneric(type, genericType);
    return false;
  }

  private static boolean checkOther(Class<?> type, Type genericType) {
    if (!(genericType instanceof ParameterizedType))
      return false;
    ParameterizedType pType = (ParameterizedType) genericType;
    boolean ok = pType.getRawType().equals(LinkedList.class);
    ok &= pType.getActualTypeArguments()[0].equals(String.class);
    return ok;
  }

  private static boolean checkResponseNongeneric(Class<?> type,
      Type genericType) {
    boolean ok = genericType.equals(LinkedList.class);
    ok &= type.equals(LinkedList.class);
    return ok;
  }

  private static boolean checkGeneric(Class<?> type, Type genericType) {
    if (ParameterizedType.class.isInstance(genericType))
      genericType = ((ParameterizedType) genericType).getRawType();
    boolean ok = genericType.getClass().equals(List.class)
        || genericType.equals(LinkedList.class);
    ok &= type.equals(LinkedList.class);
    return ok;
  }

  @Override
  public long getSize(Collection<?> t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return Response.Status.OK.name().length();
  }

  @Override
  public void writeTo(Collection<?> t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    entityStream.write(Response.Status.OK.name().getBytes());
  }

}