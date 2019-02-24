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
package javax.security.enterprise.authentication.mechanism.http;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.MessageInfo;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Set;

public class HttpMessageContextWrapper implements HttpMessageContext {
    private final HttpMessageContext httpMessageContext;

    public HttpMessageContextWrapper(final HttpMessageContext httpMessageContext) {
        this.httpMessageContext = httpMessageContext;
    }

    public HttpMessageContext getWrapped() {
        return httpMessageContext;
    }

    @Override
    public boolean isProtected() {
        return getWrapped().isProtected();
    }

    @Override
    public boolean isAuthenticationRequest() {
        return getWrapped().isAuthenticationRequest();
    }

    @Override
    public boolean isRegisterSession() {
        return getWrapped().isRegisterSession();
    }

    @Override
    public void setRegisterSession(final String callerName, final Set<String> groups) {
        getWrapped().setRegisterSession(callerName, groups);
    }

    @Override
    public void cleanClientSubject() {
        getWrapped().cleanClientSubject();
    }

    @Override
    public AuthenticationParameters getAuthParameters() {
        return getWrapped().getAuthParameters();
    }

    @Override
    public CallbackHandler getHandler() {
        return getWrapped().getHandler();
    }

    @Override
    public MessageInfo getMessageInfo() {
        return getWrapped().getMessageInfo();
    }

    @Override
    public Subject getClientSubject() {
        return getWrapped().getClientSubject();
    }

    @Override
    public HttpServletRequest getRequest() {
        return getWrapped().getRequest();
    }

    @Override
    public void setRequest(final HttpServletRequest request) {
        getWrapped().setRequest(request);
    }

    @Override
    public HttpMessageContext withRequest(final HttpServletRequest request) {
        return getWrapped().withRequest(request);
    }

    @Override
    public HttpServletResponse getResponse() {
        return getWrapped().getResponse();
    }

    @Override
    public void setResponse(final HttpServletResponse response) {
        getWrapped().setResponse(response);
    }

    @Override
    public AuthenticationStatus redirect(final String location) {
        return getWrapped().redirect(location);
    }

    @Override
    public AuthenticationStatus forward(final String path) {
        return getWrapped().forward(path);
    }

    @Override
    public AuthenticationStatus responseUnauthorized() {
        return getWrapped().responseUnauthorized();
    }

    @Override
    public AuthenticationStatus responseNotFound() {
        return getWrapped().responseNotFound();
    }

    @Override
    public AuthenticationStatus notifyContainerAboutLogin(final String callername, final Set<String> groups) {
        return getWrapped().notifyContainerAboutLogin(callername, groups);
    }

    @Override
    public AuthenticationStatus notifyContainerAboutLogin(final Principal principal, final Set<String> groups) {
        return getWrapped().notifyContainerAboutLogin(principal, groups);
    }

    @Override
    public AuthenticationStatus notifyContainerAboutLogin(final CredentialValidationResult result) {
        return getWrapped().notifyContainerAboutLogin(result);
    }

    @Override
    public AuthenticationStatus doNothing() {
        return getWrapped().doNothing();
    }

    @Override
    public Principal getCallerPrincipal() {
        return getWrapped().getCallerPrincipal();
    }

    @Override
    public Set<String> getGroups() {
        return getWrapped().getGroups();
    }
}
