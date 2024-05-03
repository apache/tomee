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

import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.credential.Credential;

public class TomEEOpenIdCredential implements Credential {
    private final TokenResponse tokenResponse;
    private final HttpMessageContext messageContext;

    public TomEEOpenIdCredential(TokenResponse tokenResponse, HttpMessageContext messageContext) {
        this.tokenResponse = tokenResponse;
        this.messageContext = messageContext;
    }

    public TokenResponse getTokenResponse() {
        return tokenResponse;
    }

    public HttpMessageContext getMessageContext() {
        return messageContext;
    }
}
