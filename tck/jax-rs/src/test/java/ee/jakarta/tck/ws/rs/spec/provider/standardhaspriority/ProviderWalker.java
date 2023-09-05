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
import java.lang.reflect.Type;

import javax.xml.transform.Source;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.xml.bind.JAXBElement;

public class ProviderWalker {

  public boolean isWritable(Class<?> clazz) {
    if (clazz == Boolean.class || clazz == Character.class
        || clazz == JAXBElement.class || clazz == MultivaluedMap.class
        || clazz == Number.class || clazz == Source.class)
      return true;
    return false;
  }

  public long getSize(Boolean b, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return new TckBooleanProvider().getSize(b, type, genericType, annotations,
        mediaType);
  }

  public long getSize(Character c, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return new TckCharacterProvider().getSize(c, type, genericType, annotations,
        mediaType);
  }

  public long getSize(JAXBElement<String> j, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return new TckJaxbProvider().getSize(j, type, genericType, annotations,
        mediaType);
  }

  public long getSize(MultivaluedMap<String, String> mm, Class<?> type,
      Type genericType, Annotation[] annotations, MediaType mediaType) {
    return new TckMapProvider().getSize(mm, type, genericType, annotations,
        mediaType);
  }

  public long getSize(Number n, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return new TckNumberProvider().getSize(n, type, genericType, annotations,
        mediaType);
  }

  public void writeTo(Boolean b, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    new TckBooleanProvider().writeTo(b, type, genericType, annotations,
        mediaType, httpHeaders, entityStream);
  }

  public void writeTo(Character c, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    new TckCharacterProvider().writeTo(c, type, genericType, annotations,
        mediaType, httpHeaders, entityStream);
  }

  public void writeTo(JAXBElement<String> t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    new TckJaxbProvider().writeTo(t, type, genericType, annotations, mediaType,
        httpHeaders, entityStream);
  }

  public void writeTo(MultivaluedMap<String, String> t, Class<?> type,
      Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    new TckMapProvider().writeTo(t, type, genericType, annotations, mediaType,
        httpHeaders, entityStream);
  }

  public void writeTo(Number t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    new TckNumberProvider().writeTo(t, type, genericType, annotations,
        mediaType, httpHeaders, entityStream);
  }

  public Boolean readFrom(Boolean b, Class<Boolean> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    return new TckBooleanProvider().readFrom(type, genericType, annotations,
        mediaType, httpHeaders, entityStream);
  }

  public Character readFrom(Character c, Class<Character> type,
      Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    return new TckCharacterProvider().readFrom(type, genericType, annotations,
        mediaType, httpHeaders, entityStream);
  }

  public JAXBElement<String> readFrom(JAXBElement<String> c,
      Class<JAXBElement<String>> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    return new TckJaxbProvider().readFrom(type, genericType, annotations,
        mediaType, httpHeaders, entityStream);
  }

  public MultivaluedMap<String, String> readFrom(
      MultivaluedMap<String, String> m,
      Class<MultivaluedMap<String, String>> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    return new TckMapProvider().readFrom(type, genericType, annotations,
        mediaType, httpHeaders, entityStream);
  }

  public Number readFrom(Number n, Class<Number> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    return new TckNumberProvider().readFrom(type, genericType, annotations,
        mediaType, httpHeaders, entityStream);
  }

}
