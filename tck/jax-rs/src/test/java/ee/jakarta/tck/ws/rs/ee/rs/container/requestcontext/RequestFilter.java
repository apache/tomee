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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import ee.jakarta.tck.ws.rs.common.impl.SecurityContextImpl;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

/**
 * The methods are called here by reflection from the superclass
 */
@Provider
@Priority(100)
@PreMatching
public class RequestFilter extends TemplateFilter {

  public static final String PRINCIPAL = "PrincipalName";

  public static final String SETENTITYSTREAMENTITY = "SetEntityStreamEntity";

  public static final String URI = "http://xx.yy:888/base/resource/sub";

  public void abortWith() {
    abortWithEntity(null);
  }

  public void getAcceptableLanguages() {
    List<Locale> langs = requestContext.getAcceptableLanguages();
    abortWithEntity(collectionToString(langs));
  }

  public void getAcceptableLanguagesIsReadOnly() {
    List<Locale> langs = requestContext.getAcceptableLanguages();
    if (assertTrue(!langs.contains(Locale.CANADA_FRENCH), Locale.CANADA_FRENCH,
        "already included"))
      return;
    try {
      langs.add(Locale.CANADA_FRENCH);
    } catch (Exception e) {
      // expected, but optional
    }
    langs = requestContext.getAcceptableLanguages();
    if (assertTrue(!langs.contains(Locale.CANADA_FRENCH), Locale.CANADA_FRENCH,
        "included, the list is not read-only"))
      return;
    abortWithEntity(collectionToString(langs));
  }

  public void getAcceptableMediaTypes() {
    List<MediaType> types = requestContext.getAcceptableMediaTypes();
    abortWithEntity(collectionToString(types));
  }

  public void getAcceptableMediaTypesIsReadOnly() {
    List<MediaType> types = requestContext.getAcceptableMediaTypes();
    if (assertTrue(!types.contains(MediaType.APPLICATION_JSON_TYPE),
        MediaType.APPLICATION_JSON, "already included"))
      return;
    try {
      types.add(MediaType.APPLICATION_JSON_TYPE);
    } catch (Exception e) {
      // expected, but optional
    }
    types = requestContext.getAcceptableMediaTypes();
    if (assertTrue(!types.contains(MediaType.APPLICATION_JSON_TYPE),
        MediaType.APPLICATION_JSON, "included, the list is not read-only"))
      return;
    abortWithEntity(collectionToString(types));
  }

  public void getCookies() {
    Map<String, Cookie> cookies = requestContext.getCookies();
    Collection<Cookie> values = cookies.values();
    abortWithEntity(collectionToString(values));
  }

  public void getCookiesIsReadOnly() {
    Map<String, Cookie> cookies = requestContext.getCookies();
    int length = cookies.size();
    try {
      cookies.put("key", new Cookie("coo", "kie"));
    } catch (Exception e) {
      // expected, but optional
    }
    cookies = requestContext.getCookies();
    if (assertTrue(cookies.size() == length, "Cookie was added present"))
      return;
    abortWithEntity("#getCookies is read-only as expected");
  }

  public void getDate() {
    Date date = requestContext.getDate();
    abortWithEntity(String.valueOf(date == null ? "NULL" : date.getTime()));
  }

  public void getEntityStream() throws IOException {
    InputStream stream = requestContext.getEntityStream();
    InputStreamReader isr = new InputStreamReader(stream);
    BufferedReader br = new BufferedReader(isr);
    String entity = br.readLine();
    br.close();
    abortWithEntity(entity);
  }

  public void getHeaders() {
    MultivaluedMap<String, String> headers = requestContext.getHeaders();
    StringBuilder sb = new StringBuilder();
    for (Entry<String, List<String>> set : headers.entrySet()) {
      String first = headers.getFirst(set.getKey());
      sb.append(set.getKey()).append(":").append(first).append(";");
    }
    abortWithEntity(sb.toString());
  }

  public void getHeadersIsMutable() {
    String key = "KEY";
    MultivaluedMap<String, String> headers = requestContext.getHeaders();
    if (assertTrue(!headers.containsKey(key), "Key", key, "is already present"))
      return;
    headers.add(key, "VALUE");
    assertTrue(headers.containsKey(key), "Key", key, "not found");
  }

