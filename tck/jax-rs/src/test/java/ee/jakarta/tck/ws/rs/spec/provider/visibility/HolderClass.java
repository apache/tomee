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

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class HolderClass {
  public static final String OK = "11111";

  private HttpHeaders headers;

  private UriInfo info;

  private Application application;

  private Request request;

  private Providers provider;

  public HolderClass(HttpHeaders headers, UriInfo info, Application application,
      Request request, Providers provider) {
    super();
    this.headers = headers;
    this.info = info;
    this.application = application;
    this.request = request;
    this.provider = provider;
  }

  public Response toResponse() {
    int ok = application != null ? 1 : 0;
    ok += headers != null ? 10 : 0;
    ok += info != null ? 100 : 0;
    ok += request != null ? 1000 : 0;
    ok += provider == null ? 10000 : 0;
    return Response.ok(String.valueOf(ok)).build();
  }
}
