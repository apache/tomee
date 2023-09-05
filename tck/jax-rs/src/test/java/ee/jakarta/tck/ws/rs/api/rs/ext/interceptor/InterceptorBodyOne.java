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
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.InterceptorContext;

public class InterceptorBodyOne<CONTEXT extends InterceptorContext>
    extends TemplateInterceptorBody<CONTEXT> {

  public void getAnnotations() {
    Annotation[] annotations = context.getAnnotations();
    setSeparatedEntity(";", annotations);
  }

  public void getGenericType() {
    Type type = context.getGenericType();
    String entity = null;
    if (type instanceof Class)
      entity = ((Class<?>) type).getName();
    else
      entity = type.toString();
    setEntity(entity);
  }

  public void getMediaType() {
    MediaType type = context.getMediaType();
    setEntity(type);
  }

  public void getProperty() {
    Object o = context.getProperty(PROPERTY);
    setEntity(o == null ? NULL : ENTITY2);
  }

  public void getPropertyNames() {
    for (int i = 0; i != 5; i++)
      context.setProperty(PROPERTY + i, PROPERTY);
  }

  public void getPropertyNamesIsReadOnly() {
    Collection<String> names = context.getPropertyNames();
    int size = names.size();
    for (int i = 0; i != 5; i++)
      try {
        names.add(PROPERTY + i);
      } catch (Exception e) {
        // exception is possible
      }
    names = context.getPropertyNames();
    assertTrue(names.size() == size, "Unexpected property names", names);
    setEntity(NULL);
  }

  public void getType() {
    Class<?> type = context.getType();
    setEntity(type.getName());
  }

  public void removeProperty() {
    context.setProperty(PROPERTY, NULL);
    assertTrue(NULL.equals(context.getProperty(PROPERTY)), PROPERTY,
        "property not found");
    context.removeProperty(PROPERTY);
    Object o = context.getProperty(PROPERTY);
    setEntity(o == null ? NULL : o.toString());
  }

  public void setAnnotations() {
    Annotation[] annotations = ContextOperation.class.getAnnotations();
    context.setAnnotations(annotations);
    getAnnotations();
  }

  public void setAnnotationsNull() {
    try {
      context.setAnnotations(null);
      setEntity(NULL);
    } catch (NullPointerException e) {
      setEntity(NPE);
    }
  }

  public void setGenericType() {
    byte[] array = new byte[0];
    context.setGenericType(array.getClass());
    getGenericType();
  }

  public void setMediaType() {
    MediaType type = MediaType.APPLICATION_FORM_URLENCODED_TYPE;
    context.setMediaType(type);
    getMediaType();
  }

  public void setProperty() {
    StringBuilder sb = new StringBuilder();
    sb.append(ENTITY2);
    context.setProperty(PROPERTY, NULL);
    context.setProperty(PROPERTY, sb);
  }

  public void setPropertyNull() {
    context.setProperty(ENTITY2, ENTITY2);
    assertTrue(context.getProperty(ENTITY2) != null, "Property", ENTITY2,
        "set but not found");
    context.setProperty(ENTITY2, null);
    String o = (String) context.getProperty(ENTITY2);
    setEntity(o == null ? NULL : o);
  }

  public void setType() {
    context.setType(InputStreamReader.class);
    getType();
  }

  public void ioException() throws IOException {
    throw new IOException(IOE);
  }

  public void webApplicationException() {
    throw new WebApplicationException(
        Response.status(Status.CONFLICT).entity(ENTITY2).build());
  }
}
