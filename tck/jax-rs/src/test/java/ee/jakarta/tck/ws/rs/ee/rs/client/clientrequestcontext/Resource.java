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

package ee.jakarta.tck.ws.rs.ee.rs.client.clientrequestcontext;

import java.util.Collection;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

@Path("resource")
public class Resource {

  @POST
  @Path("post")
  public String post(String value) {
    return value;
  }

  @PUT
  @Path("put")
  public String put(String value) {
    return value;
  }

  @GET
  @Path("headers")
  public String headers(@Context HttpHeaders headers) {
    String hdrs = collectionToString(headers.getRequestHeaders().keySet());
    return hdrs;
  }

  protected static <T> String collectionToString(Collection<T> collection) {
    StringBuilder sb = new StringBuilder();
    for (T t : collection)
      sb.append(t).append(";");
    return sb.toString();
  }

}
