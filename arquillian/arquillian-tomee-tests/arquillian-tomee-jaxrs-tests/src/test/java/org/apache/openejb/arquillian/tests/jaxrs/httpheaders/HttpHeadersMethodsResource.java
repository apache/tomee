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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
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
        List<Locale> languages = headersfield.getAcceptableLanguages();
        StringBuilder sb = new StringBuilder("acceptablelanguages:");
        for (Locale l : languages) {
            sb.append(l.getLanguage() + ":");
        }
        return sb.toString();
    }

    @GET
    @Path("acceptablemediatypes")
    public String getAcceptableMediaTypes() {
        List<MediaType> mediatypes = headersfield.getAcceptableMediaTypes();
        StringBuilder sb = new StringBuilder("acceptablemediatypes:");
        for (MediaType mt : mediatypes) {
            sb.append(mt.getType() + "/" + mt.getSubtype() + ":");
        }
        return sb.toString();
    }

    @POST
    @Path("requestmediatype")
    public String getRequestMediaType() {
        MediaType mt = headersfield.getMediaType();
        StringBuilder sb = new StringBuilder("mediatype:");
        if (mt != null) {
            sb.append(mt.getType() + "/" + mt.getSubtype() + ":");
        } else {
            sb.append("null:");
        }
        return sb.toString();
    }

    @POST
    @Path("language")
    public String getLanguage() {
        Locale l = headersfield.getLanguage();
        StringBuilder sb = new StringBuilder("language:");
        if (l != null) {
            sb.append(l.getLanguage() + ":");
        } else {
            sb.append("null:");
        }
        return sb.toString();
    }

    @POST
    @Path("cookies")
    public String getCookies() {
        Map<String, Cookie> cookies = headersfield.getCookies();
        StringBuilder sb = new StringBuilder("cookies:");
        if (cookies == null) {
            sb.append("null:");
        } else {
            List<String> cookieNames = new ArrayList<String>(cookies.keySet());
            Collections.sort(cookieNames);
            for (String c : cookieNames) {
                sb.append(c + "=" + cookies.get(c).getValue() + ":");
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
        MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
        List<String> keys = new ArrayList<String>(requestHeaders.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder("requestheaders:");
        for (String k : keys) {
            sb.append(k + "=");
            List<String> values = requestHeaders.get(k);
            if (values != null) {
                values = new ArrayList<String>(values);
                Collections.sort(values);
                sb.append(values + ":");
            }
        }
        return sb.toString();
    }
}
