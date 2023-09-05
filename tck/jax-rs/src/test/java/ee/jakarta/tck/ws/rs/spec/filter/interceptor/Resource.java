/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.spec.filter.interceptor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import ee.jakarta.tck.ws.rs.common.impl.StringDataSource;
import ee.jakarta.tck.ws.rs.common.impl.StringSource;
import ee.jakarta.tck.ws.rs.common.impl.StringStreamingOutput;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;

import jakarta.activation.DataSource;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.xml.bind.JAXBElement;

@Path("resource")
public class Resource {

  public static final String HEADERNAME = "FILTER_HEADER";

  public static final String DIRECTION = "FROM_RESOURCE";

  public static final String getName() {
    // make this long enough to let entity provider getSize()
    // be enough to let our interceptor name fit in
    return "<resource>" + Resource.class.getName() + "</resource>";
  }

  @GET
  @Path("getbytearray")
  public Response getByteArray() {
    return buildResponse(getName().getBytes());
  }

  @POST
  @Path("postbytearray")
  public Response postByteArray(byte[] array) {
    return buildResponse(new String(array));
  }

  @GET
  @Path("getstring")
  public Response getString() {
    return buildResponse(getName());
  }

  @POST
  @Path("poststring")
  public Response postString(String string) {
    return buildResponse(string);
  }

  @GET
  @Path("getinputstream")
  public Response getInputStream() {
    return buildResponse(new ByteArrayInputStream(getName().getBytes()));
  }

  @POST
  @Path("postinputstream")
  public Response postInputStream(InputStream stream) throws IOException {
    String text = JaxrsUtil.readFromStream(stream);
    stream.close();
    return buildResponse(text);
  }

  @GET
  @Path("getreader")
  public Response getReader() {
    InputStream stream = (InputStream) getInputStream().getEntity();
    InputStreamReader reader = new InputStreamReader(stream);
    return buildResponse(reader);
  }

  @POST
  @Path("postreader")
  public Response postReader(Reader reader) throws IOException {
    String text = JaxrsUtil.readFromReader(reader);
    reader.close();
    return buildResponse(text);
  }

  @GET
  @Path("getfile")
  public Response getFile() throws IOException {
    File file = File.createTempFile("filter", "tmp");
    FileWriter writer = new FileWriter(file);
    writer.append(getName());
    writer.close();
    return buildResponse(file);
  }

  @POST
  @Path("postfile")
  public Response postFile(File file) throws IOException {
    String text = JaxrsUtil.readFromFile(file);
    return buildResponse(text);
  }

  @GET
  @Path("getdatasource")
  public Response getDataSource() {
    StringDataSource source = new StringDataSource(getName(),
        MediaType.WILDCARD_TYPE);
    return buildResponse((DataSource) source);
  }

  @POST
  @Path("postdatasource")
  public Response postDataSource(DataSource source) throws IOException {
    InputStream stream = source.getInputStream();
    String text = JaxrsUtil.readFromStream(stream);
    return buildResponse(text);
  }

  @GET
  @Path("getsource")
  public Response getSource() {
    StringSource source = new StringSource(getName());
    return buildResponse(source, MediaType.TEXT_XML_TYPE);
  }

  @POST
  @Consumes(MediaType.TEXT_XML)
  @Path("postsource")
  public Response postSource(Source source) {
    String text = source.getSystemId();
    return buildResponse(text);
  }

  @GET
  @Path("getjaxb")
  public Response getJaxbElement() {
    JAXBElement<String> element = new JAXBElement<String>(new QName("resource"),
        String.class, getClass().getName());
    GenericEntity<JAXBElement<String>> generic = new GenericEntity<JAXBElement<String>>(
        element) {
    };
    return buildResponse(generic, MediaType.TEXT_XML_TYPE);
  }

  @POST
  @Path("postjaxb")
  @Consumes(MediaType.TEXT_XML)
  public Response postJaxbElement(JAXBElement<String> element) {
    String text = element.getValue();
    return buildResponse(text, MediaType.TEXT_XML_TYPE);
  }

  @GET
  @Path("getmap")
  public Response getMultivaluedMap() {
    MultivaluedMap<String, String> map = new MultivaluedHashMap<String, String>();
    map.add(getName(), getName());
    GenericEntity<MultivaluedMap<String, String>> entity = new GenericEntity<MultivaluedMap<String, String>>(
        map) {
    };
    return buildResponse(entity, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
  }

  @POST
  @Path("postmap")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response postMultivaluedMap(MultivaluedMap<String, String> map) {
    String key = map.entrySet().iterator().next().getKey();
    String value = map.getFirst(key);
    return buildResponse(value);
  }

  @GET
  @Path("getstreamingoutput")
  public Response getStreamingOutput() {
    StringStreamingOutput output = new StringStreamingOutput(getName());
    return buildResponse(output);
  }

  @GET
  @Path("getboolean")
  public Response getBoolean() {
    Boolean b = false;
    return buildResponse(b, MediaType.TEXT_PLAIN_TYPE);
  }

  @POST
  @Path("postboolean")
  public Response postBoolean(Boolean b) {
    String s = String.valueOf(b);
    return buildResponse(s);
  }

  @GET
  @Path("getchar")
  public Response getChar() {
    Character c = 'R';
    return buildResponse(c, MediaType.TEXT_PLAIN_TYPE);
  }

  @POST
  @Path("postchar")
  public Response postChar(char c) {
    String text = String.valueOf(c);
    return buildResponse(text);
  }

  @GET
  @Path("getnumber")
  public Response getNumber() {
    Number n = Integer.valueOf(getName().length());
    return buildResponse(n, MediaType.TEXT_PLAIN_TYPE);
  }

  @POST
  @Path("postnumber")
  public Response postNumber(Number n) {
    String s = String.valueOf(n.intValue());
    return buildResponse(s);
  }

  @GET
  @Path("getstringbean")
  public Response getStringBean() {
    StringBean bean = new StringBean(getName());
    return buildResponse(bean);
  }

  @POST
  @Path("poststringbean")
  public Response postStringBean(StringBean bean) {
    String text = bean.get();
    return buildResponse(text);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Send header that would have the power to enable filter / interceptor
  // The header is passed from client request
  @Context
  private HttpHeaders headers;

  private Response buildResponse(Object content) {
    return buildResponse(content, MediaType.WILDCARD_TYPE);
  }

  private Response buildResponse(Object content, MediaType type) {
    List<String> list = headers.getRequestHeader(HEADERNAME);
    String name = null;
    if (list != null && list.size() != 0)
      name = list.iterator().next();
    ResponseBuilder builder = Response.ok(content, type).type(type);
    if (name != null)
      builder.header(HEADERNAME, name + DIRECTION);
    return builder.build();
  }

}
