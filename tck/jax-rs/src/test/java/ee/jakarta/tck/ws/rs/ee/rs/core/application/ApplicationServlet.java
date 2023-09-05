/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.core.application;

import java.util.Map;
import java.util.Set;

import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

@Path("ApplicationTest")
public class ApplicationServlet {

  @GET
  @Path("/GetSingletons")
  public Response testGetSingletons(@Context Application application) {
    // Derived from the default, thus return empty implementation
    if (application.getSingletons() == null)
      return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    // Passed
    return Response.ok(String.valueOf(application.getSingletons().size()))
        .build();
  }

  @GET
  @Path("/GetClasses")
  public Response testGetClasses(@Context Application application) {
    // Context Issue
    application = getDeproxiedApplication(application);
    if (!application.getClasses().contains(ApplicationServlet.class))
      return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    // Passed
    return Response.ok(String.valueOf(application.getClasses().size())).build();
  }

  @Path("properties")
  @GET
  public Response getProperties(@Context Application application) {
    Response response = null;
    application = getDeproxiedApplication(application);
    Map<String, Object> properties = application.getProperties();
    if (properties == null || properties.size() == 0) {
      response = Response.noContent().build();
    } else {
      Object val0 = properties.get(TSAppConfig.KEYS[0]);
      Object val1 = properties.get(TSAppConfig.KEYS[1]);
      if (TSAppConfig.VALUES[0].equals(val0)
          && TSAppConfig.VALUES[1].equals(val1))
        response = Response.ok(JaxrsUtil.mapToString(properties)).build();
      else
        response = Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }
    return response;
  }

  // ////////////////////////////////////////////////////////////////////////

  /**
   * Deproxy
   */
  protected static Application getDeproxiedApplication(
      Application application) {
    Set<Object> singletons = application.getSingletons();
    for (Object s : singletons)
      if (s.getClass() == ApplicationHolderSingleton.class)
        return ((ApplicationHolderSingleton) s).getApplication();
    throw new IllegalStateException(
        "ApplicationHolderSingleton has not been found in a list of singletons");
  }

}
