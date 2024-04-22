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
package org.apache.tomee.security.http.openid.model;

import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import jakarta.json.bind.annotation.JsonbTypeDeserializer;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.identitystore.openid.Scope;
import org.jose4j.jwt.consumer.JwtContext;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TomEEOpenIdCredential implements Credential {
    @JsonbProperty(OpenIdConstant.TOKEN_TYPE)
    private String tokenType;

    @JsonbProperty(OpenIdConstant.ACCESS_TOKEN)
    private String accesToken;
    @JsonbTransient
    private JwtContext accesTokenJwt;
    @JsonbProperty(OpenIdConstant.IDENTITY_TOKEN)
    private String idToken;
    @JsonbTransient
    private JwtContext idTokenJwt;

    @JsonbProperty(OpenIdConstant.EXPIRES_IN)
    private long expiresIn;

    @JsonbProperty(OpenIdConstant.SCOPE)
    @JsonbTypeAdapter(JsonbScopeAdapter.class)
    private Scope scope;

    public String getTokenType() {
        return tokenType;
    }

    public String getAccesToken() {
        return accesToken;
    }

    public void setAccesTokenJwt(JwtContext accesTokenJwt) {
        this.accesTokenJwt = accesTokenJwt;
    }

    public JwtContext getAccesTokenJwt() {
        return accesTokenJwt;
    }

    public String getIdToken() {
        return idToken;
    }

    public JwtContext getIdTokenJwt() {
        return idTokenJwt;
    }

    public void setIdTokenJwt(JwtContext idTokenJwt) {
        this.idTokenJwt = idTokenJwt;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public Scope getScope() {
        return scope;
    }

    public static class JsonbScopeAdapter implements JsonbAdapter<Scope, String> {
        @Override
        public String adaptToJson(Scope obj) throws Exception {
            return obj == null ? null : obj.toString();
        }

        @Override
        public Scope adaptFromJson(String obj) throws Exception {
            return Scope.parse(obj);
        }
    }
}
