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
package org.apache.tomee.security.cdi.openid.storage.impl;

import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomee.security.cdi.openid.storage.OpenIdStorageHandler;

public class DefinitionAwareOpenIdStorageHandler extends OpenIdStorageHandler {
    private final OpenIdStorageHandler session = new SessionBasedOpenIdStorageHandler();
    private final OpenIdStorageHandler cookie = new CookieBasedOpenIdStorageHandler();

    @Override
    public String get(final HttpServletRequest request, final HttpServletResponse response, final String key) {
        return delegate().get(request, response, key);
    }

    @Override
    public void set(final HttpServletRequest request, final HttpServletResponse response, final String key, final String value) {
        delegate().set(request, response, key, value);
    }

    @Override
    public void delete(final HttpServletRequest request, final HttpServletResponse response, final String key) {
        delegate().delete(request, response, key);
    }

    private OpenIdStorageHandler delegate() {
        final OpenIdAuthenticationMechanismDefinition definition = currentDefinition();
        if (definition == null) {
            throw new IllegalStateException("OpenID storage access requires the selected OpenIdAuthenticationMechanismDefinition");
        }
        return definition.useSession() ? session : cookie;
    }
}
