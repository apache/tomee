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
package org.apache.tomee.security.provider;

import javax.security.auth.callback.CallbackHandler;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.config.AuthConfigFactory;
import jakarta.security.auth.message.config.AuthConfigProvider;
import jakarta.security.auth.message.config.ClientAuthConfig;
import jakarta.security.auth.message.config.ServerAuthConfig;
import java.util.Map;

public class TomEESecurityAuthConfigProvider implements AuthConfigProvider {

    public TomEESecurityAuthConfigProvider(final Map properties, final AuthConfigFactory authConfigFactory) {
    }

    @Override
    public ClientAuthConfig getClientAuthConfig(final String layer, final String appContext,
                                                final CallbackHandler handler)
            throws AuthException, SecurityException {
        return null;
    }

    @Override
    public ServerAuthConfig getServerAuthConfig(final String layer, final String appContext,
                                                final CallbackHandler handler)
            throws AuthException, SecurityException {
        return new TomEESecurityServerAuthConfig(layer, appContext, handler);
    }

    @Override
    public void refresh() {

    }
}
