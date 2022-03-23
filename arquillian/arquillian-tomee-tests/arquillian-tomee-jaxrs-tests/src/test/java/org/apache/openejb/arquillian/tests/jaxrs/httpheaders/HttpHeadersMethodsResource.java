/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.openejb.arquillian.tests.jaxrs.httpheaders;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Path("context/httpheaders")
public class HttpHeadersMethodsResource {

    @Context
    private HttpHeaders headersfield;

    @GET
    @Path("acceptablelanguages")
    public String getLanguages() {
        final List<Locale> languages = headersfield.getAcceptableLanguages();
        final StringBuilder sb = new StringBuilder("acceptablelanguages:");
        for (final Locale l : languages) {
            sb.append(l.getLanguage()).append(":");
        }
        return sb.toString();
    }

    @GET
    @Path("acceptablemediatypes")
    public String getAcceptableMediaTypes() {
        final List<MediaType> mediatypes = headersfield.getAcceptableMediaTypes();
        final StringBuilder sb = new StringBuilder("acceptablemediatypes:");
        for (final MediaType mt : mediatypes) {
            sb.append(mt.getType()).append("/").append(mt.getSubtype()).append(":");
        }
        return sb.toString();
    }

    @POST
    @Path("requestmediatype")
    public String getRequestMediaType() {
        final MediaType mt = headersfield.getMediaType();
        final StringBuilder sb = new StringBuilder("mediatype:");
        if (mt != null) {
            sb.append(mt.getType()).append("/").append(mt.getSubtype()).append(":");
        } else {
            sb.append("null:");
        }
        return sb.toString();
    }

    @POST
    @Path("language")
    public String getLanguage() {
        final Locale l = headersfield.getLanguage();
        final StringBuilder sb = new StringBuilder("language:");
        if (l != null) {
            sb.append(l.getLanguage()).append(":");
        } else {
            sb.append("null:");
        }
        return sb.toString();
    }

    @POST
    @Path("cookies")
    public String getCookies() {
        final Map<String, Cookie> cookies = headersfield.getCookies();
        final StringBuilder sb = new StringBuilder("cookies:");
        if (cookies == null) {
            sb.append("null:");
        } else {
            final List<String> cookieNames = new ArrayList<String>(cookies.keySet());
            Collections.sort(cookieNames);
            for (final String c : cookieNames) {
                sb.append(c).append("=").append(cookies.get(c).getValue()).append(":");
            }
        }
        return sb.toString();
    }

    @GET
    public String getHeader(@Context HttpHeaders headers, @QueryParam("name") String headerName) {
        try {
            List<String> values = headers.getRequestHeader(headerName);
            if (values == null) {
                return "requestheader:null:";
            } else {
                values = new ArrayList<String>(values);
            }
            Collections.sort(values);
            return "requestheader:" + values.toString();
        } catch (IllegalArgumentException e) {
            return "requestheader:illegalarg:";
        }
    }

    @GET
    @Path("/requestheaders")
    public String getHeaders(@Context HttpHeaders headers, @QueryParam("name") String headerName) {
        final MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
        final List<String> keys = new ArrayList<String>(requestHeaders.keySet());
        Collections.sort(keys);
        final StringBuilder sb = new StringBuilder("requestheaders:");
        for (final String k : keys) {
            sb.append(k).append("=");
            List<String> values = requestHeaders.get(k);
            if (values != null) {
                values = new ArrayList<String>(values);
                Collections.sort(values);
                sb.append(values).append(":");
            }
        }
        return sb.toString();
    }
}
