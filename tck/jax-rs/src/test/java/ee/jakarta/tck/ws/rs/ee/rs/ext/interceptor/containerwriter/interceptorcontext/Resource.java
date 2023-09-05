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

package ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.containerwriter.interceptorcontext;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;

import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.ContextOperation;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.TemplateInterceptorBody;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

@Path("resource")
public class Resource {

  @GET
  @Path("{id}")
  public Response genericResponse(@PathParam("id") String path) {
    ContextOperation op = ContextOperation.valueOf(path.toUpperCase());
    ResponseBuilder builder = createResponseBuilderWithHeader(op);
    switch (op) {
    case GETANNOTATIONS:
      Annotation[] annotations = ContextOperation.class.getAnnotations();
      builder = builder.entity(TemplateInterceptorBody.ENTITY, annotations);
      break;
    case GETMEDIATYPE:
      builder = builder.type(MediaType.APPLICATION_JSON_TYPE);
      break;
    case SETTYPE:
      ByteArrayInputStream bais = new ByteArrayInputStream(
          TemplateInterceptorBody.ENTITY.getBytes());
      Reader reader = new InputStreamReader(bais);
      builder = builder.entity(reader);
      break;
    default:
      break;
    }
    Response response = builder.build();
    return response;
  }

  // ///////////////////////////////////////////////////////////////////////

  ResponseBuilder createResponseBuilderWithHeader(ContextOperation op) {
    Response.ResponseBuilder builder = Response.ok();
    // set a header with ContextOperation so that the filter knows what to
    // do
    builder = builder.header(TemplateInterceptorBody.OPERATION, op.name());
    builder = builder.entity(TemplateInterceptorBody.ENTITY);
    return builder;
  }

}
