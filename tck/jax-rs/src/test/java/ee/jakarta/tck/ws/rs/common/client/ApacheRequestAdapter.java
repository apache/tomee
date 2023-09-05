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

package ee.jakarta.tck.ws.rs.common.client;

import ee.jakarta.tck.ws.rs.common.webclient.http.HttpRequest;

public class ApacheRequestAdapter extends HttpRequest {

  public ApacheRequestAdapter(String requestLine, String host, int port) {
    super(requestLine, host, port);
  }

  /**
   * <code>getRequestPath</code> returns the request path for this particular
   * request.
   *
   * @return String request path
   */
  public String getRequestPath() {
    return super.getRequestPath();
  }

}
