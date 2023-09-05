/*
 * Copyright (c) 2011, 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.resource.webappexception.nomapper;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Path(value = "resource")
public class Resource {

  static String html_content = "<html>"
      + "<head><title>CTS-get text/html</title></head>"
      + "<body>CTS-get text/html</body></html>";

  public static final String TESTID = "CTS-WebApplicationExceptionTest";

  @GET
  @Path("/EmptyConstructor")
  public Response emptyConstructor() {
    throw new WebApplicationException();
  }

  @GET
  @Path("/StatusCode404")
  public Response statusCode404() {
    throw new WebApplicationException(404);
  }

  @GET
  @Path("/StatusCode401")
  public Response statusCode401() {
    throw new WebApplicationException(401);
  }

  @GET
  @Path("/Status503")
  public Response status503() {
    throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
  }

  @GET
  @Path("/Status415")
  public Response status415() {
    throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);
  }

  @GET
  @Path("/ResponseTest")
  public Response responseTest() {
    throw new WebApplicationException(
        Response.ok(TESTID).header("CTS-HEAD", TESTID).build());
  }

  @GET
  @Path("/NullResponseTest")
  public Response nullResponseTest() {
    Response rsp = null;
    throw new WebApplicationException(rsp);
  }

  @GET
  @Path("/getResponseTest")
  public Response getResponseTest() {
    Response r = Response.ok(TESTID).header("CTS-HEAD", TESTID).build();
    WebApplicationException wae = new WebApplicationException(r);
    return wae.getResponse();
  }

  @GET
  @Path("/ThrowableTest")
  public Response throwableTest() {
    throw new WebApplicationException(
        new Throwable("CTS-WebApplicationExceptionTest-throwableTest"));
  }

  @GET
  @Path("/ThrowableResponseTest")
  public Response throwableResponseTest() {
    throw new WebApplicationException(
        new Throwable(id("-throwableResponseTest-FAIL")),
        Response.ok(id("-throwableResponseTest")).status(202)
            .header("CTS-HEAD", TESTID).build());
  }

  @GET
  @Path("/ThrowableResponseTest1")
  public Response throwableResponseTest1() {
    Response rsp = null;
    throw new WebApplicationException(
        new Throwable(id("-throwableResponseTest1-FAIL")), rsp);
  }

  @GET
  @Path("/ThrowableStatusTest")
  public Response throwableStatusTest() {
    throw new WebApplicationException(new Throwable(id("-throwableStatusTest")),
        Response.Status.SEE_OTHER);
  }

  @GET
  @Path("/ThrowableNullStatusTest")
  public Response throwableNullStatusTest() {
    try {
      throw new WebApplicationException(
          new Throwable(id("-throwableNullStatusTest")),
          (Response.Status) null);
    } catch (java.lang.IllegalArgumentException iae) {
      throw new WebApplicationException(
          new Throwable(id("-throwableNullStatusTest")),
          Response.ok(id("-throwableNullStatusTest-PASS")).build());
    } catch (Exception e) {
      throw new WebApplicationException(
          new Throwable(id("-throwableNullStatusTest")),
          Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("/ThrowableStatusCodeTest")
  public Response throwableStatusCodeTest() {
    throw new WebApplicationException(
        new Throwable(id("-throwableStatusCodeTest")), 204);
  }

  public static String id(String suffix) {
    return new StringBuilder().append(TESTID).append(suffix).toString();
  }
}