  public void getHeaderString2() {
    String string = requestContext.getHeaderString(OPERATION);
    abortWithEntity(string);
  }

  public void getLanguage() {
    Locale lang = requestContext.getLanguage();
    String entity = lang == null ? "NULL"
        : collectionToString(Collections.singleton(lang));
    abortWithEntity(entity);
  }

  public void getLength() {
    int len = requestContext.getLength();
    abortWithEntity(String.valueOf(len));
  }

  public void getMediaType() {
    MediaType media = requestContext.getMediaType();
    abortWithEntity(media == null ? "NULL" : media.toString());
  }

  public void getMethod() {
    String method = requestContext.getMethod();
    abortWithEntity(method);
  }

  public void getProperty() {
    Object o = requestContext.getProperty(PROPERTYNAME);
    abortWithEntity(o == null ? "NULL" : o.toString());
  }

  public void getPropertyNames() {
    int length = requestContext.getPropertyNames().size();
    for (int i = 0; i != 5; i++)
      requestContext.setProperty(PROPERTYNAME + i, PROPERTYNAME);
    Collection<String> names = requestContext.getPropertyNames();
    assertTrue(names.size() == 5 + length,
        "#getPropertyNames has unexpected number of elements", names.size(),
        "before any addition it was", length);
  }

  public void getPropertyNamesIsReadOnly() {
    Collection<String> names = requestContext.getPropertyNames();
    int length = names.size();
    for (int i = 0; i != 5; i++)
      try {
        names.add(PROPERTYNAME + i);
      } catch (Exception e) {
        // Exception here is possible
      }
    names = requestContext.getPropertyNames();
    if (assertTrue(names.size() == length,
        "#getPropertyNames has unexpected elements", names))
      return;
    abortWithEntity("0");
  }

  public void getRequest() {
    Request request = requestContext.getRequest();
    String method = request.getMethod();
    abortWithEntity(method);
  }

  public void getSecurityContext() {
    SecurityContext secCtx = requestContext.getSecurityContext();
    Principal principal = secCtx.getUserPrincipal();
    if (assertTrue(principal == null, "principal is not null"))
      return;
    abortWithEntity("NULL");
  }

  public void getUriInfo() {
    UriInfo info = requestContext.getUriInfo();
    abortWithEntity(info.getAbsolutePath().toASCIIString());
  }

  public void hasEntity() {
    boolean has = requestContext.hasEntity();
    abortWithEntity(String.valueOf(has).toLowerCase());
  }

  public void removeProperty() {
    setProperty();
    requestContext.removeProperty(PROPERTYNAME);
    getProperty();
  }

  public void setEntityStream() throws IOException {
    ByteArrayInputStream stream = new ByteArrayInputStream(
        SETENTITYSTREAMENTITY.getBytes());
    requestContext.setEntityStream(stream);
    getEntityStream();
  }

  public void setMethod() {
    requestContext.setMethod("OPTIONS");
    // really pass to the server to check the changes, do NOT abortWith
  }

  public void setProperty() {
    requestContext.setProperty(PROPERTYNAME, PROPERTYNAME + PROPERTYNAME);
    assertTrue(requestContext.getProperty(PROPERTYNAME) != null, "property",
        PROPERTYNAME, "not found");
  }

  public void setPropertyNull() {
    setProperty();
    requestContext.setProperty(PROPERTYNAME, null);
    getProperty();
  }

  public void setPropertyContext() {
    requestContext.setProperty(PROPERTYNAME, PROPERTYNAME);
  }

  public void setRequestUri1() throws IOException {
    URI uri;
    try {
      UriInfo info = requestContext.getUriInfo();
      String path = new StringBuilder().append(info.getAbsolutePath())
          .append("uri").toString();
      uri = new URI(path);
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
    requestContext.setRequestUri(uri);
    // pass to a resource to see whether the uri change has been reflected
  }

  public void setRequestUri2() throws IOException {
    URI base;
    URI suffix;
    try {
      base = new URI("http://localhost:888/otherbase");
      suffix = new URI(URI);
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
    requestContext.setRequestUri(base, suffix);
    getUriInfo();
  }

  public void setSecurityContext() throws IOException {
    SecurityContext secCtx = new SecurityContextImpl(PRINCIPAL, false, false,
        null);
    requestContext.setSecurityContext(secCtx);
  }

}
