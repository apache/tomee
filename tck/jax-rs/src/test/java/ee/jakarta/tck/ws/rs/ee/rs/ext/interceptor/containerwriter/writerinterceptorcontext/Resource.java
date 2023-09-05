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

package ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.containerwriter.writerinterceptorcontext;

import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.TemplateInterceptorBody;
import ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.writer.writerinterceptorcontext.ContextOperation;
import ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.writer.writerinterceptorcontext.OnWriteExceptionThrowingStringBean;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
    case GETHEADERS:
      for (int i = 0; i != 5; i++)
        builder = builder.header(TemplateInterceptorBody.PROPERTY + i, "any");
      break;
    case PROCEEDTHROWSWEBAPPEXCEPTION:
      builder.entity(new OnWriteExceptionThrowingStringBean(
          TemplateInterceptorBody.ENTITY));
      break;
    default:
      break;
    }
    Response response = builder.build();
    return response;
  }

  // ///////////////////////////////////////////////////////////////////////

  static ResponseBuilder createResponseBuilderWithHeader(ContextOperation op) {
    Response.ResponseBuilder builder = Response.ok();
    // set a header with ContextOperation so that the filter knows what to
    // do
    builder = builder.header(TemplateInterceptorBody.OPERATION, op.name());
    builder = builder.entity(TemplateInterceptorBody.ENTITY);
    return builder;
  }

}
