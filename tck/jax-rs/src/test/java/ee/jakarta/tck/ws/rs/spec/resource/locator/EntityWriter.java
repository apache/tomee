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

package ee.jakarta.tck.ws.rs.spec.resource.locator;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class EntityWriter implements MessageBodyWriter<LocatorEntity> {

  @Override
  public long getSize(LocatorEntity t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return 50;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return type == LocatorEntity.class;
  }

  @Override
  public void writeTo(LocatorEntity t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    String param;
    entityStream.write("resMatrix=".getBytes());
    param = t.resMatrix == null ? "null" : t.resMatrix;
    entityStream.write(param.getBytes());
    entityStream.write(";subMatrix=".getBytes());
    param = t.subMatrix == null ? "null" : t.subMatrix;
    entityStream.write(param.getBytes());
    entityStream.write(";entity=".getBytes());
    param = t.entity == null ? "null" : t.entity;
    entityStream.write(param.getBytes());

  }

}
