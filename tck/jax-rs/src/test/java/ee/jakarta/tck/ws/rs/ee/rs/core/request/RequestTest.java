/*
 * Copyright (c) 2011, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.core.request;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Variant;

@Path(value = "/RequestTest")
public class RequestTest {

  // ------------------ GET METHOD ----------------------------

  private static Response assertResponse(String expectedMethod,
      String actualMethod) {
    if (actualMethod.equalsIgnoreCase(expectedMethod)) {
      return Response.ok("Test PASSED").build();
    } else {
      return Response.ok("Test FAILED with " + actualMethod).build();
    }
  }

  @GET
  @Path("/GetMethodGetTest")
  public Response getTest(@Context Request req) {
    String method = req.getMethod();
    return assertResponse("GET", method);
  }

  @PUT
  @Path("/GetMethodPutTest")
  public Response putTest(@Context Request req) {
    String method = req.getMethod();
    return assertResponse("PUT", method);
  }

  @POST
  @Path("/GetMethodPostTest")
  public Response postTest(@Context Request req) {
    String method = req.getMethod();
    return assertResponse("POST", method);
  }

  @DELETE
  @Path("/GetMethodDeleteTest")
  public Response deleteTest(@Context Request req) {
    String method = req.getMethod();
    return assertResponse("DELETE", method);
  }

  @HEAD
  @Path("/GetMethodHeadTest")
  public Response headTest(@Context Request req) {
    String method = req.getMethod();

    if (method.equalsIgnoreCase("HEAD")) {
      return Response.ok().build();
    } else {
      return Response.status(400).build();
    }
  }

  // ------------------ SELECT VARIANT ----------------------------

  @GET
  @Path("/SelectVariantTestGet")
  public Response selectVariantTestGet(@Context Request req) {
    List<Variant> vs = null;

    try {
      req.selectVariant(vs);
      return Response.ok("Test FAILED - no exception thrown").build();
    } catch (IllegalArgumentException ile) {
      return Response.ok("Test PASSED - expected exception thrown").build();
    } catch (Throwable th) {
      return Response
          .ok("Test FAILED - wrong type exception thrown" + th.getMessage())
          .build();
    }
  }

  @PUT
  @Path("/SelectVariantTestPut")
  public Response selectVariantTestPut(@Context Request req) {
    return selectVariantTestGet(req);
  }

  @POST
  @Path("/SelectVariantTestPost")
  public Response selectVariantTestPost(@Context Request req) {
    return selectVariantTestGet(req);
  }

  @DELETE
  @Path("/SelectVariantTestDelete")
  public Response selectVariantTestDelete(@Context Request req) {
    return selectVariantTestGet(req);
  }

  @GET
  @Path("/SelectVariantTestResponse")
  public Response selectVariantTestResponse(@Context Request req) {
    List<Variant> list = Variant.encodings("CP1250", "UTF-8")
        .languages(Locale.ENGLISH).mediaTypes(MediaType.APPLICATION_JSON_TYPE)
        .add().build();
    Variant selectedVariant = req.selectVariant(list);
    if (null == selectedVariant)
      return Response.notAcceptable(list).build();
    return Response.ok("entity").build();
  }

  // ------------------ EVALUATE PRECONDITIONS ----------------------------
  private static boolean evaluatePreconditionsEntityTagNull(Request req) {
    try {
      req.evaluatePreconditions((EntityTag) null);
      return false;
    } catch (IllegalArgumentException iae) {
      return true;
    }
  }

  private static boolean evaluatePreconditionsNowEntityTagNull(Request req) {
    try {
      Date now = Calendar.getInstance().getTime();
      req.evaluatePreconditions(now, (EntityTag) null);
      return false;
    } catch (IllegalArgumentException iae) {
      return true;
    }
  }

  private static boolean evaluatePreconditionsDateEntityTag(Request req,
      Date date, String tag) {
    ResponseBuilder rb = req.evaluatePreconditions(date, createTag(tag));
    return rb == null;
  }

  private static boolean evaluatePreconditionsDate(Request req, Date date) {
    ResponseBuilder rb = req.evaluatePreconditions(date);
    return rb == null;
  }

  private static boolean evaluatePreconditions(Request req) {
    ResponseBuilder rb = req.evaluatePreconditions();
    return rb == null;
  }

  private static EntityTag createTag(String tag) {
    String xtag = new StringBuilder().append("\"").append(tag).append("\"")
        .toString();
    return EntityTag.valueOf(xtag);
  }

  private static boolean evaluatePreconditionsEntityTag(Request req,
      String tag) {
    ResponseBuilder rb = req.evaluatePreconditions(createTag(tag));
    return rb == null;
  }

  private static Response createResponse(boolean ok) {
    Status status = ok ? Status.OK : Status.PRECONDITION_FAILED;
    return Response.status(status).build();
  }

  private static Date getYear1900() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 1900);
    return calendar.getTime();
  }

  @GET
  @Path("/preconditionsSimpleGet")
  public Response evaluatePreconditionsEntityTagGetSimpleTest(
      @Context Request req) {
    boolean ok = evaluatePreconditionsEntityTag(req, "AAA");
    if (!ok)
      return Response.status(Status.GONE).build();
    ok &= evaluatePreconditionsNowEntityTagNull(req);
    if (!ok)
      return Response.status(Status.NOT_ACCEPTABLE).build();
    ok &= evaluatePreconditionsEntityTagNull(req);
    return createResponse(ok);
  }

  @GET
  @Path("/preconditionsAAAGet")
  public Response evaluatePreconditionsEntityTagAAAGetTest(
      @Context Request req) {
    boolean ok = evaluatePreconditionsEntityTag(req, "AAA");
    return createResponse(ok);
  }

  @PUT
  @Path("/preconditionsAAAPut")
  public Response evaluatePreconditionsEntityTagAAAPutTest(
      @Context Request req) {
    return evaluatePreconditionsEntityTagAAAGetTest(req);
  }

  @HEAD
  @Path("/preconditionsAAAHead")
  public Response evaluatePreconditionsEntityTagAAAHeadTest(
      @Context Request req) {
    return evaluatePreconditionsEntityTagAAAGetTest(req);
  }

  @GET
  @Path("/preconditionsAAAAgesAgoGet")
  public Response evaluatePreconditionsAgesAgoEntityTagAAAGetTest(
      @Context Request req) {
    Date date = getYear1900();
    boolean ok = evaluatePreconditionsDateEntityTag(req, date, "AAA");
    return createResponse(ok);
  }

  @GET
  @Path("/preconditionsNowAAAGet")
  public Response evaluatePreconditionsDateEntityTagAAAGetTest(
      @Context Request req) {
    Date date = Calendar.getInstance().getTime();
    boolean ok = evaluatePreconditionsDateEntityTag(req, date, "AAA");
    return createResponse(ok);
  }

  @GET
  @Path("/preconditionsNowGet")
  public Response evaluatePreconditionsDateGetTest(@Context Request req) {
    Date date = Calendar.getInstance().getTime();
    boolean ok = evaluatePreconditionsDate(req, date);
    return createResponse(ok);
  }

  @GET
  @Path("/preconditionsAgesAgoGet")
  public Response evaluatePreconditionsAgesAgoGetTest(@Context Request req) {
    Date date = getYear1900();
    boolean ok = evaluatePreconditionsDate(req, date);
    return createResponse(ok);
  }

  @GET
  @Path("/preconditionsGet")
  public Response evaluatePreconditionsGetTest(@Context Request req) {
    boolean ok = evaluatePreconditions(req);
    return createResponse(ok);
  }

  @GET
  @Path("/preconditionsHead")
  public Response evaluatePreconditionsHeadTest(@Context Request req) {
    boolean ok = evaluatePreconditions(req);
    return createResponse(ok);
  }
}
