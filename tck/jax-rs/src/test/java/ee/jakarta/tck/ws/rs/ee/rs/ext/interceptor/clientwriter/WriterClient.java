/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.clientwriter;

import java.io.IOException;

import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.TemplateInterceptorBody;
import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;

/**
 * Client with given ContextOperation enum, so that an enum name is passed as a
 * http header to an interceptor. Due to the ContextOperation, the proper method
 * on an interceptor is called.
 * 
 * @param <CONTEXTOPERATION>
 */
public abstract class WriterClient<CONTEXTOPERATION extends Enum<?>>
    extends JaxrsCommonClient {

  private static final long serialVersionUID = 8110273180216593061L;

  /**
   * Set the header OPERATION to a proper value Also set the entity, it is good
   * as it is here for most of the tests. For the rest, the entity needs to be
   * replaced.
   */
  protected void setOperationAndEntity(CONTEXTOPERATION op) {
    addHeader(TemplateInterceptorBody.OPERATION, op.name());
    setRequestContentEntity(TemplateInterceptorBody.ENTITY);
  }

  /**
   * Invoke and convert IOException to Fault
   */
  protected void invoke() throws Fault {
    try {
      setProperty(Property.REQUEST, buildRequest(Request.POST, ""));
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
  protected abstract void addProviders();
}
