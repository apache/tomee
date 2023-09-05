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

package ee.jakarta.tck.ws.rs.ee.rs.container.requestcontext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

@Path("resource")
public class Resource {

  /**
   * The method has been changed from POST to OPTIONS on a request filter Check
   * that correct method has been called then
   */
  @GET
  @Path("setmethod")
  public String setMethod1() {
    return getReturnValue("GET");
  }

  @OPTIONS
  @Path("setmethod")
  public String setMethod2() {
    return getReturnValue("OPTIONS");
  }

  /**
   * SetUri change
   */
  @GET
  @Path("setrequesturi1uri")
  public String setRequestUri() {
    return getReturnValue(RequestFilter.URI);
  }

  @GET
  @Path("setpropertycontext")
  public String setPropertyContext(@Context HttpServletRequest servletRequest) {
    if (servletRequest == null) {
      return "NULL";
    }
    String entity = (String) servletRequest
        .getAttribute(RequestFilter.PROPERTYNAME);
    return getReturnValue(entity);
  }

  @GET
  @Path("setrequesturi1")
  public String setRequestUriDidNotChangeUri() {
    return "Filter did not change the uri to go to";
  }

  @Context
  HttpHeaders headers;

  private String getReturnValue(String subValue) {
    String header = headers.getHeaderString(RequestFilter.OPERATION);
    return subValue + " " + header;
  }

}
