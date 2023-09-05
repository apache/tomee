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

package ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.reader;

import java.io.IOException;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.Response;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.TemplateInterceptorBody;
import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;

/**
 * Client with given ContextOperation enum, so that an enum name is passed as a
 * http header to an interceptor. Due to the ContextOperation, the proper method
 * on an interceptor is called.
 * 
 * @param <CONTEXTOPERATION>
 */
public abstract class ReaderClient<CONTEXTOPERATION extends Enum<?>>
    extends JaxrsCommonClient {

  private static final long serialVersionUID = 7145627888730121260L;

  /**
   * Create response to be faked as returned from server
   */
  protected Response.ResponseBuilder createResponse(CONTEXTOPERATION op) {
    Response.ResponseBuilder builder = Response.ok()
        .header(TemplateInterceptorBody.OPERATION, op.name())
        .entity(TemplateInterceptorBody.ENTITY);
    return builder;
  }

  /**
   * Create a request filter to be aborted with given fake response simulating
   * the resource from a request
   * 
   * @param response
   * @return
   */
  protected static ClientRequestFilter createRequestFilter(
      final Response response) {
    ClientRequestFilter outFilter = new ClientRequestFilter() {

      @Override
      public void filter(ClientRequestContext context) throws IOException {
        Response r;
        if (response == null)
          r = Response.ok().build();
        else
          r = response;
        context.abortWith(r);
      }
    };
    return outFilter;
  }

  /**
   * Invoke and convert IOException to Fault
   */
  protected void invoke() throws Fault {
    try {
      setProperty(Property.REQUEST, buildRequest(Request.GET, "404URL/"));
      super.invoke();
    } catch (Exception cause) {
      if (cause instanceof IOException)
        throw new Fault(cause.getMessage());
      else
        throw new Fault(cause);
    }
  }

  /**
   * Register providers to client configuration
   * 
   * @param response
   *          ClientRequestFilter#abortWith response
   */
  protected void addProviders(Response response) throws Fault {
    ClientRequestFilter requestFilter = createRequestFilter(response);
    addProvider(requestFilter);
  }
}
