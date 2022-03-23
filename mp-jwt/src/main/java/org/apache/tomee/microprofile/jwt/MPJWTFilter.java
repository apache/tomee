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

import org.apache.commons.lang3.Validate;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Logger;
import org.apache.tomee.catalina.OpenEJBSecurityListener;
import org.apache.tomee.catalina.TomcatSecurityService;
import org.apache.tomee.microprofile.jwt.bval.ValidationInterceptor;
import org.apache.tomee.microprofile.jwt.config.JWTAuthConfiguration;
import org.apache.tomee.microprofile.jwt.config.JWTAuthConfigurationProperties;
import org.apache.tomee.microprofile.jwt.principal.JWTCallerPrincipal;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javax.security.auth.Subject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// async is supported because we only need to do work on the way in
//@WebFilter(asyncSupported = true, urlPatterns = "/*")
public class MPJWTFilter implements Filter {

    private static final Logger VALIDATION = Logger.getInstance(JWTLogCategories.VALIDATION, MPJWTFilter.class);

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final Optional<JWTAuthConfiguration> authContextInfo = getAuthContextInfo();
        if (!authContextInfo.isPresent()) {
            chain.doFilter(request, response);
            return;
        }

        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        // now wrap the httpServletRequest and override the principal so CXF can propagate into the SecurityContext
        try {
            final MPJWTServletRequestWrapper wrappedRequest = new MPJWTServletRequestWrapper(httpServletRequest, authContextInfo.get());
            chain.doFilter(wrappedRequest, response);

            Object state = request.getAttribute("MP_JWT_PRE_LOGIN_STATE");
            final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
            if (TomcatSecurityService.class.isInstance(securityService) && state != null) {
                final TomcatSecurityService tomcatSecurityService = TomcatSecurityService.class.cast(securityService);
                tomcatSecurityService.exitWebApp(state);
            }
        } catch (final Exception e) {
            // this is an alternative to the @Provider bellow which requires registration on the fly
            // or users to add it into their webapp for scanning or into the Application itself
            if (MPJWTException.class.isInstance(e)) {
                final MPJWTException jwtException = MPJWTException.class.cast(e);
                HttpServletResponse.class.cast(response).sendError(jwtException.getStatus(), jwtException.getMessage());
            } else if (MPJWTException.class.isInstance(e.getCause())) {
                final MPJWTException jwtException = MPJWTException.class.cast(e.getCause());
                HttpServletResponse.class.cast(response).sendError(jwtException.getStatus(), jwtException.getMessage());
            } else {
                throw e;
            }
        }
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Inject
    private Instance<JWTAuthConfiguration> authContextInfo;

    @Inject
    private JWTAuthConfigurationProperties jwtAuthConfigurationProperties;

    private Optional<JWTAuthConfiguration> getAuthContextInfo() {
        if (!authContextInfo.isUnsatisfied()) {
            return Optional.of(authContextInfo.get());
        }

        return jwtAuthConfigurationProperties.getJWTAuthConfiguration();
    }

    private static Function<HttpServletRequest, JsonWebToken> token(final HttpServletRequest httpServletRequest, final JWTAuthConfiguration authContextInfo) {

        return new ValidateJSonWebToken(httpServletRequest, authContextInfo)::validate;

    }

    public static class MPJWTServletRequestWrapper extends HttpServletRequestWrapper {

