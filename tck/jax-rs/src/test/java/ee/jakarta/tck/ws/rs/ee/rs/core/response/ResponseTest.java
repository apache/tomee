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

package ee.jakarta.tck.ws.rs.ee.rs.core.response;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanRuntimeDelegate;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.RuntimeDelegate;

@Path("resource")
public class ResponseTest {
  public static final String ENTITY = "ENtiTy";

  @GET
  @Path("status")
  @Produces(value = "text/plain")
  public Response statusTest(@QueryParam("status") int status) {
    StringBuffer sb = new StringBuffer();
    sb.append("status code in request = " + status);
    ResponseBuilder builder = Response.status(status);
    Response res = builder.header("TESTHEADER", sb.toString()).build();
    return res;
  }

  @GET
  @Path("entity")
  @Produces(MediaType.TEXT_PLAIN)
  public String entity() {
    return ENTITY;
  }

  @GET
  @Path("corrupted")
  public CorruptedInputStream corrupted() {
    return new CorruptedInputStream(ENTITY.getBytes(), null);
  }

  @GET
  @Path("date")
  public String date(@QueryParam("date") String date) {
    return date;
  }

  @POST
  @Path("allowedmethods")
  public Response getAllowedMethods(String methods) {
    ResponseBuilder builder = Response.ok();
    StringTokenizer tokenizer = new StringTokenizer(methods);
    List<String> allowed = new LinkedList<String>();
    while (tokenizer.hasMoreTokens())
      allowed.add(tokenizer.nextToken());
    builder.allow(allowed.toArray(new String[0]));
    return builder.build();
  }

  @GET
  @Path("cookies")
  public Response getCookies() {
    NewCookie cookie1 = new NewCookie("c1", "v1");
    NewCookie cookie2 = new NewCookie("c2", "v2");
    Response response = Response.ok().cookie(cookie1).cookie(cookie2).build();
    return response;
  }

