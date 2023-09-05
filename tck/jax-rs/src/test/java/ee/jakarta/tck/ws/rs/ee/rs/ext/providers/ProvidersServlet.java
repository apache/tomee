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

package ee.jakarta.tck.ws.rs.ee.rs.ext.providers;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import ee.jakarta.tck.ws.rs.ee.rs.core.application.ApplicationServlet;
import ee.jakarta.tck.ws.rs.ee.rs.ext.contextresolver.EnumProvider;
import ee.jakarta.tck.ws.rs.ee.rs.ext.exceptionmapper.AnyExceptionExceptionMapper;
import ee.jakarta.tck.ws.rs.ee.rs.ext.messagebodyreaderwriter.EntityAnnotation;
import ee.jakarta.tck.ws.rs.ee.rs.ext.messagebodyreaderwriter.ReadableWritableEntity;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;

@Path("ProvidersServlet")
public class ProvidersServlet extends ApplicationServlet {
  @Context
  Providers providers;

  private EnumProvider getEnumProvider(MediaType type) {
    ContextResolver<EnumProvider> scr = providers
        .getContextResolver(EnumProvider.class, type);
    EnumProvider ep = scr.getContext(EnumProvider.class);
    return ep;
  }

  Response getResponseByEnumProvider(EnumProvider expected,
      EnumProvider given) {
    Status status = Status.NO_CONTENT;
    if (given != null)
      status = given != expected ? Status.NOT_ACCEPTABLE : Status.OK;
    return Response.status(status).build();
  }

  @GET
  @Path("isRegisteredContextResolver")
  public Response isRegisteredContextResolver() {
    EnumProvider ep = getEnumProvider(MediaType.WILDCARD_TYPE);
    return getResponseByEnumProvider(EnumProvider.JAXRS, ep);
  }

  @GET
  @Path("isRegisteredTextPlainContextResolver")
  public Response isRegisteredTextPlainContextResolver() {
    EnumProvider ep = getEnumProvider(MediaType.TEXT_PLAIN_TYPE);
    return getResponseByEnumProvider(EnumProvider.CTS, ep);
  }

  @GET
  @Path("isRegisteredAppJsonContextResolver")
  public Response isRegisteredAppJsonContextResolver() {
    EnumProvider ep = getEnumProvider(MediaType.APPLICATION_JSON_TYPE);
    return getResponseByEnumProvider(EnumProvider.JAXRS, ep);
  }

  @GET
  @Path("isRegisteredExceptionMapperRuntimeEx")
  public Response isRegisteredExceptionMapperRuntimeException() {
    ExceptionMapper<Exception> em = providers
        .getExceptionMapper(Exception.class);
    return em.toResponse(new RuntimeException());
  }

  @GET
  @Path("isRegisteredExceptionMapperNullEx")
  public Response isRegisteredExceptionMapperNullException() {
    ExceptionMapper<Exception> em = providers
        .getExceptionMapper(Exception.class);
    return em.toResponse(null);
  }

  @GET
  @Path("isRegisteredRuntimeExceptionMapper")
  public Response isRegisteredRuntimeExceptionMapper() {
    ExceptionMapper<RuntimeException> em = providers
        .getExceptionMapper(RuntimeException.class);
    Status status = Status.NOT_ACCEPTABLE;
    if (em != null && AnyExceptionExceptionMapper.class.isInstance(em))
      status = Status.OK;
    // This serverError() is to get ResponseBuilder with status != OK
    return Response.serverError().status(status).build();
  }

  @GET
  @Path("isRegisteredIOExceptionMapper")
  public Response isRegisteredIOExceptionExceptionMapper() {
    ExceptionMapper<IOException> em = providers
        .getExceptionMapper(IOException.class);
    return em.toResponse(new IOException());
  }

  @GET
  @Path("isRegisteredMessageReaderWildCard")
  public Response isRegisteredEntityMessageReaderWildcard() {
    MessageBodyReader<ReadableWritableEntity> reader;
    reader = providers.getMessageBodyReader(ReadableWritableEntity.class, null,
        getArgumentAnnotations("readEntityFromBody"), MediaType.WILDCARD_TYPE);
    Status status = reader == null ? Status.NOT_ACCEPTABLE : Status.OK;
    return Response.status(status).build();
  }

