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
package org.apache.tomee.security.http;

import org.apache.catalina.authenticator.jaspic.MessageInfoImpl;
import org.apache.tomee.security.TomEESecurityContext;
import org.apache.tomee.security.message.TomEEMessageInfo;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.CallerPrincipal;
import javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import static javax.security.enterprise.AuthenticationStatus.NOT_DONE;
import static javax.security.enterprise.AuthenticationStatus.SEND_CONTINUE;
import static javax.security.enterprise.AuthenticationStatus.SEND_FAILURE;
import static javax.security.enterprise.AuthenticationStatus.SUCCESS;
import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

public final class TomEEHttpMessageContext implements HttpMessageContext {
    private final CallbackHandler handler;
    private final MessageInfo messageInfo;
    private final Subject clientSubject;
    private final Subject serviceSubject;

    private Principal principal;
    private Set<String> groups;

    private TomEEHttpMessageContext(
            final CallbackHandler handler,
            final MessageInfo messageInfo,
            final Subject clientSubject,
            final Subject serviceSubject) {
        this.handler = handler;
        this.messageInfo = messageInfo;
        this.clientSubject = clientSubject;
        this.serviceSubject = serviceSubject;
    }

    public static TomEEHttpMessageContext httpMessageContext(
            final CallbackHandler handler,
            final MessageInfo messageInfo,
            final Subject clientSubject,
            final Subject serviceSubject) {
        return new TomEEHttpMessageContext(handler, messageInfo, clientSubject, serviceSubject);
    }

    @Override
    public boolean isProtected() {
        return Boolean.valueOf((String) messageInfo.getMap().getOrDefault(MessageInfoImpl.IS_MANDATORY, "false"));
    }

    @Override
    public boolean isAuthenticationRequest() {
        return Boolean.valueOf((String) messageInfo.getMap().getOrDefault(TomEEMessageInfo.AUTHENTICATE, "false"));
    }

    @Override
    public boolean isRegisterSession() {
        return false;
    }

    @Override
    public void setRegisterSession(final String callerName, final Set<String> groups) {

    }

    @Override
    public void cleanClientSubject() {

    }

    @Override
    public AuthenticationParameters getAuthParameters() {
        return (AuthenticationParameters) messageInfo.getMap()
                                                     .getOrDefault(TomEEMessageInfo.AUTH_PARAMS,
                                                                   new AuthenticationParameters());
    }

    @Override
    public CallbackHandler getHandler() {
        return handler;
    }

    @Override
    public MessageInfo getMessageInfo() {
        return messageInfo;
    }

    @Override
    public Subject getClientSubject() {
        return clientSubject;
    }

    @Override
    public HttpServletRequest getRequest() {
        return (HttpServletRequest) messageInfo.getRequestMessage();
    }

    @Override
    public void setRequest(final HttpServletRequest request) {
        messageInfo.setRequestMessage(request);
    }

    @Override
    public HttpMessageContext withRequest(final HttpServletRequest request) {
        setRequest(request);
        return this;
    }

    @Override
    public HttpServletResponse getResponse() {
        return (HttpServletResponse) messageInfo.getResponseMessage();
    }

    @Override
    public void setResponse(final HttpServletResponse response) {
        messageInfo.setResponseMessage(response);
    }

    @Override
    public AuthenticationStatus redirect(final String location) {
        try {
            getResponse().sendRedirect(location);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return SEND_CONTINUE;
    }

    @Override
    public AuthenticationStatus forward(final String path) {
        try {
            getRequest().getRequestDispatcher(path).forward(getRequest(), getResponse());
        } catch (final ServletException | IOException e) {
            e.printStackTrace();
        }

        return SEND_CONTINUE;
    }

    @Override
    public AuthenticationStatus responseUnauthorized() {
        try {
            getResponse().sendError(SC_UNAUTHORIZED);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        return SEND_FAILURE;
    }

    @Override
    public AuthenticationStatus responseNotFound() {
        return null;
    }

    @Override
    public AuthenticationStatus notifyContainerAboutLogin(final String callername, final Set<String> groups) {
        return notifyContainerAboutLogin(new CallerPrincipal(callername), groups);
    }

    @Override
    public AuthenticationStatus notifyContainerAboutLogin(final Principal principal, final Set<String> groups) {

        try {
            handler.handle(new Callback[] {
                    new CallerPrincipalCallback(clientSubject, principal),
                    new GroupPrincipalCallback(clientSubject, groups.toArray(new String[groups.size()]))
            });
        } catch (final IOException | UnsupportedCallbackException e) {
            e.printStackTrace();
        }

        this.principal = principal;
        this.groups = groups;

        TomEESecurityContext.registerContainerAboutLogin(principal, groups);

        return SUCCESS;
    }

    @Override
    public AuthenticationStatus notifyContainerAboutLogin(final CredentialValidationResult result) {
        if (result.getStatus().equals(VALID)) {
            return notifyContainerAboutLogin(result.getCallerPrincipal(), result.getCallerGroups());
        }

        return SEND_FAILURE;
    }

    @Override
    public AuthenticationStatus doNothing() {
        return NOT_DONE;
    }

    @Override
    public Principal getCallerPrincipal() {
        return principal;
    }

    @Override
    public Set<String> getGroups() {
        return groups;
    }
}
