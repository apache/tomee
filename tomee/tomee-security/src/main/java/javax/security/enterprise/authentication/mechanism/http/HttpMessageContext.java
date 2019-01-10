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

public interface HttpMessageContext {
    boolean isProtected();

    boolean isAuthenticationRequest();

    boolean isRegisterSession();

    void setRegisterSession(String callerName, Set<String> groups);

    void cleanClientSubject();

    AuthenticationParameters getAuthParameters();

    CallbackHandler getHandler();

    MessageInfo getMessageInfo();

    Subject getClientSubject();

    HttpServletRequest getRequest();

    void setRequest(HttpServletRequest request);

    HttpMessageContext withRequest(HttpServletRequest request);

    HttpServletResponse getResponse();

    void setResponse(HttpServletResponse response);

    AuthenticationStatus redirect(String location);

    AuthenticationStatus forward(String path);

    AuthenticationStatus responseUnauthorized();

    AuthenticationStatus responseNotFound();

    AuthenticationStatus notifyContainerAboutLogin(String callername, Set<String> groups);

    AuthenticationStatus notifyContainerAboutLogin(Principal principal, Set<String> groups);

    AuthenticationStatus notifyContainerAboutLogin(CredentialValidationResult result);

    AuthenticationStatus doNothing();

    Principal getCallerPrincipal();

    Set<String> getGroups();
}
