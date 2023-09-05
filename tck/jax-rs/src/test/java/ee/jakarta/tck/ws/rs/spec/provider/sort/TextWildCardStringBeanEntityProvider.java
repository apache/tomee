/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.spec.provider.sort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanEntityProvider;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

@Provider
@Consumes("text/*")
@Produces("text/*")
public class TextWildCardStringBeanEntityProvider
    extends StringBeanEntityProvider {
  @Override
  public long getSize(StringBean t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return super.getSize(t, type, genericType, annotations, mediaType)
        + "/*".length();
  }

  @Override
  public StringBean readFrom(Class<StringBean> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    StringBean bean = super.readFrom(type, genericType, annotations, mediaType,
        httpHeaders, entityStream);
    bean.set(bean.get() + "text");
    return bean;
  }

  @Override
  public void writeTo(StringBean t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    super.writeTo(t, type, genericType, annotations, mediaType, httpHeaders,
        entityStream);
    entityStream.write("/*".getBytes());
  }
}
