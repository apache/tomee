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

import org.apache.tomee.microprofile.jwt.cdi.MPJWTProducer;
import org.apache.tomee.microprofile.jwt.config.JWTAuthContextInfo;
import org.apache.tomee.microprofile.jwt.principal.DefaultJWTCallerPrincipalFactory;
import org.apache.tomee.microprofile.jwt.principal.JWTCallerPrincipalFactory;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.inject.Inject;
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
import java.util.Map;
import java.util.Optional;

// async is supported because we only need to do work on the way in
@WebFilter(asyncSupported = true, urlPatterns = "/*")
public class MPJWTFilter implements Filter {

    @Inject
    private JWTAuthContextInfo authContextInfo;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // get configuration

    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final Optional<Map.Entry<MPJWTContext.MPJWTConfigKey, MPJWTContext.MPJWTConfigValue>> first =
                MPJWTContext.findFirst(httpServletRequest.getRequestURI());

        if (!first.isPresent()) { // nothing found in the context
            chain.doFilter(request, response);
            return;
        }

        // todo get JWT and do validation
        // todo not sure what to do with the realm

        final String authorizationHeader = ((HttpServletRequest) request).getHeader("Authorization");
        final String token = authorizationHeader.substring("bearer ".length());
        final JsonWebToken jsonWebToken;
        try {
            jsonWebToken = DefaultJWTCallerPrincipalFactory.instance().parse(token, authContextInfo);

        } catch (final ParseException e) {
            // todo properly handle the exception as required per spec
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // associate with the producer. Should not be needed.
        // todo We should be able to retrieve it based on the HTTP Servlet Request in the producer
        MPJWTProducer.setJWTPrincipal(jsonWebToken);

        // now wrap the httpServletRequest and override the principal so CXF can propagate into the SecurityContext
        chain.doFilter(new HttpServletRequestWrapper(httpServletRequest) {

            @Override
            public Principal getUserPrincipal() {
                return jsonWebToken;
            }

            @Override
            public boolean isUserInRole(String role) {
                return jsonWebToken.getGroups().contains(role);
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

    protected JsonWebToken validate(String bearerToken) throws ParseException {
        JWTCallerPrincipalFactory factory = JWTCallerPrincipalFactory.instance();
        return factory.parse(bearerToken, authContextInfo);
    }

}
