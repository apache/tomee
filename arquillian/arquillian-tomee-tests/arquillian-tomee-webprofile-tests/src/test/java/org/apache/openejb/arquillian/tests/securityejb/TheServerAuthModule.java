/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.tests.securityejb;

import jakarta.enterprise.inject.spi.CDI;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.auth.message.callback.GroupPrincipalCallback;
import jakarta.security.auth.message.module.ServerAuthModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import static jakarta.security.auth.message.AuthStatus.SUCCESS;

/**
 * @author Arjan Tijms
 */
public class TheServerAuthModule implements ServerAuthModule {

    private CallbackHandler handler;
    private Class<?>[] supportedMessageTypes = new Class[]{HttpServletRequest.class, HttpServletResponse.class};

    @Override
    public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler,
                           @SuppressWarnings("rawtypes") Map options) throws AuthException {
        this.handler = handler;
    }

    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject)
            throws AuthException {

        HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();

        Callback[] callbacks;

        if (request.getParameter("doLogin") != null) {
            callbacks = new Callback[]{new CallerPrincipalCallback(clientSubject, "test"),
                    new GroupPrincipalCallback(clientSubject, new String[]{"architect"})};
        } else {
            callbacks = new Callback[]{new CallerPrincipalCallback(clientSubject, (Principal) null)};
        }

        try {
            handler.handle(callbacks);
        } catch (IOException | UnsupportedCallbackException e) {
            throw (AuthException) new AuthException().initCause(e);
        }

        cdi(messageInfo, "vr");

        return SUCCESS;
    }

    @Override
    public Class<?>[] getSupportedMessageTypes() {
        return supportedMessageTypes;
    }

    @Override
    public AuthStatus secureResponse(final MessageInfo messageInfo, final Subject serviceSubject) throws AuthException {
        cdi(messageInfo, "sr");
        return AuthStatus.SEND_SUCCESS;
    }

    private void cdi(final MessageInfo messageInfo, final String msg) throws AuthException {
        final HttpServletRequest request = HttpServletRequest.class.cast(messageInfo.getRequestMessage());
        final HttpServletResponse response = HttpServletResponse.class.cast(messageInfo.getResponseMessage());
        if (request.getParameter("bean") != null) {
            final TheBean cdiBean = CDI.current().select(TheBean.class).get();
            cdiBean.set(msg);
            try {
                response.getWriter().write(String.valueOf(request.getAttribute("cdi")));
            } catch (final IOException e) {
                throw new AuthException(e.getMessage());
            }
        }
    }

    @Override
    public void cleanSubject(final MessageInfo messageInfo, final Subject subject) throws AuthException {
        cdi(messageInfo, "cs");
    }
}
