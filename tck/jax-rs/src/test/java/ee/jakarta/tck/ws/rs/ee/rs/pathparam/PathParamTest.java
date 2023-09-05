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

package ee.jakarta.tck.ws.rs.ee.rs.pathparam;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingExceptionGivenByName;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingWebApplicationException;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithConstructor;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithFromString;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithValueOf;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;

@Path(value = "/PathParamTest")
public class PathParamTest {
  @Produces(MediaType.TEXT_HTML)
  @GET
  @Path("/{id}")
  public String single(@PathParam("id") String id) {
    return "single=" + id;
  }

  @Produces(MediaType.TEXT_HTML)
  @GET
  @Path("/{id}/{id1}")
  public String two(@PathParam("id") String id,
      @PathParam("id1") PathSegment id1) {
    return "double=" + id + id1.getPath();
  }

  @GET
  @Path("/{id}/{id1}/{id2}")
  public String triple(@PathParam("id") int id,
      @PathParam("id1") PathSegment id1, @PathParam("id2") float id2) {
    return "triple=" + id + id1.getPath() + id2;
  }

  @GET
  @Path("/{id}/{id1}/{id2}/{id3}")
  public String quard(@PathParam("id") double id, @PathParam("id1") boolean id1,
      @PathParam("id2") byte id2, @PathParam("id3") PathSegment id3) {
    return "quard=" + id + id1 + id2 + id3.getPath();
  }

  @GET
  @Path("/{id}/{id1}/{id2}/{id3}/{id4}")
  public String penta(@PathParam("id") long id, @PathParam("id1") String id1,
      @PathParam("id2") short id2, @PathParam("id3") boolean id3,
      @PathParam("id4") PathSegment id4) {
    return "penta=" + id + id1 + id2 + id3 + id4.getPath();
  }

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/{id}/{id}/{id}/{id}/{id}/{id}")
  public String list(@PathParam("id") List<String> id) {
    StringBuffer sb = new StringBuffer();
    sb.append("list=");
    for (String tmp : id) {
      sb.append(tmp);
    }
    return sb.toString();
  }

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/matrix/{id}")
  public String matrixparamtest(@PathParam("id") PathSegment id) {
    StringBuffer sb = new StringBuffer();
    sb.append("matrix=");

    sb.append("/" + id.getPath());
    MultivaluedMap<String, String> matrix = id.getMatrixParameters();
    Set<String> keys = matrix.keySet();
    for (Object key : keys) {
      sb.append(";" + key.toString() + "=" + matrix.getFirst(key.toString()));

    }
    return sb.toString();
  }

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/ParamEntityWithConstructor/{id}")
  public String paramEntityWithConstructorTest(
      @DefaultValue("PathParamTest") @PathParam("id") ParamEntityWithConstructor paramEntityWithConstructor) {
    return paramEntityWithConstructor.getValue();
  }

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/ParamEntityWithFromString/{id}")
  public String paramEntityWithFromStringTest(
      @Encoded @DefaultValue("PathParamTest") @PathParam("id") ParamEntityWithFromString paramEntityWithFromString) {
    return paramEntityWithFromString.getValue();
  }

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/ParamEntityWithValueOf/{id}")
  public String paramEntityWithValueOfTest(
      @DefaultValue("PathParamTest") @PathParam("id") ParamEntityWithValueOf paramEntityWithValueOf) {
    return paramEntityWithValueOf.getValue();
  }

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/SetParamEntityWithFromString/{id}")
  public String setParamEntityWithFromStringTest(
      @DefaultValue("PathParamTest") @PathParam("id") Set<ParamEntityWithFromString> setParamEntityWithFromString) {
    return setParamEntityWithFromString.iterator().next().getValue();
  }

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/SortedSetParamEntityWithFromString/{id}")
  public String sortedSetParamEntityWithFromStringTest(
      @DefaultValue("PathParamTest") @PathParam("id") SortedSet<ParamEntityWithFromString> sortedSetParamEntityWithFromString) {
    return sortedSetParamEntityWithFromString.iterator().next().getValue();
  }

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/ListParamEntityWithFromString/{id}")
  public String listParamEntityWithFromStringTest(
      @DefaultValue("PathParamTest") @PathParam("id") List<ParamEntityWithFromString> listParamEntityWithFromString) {
    return listParamEntityWithFromString.iterator().next().getValue();
  }

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/ParamEntityThrowingWebApplicationException/{id}")
  public String paramEntityThrowingWebApplicationException(
      @PathParam("id") ParamEntityThrowingWebApplicationException paramEntityThrowingWebApplicationException) {
    return "";
  }

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/ParamEntityThrowingExceptionGivenByName/{id}")
  public String paramEntityThrowingExceptionGivenByName(
      @PathParam("id") ParamEntityThrowingExceptionGivenByName paramEntityThrowingExceptionGivenByName) {
    return "";
  }

  @DefaultValue("PathParamTest")
  @PathParam("FieldParamEntityWithConstructor")
  ParamEntityWithConstructor fieldParamEntityWithConstructor;

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/FieldParamEntityWithConstructor/{FieldParamEntityWithConstructor}")
  public String fieldEntityWithConstructorTest() {
    return fieldParamEntityWithConstructor.getValue();
  }

  @DefaultValue("PathParamTest")
  @PathParam("FieldParamEntityWithFromString")
  ParamEntityWithFromString fieldParamEntityWithFromString;

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/FieldParamEntityWithFromString/{FieldParamEntityWithFromString}")
  public String fieldEntityWithFromStringTest() {
    return fieldParamEntityWithFromString.getValue();
  }

  @DefaultValue("PathParamTest")
  @PathParam("FieldParamEntityWithValueOf")
  ParamEntityWithValueOf fieldParamEntityWithValueOf;

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/FieldParamEntityWithValueOf/{FieldParamEntityWithValueOf}")
  public String fieldEntityWithValueOfTest() {
    return fieldParamEntityWithValueOf.getValue();
  }

  @DefaultValue("PathParamTest")
  @PathParam("FieldSetParamEntityWithFromString")
  Set<ParamEntityWithFromString> fieldSetParamEntityWithFromString;

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/FieldSetParamEntityWithFromString/{FieldSetParamEntityWithFromString}")
  public String fieldSetParamEntityWithFromStringTest() {
    return fieldSetParamEntityWithFromString.iterator().next().getValue();
  }

  @DefaultValue("PathParamTest")
  @PathParam("FieldSortedSetParamEntityWithFromString")
  SortedSet<ParamEntityWithFromString> fieldSortedSetParamEntityWithFromString;

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/FieldSortedSetParamEntityWithFromString/{fieldSortedSetParamEntityWithFromString}")
  public String fieldSortedSetParamEntityWithFromStringTest() {
    return fieldSortedSetParamEntityWithFromString.iterator().next().getValue();
  }

  @DefaultValue("PathParamTest")
  @PathParam("FieldListParamEntityWithFromString")
  List<ParamEntityWithFromString> fieldListParamEntityWithFromString;

  @Produces(MediaType.TEXT_PLAIN)
  @GET
  @Path("/FieldListParamEntityWithFromString/{FieldListParamEntityWithFromString}")
  public String fieldListParamEntityWithFromStringTest() {
    return fieldListParamEntityWithFromString.iterator().next().getValue();
  }
}
