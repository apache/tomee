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

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

public class ThreadLocalUriInfo extends AbstractRestThreadLocalProxy<UriInfo>
    implements UriInfo {

    protected ThreadLocalUriInfo() {
        super(UriInfo.class);
    }

    @Override
    public URI getAbsolutePath() {
        return get().getAbsolutePath();
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
        return get().getAbsolutePathBuilder();
    }

    @Override
    public URI getBaseUri() {
        return get().getBaseUri();
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
        return get().getBaseUriBuilder();
    }

    @Override
    public String getPath() {
        return get().getPath();
    }

    @Override
    public String getPath(final boolean decode) {
        return get().getPath(decode);
    }

    @Override
    public List<PathSegment> getPathSegments() {
        return get().getPathSegments();
    }

    @Override
    public List<PathSegment> getPathSegments(final boolean decode) {
        return get().getPathSegments(decode);
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return get().getQueryParameters();
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(final boolean decode) {
        return get().getQueryParameters(decode);
    }

    @Override
    public URI getRequestUri() {
        return get().getRequestUri();
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
        return get().getRequestUriBuilder();
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters() {
        return get().getPathParameters();
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters(final boolean decode) {
        return get().getPathParameters(decode);
    }

    @Override
    public List<Object> getMatchedResources() {
        return get().getMatchedResources();
    }

    @Override
    public URI resolve(final URI uri) {
        return get().resolve(uri);
    }

    @Override
    public URI relativize(final URI uri) {
        return get().relativize(uri);
    }

    @Override
    public List<String> getMatchedURIs() {
        return get().getMatchedURIs();
    }

    @Override
    public List<String> getMatchedURIs(final boolean decode) {
        return get().getMatchedURIs(decode);
    }

}
