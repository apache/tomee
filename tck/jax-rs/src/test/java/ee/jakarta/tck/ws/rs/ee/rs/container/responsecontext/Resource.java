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

package ee.jakarta.tck.ws.rs.ee.rs.container.responsecontext;

import java.io.ByteArrayInputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanRuntimeDelegate;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanWithAnnotation;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.RuntimeDelegate;

@Path("resource")
public class Resource {

  @Context
  UriInfo info;

  @Path("getallowedmethods")
  @GET
  public Response getAllowedMethods() {
    ResponseBuilder builder = createResponseWithHeader();
    Response response = builder.allow("OPTIONS", "TRACE").build();
    return response;
  }

  @Path("getcookies")
  @GET
  public Response getCookies() {
    NewCookie cookie = new NewCookie(ResponseFilter.COOKIENAME,
        ResponseFilter.COOKIENAME);
    ResponseBuilder builder = createResponseWithHeader();
    Response response = builder.cookie(cookie).build();
    return response;
  }

  @Path("getcookiesisreadonly")
  @GET
  public Response getCookiesIsReadOnly() {
    ResponseBuilder builder = createResponseWithHeader();
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getdate")
  public Response getDate(String text) {
    ResponseBuilder builder = createResponseWithHeader();
    if (text != null && text.length() != 0) {
      long milis = Long.valueOf(text);
      Date date = new Date(milis);
      builder = builder.header("Date", date);
    }
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getentity")
  public Response getEntity(String entity) {
    ResponseBuilder builder = createResponseWithHeader();
    if (entity != null && entity.length() != 0)
      builder = builder.entity(entity.getBytes());
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getentityannotations")
  public Response getEntityAnnotations(String setEntity) {
    Annotation[] annotations = ResponseFilter.class.getAnnotations();
    ResponseBuilder builder = createResponseWithHeader();
    if (Boolean.parseBoolean(setEntity))
      builder = builder.entity("entity", annotations);
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getentityannotationsonentity")
  public Response getEntityAnnotationsOnEntity(String entity) {
    ResponseBuilder builder = createResponseWithHeader();
    builder = builder.entity(new StringBeanWithAnnotation(entity));
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getentityclass")
  public Response getEntityClass(String clazz) {
    String content = "ENTity";
    ResponseBuilder builder = createResponseWithHeader();
    Object entity = null;
    if ("string".equals(clazz))
      entity = content;
    else if ("bytearray".equals(clazz))
      entity = content.getBytes();
    else if ("inputstream".equals(clazz))
      entity = new ByteArrayInputStream(content.getBytes());
    builder = builder.entity(entity);
    Response response = builder.build();
    return response;
  }

  @GET
  @Path("getentitystream")
  public Response getEntityStream() {
    ResponseBuilder builder = createResponseWithHeader();
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getentitytag")
  public Response getEntityTag(String tagName) {
    ResponseBuilder builder = createResponseWithHeader();
    if (tagName != null && tagName.length() != 0) {
      EntityTag tag = new EntityTag(tagName);
      builder = builder.tag(tag);
    }
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getentitytype")
  public Response getEntityType(String type) {
    ResponseBuilder builder = createResponseWithHeader();
    Object entity = null;
    String content = "ENTity";
    if ("string".equals(type))
      entity = content;
    else if ("bytearray".equals(type))
      entity = content.getBytes();
    else if ("inputstream".equals(type))
      entity = new ByteArrayInputStream(content.getBytes());
    builder = builder.entity(entity);
    Response response = builder.build();
    return response;
  }

  @GET
  @Path("setentitystream")
  public Response setEntityStream() {
    ResponseBuilder builder = createResponseWithHeader();
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getheaders")
  public Response getHeaders(String header) {
    ResponseBuilder builder = createResponseWithHeader();
    for (int i = 0; i != 5; i++)
      builder = builder.header(header + i, header);
    Response response = builder.build();
    return response;
  }

  @GET
  @Path("getheadersismutable")
  public Response getHeadersIsMutable() {
    ResponseBuilder builder = createResponseWithHeader();
    Response response = builder.build();
    return response;
  }

  @GET
  @Path("getheaderstringoperation")
  public Response getHeaderStringByOperation() {
    ResponseBuilder builder = createResponseWithHeader();
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getheaderstringheader")
  public Response getHeaderStringByHeader(String content) {
    ResponseBuilder builder = createResponseWithHeader();
    if (content.equals("toString"))
      builder = builder.header(ResponseFilter.HEADER,
          new StringBuilder().append(ResponseFilter.ENTITY));
    if (content.equals("commaSeparated")) {
      builder = builder.header(ResponseFilter.HEADER,
          new StringBuilder().append(ResponseFilter.ENTITY));
      builder = builder.header(ResponseFilter.HEADER, ResponseFilter.ENTITY);
    }
    if (content.equals("headerDelegate")) {
      StringBean bean = new StringBean(ResponseFilter.ENTITY);
      builder = builder.header(ResponseFilter.HEADER, bean);
    }
    if (content.equalsIgnoreCase("entity"))
      builder = builder.header(ResponseFilter.HEADER, content);
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getlanguage")
  public Response getLanguage(String language) {
    ResponseBuilder builder = createResponseWithHeader();
    if (language != null && language.length() != 0)
      builder = builder.language(language);
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getlastmodified")
  public Response getLastModified(String last) {
    ResponseBuilder builder = createResponseWithHeader();
    if (last != null && last.length() != 0) {
      long lastMilist = Long.parseLong(last);
      Date date = new Date(lastMilist);
      builder = builder.lastModified(date);
    }
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getlength")
  public Response getLength(String entity) {
    ResponseBuilder builder = createResponseWithHeader();
    if (entity != null && entity.length() != 0)
      builder = builder.entity(entity).header(HttpHeaders.CONTENT_LENGTH,
          entity.length());
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getlink")
  public Response getLink(String uri) {
    ResponseBuilder builder = createResponseWithHeader();
    if (uri != null && uri.length() != 0) {
      Link link = Link.fromUri(uri).rel(ResponseFilter.RELATION).build();
      builder = builder.links(link);
    }
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getlinkbuilder")
  public Response getLinkBuilder(String uri) {
    return getLink(uri);
  }

  @POST
  @Path("getlinks")
  public Response getLinks(String uris) {
    ResponseBuilder builder = createResponseWithHeader();
    if (uris != null && uris.length() != 0) {
      String[] tokens = uris.split(";");
      Link[] links = new Link[tokens.length];
      for (int i = 0; i != tokens.length; i++)
        links[i] = Link.fromUri(tokens[i]).build();
      builder = builder.links(links);
    }
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getlocation")
  public Response getLocation(String uri) throws URISyntaxException {
    ResponseBuilder builder = createResponseWithHeader();
    if (uri != null && uri.length() != 0)
      builder = builder.location(new URI(uri));
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getmediatype")
  public Response getMediaType(String media) throws URISyntaxException {
    ResponseBuilder builder = createResponseWithHeader();
    if (media != null && media.length() != 0)
      builder = builder.type(media);
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("getstatus")
  public Response getStatus(String entity) {
    int status = Integer.parseInt(entity);
    ResponseBuilder builder = createResponseWithHeader();
    Response response = builder.status(status).build();
    return response;
  }

  @POST
  @Path("getstatusinfo")
  public Response getStatusinfo(String entity) {
    return getStatus(entity);
  }

  @POST
  @Path("getstringheaders")
  public Response getStringHeaders(String content) {
    return getHeaderStringByHeader(content);
  }

  @POST
  @Path("hasentity")
  public Response hasEntity(String entity) {
    ResponseBuilder builder = createResponseWithHeader();
    if (entity != null && entity.length() != 0)
      builder = builder.entity(entity);
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("haslink")
  public Response hasLink(String uri) {
    return getLink(uri);
  }

  @GET
  @Path("setentity")
  public Response setEntity() {
    ResponseBuilder builder = createResponseWithHeader();
    Response response = builder.build();
    return response;
  }

  @POST
  @Path("setstatus")
  public Response setStatus(String status) {
    ResponseBuilder builder = createResponseWithHeader();
    Response response = builder.entity(status).build();
    return response;
  }

  @POST
  @Path("setstatusinfo")
  public Response setStatusInfo(String status) {
    return setStatus(status);
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
    return createResponseWithHeader().build();
  }

  // We need to switch back to the original runtime delegate since
  // we cannot be sure what happen when the war with our runtimedelegate gets
  // undeployed
  @Path("setoriginalruntime")
  @GET
  public Response setOriginalRuntime() {
    ResponseBuilder builder = createResponseWithHeader();
    RuntimeDelegate stringBeanDelegate = RuntimeDelegate.getInstance();
    if (stringBeanDelegate instanceof StringBeanRuntimeDelegate) {
      RuntimeDelegate original = ((StringBeanRuntimeDelegate) stringBeanDelegate)
          .getOriginal();
      RuntimeDelegate.setInstance(original);
    } else
      builder = builder.status(Status.NO_CONTENT);
    return builder.build();
  }

  // //////////////////////////////////////////////////////////////////

  private ResponseBuilder createResponseWithHeader() {
    // get value of @Path(value)
    List<PathSegment> segments = info.getPathSegments();
    PathSegment last = segments.get(segments.size() - 1);
    // convert the value to ContextOperation
    ContextOperation op = ContextOperation
        .valueOf(last.getPath().toUpperCase());
    Response.ResponseBuilder builder = Response.ok();
    // set a header with ContextOperation so that the filter knows what to
    // do
    builder = builder.header(ResponseFilter.OPERATION, op.name());
    return builder;
  }

}
