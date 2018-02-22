/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.tomee.microprofile.jwt;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.security.Principal;

// async is supported because we only need to do work on the way in
@WebFilter(asyncSupported = true, urlPatterns = "/*")
public class MPJWTFilter implements Filter {


    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // get configuration

    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest httpServletRequest = (HttpServletRequest)request;

        // todo get JWT and do validation

        // now wrap the httpServletRequest and override the principal so CXF can propagate into the SecurityContext
        chain.doFilter(new HttpServletRequestWrapper(httpServletRequest) {

            @Override
            public Principal getUserPrincipal() {
                return null; // todo, during parsing and validation, we need to convert into the JWT Principal as specified by the spec
            }

            @Override
            public boolean isUserInRole(String role) {
                return true; // replace with a check based on the claims content
            }

            @Override
            public String getAuthType() {
                return "MP-JWT";
            }

        }, response);

    }

    @Override
    public void destroy() {

    }

}
