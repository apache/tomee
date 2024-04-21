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
package org.apache.tomee.security.cdi.openid;

import org.apache.tomee.security.http.openid.OpenIdStorageHandler;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.identitystore.openid.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.function.Supplier;

@RequestScoped
public class TomEEOpenIdContext implements OpenIdContext {
    @Inject private Instance<Supplier<OpenIdAuthenticationMechanismDefinition>> definition;

    @PostConstruct
    public void init() {
        if (definition.isUnsatisfied()) {
            throw new IllegalStateException("OpenIdCContext is not available if no @OpenIdAuthenticationMechanismDefinition is defined");
        }
    }

    @Override
    public String getSubject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTokenType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AccessToken getAccessToken() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IdentityToken getIdentityToken() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<RefreshToken> getRefreshToken() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Long> getExpiresIn() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonObject getClaimsJson() {
        throw new UnsupportedOperationException();
    }

    @Override
    public OpenIdClaims getClaims() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonObject getProviderMetadata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<T> getStoredValue(HttpServletRequest request, HttpServletResponse response, String key) {
        return Optional.ofNullable((T) OpenIdStorageHandler.get(
                definition.get().get().useSession()).get(request, response, key));
    }
}