  @GET
  @Path("isRegisteredMessageReaderXml")
  public Response isRegisteredEntityMessageReaderXml() {
    MessageBodyReader<ReadableWritableEntity> reader;
    reader = providers.getMessageBodyReader(ReadableWritableEntity.class, null,
        getArgumentAnnotations("readEntityFromBody"), MediaType.TEXT_XML_TYPE);
    Status status = reader == null ? Status.NOT_ACCEPTABLE : Status.OK;
    return Response.status(status).build();
  }

  @GET
  @Path("isRegisteredWriterWildcard")
  public Response isRegisteredWriterWildCard() {
    MessageBodyWriter<ReadableWritableEntity> writer;
    writer = providers.getMessageBodyWriter(ReadableWritableEntity.class, null,
        getMethodAnnotations("writeBodyEntityUsingWriter"),
        MediaType.WILDCARD_TYPE);
    Status status = writer == null ? Status.NOT_ACCEPTABLE : Status.OK;
    return Response.status(status).build();
  }

  @GET
  @Path("isRegisteredMessageWriterXml")
  public Response isRegisteredWriterXml() {
    MessageBodyWriter<ReadableWritableEntity> entity;
    entity = providers.getMessageBodyWriter(ReadableWritableEntity.class, null,
        getMethodAnnotations("writeBodyEntityUsingWriter"),
        MediaType.TEXT_XML_TYPE);
    Status status = entity == null ? Status.NOT_ACCEPTABLE : Status.OK;
    return Response.status(status).build();
  }

  @GET
  @Produces(MediaType.TEXT_XML)
  @Path("writeBodyEntityUsingWriter")
  public Response writeBodyEntityUsingWriter() {
    ReadableWritableEntity rwe = new ReadableWritableEntity(
        EnumProvider.JAXRS.name());
    return Response.ok(rwe).build();
  }

  @GET
  @Produces(MediaType.TEXT_XML)
  @Path("writeHeaderEntityUsingWriter")
  public Response writeHeaderEntityUsingWriter() {
    ReadableWritableEntity rwe = new ReadableWritableEntity(
        EnumProvider.JAXRS.name());
    return Response.ok(rwe).build();
  }

  @GET
  @Produces(MediaType.TEXT_XML)
  @Path("writeIOExceptionWithoutWriter")
  public Response writeIOExceptionWithoutWriter() throws IOException {
    throw new IOException("123 exception");
  }

  @GET
  @Produces(MediaType.TEXT_XML)
  @Path("writeIOExceptionUsingWriter")
  public Response writeIOExceptionUsingWriter() throws IOException {
    ReadableWritableEntity rwe = new ReadableWritableEntity("");
    return Response.ok(rwe).build();
  }

  @POST
  @Consumes(MediaType.TEXT_XML)
  @Path("readEntityFromHeader")
  public Response readEntityFromHeader(
      @EntityAnnotation("Header") ReadableWritableEntity entity) {
    Status status = Status.NO_CONTENT;
    if (entity != null) {
      boolean b = entity.toString().equals(EnumProvider.JAXRS.name());
      status = b ? Status.OK : Status.NOT_ACCEPTABLE;
    }
    return Response.status(status).build();
  }

  @POST
  @Consumes(MediaType.TEXT_XML)
  @Path("readEntityFromBody")
  public Response readEntityFromBody(
      @EntityAnnotation("Body") ReadableWritableEntity entity) {
    Status status = Status.NO_CONTENT;
    if (entity != null) {
      boolean b = entity.toString().equals(EnumProvider.JAXRS.name());
      status = b ? Status.OK : Status.NOT_ACCEPTABLE;
    }
    return Response.status(status).build();
  }

  @POST
  @Consumes(MediaType.TEXT_XML)
  @Path("readEntityIOException")
  public Response readEntityIOException(
      @EntityAnnotation("IOException") ReadableWritableEntity entity) {
    return Response.ok().build();
  }

  @POST
  @Consumes(MediaType.TEXT_XML)
  @Path("readEntityWebException")
  public Response readEntityWebException(
      @EntityAnnotation("WebException") ReadableWritableEntity entity) {
    return Response.ok().build();
  }

  private Annotation[] getMethodAnnotations(String methodName) {
    Method[] methods = getClass().getMethods();
    for (Method method : methods)
      if (method.getName().equals(methodName))
        return method.getAnnotations();
    return null;
  }

  private Annotation[] getArgumentAnnotations(String methodName) {
    Method[] methods = getClass().getMethods();
    for (Method method : methods)
      if (method.getName().equals(methodName))
        return method.getParameterAnnotations()[0];
    return null;
  }

}
