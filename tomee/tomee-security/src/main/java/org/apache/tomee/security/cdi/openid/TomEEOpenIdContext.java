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

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.identitystore.openid.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.tomee.security.cdi.openid.storage.OpenIdStorageHandler;
import org.apache.tomee.security.http.openid.model.TomEEOpenIdClaims;

import java.util.Optional;

@SessionScoped
public class TomEEOpenIdContext implements OpenIdContext {
    @Inject private Instance<OpenIdAuthenticationMechanismDefinition> definition;
    @Inject private OpenIdStorageHandler storageHandler;

    private JsonObject userInfoClaims;
    private String tokenType;

    private AccessToken accessToken;
    private IdentityToken identityToken;
    private RefreshToken refreshToken;
    private Long expiresIn;

    @PostConstruct
    public void init() {
        if (definition.isUnsatisfied()) {
            throw new IllegalStateException("OpenIdContext is not available if no @OpenIdAuthenticationMechanismDefinition is defined");
        }
    }

    @Override
    public String getSubject() {
        return getIdentityToken().getJwtClaims().getSubject()
                .orElseThrow(() -> new IllegalStateException("No subject received from openid provider in id_token"));
    }

    @Override
    public String getTokenType() {
        return tokenType;
    }

    @Override
    public AccessToken getAccessToken() {
        return accessToken;
    }

    @Override
    public IdentityToken getIdentityToken() {
        return identityToken;
    }

    @Override
    public Optional<RefreshToken> getRefreshToken() {
        return Optional.ofNullable(refreshToken);
    }

    @Override
    public Optional<Long> getExpiresIn() {
        return Optional.ofNullable(expiresIn);
    }

    @Override
    public JsonObject getClaimsJson() {
        return userInfoClaims;
    }

    @Override
    public OpenIdClaims getClaims() {
        return new TomEEOpenIdClaims(getClaimsJson());
    }

    @Override
    public JsonObject getProviderMetadata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<T> getStoredValue(HttpServletRequest request, HttpServletResponse response, String key) {
        return Optional.ofNullable((T) storageHandler.get(request, response, key));
    }

    public void setUserInfoClaims(JsonObject userInfoClaims) {
        this.userInfoClaims = userInfoClaims;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public void setIdentityToken(IdentityToken identityToken) {
        this.identityToken = identityToken;
    }

    public void setRefreshToken(RefreshToken refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