  @POST
  @Path("date")
  public Response getDate(String date) throws InterruptedException {
    ResponseBuilder builder = Response.ok();
    Thread.sleep(1500L);
    if (date != null && date.length() != 0) {
      long millis = Long.parseLong(date);
      Date dateFromMillis = new Date(millis);
      builder = builder.header(HttpHeaders.DATE, dateFromMillis);
    }
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("entitytag")
  public Response getEntityTag(String tag) {
    ResponseBuilder builder;
    if (tag != null && tag.length() != 0) {
      EntityTag entityTag = new EntityTag(tag);
      builder = Response.notModified(entityTag);
    } else
      builder = Response.ok();
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("headers")
  public Response getHeaders(String headers) {
    CacheControl ccl = new CacheControl();
    NewCookie cookie = new NewCookie("cookie", "eikooc");
    String encoding = "gzip";
    Date date = Calendar.getInstance().getTime();
    ResponseBuilder builder = Response.ok();
    if (headers != null && headers.length() != 0) {
      builder = builder.cacheControl(ccl).cookie(cookie).encoding(encoding)
          .expires(date).language(Locale.CANADA_FRENCH);
    }
    return builder.build();
  }

  @POST
  @Path("headerstring")
  public Response getHeaderString(String headers) {
    StringBuilder builder = new StringBuilder("s1");
    StringBuffer buffer = new StringBuffer("s2");
    StringBean bean = new StringBean("s3");
    ResponseBuilder response = Response.ok();
    if (headers != null && headers.length() != 0)
      response = response.header(builder.toString(), builder)
          .header(buffer.toString(), buffer).header(bean.get(), bean);
    return response.build();
  }

  @POST
  @Path("language")
  public Response getLanguage(String lang) {
    ResponseBuilder builder = Response.ok();
    Locale locale = null;
    if (Locale.CANADA_FRENCH.getCountry().equals(lang))
      locale = Locale.CANADA_FRENCH;
    if (locale != null)
      builder = builder.language(locale);
    return builder.build();
  }

  @POST
  @Path("lastmodified")
  public Response lastModified(String date) {
    ResponseBuilder builder = Response.ok();
    if (date != null && date.length() != 0) {
      long millis = Long.parseLong(date);
      Date dateFromMillis = new Date(millis);
      builder = builder.lastModified(dateFromMillis);
    }
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("length")
  public Response getLength(String entity) {
    Response response = null;
    if (entity == null || entity.length() == 0)
      response = Response.ok().build();
    else
      response = Response.ok(entity)
          .header(HttpHeaders.CONTENT_LENGTH, entity.length()).build();
    return response;
  }

  @POST
  @Path("link")
  public Response getLink(String rel) {
    ResponseBuilder builder = Response.ok();
    if (rel != null && rel.length() != 0)
      builder.links(createLink("path", rel));
    return builder.build();
  }

  @POST
  @Path("linkbuilder")
  public Response getLinkBuilder(String rel) {
    Link link1 = createLink("path1", rel);
    Response response = Response.ok().links(link1).build();
    Link builderLink = response.getLinkBuilder(rel).build();
    response = Response.ok().links(builderLink).build();
    return response;
  }

  @GET
  @Path("links")
  public Response getLinks() {
    Link link1 = createLink("path1", "rel1");
    Link link2 = createLink("path2", "rel2");
    Response response = Response.ok().links(link1, link2).build();
    return response;
  }

  @POST
  @Path("location")
  public Response getLocation(String path) {
    URI location = createUri(path);
    Response response = Response.ok().location(location).build();
    return response;
  }

  @POST
  @Path("mediatype")
  public Response getMediaType(String mediaType) {
    MediaType media = MediaType.WILDCARD_TYPE;
    if (mediaType.equals(MediaType.APPLICATION_ATOM_XML))
      media = MediaType.APPLICATION_ATOM_XML_TYPE;
    Response response = Response.ok().type(media).build();
    return response;
  }

  @POST
  @Path("statusinfo")
  public Response getStatusInfo(String status) {
    Status stat = Status.valueOf(status);
    Response response = Response.status(stat).build();
    return response;
  }

  // For the methods that checks for the header delegate this is the way
  // to add header delegate by switching runtimedelegate
  // As long as the runtime delegate is one only for whole classloader
  @Path("setstringbeanruntime")
  @GET
  public Response setStringBeanRuntime() {
    RuntimeDelegate original = RuntimeDelegate.getInstance();
    if (!(original instanceof StringBeanRuntimeDelegate)) {
      StringBeanRuntimeDelegate sbrd = new StringBeanRuntimeDelegate(original);
      RuntimeDelegate.setInstance(sbrd);
    }
    return Response.ok().build();
  }

  // We need to switch back to the original runtime delegate since
  // we cannot be sure what happen when the war with our runtimedelegate gets
  // undeployed
  @Path("setoriginalruntime")
  @GET
  public Response setOriginalRuntime() {
    Response response = null;
    RuntimeDelegate stringBeanDelegate = RuntimeDelegate.getInstance();
    if (stringBeanDelegate instanceof StringBeanRuntimeDelegate) {
      RuntimeDelegate original = ((StringBeanRuntimeDelegate) stringBeanDelegate)
          .getOriginal();
      RuntimeDelegate.setInstance(original);
      response = Response.ok().build();
    } else
      response = Response.status(Status.NO_CONTENT).build();
    return response;
  }

  @Path("created")
  @GET
  public Response setLocationHeader() {
    try {
      Response response = Response.created(new URI("created")).status(200).build();
      return response;
    } catch (URISyntaxException e) {
       throw new RuntimeException(e);
    }
  }

  // ////////////////////////////////////////////////////////////////////
  protected static Link createLink(String path, String rel) {
    return Link.fromUri(createUri(path)).rel(rel).build();
  }

  protected static URI createUri(String path) {
    URI uri;
    try {
      uri = new URI("http://localhost.tck:888/url404/" + path);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    return uri;
  }

}