        private final Function<HttpServletRequest, JsonWebToken> tokenFunction;
        private final HttpServletRequest request;

        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request         The request to wrap
         * @param authContextInfo the context configuration to validate the token
         * @throws IllegalArgumentException if the request is null
         */
        public MPJWTServletRequestWrapper(final HttpServletRequest request, final JWTAuthConfiguration authContextInfo) {
            super(request);
            this.request = request;
            tokenFunction = token(request, authContextInfo);

            final Supplier<JsonWebToken> tokenSupplier = () -> tokenFunction.apply(request);

            // this is so that the MPJWTProducer can find the function and apply it if necessary
            request.setAttribute(JsonWebToken.class.getName(), tokenFunction);
            request.setAttribute(ValidationInterceptor.JWT_SUPPLIER, tokenSupplier);
            request.setAttribute("javax.security.auth.subject.callable", (Callable<Subject>) new Callable<Subject>() {
                @Override
                public Subject call() throws Exception {
                    final Set<Principal> principals = new LinkedHashSet<>();
                    final JsonWebToken namePrincipal = tokenFunction.apply(request);
                    principals.add(namePrincipal);
                    principals.addAll(namePrincipal.getGroups().stream().map(role -> (Principal) () -> role).collect(Collectors.<Principal>toList()));
                    return new Subject(true, principals, Collections.emptySet(), Collections.emptySet());
                }
            });
        }

        @Override
        public Principal getUserPrincipal() {
            return tokenFunction.apply(request);
        }

        @Override
        public boolean isUserInRole(String role) {
            final JsonWebToken jsonWebToken = tokenFunction.apply(request);
            return jsonWebToken.getGroups().contains(role);
        }

        @Override
        public String getAuthType() {
            return "MP-JWT";
        }

    }

    private abstract static class MPJWTException extends RuntimeException {

        public MPJWTException() {
            super();
        }

        public MPJWTException(final Throwable cause) {
            super(cause);
        }

        public abstract int getStatus();

        @Override
        public abstract String getMessage();
    }

    private static class MissingAuthorizationHeaderException extends MPJWTException {

        @Override
        public int getStatus() {
            return HttpServletResponse.SC_UNAUTHORIZED;
        }

        @Override
        public String getMessage() {
            return "No authorization header provided. Can't validate the JWT.";
        }
    }

    private static class BadAuthorizationPrefixException extends MPJWTException {

        private final String authorizationHeader;

        public BadAuthorizationPrefixException(final String authorizationHeader) {
            this.authorizationHeader = authorizationHeader;
        }

        @Override
        public int getStatus() {
            return HttpServletResponse.SC_UNAUTHORIZED;
        }

        @Override
        public String getMessage() {
            return "Authorization header does not use the Bearer prefix. Can't validate header " + authorizationHeader;
        }
    }

    private static class InvalidTokenException extends MPJWTException {

        private final String token;

        public InvalidTokenException(final String token, final Throwable cause) {
            super(cause);
            this.token = token;
        }

        @Override
        public int getStatus() {
            return HttpServletResponse.SC_UNAUTHORIZED;
        }

        @Override
        public String getMessage() {
            return "Invalid or not parsable JWT " + token; // we might want to break down the exceptions so we can have more messages.
        }
    }

    @Provider // would be the ideal but not automatically registered
    public static class MPJWTExceptionMapper implements ExceptionMapper<MPJWTException> {

        @Override
        public Response toResponse(final MPJWTException exception) {
            return Response.status(exception.getStatus()).entity(exception.getMessage()).build();
        }

    }

    public static class ValidateJSonWebToken {

        private final HttpServletRequest httpServletRequest;
        private final JWTAuthConfiguration jwtAuthConfiguration;
        private JsonWebToken jsonWebToken;

        public ValidateJSonWebToken(final HttpServletRequest httpServletRequest, final JWTAuthConfiguration authContextInfo) {
            this.httpServletRequest = httpServletRequest;
            this.jwtAuthConfiguration = authContextInfo;
        }


        public JsonWebToken validate(final HttpServletRequest request) {

            // not sure it's worth having synchronization inside a single request
            // worth case, we would parse and validate the token twice
            if (jsonWebToken != null) {
                return jsonWebToken;
            }

            final String headerName = jwtAuthConfiguration.getHeaderName();
            final String authorizationHeader = httpServletRequest.getHeader(headerName);
            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                throw new MissingAuthorizationHeaderException();
            }

