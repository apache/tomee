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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import ee.jakarta.tck.ws.rs.common.provider.StringBeanWithAnnotation;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;

import jakarta.annotation.Priority;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Link.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.Status.Family;
import jakarta.ws.rs.core.Response.StatusType;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(500)
// reverse order
public class ResponseFilter extends TemplateFilter {
  public static final String COOKIENAME = "CookieName";

  public static final String ENTITY = "ResponseFilterEntity";

  public static final String NULL = "NULL";

  public static final String RELATION = "relation";

  public void getAllowedMethods() {
    Set<String> set = responseContext.getAllowedMethods();
    setEntity(collectionToString(set));
    for (String s : set)
      if (!s.toUpperCase().equals(s)) {
        setEntity(s + " is not uppercase");
        break;
      }
  }

  public void getCookies() {
    Map<String, NewCookie> cookies = responseContext.getCookies();
    setEntity(collectionToString(cookies.keySet()));
  }

  public void getCookiesIsReadOnly() {
    NewCookie cookie = new NewCookie(COOKIENAME, COOKIENAME);
    Map<String, NewCookie> cookies = responseContext.getCookies();
    if (assertTrue(!cookies.containsKey(COOKIENAME), COOKIENAME,
        "is already present"))
      return;
    try {
      cookies.put(COOKIENAME, cookie);
    } catch (Exception e) {
      // Not mandatory, but possible as cookies is read-only
    }
    cookies = responseContext.getCookies();
    if (assertTrue(!cookies.containsKey(COOKIENAME),
        "#getCookies is Not readOnly"))
      return;
    setEntity("getCookies is read-only as expected");
  }

  public void getDate() {
    Date date = responseContext.getDate();
    long milis = date == null ? 0 : date.getTime();
    setEntity(String.valueOf(milis));
  }

  public void getEntity() {
    byte[] entity = (byte[]) responseContext.getEntity();
    if (entity != null)
      setEntity(new String(entity) + new String(entity));
    else
      setEntity(NULL);
  }

  public void getEntityAnnotations() {
    Annotation[] annotations = responseContext.getEntityAnnotations();
    if (annotations != null && annotations.length != 0) {
      String[] names = new String[annotations.length];
      for (int i = 0; i != annotations.length; i++)
        names[i] = annotations[i].annotationType().getName();
      setEntity(collectionToString(Arrays.asList(names)));
    } else
      setEntity(NULL);
  }

  public void getEntityAnnotationsOnEntity() {
    Annotation[] annotations = responseContext.getEntityAnnotations();
    String entity = responseContext.hasEntity()
        ? ((StringBeanWithAnnotation) responseContext.getEntity()).get()
        : NULL;
    String annotationCount = annotations == null ? NULL
        : String.valueOf(annotations.length);
    setEntity(entity + " " + annotationCount);
  }

  public void getEntityClass() {
    Class<?> clazz = responseContext.getEntityClass();
    setEntity(clazz.getName());
  }

  public void getEntityStream() throws IOException {
    OutputStream stream = responseContext.getEntityStream();
    if (stream == null)
      setEntity(NULL);
    else
      setEntity(ENTITY);
  }

  public void getEntityTag() {
    EntityTag tag = responseContext.getEntityTag();
    setEntity(tag == null ? NULL : tag.getValue());
  }

  public void getEntityType() {
    Type type = responseContext.getEntityType();
    String name = NULL;
    if (type instanceof Class)
      name = ((Class<?>) type).getName();
    else if (type != null)
      name = type.getClass().getName();
    setEntity(name);
  }

  public void getHeaders() {
    MultivaluedMap<String, Object> headers = responseContext.getHeaders();
    setEntity(collectionToString(headers.keySet()));
  }

  public void getHeadersIsMutable() {
    MultivaluedMap<String, Object> headers = responseContext.getHeaders();
    if (assertTrue(!headers.containsKey(HEADER), HEADER,
        "header is alredy in header map"))
      return;
    headers.add(HEADER, HEADER);
    headers = responseContext.getHeaders();
    if (assertTrue(headers.containsKey(HEADER), HEADER,
        "header is not in header map"))
      return;
    // second filter run
  }

  public void getHeaderStringOperation() {
    String header = responseContext.getHeaderString(OPERATION);
    setEntity(header);
  }

  public void getHeaderStringHeader() {
    String header = responseContext.getHeaderString(HEADER);
    setEntity(header == null ? NULL : header);
  }

  public void getLanguage() {
    Locale locale = responseContext.getLanguage();
    setEntity(locale == null ? NULL : locale.toString());
  }

  public void getLastModified() {
    Date date = responseContext.getLastModified();
    setEntity(date == null ? NULL : String.valueOf(date.getTime()));
  }

