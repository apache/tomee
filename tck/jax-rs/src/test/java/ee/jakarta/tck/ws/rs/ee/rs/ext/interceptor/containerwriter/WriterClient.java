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

package ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.containerwriter;

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

  private static final long serialVersionUID = -9222693803307311300L;

  /**
   * Invoke and convert CONTEXTOPERATION to a path
   */
  protected void invoke(CONTEXTOPERATION op) throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, op.name().toLowerCase()));
    super.invoke();
  }

  /**
   * Invoke and convert CONTEXTOPERATION to a path
   */
  protected void invoke(CONTEXTOPERATION op, Request method) throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(method, op.name().toLowerCase()));
    super.invoke();
  }

}