            final String headerScheme = (jwtAuthConfiguration.getHeaderScheme() + " ").toLowerCase(Locale.ENGLISH);
            if (headerScheme.trim().length() > 0 &&  !authorizationHeader.toLowerCase(Locale.ENGLISH).startsWith(headerScheme)) {
                throw new BadAuthorizationPrefixException(authorizationHeader);
            }

            final String token = authorizationHeader.substring(headerScheme.length());
            try {
                jsonWebToken = parse(token, jwtAuthConfiguration);

            } catch (final ParseException e) {
                throw new InvalidTokenException(token, e);
            }

            // TODO - do the login here, save the state to the request so we can recover it later.

            final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
            if (TomcatSecurityService.class.isInstance(securityService)) {
                TomcatSecurityService tomcatSecurityService = TomcatSecurityService.class.cast(securityService);
                final org.apache.catalina.connector.Request req = OpenEJBSecurityListener.requests.get();
                Object state = tomcatSecurityService.enterWebApp(req.getWrapper().getRealm(), jsonWebToken, req.getWrapper().getRunAs());

                request.setAttribute("MP_JWT_PRE_LOGIN_STATE", state);
            }

            // TODO Also check if it is an async request and add a listener to close off the state

            return jsonWebToken;

        }

        public static JWTCallerPrincipal parse(final String token, final JWTAuthConfiguration authContextInfo) throws ParseException {
            JWTCallerPrincipal principal;

            try {
                final JwtConsumerBuilder builder = new JwtConsumerBuilder()
                        .setRelaxVerificationKeyValidation()
                        .setRequireSubject()
                        .setSkipDefaultAudienceValidation()
                        .setJwsAlgorithmConstraints(
                                new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                                        AlgorithmIdentifiers.RSA_USING_SHA256,
                                        AlgorithmIdentifiers.RSA_USING_SHA384,
                                        AlgorithmIdentifiers.RSA_USING_SHA512
                                ));

                if (!authContextInfo.isAllowNoExpiryClaim()) {
                    builder.setRequireExpirationTime();
                }
                if (authContextInfo.getIssuer() != null) {
                    builder.setExpectedIssuer(authContextInfo.getIssuer());
                }
                if (authContextInfo.getExpGracePeriodSecs() > 0) {
                    builder.setAllowedClockSkewInSeconds(authContextInfo.getExpGracePeriodSecs());
                } else {
                    builder.setEvaluationTime(NumericDate.fromSeconds(0));
                }

                if (authContextInfo.isSingleKey()) {
                    builder.setVerificationKey(authContextInfo.getPublicKey());
                } else {
                    builder.setVerificationKeyResolver(new JwksVerificationKeyResolver(authContextInfo.getPublicKeys()));
                }

                final JwtConsumer jwtConsumer = builder.build();
                final JwtContext jwtContext = jwtConsumer.process(token);
                final String type = jwtContext.getJoseObjects().get(0).getHeader("typ");
                //  Validate the JWT and process it to the Claims
                jwtConsumer.processContext(jwtContext);
                JwtClaims claimsSet = jwtContext.getJwtClaims();

                // We have to determine the unique name to use as the principal name. It comes from upn, preferred_username, sub in that order
                String principalName = claimsSet.getClaimValue("upn", String.class);
                if (principalName == null) {
                    principalName = claimsSet.getClaimValue("preferred_username", String.class);
                    if (principalName == null) {
                        principalName = claimsSet.getSubject();
                    }
                }
                claimsSet.setClaim(Claims.raw_token.name(), token);
                principal = new JWTCallerPrincipal(token, type, claimsSet, principalName);

            } catch (final InvalidJwtException e) {
                VALIDATION.warning(e.getMessage());
                throw new ParseException("Failed to verify token", e);

            } catch (final MalformedClaimException e) {
                VALIDATION.warning(e.getMessage());
                throw new ParseException("Failed to verify token claims", e);
            }

            return principal;
        }
    }
}
