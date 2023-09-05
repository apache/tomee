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

package ee.jakarta.tck.ws.rs.spec.provider.visibility;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.Providers;

@Provider
public class DummyWriter implements MessageBodyWriter<DummyClass> {
  private HttpHeaders headers;

  private UriInfo info;

  private Application application;

  private Request request;

  private Providers provider;

  protected DummyWriter(@Context HttpHeaders headers, @Context UriInfo info,
      @Context Application application, @Context Request request,
      @Context Providers provider) {
    super();
    this.headers = headers;
    this.info = info;
    this.application = application;
    this.request = request;
    this.provider = provider;
  }

  public DummyWriter(@Context HttpHeaders headers, @Context UriInfo info,
      @Context Application application, @Context Request request) {
    super();
    this.headers = headers;
    this.info = info;
    this.application = application;
    this.request = request;
  }

  public DummyWriter(@Context HttpHeaders headers, @Context UriInfo info,
      @Context Application application) {
    super();
    this.headers = headers;
    this.info = info;
    this.application = application;
  }

  public DummyWriter(@Context HttpHeaders headers, @Context UriInfo info) {
    super();
    this.headers = headers;
    this.info = info;
  }

  public DummyWriter(@Context HttpHeaders headers) {
    super();
    this.headers = headers;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return type == DummyClass.class;
  }

  @Override
  public long getSize(DummyClass t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return HolderClass.OK.length();
  }

  @Override
  public void writeTo(DummyClass t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    entityStream
        .write(new HolderClass(headers, info, application, request, provider)
            .toResponse().getEntity().toString().getBytes());
  }

}
