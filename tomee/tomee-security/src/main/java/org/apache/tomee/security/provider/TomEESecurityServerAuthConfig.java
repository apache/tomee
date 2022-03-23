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

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.config.ServerAuthConfig;
import jakarta.security.auth.message.config.ServerAuthContext;
import java.util.Map;

public class TomEESecurityServerAuthConfig implements ServerAuthConfig {
    private String layer;
    private String appContext;
    private CallbackHandler handler;

    public TomEESecurityServerAuthConfig(final String layer, final String appContext, final CallbackHandler handler) {
        this.layer = layer;
        this.appContext = appContext;
        this.handler = handler;
    }

    @Override
    public ServerAuthContext getAuthContext(final String authContextID, final Subject serviceSubject,
                                            final Map properties)
            throws AuthException {
        return new TomEESecurityServerAuthContext(handler);
    }

    @Override
    public String getAppContext() {
        return appContext;
    }

    @Override
    public String getAuthContextID(final MessageInfo messageInfo) throws IllegalArgumentException {
        return appContext; // not sure what's the difference with getAppContext()
    }

    @Override
    public String getMessageLayer() {
        return layer;
    }

    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public void refresh() {

    }
}
