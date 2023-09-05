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

package ee.jakarta.tck.ws.rs.ee.rs.core.headers;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

@Path(value = "/HeadersTest")
public class HttpHeadersTest {

  @Context
  HttpHeaders hs;

  StringBuffer sb;

  @GET
  @Path("/headers")
  public String headersGet() {
    sb = new StringBuffer();
    List<String> myHeaders = Arrays.asList("Accept", "Content-Type");

    try {
      MultivaluedMap<String, String> rqhdrs = hs.getRequestHeaders();
      Set<String> keys = rqhdrs.keySet();
      sb.append("getRequestHeaders= ");
      for (String header : myHeaders) {
        if (keys.contains(header)) {
          sb.append(
              "Found " + header + ": " + hs.getRequestHeader(header) + "; ");
        }
      }
    } catch (Throwable ex) {
      sb.append("Unexpected exception thrown in getRequestHeaders: "
          + ex.getMessage());
      ex.printStackTrace();
    }
    return sb.toString();
  }

  @GET
  @Path("/acl")
  public String aclGet() {
    sb = new StringBuffer();
    try {
      sb.append("Accept-Language");

      List<Locale> acl = hs.getAcceptableLanguages();
      sb.append("getLanguage= ");
      for (Locale tmp : acl) {
        sb.append(langToString(tmp)).append("; ");
      }
    } catch (Throwable ex) {
      sb.append("Unexpected exception thrown in getAcceptableLanguages: "
          + ex.getMessage());
      ex.printStackTrace();
    }
    return sb.toString();
  }

  @GET
  @Path("/amt")
  public String amtGet() {
    sb = new StringBuffer();
    try {
      sb.append("getAcceptableMediaTypes");
      List<MediaType> acmts = hs.getAcceptableMediaTypes();

      for (MediaType mt : acmts) {
        sb.append(mt.getType());
        sb.append("/");
        sb.append(mt.getSubtype());
      }
    } catch (Throwable ex) {
      sb.append("Unexpected exception thrown: " + ex.getMessage());
      ex.printStackTrace();
    }
    return sb.toString();
  }

  @GET
  @Path("/mt")
  public String mtGet() {
    sb = new StringBuffer();

    try {
      sb.append("getMediaType");
      MediaType mt = hs.getMediaType();
      if (mt != null) {
        sb.append(mt.getType());
        sb.append("/");
        sb.append(mt.getSubtype());
        sb.append(" ");

        java.util.Map<java.lang.String, java.lang.String> pmap = mt
            .getParameters();

        sb.append("MediaType size=" + pmap.size());

        Iterator<Entry<String, String>> k = pmap.entrySet().iterator();
        while (k.hasNext()) {
          Entry<String, String> next = k.next();
          String key = next.getKey();
          sb.append("Key " + key + "; Value " + next.getValue());
        }

        sb.append(mt.toString());

        sb.append("MediaType= " + mt.toString() + "; ");
      } else {
        sb.append("MediaType= null; ");
      }
    } catch (Throwable ex) {
      sb.append("Unexpected exception thrown: " + ex.getMessage());
      ex.printStackTrace();
    }
    return sb.toString();
  }

  @GET
  @Path("/cookie")
  public String cookieGet() {
    sb = new StringBuffer();

    try {
      sb.append("getCookies= ");
      Map<String, Cookie> cookies = hs.getCookies();
      sb.append("Cookie Size=" + cookies.size());

      for (Entry<String, Cookie> entry : cookies.entrySet()) {
        sb.append("key=" + entry.getKey() + "; value=" + entry.getValue());
        Cookie c = entry.getValue();
        sb.append("Cookie Name=" + c.getName());
        sb.append("Cookie Value=" + c.getValue());
        sb.append("Cookie Path=" + c.getPath());
        sb.append("Cookie Domain=" + c.getDomain());
        sb.append("Cookie Version=" + c.getVersion());

      }
    } catch (Throwable ex) {
      sb.append("Unexpected exception thrown: " + ex.getMessage());
      ex.printStackTrace();
    }
    return sb.toString();
  }

  @PUT
  public String headersPlainPut(String nil) {
    sb = new StringBuffer();
    sb.append("Content-Language");
    sb.append(langToString(hs.getLanguage()));
    return sb.toString();
  }

  @POST
  @Path("date")
  public String date(@Context HttpHeaders headers, String nil) {
    Date date = headers.getDate();
    long time = date.getTime();
    return String.valueOf(time);
  }

  @POST
  @Path("headerstring")
  public String headerString(@Context HttpHeaders headers, String headerName) {
    String header = headers.getHeaderString(headerName);
    return header;
  }

  @GET
  @Path("length")
  public String headerLength(@Context HttpHeaders headers) {
    return String.valueOf(headers.getLength());
  }

  private static String langToString(Locale locale) {
    return locale.toString().replace("_", "-");
  }
}
