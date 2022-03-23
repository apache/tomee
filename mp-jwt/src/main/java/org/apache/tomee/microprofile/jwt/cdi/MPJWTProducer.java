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
package org.apache.tomee.microprofile.jwt.cdi;

import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.function.Function;

@ApplicationScoped
public class MPJWTProducer {

    @Inject
    private HttpServletRequest httpServletRequest;

    @Produces
    @RequestScoped
    public JsonWebToken currentPrincipal() {
        Objects.requireNonNull(httpServletRequest, "HTTP Servlet Request is required to produce a JSonWebToken principal.");

        // not very beautiful, but avoids having the MPJWTFilter setting the request or the principal in a thread local
        // CDI integration already has one - dunno which approach is the best for now
        final Object tokenAttribute = httpServletRequest.getAttribute(JsonWebToken.class.getName());
        if (Function.class.isInstance(tokenAttribute)) {
            return (JsonWebToken) Function.class.cast(tokenAttribute).apply(httpServletRequest);
        }

        return null;
    }
}