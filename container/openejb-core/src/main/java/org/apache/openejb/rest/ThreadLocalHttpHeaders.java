/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.openejb.rest;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ThreadLocalHttpHeaders extends AbstractRestThreadLocalProxy<HttpHeaders>
    implements HttpHeaders {

    protected ThreadLocalHttpHeaders() {
        super(HttpHeaders.class);
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        return get().getAcceptableMediaTypes();
    }

    @Override
    public Map<String, Cookie> getCookies() {
        return get().getCookies();
    }

    @Override
    public Date getDate() {
        return get().getDate();
    }

    @Override
    public int getLength() {
        return get().getLength();
    }

    @Override
    public Locale getLanguage() {
        return get().getLanguage();
    }

    @Override
    public MediaType getMediaType() {
        return get().getMediaType();
    }

    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {
        return new MultivaluedMapWithCaseInsensitiveKeySet<>(get().getRequestHeaders());
    }

    @Override
    public List<Locale> getAcceptableLanguages() {
        return get().getAcceptableLanguages();
    }

    @Override
    public List<String> getRequestHeader(final String name) {
        return get().getRequestHeader(name);
    }

    @Override
    public String getHeaderString(final String name) {
        return get().getHeaderString(name);
    }

}
