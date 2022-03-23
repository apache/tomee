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
package org.apache.tomee.security.message;

import org.apache.catalina.authenticator.jaspic.MessageInfoImpl;

import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TomEEMessageInfo extends MessageInfoImpl {
    public static final String AUTH_PARAMS = "org.apache.tomee.security.context.authParams";
    public static final String AUTHENTICATE = "org.apache.tomee.security.context.authenticate";
    public static final String IS_MANDATORY = "jakarta.security.auth.message.MessagePolicy.isMandatory";
    public static final String REGISTER_SESSION = "jakarta.servlet.http.registerSession";

    public TomEEMessageInfo(final HttpServletRequest request,
                            final HttpServletResponse response,
                            final boolean authMandatory) {
        super(request, response, authMandatory);
    }

    public TomEEMessageInfo(final HttpServletRequest request,
                            final HttpServletResponse response,
                            final boolean authMandatory,
                            final AuthenticationParameters authParameters) {
        super(request, response, authMandatory);
        getMap().put(AUTH_PARAMS, authParameters);
        getMap().put(AUTHENTICATE, Boolean.toString(true));
    }
}