  public void getLength() {
    int contentLen = responseContext.getLength();
    String entity = (String) responseContext.getEntity();
    setEntity(replaceStart(entity, contentLen));
  }

  public static String replaceStart(String where, int by) {
    StringBuilder sb = new StringBuilder().append(by);
    if (where != null)
      sb.append(where.substring(String.valueOf(by).length()));
    return sb.toString();
  }

  public void getLink() {
    Link link = responseContext.getLink(RELATION);
    setLinkForGetLink(link);
  }

  public void getLinkBuilder() {
    Builder builder = responseContext.getLinkBuilder(RELATION);
    if (builder != null) {
      Link link = builder.build();
      setLinkForGetLink(link);
    } else
      setEntity(NULL);
  }

  private void setLinkForGetLink(Link link) {
    String entity = NULL;
    if (link != null && link.getUri() != null)
      entity = link.getUri().toASCIIString();
    setEntity(entity);
  }

  public void getLinks() {
    Set<Link> set = responseContext.getLinks();
    if (assertTrue(set != null, "#getLinks shall never be null"))
      return;
    if (set.size() == 0) {
      setEntity(NULL);
      return;
    }
    Set<String> strings = new HashSet<String>();
    for (Link link : set)
      strings.add(link.toString());
    setEntity(JaxrsUtil.iterableToString(";", strings));
  }

  public void getLocation() {
    URI uri = responseContext.getLocation();
    setEntity(uri == null ? NULL : uri.toASCIIString());
  }

  public void getMediaType() {
    MediaType type = responseContext.getMediaType();
    setEntity(type == null ? NULL : type.toString());
  }

  public void getStatus() {
    int status = responseContext.getStatus();
    responseContext.setStatus(Status.OK.getStatusCode());
    setEntity(String.valueOf(status));
  }

  public void getStatusNotSet() {
    getStatus();
  }

  public void getStatusInfo() {
    StatusType type = responseContext.getStatusInfo();
    if (type == null) {
      setEntity(NULL);
      responseContext.setStatus(Status.OK.getStatusCode());
      return;
    }
    int status = type.getStatusCode();
    responseContext.setStatus(Status.OK.getStatusCode());
    setEntity(String.valueOf(status));
  }

  public void getStatusInfoNotSet() {
    getStatusInfo();
  }

  public void getStringHeaders() {
    MultivaluedMap<String, String> map = responseContext.getStringHeaders();
    List<String> list = map.get(HEADER);
    setEntity(list == null ? NULL
        : list.size() == 1 ? list.iterator().next() : collectionToString(list));
  }

  public void hasEntity() {
    boolean has = responseContext.hasEntity();
    setEntity(String.valueOf(has));
  }

  public void hasLink() {
    boolean has = responseContext.hasLink(RELATION);
    setEntity(String.valueOf(has));
  }

  public void setEntity() {
    Annotation[] annotations = getClass().getAnnotations();
    MediaType type = MediaType.APPLICATION_SVG_XML_TYPE;
    responseContext.setEntity(ENTITY, annotations, type);
  }

  public void setEntityStream() {
    final OutputStream stream = responseContext.getEntityStream();
    OutputStream byteStream = new OutputStream() {
      @Override
      public void write(byte[] b) throws IOException {
        stream.write(ENTITY.getBytes());
        stream.write(b);
      }

      @Override
      public void write(int b) throws IOException {
        write(intToByteArray(b));
      }

      @Override
      public synchronized void write(byte[] b, int off, int len)
          throws IOException {
        write(b);
      }

      public final byte[] intToByteArray(int value) {
        return new byte[] { (byte) (value & 0xff) };
      }
    };
    responseContext.setEntityStream(byteStream);
    StringBuilder sb = new StringBuilder(ENTITY.length() + 4);
    for (int i = 0; i < ENTITY.length() + 3; i += 2)
      sb.append("OK");
    setEntity(sb.toString());
  }

  public void setStatus() {
    String entity = (String) responseContext.getEntity();
    int status = Integer.parseInt(entity);
    responseContext.setStatus(status);
    resetStatusEntity(status);
  }

  public void setStatusInfo() {
    String entity = (String) responseContext.getEntity();
    final int status = Integer.parseInt(entity);
    StatusType type = new StatusType() {

      @Override
      public int getStatusCode() {
        return status;
      }

      @Override
      public String getReasonPhrase() {
        return null;
      }

      @Override
      public Family getFamily() {
        return Family.familyOf(status);
      }
    };
    responseContext.setStatusInfo(type);
    resetStatusEntity(status);
  }

  private void resetStatusEntity(int status) {
    switch (status) {
    case 204:
    case 205:
      responseContext.setEntity(null);
      break;
    }
  }

  public void setStringBeanRuntime() {
    // pass
  }

  public void setOriginalRuntime() {
    // pass
  }
}
